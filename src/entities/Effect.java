package entities;

import enums.Effects;

public class Effect {

	private Effects effect;
	private int framesDuration;
	private Snake from;
	
	public Effect(Effects effect, int framesDuration, Snake from) {
		this.effect = effect;
		this.framesDuration = framesDuration;
		this.from = from;
	}

	public Effect(Effects effect, Snake from)
		{ this(effect, -1, from); }

	public Effect(Effects effect, int duration)
		{ this(effect, duration, null); }

	public Effect(Effects effect)
		{ this(effect, -1, null); }

	public Effects getEffect()
		{ return effect; }

	public int getFramesDuration()
		{ return framesDuration; }
	
	public int decDuration(int duration)
		{ return framesDuration -= duration; }

	public int incDuration(int duration)
		{ return framesDuration += duration; }

	public Snake getFrom()
		{ return from; }

}
