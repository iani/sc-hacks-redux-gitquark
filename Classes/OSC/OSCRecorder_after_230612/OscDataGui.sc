/* 14 Jun 2023 01:03

*/

OscDataFileList {
	classvar >fileListHistoryPath;
	classvar fileListHistory;
	classvar <selectedList, <selectedPath;

	*gui {
		fileListHistory ?? { this.readFileListHistory; };
		this.vlayout(
			HLayout(
				Button().states_([["Add Filelist"]])
				.action_({ this.addListFromUser }),
				Button().states_([["Open in GUI"]])
				.action_({ this.changed(\viewSelection) }),
				Button().states_([["Edit"]])
				.action_({ this.changed(\editSelection) })
			),
			HLayout(
				ListView() // List of lists
				.palette_(QPalette.light
					.highlight_(Color(1.0, 0.9, 0.7))
					.highlightText_(Color(0.0, 0.0, 0.0))
				)
				.addNotifier(fileListHistory, \history, { | n |
					n.listener.items = fileListHistory.lists collect: _.asString;
					selectedList = fileListHistory.lists.first;
					this.changed(\selectedList);
				})
				.action_({ | me |
					selectedList = fileListHistory.lists[me.value];
					fileListHistory.changed(\fileList, me.value);
					this.changed(\selectedList);
				})
				.keyDownAction_({ | me, char ... args |
					case
					{ char == 127.asAscii } {
							{ fileListHistory removeAt: me.value; }
							.confirm("Do you really want to delete" + me.item + "?")
					}
					{ char == $r } {
						{ | name |
							fileListHistory.lists[me.value].name = name;
							fileListHistory.save;
							this.changed(\mainList);
						}.inputText(
							fileListHistory.lists[me.value].name,
							"Enter a new name for " + me.item
						)
					}
					{ true }{ me.keyDownAction(me, char, *args)};
				})
				.addNotifier(this, \mainList, { | n |
					n.listener.items = fileListHistory.lists collect: _.asString
				}),
				ListView() // list of files in a list
				.palette_(QPalette.light
					.highlight_(Color(0.7, 1.0, 0.9))
					.highlightText_(Color(0.0, 0.0, 0.0))
				)
				.selectionMode_(\contiguous)
				.addNotifier(this, \selectedList, { | n |
					n.listener.items = selectedList.paths collect: _.basename;
					selectedPath = selectedList.paths.first;
				})
				.enterKeyAction_({ | me |
					// me.selection.postln;
					// selectedList.paths[me.selection].postln;
					this.makeOscDataGui(selectedList.paths[me.selection]);
					// OscData(selectedList.paths[me.selection]).gui;
				})
				.keyDownAction_({ | me, char ... args |
					case

					{ char == 127.asAscii } {
						// me.selection.postln;
						// me.items[me.selection].postln;
						{
							selectedList.removePathsAt(me.selection);
							fileListHistory.save;
							this changed: \selectedList;
						}.confirm(
							"Do you really want to remove" +
							me.items[me.selection] +
							"?"
						)
						// { fileListHistory removeAt: me.value; }
						// .confirm("Do you really want to delete" + me.item + "?")
					}
					{ char == $+ } {
						// "I will add some files to this slist".postln;
						var thePaths;
						thePaths = selectedList.paths;
						Dialog.openPanel({ | argPaths |
							argPaths do: { | p |
								thePaths = thePaths addString: p
							};
							selectedList.paths = thePaths;
							fileListHistory.save;
							postln("Added" + argPaths);
						}, multipleSelection: true)
					}
					{ char == $e } {
						this.changed(\editSelection);
					}
					{ char == $v } {
						this.changed(\viewSelection);
					}
					{ true } {
						me.keyDownAction(me, char, *args)
					};
				})
				.addNotifier(this, \viewSelection, { | n |
					// OscData(selectedList.paths[n.listener.selection]).gui;
					this.makeOscDataGui(selectedList.paths[n.listener.selection]);
				})
				.addNotifier(this, \editSelection, { | n |
					selectedList.paths[n.listener.selection] do: Document.open(_);
				})
			)
		);
		{ fileListHistory.changed(\history) }.defer(0.1);
	}

	*makeOscDataGui { | paths |
		// decide whether to use OscData or OscDataScore,
		// based on the header of the first file.
		var isCode;
		File.use(paths.first,"r", { | f |
			var h;
			// f.postln;
			h = f.getLine(1024); // .postln;
			if (h == "//code") {
				//	OscDataScore(paths).gui;
				// "THIS IS CODE".postln;
				isCode = true;
			}{
				// OscData(paths).gui;
				// "THIS IS MESSSAGES".postln;
				isCode = false;
			};
		});
		if (isCode) {
			OscDataScore(paths).gui;
		}{
			OscData(paths).gui;
		}

	}

	*addListFromUser {
		this.fileListHistory.addListFromUser;
	}

	*fileListHistory {
		fileListHistory ?? { this.readFileListHistory };
		^fileListHistory;
	}

	*readFileListHistory {
		fileListHistory = Object.readArchive(this.fileListHistoryPath);
		fileListHistory ?? {
			fileListHistory = FileListHistory(this.fileListHistoryPath);
			this.writeFileListHistory;
		}
	}

	*fileListHistoryPath {
		fileListHistoryPath ?? {
			fileListHistoryPath = Platform.userAppSupportDir +/+ "OscDataFileLists.scd";
		};
		^fileListHistoryPath;
	}

	*writeFileListHistory {
		fileListHistory.writeArchive(this.fileListHistoryPath);
	}

	// not yet used.
	*fileListHistoryPathDialog {
		FileDialog({ | path |
			fileListHistoryPath = path;
			this.readFileListHistory;
		})
	}

}