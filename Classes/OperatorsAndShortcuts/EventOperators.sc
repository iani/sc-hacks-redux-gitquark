/* 27 Feb 2022 09:49

*/

+ Event {
	+> { | key, envir | ^this.playInEnvir(key, envir) }

	playInEnvir { | key, envir |
		var atKey, new;
		Mediator.wrap({
			atKey = currentEnvironment[key];
			atKey.stop;
			new = this.splay(key);
			currentEnvironment[key] = new;
		}, envir);
		^new;
	}

	splay { | key | ^EventStream(this).start; }

	+>! { | key |
		var new;
		new = EventStream(this);
		currentEnvironment.put(key, new);
		^new;
	}

	++> { | key |
		var e;
		e = currentEnvironment[key];
		if (e isKindOf: EventStream) {
			^e addEvent: this;
		}{
			^this +> key
		}
	}

	@> { | beatKey |
		beatKey.beat.addDependant(EventStream(this));
	}

	addBeat { | beatKey |
		this @> (beatKey ? this);
	}
}
