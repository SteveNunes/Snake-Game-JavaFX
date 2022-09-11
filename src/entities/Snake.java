package entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import enums.Direction;
import enums.Effects;
import game.Game;
import gameutil.Position;
import javafx.scene.input.KeyCode;

public class Snake extends Position {

	private static List<Snake> snakes = new ArrayList<>();
	private static List<List<KeyCode>> playerKeys = 
		Arrays.asList(Arrays.asList(KeyCode.LEFT, KeyCode.UP, KeyCode.RIGHT, KeyCode.DOWN),
		Arrays.asList(KeyCode.A, KeyCode.W, KeyCode.D, KeyCode.S));
	
	private List<Position> body;
	private List<Position> headlessBody;
	private List<Effect> effects;
	private List<KeyCode> keys;

	private Direction direction;
	private int framesPerStep;
	private int speedVal;
	private int deadFrames;
	private int removeBody;
	
	public Snake(int startX, int startY, int initialBodySize, Direction direction, int framesPerStep) {
		super(startX, startY);
		this.direction = direction;
		this.framesPerStep = framesPerStep;
		body = new ArrayList<>();
		headlessBody = new ArrayList<>();
		effects = new ArrayList<>();
		speedVal = 0;
		removeBody = 0;
		deadFrames = -1;
		body.add(getPosition());
		while (--initialBodySize > 0) {
			incBody();
			getTail().incPositionByDirection(direction.getReverseDirection());
		}
		keys = playerKeys.get(snakes.size());
	}
	
	public List<KeyCode> getKeys()
		{ return keys; }
	
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
	
	public Boolean isUnderEffect(Effects effect) {
		for (Effect e : effects)
			if (e.getEffect() == effect)
				return true;
		return false;
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
		while (value > 0 && removeBody > 0) {
			removeBody--;
			value--;
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

	private void updateHeadlessBody() {
		headlessBody = new ArrayList<>(body);
		headlessBody.remove(0);
	}
	
	public void move() {
		if (removeBody > 0 && --removeBody >= 0 && body.size() > 3) {
			body.remove(body.size() - 1);
			updateHeadlessBody();
		}
		if (isDead() && deadFrames < getBodySize())
			deadFrames++;
		else if (!isDead() && ++speedVal >= framesPerStep) {
			speedVal = 0;
			for (int n = getBodySize() - 1; n > 0; n--)
				body.get(n).setPosition(body.get(n - 1).getPosition());
			updateHeadlessBody();
			decEffectsDuration();
			getHead().incPositionByDirection(direction);
			if (Game.getWalls().contains(getHead()) ||
					getHeadlessBody().contains(getHead()) ||
					!snakes.stream()
					.filter(s -> s != this && s.getBody().contains(getHead()))
					.collect(Collectors.toList()).isEmpty()) {
						deadFrames = 0;
						effects.clear();
			}
		}
	}

}
