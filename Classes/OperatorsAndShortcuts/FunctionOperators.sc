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
			// Function.changed(\player, envir, player, Main.elapsedTime,
			// 	format("% +>.% %", this.def.sourceCode, envir, player.asCompileString)
			// );
			if (Server.default.serverRunning) {
				currentEnvironment[player] = synth = this.play(player: player, envir: envir)
				.notify(player, envir);
			}{
				Server.default.waitForBoot({
					currentEnvironment[player] = synth = this.play(player: player, envir: envir)
					.notify(player, envir)
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

	**> { | player, envir | // infinite loop in envir
		{
			inf do: {
				var dur;
				dur = this.(player, envir);
				if (dur.isKindOf(SimpleNumber).not) { dur = 1 };
				dur.wait;
			}
		}.routineInEnvir(player, envir);
	}

	*> { | player, envir | // play as routine
		this.routineInEnvir(player, envir);
	}

	routineInEnvir { | player, envir |
		var routine;
		envir = envir ? currentEnvironment.name;
		Mediator.wrap({
			currentEnvironment[player] = routine = this.fork;
		}, envir);
		^routine
	}
}
