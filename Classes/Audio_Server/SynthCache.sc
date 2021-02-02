/*  1 Feb 2021 17:02

- Create a function for playing a SynthDef in the envir of a Player/Mediator, providing argument values from envir and mapping busses when needed. (get arguments from synthdesclib). 
- Convert Functions to SynthDefs, send these to server. If needed, start a synth from the compiled SynthDef immediately after loading it, using "sync" mechanism.

Note: Wrapping functions to custom templates providing commonly used ugens (PlayBuf, GrainBuf, FFT/PV, SendTrig, etc.) should probably be done in additional methods to Function. 

*/

SynthCache {

	handle
	
}