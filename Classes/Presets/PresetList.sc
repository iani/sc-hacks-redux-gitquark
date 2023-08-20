/* 29 Jul 2023 15:27
Create gui for handling an Array of Presets.

A PresetList can play its presets only in one player.  To keep things clean,
when a new preset is started, the synths from the previous preset
of this player are stopped.

*/

PresetList {
	classvar <dict, <players, <playerIdConverters;
	classvar <activeLists; // lists whose gui is open
	var <path, <code, <snippets, <presets;
	var <player; // setting the player stops the previous one
	var <currentPreset;
	var scoremenu; // cache

	scoremenu {
		^scoremenu ??
		{
			scoremenu = this.class.scoreNamesInLib
			collect: { | p | [p.name, { this.addScore(p) }] };
		}
	}

	currentPreset_ { | p |
		currentPreset = p;
		postln("PresetList:currentPreset_ new index is:" + p.index);
	}
	*initClass {
		StartUp add: { this.init }
	}
	*parentPath { ^PathName(this.filenameSymbol.asString).parentPath }
	*scriptsInLib {^PathName(this.parentPath +/+ "PresetScripts" +/+ "*.scd").pathMatch; }

	addScore { | name |
		var i = currentPreset.index;
		this.insert(ScorePlayer(this, i, name), i);
	}

	*init {
		activeLists = Set();
		this.loadPlayers;
		this.loadPresets;
	}

	*loadPlayers {
		var loaded;
		loaded = (this.parentPath +/+ "playerIdConverters.scd").load.value;
		players = loaded collect: _.player; // collect the player's names
		playerIdConverters = IdentityDictionary();
		loaded do: { | l | playerIdConverters[l.player] = l };
	}

	*loadPresets {
		dict = IdentityDictionary();
		this.scriptsInLib do: { | p |
			var n;
			n = PathName(p).fileNameWithoutExtension.asSymbol;
			n.postln;
			p.postln;
			dict[n] = this.new(p);
		}
	}

	*fromPath { | path, player | ^this.newCopyArgs(path).init(player ?? { players.first }); }
	// To be rewritten:
	*new { | path, player | ^this.newCopyArgs(path).init(player ?? { players.first }); }

	*allNames { ^dict.keys.asArray.sort}
	*all { ^this.allNames collect: { | n | dict[n] } }
	*first { ^this.all.first }

	init { | argPlayer |
		this.reload;
		player = argPlayer; // gui's should not permit 2 players in same system?
		// when a list opens, it checks available players by consulting activeLists.
	}

	reload {
		this.readCode;
		this.makeSnippets;
		this.makePresets;
		this.changed(\reload); // remove preset views
		this.changed(\remakeViews); // remake preset views
		currentPreset = presets.first;
	}

	readCode { code = File(path, "r").readAllString }
	makeSnippets { snippets = code.snippets;  }
	makePresets {
		presets = snippets collect: { | s, i | this.makePreset(s, i) };
	}

	makePreset { | s, i |
		var seed;
		seed = s.interpret;
		^switch (seed.class,
			Event, { Preset.newCopyArgs(this, i, s).importDict(seed) },
			Symbol, { ScorePlayer(this, i, seed);},
			String, { ScorePlayer(this, i, seed); }
		)
	}

	makeScore { | s, i |
		^ScorePlayer(this, i, s)
	}

	openSource { Document open: path }
	name { ^PathName(path).fileNameWithoutExtension.asSymbol }

	gui {
		this.addActive;
		Registry(this, this.name, {
			this.changed(\activeLists);
			PresetListGui(this).gui;
		})
	}

	*playerMenu {
		// this.availablePlayers.postln;
		^this.availablePlayers collect: { | p | [p, { | me |
			// postln("you selected player" + p ++". Now making gui!");
			// TODO: customize path choice.
			// PresetList(this.first.path, p.asSymbol).gui;
			this.presetListChoiceGui(p)
		}] }
	}

	*presetListChoiceGui { | p |
		^this.vlayoutKey(\listChoice,
			StaticText().string_("Choose preset list for player" + p + "(Press enter to open)"),
			ListView().items_(dict.keys.asArray.sort)
			.action_({ | me |
				postln("Selected preset:" + me.item);
				// postln("chosen preset" + dict[me.item]);
			})
			.enterKeyAction_({ | me |
				// postln("my item" + me.item);
				postln("Selected preset:" + me.item);
				// postln("chosen preset" + dict[me.item]);
				// postln("chosen preset" + dict[me.item]);
				postln("chosen player" + p);
				postln("chosen path" + dict[me.item].path);
				this.fromPath(dict[me.item].path, p).gui;
			})
		)
	}

	*presetSelectionGui {
		var presetnames, selectedPreset, selectedPlayer;
		this.loadPresets; // update every time
		presetnames = dict.keys.asArray.sort;
		selectedPreset = presetnames.first;
		selectedPlayer = this.availablePlayers.first;
		this.vlayoutKey(\presetSelection,
			HLayout(
				StaticText().string_("SelectPreset:"),
				StaticText().string_("Select Player:")
			),
			HLayout(
				ListView()
				.items_(presetnames)
				.action_({ | me | selectedPreset = me.item.postln; }),
				ListView()
				.items_(this.availablePlayers)
				.action_({ | me | selectedPlayer = me.item.postln; })
				.addNotifier(this, \activeLists, { | n |
					n.listener.items = this.availablePlayers;
					selectedPlayer = this.availablePlayers.first;
				})
			),
			Button().states_([["open"]])
			.action_({
				postln("you selected preset list:" + selectedPreset + "and player:" + selectedPlayer);
				// postln("the preset list has player" + selectedPreset.player);
				postln("the path is" + dict[selectedPreset].path);
				this.fromPath(dict[selectedPreset].path, selectedPlayer).gui;
			})
		)
		.bounds_(Rect(0, 0, 400, 300).center_(Window.availableBounds.center))
	}

	availablePlayers { ^this.class.availablePlayers }

	*availablePlayers {
		var available;
		available = players.copy;
		// postln("available players before test:" + available);
		// postln("activeLists:" + activeLists);
		activeLists do: available.remove(_);
		// postln("available players after test:" + available);
		^available;
	}

	addActive {
		activeLists add: this.player;
		this.class.changed(\activeLists);
	}

	removeActive {
		// postln("before removeActive availablePlayers:" + this.availablePlayers);
		activeLists remove: this.player;
		this.class.changed(\activeLists);
		// postln("after removeActive availablePlayers:" + this.availablePlayers);
	}

	save {
		postln("Saving" + presets.size + "presets to");
		postln(path);
		post("...");
		File.use(path, "w", { | f |
			f write: format ("/*presets for % saved at %*/\n", player, Date.getDate.stamp);
			presets do: { | p | f write: p.asScript; };
			f write: "\n/* THE END */";
		});
		"done.".postln;
	}

	// NOTE: remove/add view outside of these methods!
	remove { | preset | presets remove: preset; this.renumber; }
	renumber { presets do: { | p, i | p.index = i; } }
	insert { | item, index |
		item.presetList = this;
		presets = presets.insert(index, item);
		this.changed(\insert, item.view, index);
		this.renumber;
	}

	clean { presets do: _.clean; } // remove legacy keys

	bufferWindow { | buf = \default |
		var gui;
		// postln("Debugging PresetList:bufferWindow. BEFORE THE REGISTRY" + gui);
		Registry.at(this, \bufferWindow).postln;
		gui = Registry(this, \bufferWindow, { SoundBufferGui().gui });
		gui.buffer = buf;
		// Registry is broken with closing windows - fixing so that windiw will be made again.
		// TODO: Fix/debug this!
		this.addNotifier(gui, \closed, {
			Registry.remove(this, \bufferWindow);
		});
		this.addNotifier(gui, \selection, { | n, sel, start, end |
			if (sel != 63) { // skip diverted selection from SoundFileGui
				// [buf, start, end].postln;
				// postln("current preset" + currentPreset + "dict" + dict);
				#start, end = [start, end].sort;
				currentPreset.setBufSelection(start, end);
			};
			// postln("PresetList bufferWindow" + buf + args);
		})
	}
}