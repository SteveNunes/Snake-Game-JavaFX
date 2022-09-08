package entities;

import java.util.Objects;

public class Dot {

	private Position position;
	private static int snakeDotSize = 20;
	
	public Dot(int x, int y, int dotSize) {
		snakeDotSize = dotSize;
		position = new Position(x, y);
	}
	
	public Dot(int x, int y)
		{ this(x, y, snakeDotSize); }

	public static int getSnakeDotSize()
		{ return snakeDotSize; }

	public static void setSnakeDotSize(int size)
		{ snakeDotSize = size; }

	public Position getPosition()
		{ return position; }
	
	public void setPosition(Dot snakeDot)
		{ setPosition(snakeDot.getPosition()); }

	public void setPosition(Position position)
		{ this.position = new Position(position); }

	public void setPosition(int x, int y)
		{ position.setValues(x, y); }
	
	public int getX()
		{ return position.getX(); }

	public int getY()
		{ return position.getY(); }

	public void setX(int x)
		{ position.setX(x); }

	public void setY(int y)
		{ position.setX(y); }

	@Override
	public int hashCode()
		{ return Objects.hash(position); }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		return ((Dot) obj).getPosition().equals(getPosition());
	}

}
