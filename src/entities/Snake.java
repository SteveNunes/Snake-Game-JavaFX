package entities;

import java.util.ArrayList;
import java.util.List;

import enums.Direction;
import enums.Effects;
import game.Game;
import gameutil.Position;

public class Snake extends Position {

	private static List<Snake> snakes = new ArrayList<>();
	private List<Position> body = new ArrayList<>();
	private List<Position> headlessBody = new ArrayList<>();
	private List<Effect> effects = new ArrayList<>();

	private Direction direction;
	private int framesPerStep;
	private int speedVal;
	private int deadFrames;
	private int removeBody;
	
	public Snake(int startX, int startY, int initialBodySize, Direction direction, int framesPerStep) {
		super(startX, startY);
		this.direction = direction;
		this.framesPerStep = framesPerStep;
		speedVal = 0;
		removeBody = 0;
		deadFrames = -1;
		body.add(getPosition());
		while (--initialBodySize > 0) {
			incBody();
			getTail().incPositionByDirection(direction.getReverseDirection());
		}
	}
	
	public static List<Snake> getSnakes()
		{ return snakes; }
	
	public List<Effect> getEffects()
		{ return effects; }

	public void addEffect(Effects effect) {
		for (Effect e : effects)
			if (e.getEffect() == effect)
				return;
		effects.add(new Effect(effect));
	}

	public void removeEffect(Effects effect) {
		for (Effect e : effects)
			if (e.getEffect() == effect) {
				effects.remove(e);
				return;
			}
	}
	
	public Direction getDirection()
		{ return direction; }
	
	public void setDirection(Direction direction) {
		if (!isDead()) {
			this.direction = direction;
			speedVal = framesPerStep;
		}
	}
	
	public int getFramesPerStep()
		{ return framesPerStep; }

	public void setFramesPerStep(int val) {
		if (!isDead()) {
			framesPerStep = val;
			speedVal = val;
		}
	}
	
	public List<Position> getBody()
		{ return body; }

	public List<Position> getHeadlessBody()
		{ return headlessBody; }

	public void incBody(int value) {
		if (value < 0) {
			removeBody = Math.abs(value);
			return;
		}
		while (--value >= 0)
			body.add(new Position(getTail()));
	}

	public void incBody()
		{ incBody(1); }

	public Boolean isHead()
		{ return this == getHead(); }
	
	public Boolean isTail()
		{ return this == getTail(); }
	
	public Position getHead()
		{ return body.get(0); }

	public Position getTail()
		{ return body.get(body.size() - 1); }
	
	public int getBodySize()
		{ return body.size(); }
	
	public Boolean isDead()
		{ return deadFrames > -1; }
	
	public int getDeadFrames()
		{ return deadFrames; }
	
	private void decEffectsDuration() {
		for (int n = 0; n < effects.size(); n++)
			if (effects.get(n).decDuration(1) <= 0)
				effects.remove(n--);
	}
	
	public void move() {
		decEffectsDuration();
		if (removeBody > 0 && --removeBody >= 0)
			body.remove(body.size() - 1);
		if (isDead() && deadFrames < getBodySize())
			deadFrames++;
		else if (!isDead() && ++speedVal >= framesPerStep) {
			speedVal = 0;
			for (int n = getBodySize() - 1; n > 0; n--)
				body.get(n).setPosition(body.get(n - 1).getPosition());
			headlessBody = new ArrayList<>(body);
			headlessBody.remove(0);
			getHead().incPositionByDirection(direction);
			if (Game.getWalls().contains(getHead()) || getHeadlessBody().contains(getHead()))
				deadFrames = 0;
		}
	}

}
