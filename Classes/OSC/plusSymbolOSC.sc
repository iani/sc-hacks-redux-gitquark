/* 14 Feb 2022 10:57
Use Notification to add OSC functions.
*/

+ Symbol {
	>>> { | func, receiver |
		OSC.add(receiver ? this, this, func)
	}

	<<< { | receiver |
		this.removeOSC(receiver);
	}

	removeOSC { | receiver |
		OSC.remove(receiver ? this, this);
	}

	>>@ { | address |
		this.forwardOSC(address)
	}

	forwardOSC { | address, receiver = \forward |
		OSC.add(receiver, this, { | notification, message |
			// postf("received this over osc: %\n", message);
			// postf("I'll be forwarding it to: %\n", address);
			address.sendMsg(*message);
		});
	}

	unforwardOSC { | receiver = \forward |
		this.removeOSC(receiver);
	}

	evalOSC { | index = 1, receiver = \eval |
		OSC.add(receiver, this, { | notification, message |
			// postf("received this over osc: %\n", message);
			// postf("I'll be forwarding it to: %\n", address);
			thisProcess.interpreter.interpret(message[index]);
		})
	}

	unevalOSC { | receiver = \eval |
		this.removeOSC(receiver);
	}

	share { | address, message = \code, userName |
		thisProcess.interpreter.preprocessor = { | code |
			postf(
				"I will send this code %
to this address: %\n with this message %
and this user name: %\n",
				code, address, message, userName
			)
		}
	}

	unshare {
		thisProcess.interpreter.preprocessor = nil;
		"Stopped sharing code with OSCGroups".postln;
	}

	record {

	}

	stopRecording {

	}

}