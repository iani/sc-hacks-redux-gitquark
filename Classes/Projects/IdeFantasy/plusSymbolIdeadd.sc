/*  8 Sep 2021 15:19
	Utilities for adding - removing actions to symbols listening
	to IdeFantasy notifications issued by OSCFunctions.
*/

+ Symbol {
	oscify {
		// add initial / to self to make into a regular osc command name
		var myString;
		myString = this.asString;
		if (myString[0] == $/) { ^this };
		^("/" ++ myString).asSymbol;
	}

	fox { | action, sender |
		\x.addNotifier(sender ? IdeFantasy, this.oscify, { | notification, id, x, y z |
			action.(x)
		});
	}

	foy { | action, sender |
		\y.addNotifier(sender ? IdeFantasy, this.oscify, { | notification, id, x, y z |
			action.(y)
		});
	}

	foz { | action, sender |
		\z.addNotifier(sender ? IdeFantasy, this.oscify, { | notification, id, x, y z |
			action.(z)
		});
	}

	+++> { | action, key = \default |
		postf("adding IDE action named % to %\n", key, this);
		// TODO: make customizable sender (project name?)
		key.addNotifier(IdeFantasy, this.oscify, action);
	}

	---> { | key = \default | this remove: key }

	remove { | key = \default |
		postf("removing IDE action named % from %\n", key, this);
		// TODO: make customizable sender (project name?) + use better method name
		key.removeNotifier(IdeFantasy, this.oscify);
	}
	set { | param = \trig, value = 1 |
		currentEnvironment[this].set(param, value)
	}
}