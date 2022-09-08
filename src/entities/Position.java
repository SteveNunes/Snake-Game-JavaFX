package entities;

import java.util.Objects;

public class Position {

	private int x;
	private int y;
	
	public Position(int x, int y)
		{ setValues(x, y); }
	
	public Position(Position position)
		{ setValues(position); }
	
	public int getX()
		{ return x; }
	
	public int getY()
		{ return y; }
	
	public void setX(int x)
		{ this.x = x; }
	
	public void setY(int y)
		{ this.y = y; }
	
	public void incX(int value)
		{ x += value; }
	
	public void incY(int value)
		{ y += value; }

	public void setValues(int x, int y) {
		setX(x);
		setY(y);
	}
	
	public void setValues(Position position)
		{ setValues(position.getX(), position.getY()); }
	
	public void incValues(int incX, int incY) {
		incX(incX);
		incY(incY);
	}
	
	@Override
	public int hashCode()
		{ return Objects.hash(x, y); }

	@Override
	public boolean equals(Object obj) {
		Position other = (Position) obj;
		return obj != null && y == other.y && x == other.x;
	}

	public static Boolean equals(Position position1, Position position2)
		{ return position1.getX() == position2.getX() && position1.getY() == position2.getY(); }
	
	@Override
	public String toString()
		{ return "[" + getY() + "," + getX() + "]"; }
	
}
