/*
EventPattern can be embedded in another Pattern, 
in similar way as Pdef can be embedded in a Pseq (see Pdef help entry).

IZ Mon, Apr 21 2014, 09:58 EEST

*/

EventPattern : Pattern {
	var <>event; // contains patterns

	*new { | event | ^this.newCopyArgs(event ?? { () }) }

	asStream { ^EventStream(event) }

	pattern { ^this }

	addEvent { | inEvent | // also accepts key-value pairs in Arrays
		// Add event's keys/values and also update the event
		// of the currently playing stream.
		inEvent keysValuesDo: { | key value |
			event[key] = value;
			if (key === \degree) {
				event[\freq] = nil;
				event[\note] = nil;
			};
			if (key === \note) {
				event[\freq] = nil;
			}
		}
	}
	
	put { | key, value |
		event[key] = value
	}
}

EventStream : Stream {
	var <>event; // contains streams
	*new { | event |
		^super.new.initEventStream(event);
	}

	initEventStream { | inEvent |
		event = ();
		inEvent keysValuesDo: { | key, value | event[key] = value.asStream(this); };
	}

	next { | inEvent |
		// If inEvent is provided, add its contents,
		// filtering them through own values.
		var outEvent, outValue;

		if (inEvent.isNil) {
			outEvent = ();
			event keysValuesDo: { | key, value |
				outValue = value.next(this);
				if (outValue.isNil) { ^nil };
				outEvent[key] = outValue;
			}
		}{
			// Use inEvent as main event to play,
			// and then filter any of its values through the present event
			outEvent = inEvent.copy;
			outEvent use: { //  evaluate using outEvent as environment
				// makes outEvent values available as environmentVariables
				event keysValuesDo: { | key value |
					outValue = value.(this);
					if (outValue.isNil) { ^nil };
					outEvent [key] = outValue;
				}
			}
		};
		^outEvent;
	}

	addEvent { | inEvent |
		inEvent keysValuesDo: { | key, value |
			event[key] = value.asStream;
			if (key === \degree) {
				event[\freq] = nil;
				event[\note] = nil;
			};
			if (key === \note) {
				event[\freq] = nil;
			}
		}
	}

	/* Needed to embed an EventPattern in a Stream as in: 
		Pseq([EventPattern((degree: (1..8).pseq(2)))]).play;
	*/
	embedInStream { arg inval;
		var outval;
		// this.changed(\started); // Put this in when the need arises.
		while {
			outval = this.next;
			outval.notNil;
		}{
			outval.yield;
		};
		// this.changed(\stopped); // Put this in when the need arises.
		nil;
	}

	play2 {
		var myevent;
		postf("% starting to build playing mechanism from scratch\n", this);
		postf("this is what happens when I call next to self: %\n",
			myevent = this.next);
		^myevent;
		/* 
			per default, myevent has no parent.
			myevent.play however provides defaultParentEvent as parent.
		*/
		
	}
}
