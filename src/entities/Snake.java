package entities;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import enums.Direction;
import enums.Effects;
import game.Game;
import gameutil.Position;
import javafx.scene.input.KeyCode;

public class Snake extends Position {

	private static List<Snake> snakes = new ArrayList<>();

	private static List<List<KeyCode>> playerKeys = 
		Arrays.asList(Arrays.asList(KeyCode.LEFT, KeyCode.UP, KeyCode.RIGHT, KeyCode.DOWN),
		Arrays.asList(KeyCode.A, KeyCode.W, KeyCode.D, KeyCode.S),
		Arrays.asList(KeyCode.J, KeyCode.I, KeyCode.L, KeyCode.K));
	
	private List<Position> body;
	private List<Position> headlessBody;
	private List<Effect> effects;
	private List<KeyCode> keys;

	private Direction direction;
	private int framesPerStep;
	private int speedVal;
	private int deadFrames;
	private int removeBody;
	
	public Snake(int startX, int startY, int initialBodySize, Direction direction) {
		super(startX, startY);
		this.direction = direction;
		framesPerStep = Game.defaultFramesPerStep();
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
		keys = playerKeys.get(getSnakes().size());
	}
	
	public void swapPositon(Snake anotherSnake) {
		List<Position> me = new ArrayList<>(body);
		List<Position> other = new ArrayList<>(anotherSnake.body);
		body = other;
		updateHeadlessBody();
		anotherSnake.body = me;
		anotherSnake.updateHeadlessBody();
		Direction dir = direction;
		direction = anotherSnake.direction;
		anotherSnake.direction = dir;
	}
	
	public List<KeyCode> getKeys()
		{ return keys; }
	
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
	
	private Boolean isUnderAnEffectThatNotAllowToAccelerate() {
		for (Effect effect : effects)
			if (effect.getEffect().notAbleToAcelerateByHoldingKey())
				return true;
		return false;
	}
	
	private Direction directionThroughEffect(Direction direction) {
		if (isUnderEffect(Effects.CLOCKWISE_CONTROLS))
			return direction.getClockwiseDirection(2);
		if (isUnderEffect(Effects.REVERSE_CLOCKWISE_CONTROLS))
			return direction.getClockwiseDirection(-2);
		if (isUnderEffect(Effects.REVERSE_CONTROLS))
			return direction.getClockwiseDirection(4);
		return direction;
	}
	
	public void setDirection(Direction direction) {
		direction = directionThroughEffect(direction);
		if (!isDead() && (direction != this.direction || !isUnderAnEffectThatNotAllowToAccelerate())) {
			this.direction = direction;
			if (!isUnderAnEffectThatNotAllowToAccelerate())
				speedVal = framesPerStep;
		}
	}
	
	public boolean canTurnToDirection(Direction direction) {
		direction = directionThroughEffect(direction);
		Position pos = new Position(getHead());
		pos.incPositionByDirection(direction);
		return !pos.equals(body.get(1));
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
		for (int n = 0; n < effects.size(); n++) {
			Effects effect = effects.get(n).getEffect();
			if (effects.get(n).decDuration(1) <= 0) {
				effects.remove(n--);
				if (effect == Effects.DROP_BODY_AS_WALL_AFTER_FEW_STEPS)
					dropBodyAsWall(3);
				if (effect == Effects.MIN_SPEED ||
						effect == Effects.HALF_SPEED ||
						effect == Effects.DOUBLE_SPEED ||
						effect == Effects.QUAD_SPEED)
							framesPerStep = Game.defaultFramesPerStep();
			}
		}
	}

	private void updateHeadlessBody()
		{ headlessBody = new ArrayList<>(body.subList(1, body.size())); }
	
	public void removeTailDot(int quantityToRemove) {
		body = body.subList(0, body.size() - quantityToRemove);
		updateHeadlessBody();
	}
	
	public void move(int keyPressedType) {
		/**
		 * keyPressedType == -1 - Chamado internamente
		 * keyPressedType == 0 - Chamado por pressão única de tecla
		 * keyPressedType == 1 - Chamado por pressão pressionada de tecla
		 */
		if (removeBody > 0 && --removeBody >= 0 && body.size() > 3)
			removeTailDot(1);
		if (isDead() && deadFrames <= getBodySize())
			deadFrames++;
		else if (!isDead() && (!isUnderEffect(Effects.ONLY_MOVE_IF_CONSTANTLY_PRESS) || keyPressedType == 0) &&
							(!isUnderAnEffectThatNotAllowToAccelerate() || keyPressedType == -1) &&
							++speedVal >= framesPerStep) {
								speedVal = 0;
								for (int n = getBodySize() - 1; n > 0; n--)
									body.get(n).setPosition(body.get(n - 1).getPosition());
								getHead().incPositionByDirection(direction);
								updateHeadlessBody();
								decEffectsDuration();
								if (isUnderEffect(Effects.CONSTANTLY_CHANGES_SPEED_TO_RANDOM))
									framesPerStep = new SecureRandom().nextInt(105) + 15;
								else if (isUnderEffect(Effects.QUAD_SPEED))
									framesPerStep = 5;
								else if (isUnderEffect(Effects.DOUBLE_SPEED))
									framesPerStep = 15;
								else if (isUnderEffect(Effects.HALF_SPEED))
									framesPerStep = 60;
								else if (isUnderEffect(Effects.MIN_SPEED))
									framesPerStep = 120;
		}
	}
	
	public void dropBodyAsWall(int pos) {
		Game.drawBG(body.subList(pos + 1, body.size()));
		cutBodyFrom(pos);
	}

	private void cutBodyFrom(int pos)
		{ body = body.subList(0, pos); }

	public void setDead(boolean b) {
		if (deadFrames == -1)
			deadFrames = 0;
	}

	public void clearEffects()
		{ effects.clear(); }
	
	public static List<Snake> getSnakes()
		{ return snakes; }

	public void teleportHeadTo(Position position) {
		for (int n = body.size() - 1; n > 0; n--)
			body.get(n).setPosition(body.get(n - 1));
		getHead().setPosition(position);
	}

}
