/*  3 Jan 2023 23:44
Navigate in a File Directory using 2 panes.
Save / reload the current position on disc.

Creates 2 ListViews (inner / outer) that can be built into other guis.

*/

FileNavigator {
	var <>key = \default;
	var >homeDir;  // top folder holding all items.
	var >currentRoot; // path for getting outerList items
	var <>outerList, <>outerItem, <outerIndex = 0;
	var <>innerList, <>innerItem, <innerIndex = 0;

	*new { | key = \default | ^this.newCopyArgs(key).getOuterItems }

	save {
		postln("Saving preferences for:" + this.class.name + "at key" + key + "prefs" + this.prefs);
		Preferences.put(this.class.name, key, this.prefs);
	}

	prefs {
		// save info needed to regenerate state,
		// plus human-readable relevant info.
		^(
			homeDir: homeDir,
			currentRoot: currentRoot,
			outerItem: outerItem, // human readable
			outerIndex: outerIndex,
			innerItem: innerItem, // human readable
			innerIndex: innerIndex
		)
	}

	help {
		"Keyboard shortcuts on list views:".postln;
		". : save bookmark".postln;
		", : go to bookmark".postln;
		"/ : set project home file dialog".postln;
		"! : go home (home root folder)".postln;
		"> : zoom in".postln;
		"< : zoom out".postln;
		"^ : zoom out".postln;
		"? : help".postln;
	}

	// Load and go to last saved bookmark position from preferences.
	load {
		var prefs;
		prefs = Preferences.get(this.class.name, key);
		prefs ?? {
			^postln("Could not get preferences for" + this.class.name + "at" + key);
		};
		homeDir = prefs[\homeDir];
		currentRoot = prefs[\currentRoot];
		outerIndex = prefs[\outerIndex];
		innerIndex = prefs[\innerIndex];
		this.changed(\setOuterListAndIndex,
			currentRoot.folders,
			outerIndex
		);
	}

	goHome {
		currentRoot = this.homeDir;
		this.getOuterItems;
	}

	setProjectHomeDialog {
		FileDialog({ | path |
			homeDir = PathName(path[0]).asDir;
			this.goHome;
		}, {}, 2)
	}
	getOuterItems {
		outerList = this.getOuterItemsList;
		if (outerList.size == 0) {
			^postln("No items found in" + currentRoot.fullPath);
		};
		outerIndex = 0;
		outerItem = outerList[outerIndex];
		this.changed(\outerItems);
		// when updating outer items, always update inner items too
		innerIndex = 0;
		this.getInnerItems;
	}

	// update contents of inner list when selecting new outer item
	getInnerItems {
		innerList = this.getInnerItemsList;
		innerItem = innerList[innerIndex];
		this.changed(\innerItems);
	}

	// subclasses may overwrite this:
	getOuterItemsList { ^this.currentRoot.folders; }
	getInnerItemsList { ^outerItem.entries; }
	// provide defaults for homeDir and currentRoot
	currentRoot { ^currentRoot ?? { currentRoot = this.homeDir } }
	homeDir { ^homeDir ?? { homeDir = (PathName(Platform.userHomeDir) +/+ "sc-projects").asDir; } }

	// make outerItem the currentRoot
	zoomIn {
		this.setCurrentRoot(outerItem);
	}

	setCurrentRoot { | argNewRoot |
		if (argNewRoot.folders.size == 0) {
			postln("Cannot zoom into this folder");
			postln(argNewRoot);
			"Because it has no subfolders".postln;
			^nil;
		};
		currentRoot = argNewRoot;
		this.getOuterItems;
	}

	// make currentRoot's superfolder the currentRoot;
	zoomOut {
		if (this.currentRoot.asDir.fullPath == this.homeDir.asDir.fullPath) {
			^postln("Cannot go above the homeDir: " + homeDir)
		};
		currentRoot = currentRoot.up;
		this.getOuterItems;
	}

	*gui {
		var fn;
		fn = this.new;
		{ fn.getOuterItems }.defer(0.1);
		^fn.hlayout(
			fn.outerListView,
			fn.innerListView
		)
	}
	outerListView {
		^ListView()
		.addNotifier(this, \setOuterListAndIndex, { | n, list, outerIndex |
			outerList = list;
			n.listener.items = outerList collect: _.shortName;
			n.listener.value = outerIndex;
			outerItem = outerList[outerIndex];
			innerList = outerItem.entries;
			// NOTE: deferred selectionAction triggers update of inner list!!!
		})
		.addNotifier(this, \outerItems, { | n |
			var index;
			// restore index overwritten by selectionAction!
			index = outerIndex;
			{
				outerIndex = index;
				this.changed(\outerItem);
			}.defer(0.01);
			n.listener.items = this.outerListNames;
		})
		.addNotifier(this, \outerItem, { | n |
			n.listener.value = outerIndex;
		})
		.selectionAction_({ | me |
			this.outerIndex = me.value;
		})
		.keyDownAction_(this.keyboardShortcutAction);
	}

	outerIndex_ { | val |
		outerIndex = val;
		outerItem = this.outerList[outerIndex];
		this.changed(\outerItem);
		this.getInnerItems;
	}
	outerListNames {
		^(outerList ? []) collect: _.shortName;
	}

	innerListView {
		^ListView()
		.addNotifier(this, \innerItems, { | n |
			// restore index overwritten by selectionAction!
			var index;
			index = innerIndex;
			{
				innerIndex = index;
				this.changed(\innerItem);
			}.defer(0.01);
			n.listener.items = this.innerListNames;
		})
		.addNotifier(this, \innerItem, { | n |
			n.listener.value = innerIndex;
		})
		.selectionAction_({ | me |
			this.innerIndex = me.value;
		})
		.keyDownAction_(this.keyboardShortcutAction);
	}

	innerIndex_ { | val |
		val ?? {
			"No items found in this folder".postln;
			outerItem.postln;
			^"Exiting. Please select a different folder".postln;
		};
		innerIndex = val;
		innerItem = innerList[innerIndex];
		this.changed(\innerItem);
	}

	innerListNames {
		^(innerList ? []) collect: _.shortName;
	}
	keyboardShortcutAction {
		^{ | me, char, modifiers, unicode, keycode, key |
			switch (char,
				$., { this.saveBookmark; },
				$,, { this.gotoBookmark; },
				$/, { this.setProjectHomeDialog; },
				$!, { this.goHome; },
				$>, { this.zoomIn; },
				$<, { this.zoomOut; },
				$^, { this.zoomOut; },
				$?, { this.help; },
				me.defaultKeyDownAction(char, modifiers, unicode, keycode, key);
			)
		}
	}
	saveBookmark { this.save }
	gotoBookmark { this.load }

}