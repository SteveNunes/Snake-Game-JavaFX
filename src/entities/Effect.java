package entities;

import enums.Effects;

public class Effect {

	private Effects effect;
	private int stepsDuration;
	private Snake from;

	public Effect(Effects effect, Snake from) {
		this.effect = effect;
		this.stepsDuration = Effects.getDuration(effect);
		this.from = from;
	}

	public Effect(Effects effect) {
		this(effect, null);
	}

	public Effects getEffect() {
		return effect;
	}

	public int getStepsDuration() {
		return stepsDuration;
	}

	public int decDuration(int duration) {
		return stepsDuration -= duration;
	}

	public int incDuration(int duration) {
		return stepsDuration += duration;
	}

	public Snake getFrom() {
		return from;
	}

}
