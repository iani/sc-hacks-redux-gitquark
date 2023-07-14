/* 13 Jul 2023 10:20

*/

Param {
	var <model, <spec, <name, <value;

	*new { | model, spec |
		^this.newCopyArgs(model, spec).init;
	}

	init {
		name = spec.units.asSymbol;
		// spec = spec.asSpec;
		value = spec.default;
	}
	gui {
		^HLayout(
			StaticText().minWidth_(100)
			.minWidth_(100).string_(name),
			NumberBox().maxWidth_(80)
			.clipLo_(spec.clipLo)
			.clipHi_(spec.clipHi)
			.addNotifier(this, \value, { | n |
				n.listener.value = value;
				this.sendValueToSynthEnvir;
			})
			.value_(spec.default)
			.action_({ | me |
				value = me.value;
				// model.changed(name);
			}),
			Slider().orientation_(\horizontal)
			.addNotifier(this, \value, { | n |
				n.listener.value = spec.unmap(value);
			})
			.palette_(QPalette.light)
			.background_(Color.grey(0.65))
			.value_(spec.unmap(spec.default))
			.action_{ | me |
				value = spec.map(me.value);
				this.changed(\value);
			},
		)
	}

	sendValueToSynthEnvir {
		postln("sending" + value + "to param" + name + "of" + this.bufName);
		value.perform('@>', name, this.bufName);
	}

	bufName {
		^model.bufName;
		// ^model.bufName;
	}
	notifyModel {
		// model.notifyModel(index, name )
	}
}