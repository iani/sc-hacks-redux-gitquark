/* 31 Aug 2022 11:57
Record all incoming data as well as evaluated code (when shared),
in a single array, stored in classvar "data".  Format:

[
	[timereceived, [msg]],
	[timereceived, [msg]]
	[timereceived, [msg]]
	...
	]

Current tests have shown that sclang can store an array with
4.000.000 elements without noticeable loss of performance.
This amounts to more than 10 hours of recording of OSC data at
a rate or 100 messages per second.

OSCDataPlayer will keep adding received messages to data as long
as it is enabled.

OSCDataPlayer records all messages received from OSC.changed, and stores
the time contained in the message as "timereceived".  It also records
all messages received from OscGroups.changed(\localcode), and stores
Main.elapsedTime as "timereceived".

*/

OSCDataPlayer {
	var <>data, <>from = 0, >to, <>addr, <>rate = 1, <routine;

	play { | argFrom = 0, argTo |

	}
}