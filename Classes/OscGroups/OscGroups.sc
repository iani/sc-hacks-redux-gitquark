/* 23 May 2021 14:02
Share evaluated code via OSCGroups
(see link Ross Bencina...)

This could be a NamedSingleton, but let's keep things simple for now.
*/

OscGroups {
	classvar <sendAddress, <oscRecvFunc;

	*initClass {
		StartUp add: { this.init };
	}

	*init {
		"INITING OSCGROUPS =====================================".postln;
		sendAddress = NetAddr("127.0.0.1", 22244);
		oscRecvFunc = OSCFunc({ | msg |
			var code, result;
			code = msg[1];
			postf("REMOTE EVALUATION: %\n", code);
			result = thisProcess.interpreter.interpret(code.asString);
			postf("remote: -> %\n", result);
		}, "/code", recvPort: 22245).fix;
		
		thisProcess.interpreter.preProcessor = { | code |
			\tester.changed(\code, code);
			code;
		};

		\forwarder.addNotifier(\tester, \code, { | notifier, message |
			sendAddress.sendMsg('/code', message);
		});
	}
}