/* 30 Jan 2022 10:44
Adding some commmonly used ugens to a signal source:

Basic:
	Gated adsr envelope

To explore:
	PV effects
	Grain effects
	Pan (various versions)
	Resonance and filters
*/

+ UGen {
	// early test: return self. Just making sure this works
	idem { ^this }

	adsr { | attackTime=0.01, decayTime=0.3, sustainLevel=1, releaseTime=1.0,
		peakLevel=1.0, curve = -4.0, bias = 0.0 |
		^this *
		Env.adsr(attackTime, decayTime, sustainLevel, releaseTime, peakLevel, curve, bias)
		.kr(doneAction: 2, gate: \gate.kr(1))
	}

}