/* 23 May 2021 14:02

Share evaluated code via OSCGroups
(see link Ross Bencina...)

This could be a NamedSingleton, but let's keep things simple for now.

Note: You should have compiled OscGroupsClient and have started it via command line.

Example: 

./OscGroupClient 64.225.97.89 22242 22243 22244 22245 username userpass nikkgroup nikkpass

OscGroups.disable;
OscGroups.enable;
*/

OscGroups {
	classvar <>oscSendPort = 22244, <>oscRecvPort = 22245;
	classvar sendAddress, <oscRecvFunc;
	classvar <>verbose = false;
	classvar <codeMessage = '/code';
	classvar <>notifier; // Only if notifier is OscGroups, then
	// the message is broadcast via OscGroups
	// See methods enableCodeBroadcasting, disableCodeBroadcasting
	classvar <>localUser = \localuser; // TODO: delete this if it is not used!!!
	classvar localAddress;

	*initClass {
		StartUp add: {
			thisProcess.interpreter.preProcessor = { | code |
				// Interpreter.changed(\code, code);
				Interpreter.changed(\code, code);
				code;
			};
			localAddress = NetAddr.localAddr;
			this.activateCodeMessage;
		}
	}

	*activateCodeMessage {
		OSC.add(codeMessage, { | n, msg |
			var code;
			code = msg[1].asString;
			if (code.isSafe) {
				postf("========= Remote evaluation: ========= \n\(\n\%\n\)\n", code);
				{	// permit window operations via remote evaluated code
					code.interpret.postln;
					this.changed(\evalCode, code);
				}.defer;
			}{
				"WARNING: UNSAFE CODE RECEIVED!:".postln;
				code.postln;
			}
		}, \codeEvaluation)
	}

	*codeMessage_ { | newCodeMessage |
		OSC.remove(codeMessage, \codeEvaluation);
		codeMessage = newCodeMessage;
		this.activateCodeMessage;
	}

	// this may no longer be valid on  1 Sep 2022 20:13
	*isEnabled {
		^OSC.listensTo(codeMessage, codeMessage) and: {
			sendAddress.notNil
		};
	} // NEEDS CHECKING!
	*isForwarding {
		// ^thisProcess.interpreter.preProcessor.notNil;
		// "isForwarding not implemented! look into NOtification!".postln;
		// look into this:
		//			*listeningto { | notifier |
		^this isListeningTo: Interpreter;
	}

	*forwardprocessed { | message, newmessage, preprocessor |
		// see class MapXyz for a separate implementation of this.
		// process incoming values (normalize range etc.)
		// Send processed data to oscgroups AND TO SELF
		// with newmessage as osc message.
		// Group members as well as self react to new message.
		// Only forward messages received locally (from sensors etc.)
		message.asOscMessage >>>.processforward { | n, msg, time, addr, port |
			var processed;
			if (port == 57120) { // only forward locally received messages
				processed = preprocessor.(*msg[1..]);
				sendAddress.sendMsg(newmessage, *processed); // send to others
				localAddress.sendMsg(newmessage, *processed); // send to self
			}
		};
	}

	*send { | message | // send to oscgroups client
		sendAddress !? { sendAddress.sendMsg(*message); }
	}

	*forward { | message |
		// forward an osc message to OscGroupsClient
		// BUILT-IN SAFETY: ONLY FORWARD MESSAGES RECEIVED FROM LOCAL PORT 57120
		// DO NOT FORWARD MESSAGES FROM OSCGROUPS:
		// !!!!!!! PREVENT FEEDBACK LOOPS ON OSCGROUPS !!!!!!!
		var addr; // cache sendAddress obtained lazily
		addr = this.sendAddress;
		message ?? { message = '/minibee/data' }; // default message is '/minibee/data'
		message.asOscMessage >>>.forward { | n, msg, time, addr, port |
			if (verbose) {
				postln("forwarding " + msg);
				this.sendAddress.postln;
			};
			if (port == 57120) { this.sendAddress.sendMsg(*msg); };
		}
	}

	*unforward { | message | // stop forwarding message to OscGroupClient
		//
		message ?? { message = '/minibee/data' };
		message = message.asOscMessage;
		OSC.remove(message, 'forward');
	}

	*enable { | sendPort = 22244, receivePort = 22245 |
		postln("Enabling OscGroups. Send Port:" + sendPort + "Receive Port:" + receivePort);
		oscSendPort = sendPort;
		oscRecvPort = receivePort;
		this.makeSendAddress;
		this.enableCodeForwarding;
		this.enableCodeReception;
		this.enableCmdPeriod;
		"OscGroups enabled".postln;
		this.changedStatus;
	}
	*disable {
		this.disableCodeForwarding;
		this.disableCodeReception;
		this.disableCmdPeriod;
		"OscGroups disabled".postln;
		this.changedStatus;
	}

	*enableCmdPeriod { CmdPeriod add: this; }
	*disableCmdPeriod { CmdPeriod remove: this; }

	*broadcast { | message ... args |
		if (this.isEnabled) {
		sendAddress.sendMsg(message, *args);
		}{
			postln("OscGroups is disabled.");
			postln("Cannot broadcast " + message + args);
			postln("To enable OscGroups run: OscGrups.enable");
		}
	}

	*enableCodeForwarding {
		// send evaluated code to sendAddress using oscMessage and adding localUser
		this.addNotifier(Interpreter, \code, { | n, code |
			this.changed(\localcode, code); // OSCRecorder records the code here.
			postln("sending message" + codeMessage + "to address " + sendAddress);
			sendAddress.sendMsg(codeMessage, code);
		});
		this.changedStatus;
	}

	*disableCodeForwarding {
		 // deactivate sharing by settging Interpreter's preprocessor to nil.
		// localUser.unshare;
		this.removeNotifier(Interpreter, codeMessage);
		this.changedStatus;
	}

	*enableCodeReception {
		// TODO: Use oscRecvPort instead
		thisProcess.openUDPPort(22245); // oscRecvPort
		this.enableCodeEvaluation;
		this.changedStatus;
	}

	// obsolete - must rewrite for compatibility with setting codeMessage var!!!
	*enableCodeEvaluation {
		OSC.enableCodeEvaluation;
	}

	*disableCodeReception {
		codeMessage.unevalOSC;
		this.changedStatus;
	}

	*openUDPPort { // TODO: Use oscRecvPort instead
		thisProcess.openUDPPort(22245); // oscRecvPort
	}

	*changedStatus {  this.changed(\status) }

	*sendAddress { ^sendAddress ?? { sendAddress = this.makeSendAddress } }

	*makeSendAddress {
		sendAddress = NetAddr("127.0.0.1", oscSendPort);
		postf("OscGroups set OSC send port to: %\n", oscSendPort);
		^sendAddress;
	}

	*askLocalUser { "OscGroups askLocalUser method disabled" }

	*forceBroadcastCode { | string |
		// TODO: is this method superseeded by broadcast?
		// send string to sendAddress independently of current status.
		// Sends even if OscGroups is disabled.
		sendAddress.sendMsg(codeMessage, string, localUser);
	}

	*runLocally { | func |
		var isCodeForwarding;
		isCodeForwarding = this.isForwarding;
		// postln("BEFORE: should I restore forwarding?" + isCodeForwarding);
		this.disableCodeForwarding;
		func.value;
		// postln("AFTER: should I restore forwarding?" + isCodeForwarding);
		if (isCodeForwarding) {
			// "Enabling code f orwarding - return to original state".postln;
			this.enableCodeForwarding;

		}{
			// "I will not enable code forwarding!".postln;
		};
	}

	*cmdPeriod {
		// Remotely only execute core CmdPeriod method.
		sendAddress ?? { ^nil };
		"Sending CmdPeriod to OscGroups".postln;
		sendAddress.sendMsg(codeMessage, "OscGroups.remoteCmdPeriod")
	}

	*remoteCmdPeriod {
		// run basic cmdperiod actions when called via OscGroups
		// Skip cmdPeriod as it would loop sending cmd period to OscGroups
		SystemClock.clear;
		AppClock.clear;
		TempoClock.default.clear;
		// Following would cause endless loop inside OscGroups:
		// objects.copy.do({ arg item; item.doOnCmdPeriod;  });
		Server.hardFreeAll; // stop all sounds on local servers
		Server.resumeThreads;
	}

	*startClient {
		// 22243 port number should be different for each computer running in one LAN
		"OscGroupClient 64.225.97.89 22242 22243 22244 22245 iani ianipass nikkgroup nikkpass".runInTerminal;
	}

	*gui { // Window with button to enable / disable code broadcasting
		var myself;
		myself = this;
		this.br_(200, 100).window({ | w |
			w.layout = VLayout(
				Button()
				.states_([
					["Disable OscGroups"],
					["Enable OscGroups"]
				])
				.action_({ | me |
					myself.perform([
						\enable,
						\disable
					][me.value]);
				})
				.addNotifier(this, \status, { | n |
					// "Checking OscGroups gui status button".postln;
					// n.notifier.postln;
					postf(
						"osc groups enabled? %\n",
						n.notifier.isEnabled
					);
					if (n.notifier.isEnabled) {
						n.listener.value = 0
					}{
						n.listener.value = 1
					}
				}),
				Button()
				.states_([
					["Disable code forwarding"],
					["Enable code forwarding"]
				])
				.action_({ | me |
					myself.perform([
						\enableCodeForwarding,
						\disableCodeForwarding
					][me.value]);
				})
				.addNotifier(this, \status, { | n |
					// "Checking OscGroups gui forwarding button".postln;
					// n.notifier.postln;
					postf(
						"osc groups forwarding? %\n",
						n.notifier.isForwarding
					);
					if (n.notifier.isForwarding) {
						n.listener.value = 0
					}{
						n.listener.value = 1
					}
				})
			);
			this.changed(\status);
		});
	}
}