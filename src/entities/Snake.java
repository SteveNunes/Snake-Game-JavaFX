package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import application.Main;
import enums.Direction;

public class Snake {

	private Direction direction;
	private List<Dot> body = new ArrayList<>();
	private Dot dot;
	private int speed;
	private int speedVal;
	private Boolean isDead;
	
	private Snake(int startX, int startY, Direction dir, int initialSpeed) {
		body.add(dot = new Dot(startX, startY));
		direction = dir;
		speed = initialSpeed;
		speedVal = 0;
		isDead = false;
	}
	
	public Snake(int startX, int startY, int initialSnakeSize, Direction direction, int initialSpeed) {
		this(startX, startY, direction, initialSpeed);
		while (--initialSnakeSize > 0)
			incBody();
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
		speedVal = speed;
	}
	
	public void incBody() {
		body.add(new Dot(getTail().getX(), getTail().getY()));
	}

	public Boolean isHead()
		{ return getHead() == dot; }
	
	public Boolean isTail()
		{ return getTail() == dot; }
	
	public List<Dot> getBody()
		{ return body; }

	public Dot getHead()
		{ return body.get(0); }

	public Dot getTail()
		{ return body.get(body.size() - 1); }
	
	public List<Dot> getSnakeBody()
		{ return body; }
	
	public int getSnakeBodySize()
		{ return body.size(); }
	
	public Boolean isDead()
		{ return isDead; }
	
	public void move() {
		if (!isDead() && ++speedVal >= speed) {
			speedVal = 0;
			for (int n = getSnakeBodySize() - 1; n > 0; n--)
				body.get(n).setPosition(body.get(n - 1).getPosition());
			if (direction == Direction.LEFT)
				getHead().getPosition().incX(-1);
			else if (direction == Direction.RIGHT)
				getHead().getPosition().incX(1);
			else if (direction == Direction.UP)
				getHead().getPosition().incY(-1);
			else
				getHead().getPosition().incY(1);
			isDead = Main.getWalls().contains(getHead()) || 
					getSnakeBodySize() > 4 && getSnakeBody().stream().filter(d -> d != getHead()).collect(Collectors.toList()).contains(getHead());
		}
	}
	
	public Direction getDirection()
		{ return direction; }
	
	public void setDirection(Direction dir) {
		direction = dir;
		speedVal = speed;
	}

	public void setSnakePosition(Snake b)
		{ body = new ArrayList<>(b.getSnakeBody()); }
	
	public Position getPosition()
		{ return dot.getPosition(); }

	public void setPosition(int x, int y)
		{ dot.setPosition(x, y); }

	public void setPosition(Position position)
		{ dot.setPosition(position); }

	public int getX()
		{ return dot.getX(); }
	
	public int getY()
		{ return dot.getY(); }
	
	public void setX(int x)
		{ dot.setX(x); }
	
	public void setY(int y)
		{ dot.setX(y); }

	public  Boolean containDot(Dot dot) {
		return body.contains(dot);
	}

	public Boolean containPosition(Position pos) {
		for (Dot dot : body)
			if (dot.getPosition().equals(pos))
				return true;
		return false;
	}
	
}
