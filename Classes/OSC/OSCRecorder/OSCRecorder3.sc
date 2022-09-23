/* 30 Aug 2022 13:17
Redo of OSCRecorder2

Using new format to enable saving of code and of large files (> 100 entries).

OSCRecorder3.rootDir = "/tmp/";

*/

OSCRecorder3 {
	classvar <rootFolder = "OSCData", <subFolder = "", <>fileHeader = "", <data;
	classvar <>rootDir;
	classvar <>maxItems = 1000; // Keep files small!
	classvar <file; // the file where the data are stored.

	*initClass {
		ShutDown add: { this.closeFile };
	}
	*update { | self, cmd, msg, time, addr, port |
		this.addData(time, msg);
	}

	*rootFolder_ { | argRootFolder |
		rootFolder = argRootFolder;
		this.makeDirectory;
	}

	*subFolder_ { | argSubFolder |
		subFolder = argSubFolder;
		this.makeDirectory;
	}

	*addData { | time, msg |
		if (file.isNil or: { file.isOpen.not}) {
			^nil; // do not save or record if file is closed!
		};
		data = data add: [time, msg];
		file.putString("\n//:--[" ++ time.asCompileString ++ "]\n");
		file.putString(msg.asCompileString);
		this.saveIfNeeded;
	}

	*saveIfNeeded {
		if (data.size >= maxItems) {
			"saving OSC data in new file: ".post;
			this.fullPath.postln;
			this.saveAndContinue;
		}
	}

	*newFile {
		file = File.open(this.fullPath, "w");
	}

	*saveAndContinue {
		var oldFile;
		file !? {
			oldFile = file;
			this.newFile;
			{ oldFile.close; }.defer(0.1);
		};
		data = [];
	}

	*closeFile {
		// "DEBUGGING. file is:".postln;
		// file.postln;
		file !? {
			// "I will closse the file".postln;
			file.close;
		}
	}


	*folderPath {
		^(this.root +/+ rootFolder +/+ subFolder).fullPath;
	}

	*root {
		if (rootDir.isNil) {
			^PathName(Platform.userAppSupportDir);
		}{
			^PathName(rootDir)
		}
	}

	*fullPath {
		^this.folderPath +/+ fileHeader ++ Date.getDate.stamp ++ ".scd";
	}

	*makeDirectory {
		("mkdir -p " ++ this.folderPath.replace(" ", "\\ ")).unixCmd
	}

	*enable {
		{ // leave time for directory to exist before making file!
			this.makeDirectory;
			0.1.wait;
			this.newFile;
			OSC addDependant: this;
			this.addNotifier(OscGroups, \localcode, { | n, code |
				// "Testing recording of local code".postln;
				this.addData(Main.elapsedTime, ['/code', code]);
			}); // TODO: add code to self
		}.fork
	}

	*disable {
		OSC removeDependant: this;
		this.removeNotifier(OscGroups, \localcode);
		this.closeFile;
		// TODO: Check with OscGroups if \code is the message watched
		this.removeNotifier(OscGroups, \code);
	}

	*isEnabled { ^OSC.dependants includes: this }

	*addLocalCode { | code |
		// also record code executed locally
		if (this.isEnabled) {
			/*	*update { | self, cmd, msg, time, addr, port |
		this.addData(time, msg, addr.addr, port);
	}
			*/
			//			this.addData(this, '/code',
			//			['/code', code],
			//          Main.elapsedTime,
			//          NetAddr.localAddr.addr,
			//          NetAddr.localAddr.port
			//          )
		}
	}
}