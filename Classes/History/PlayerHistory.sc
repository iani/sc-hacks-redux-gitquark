/* 14 Oct 2022 15:50

EventStream history cannot be recorded.
Synth history is recorded.

However, both Synhs and EvenStreams are managed by this gui.
EventStreams can be restarted without using history.

*/

PlayerHistory : MultiLevelIdentityDictionary {
	classvar default;

	// currently chosen environment and player:
	// Used by gui to obtain list items.
	var <envir, <player;

	*initClass {
		StartUp add: { this.enable; }
	}

	*enable {
		this.addNotifier(Function, \player, { | n, event, player, time, code |
			this.add(event, player, time, code);
		});
	}

	*disable {
		this.removeNotifier(Function, \player);
	}
	*default { ^default ?? { default = this.fromLib(\default) } }

	*add { | event, player, time, code |
		var all, thisOne;
		all = this.default;
		event ?? { event = currentEnvironment };
		thisOne = all.at(event, player);
		thisOne = thisOne add: [time, code];
		all.put(event, player, thisOne);
	}

	*at { | event, player |
		^this.default.at(event, player);
	}

	*gui { this.default.gui }
	gui {
		this.vlayout(
			HLayout(
				ListView()
				.items_(Mediator.envirNames.sort),
				ListView()
				.items_(Mediator.playerNames(
					\defaul
					// Mediator.envirNames.sort.first).sort
				).sort),
				ListView()
			),
			TextView()
		).name_("Player History")
	}
}