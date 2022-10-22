/* 27 Feb 2022 09:52

*/
+ Function {
	+> { | player, envir |
		^this.playInEnvir(player, envir);
	}

	playInEnvir { | player, envir |
		// TODO: add arguments setting, bus mapping
		var synth;
		envir = envir ? currentEnvironment.name;
		// postln("playInEnvir envir is now: " + envir);
		Mediator.wrap({
			// enable storing of source code:
			Function.changed(\player, envir, player, Main.elapsedTime,
				format("% +>.% %", this.def.sourceCode, envir, player.asCompileString);
			);
			if (Server.default.serverRunning) {
				// "INSIDE SERVER RUNNING - DEBUGGING".postln;
				// postln("currentEnvironment before the put" + currentEnvironment);
				// postln("the player where I will put it is" + player);
				currentEnvironment[player] = synth = this.play.notify(player, envir);
				// postln("currentEnvironment AFTER the put" + currentEnvironment);
			}{
				Server.default.waitForBoot({
					currentEnvironment[player] = synth = this.play.notify(player, envir)
				})
			}
		}, envir);
		// "DEBUGGING Just before leaving playInEnvir method".postln;
		// postln("envir is: " + envir);
		// postln("currentEnvironment is", currentEnvironment);
		^synth;
	}

	+>> { | cmdName, player |
		this.sendReply(cmdName, player)
	}

	sendReply { | cmdName, player, values = 1, replyID = 1 |
		// always save in environment \triggers (special envir)
		{
			SendReply.kr(
				this.value.kdsr, cmdName.asOscMessage,
				values.value, replyID.value
			)
		}.playInEnvir(player ? cmdName, \triggers)
	}
}
