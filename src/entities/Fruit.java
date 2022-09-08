package entities;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import application.Main;

public class Fruit {

	private static List<Dot> fruits = new ArrayList<>();
	private Dot dot;
	
	public Fruit(int x, int y)
		{ fruits.add(dot = new Dot(x, y)); }
	
	public static void addFruit(int startX, int startY)
		{ fruits.add(new Dot(startX, startY)); }
	
	public static void addRandomFruits(int startAreaX, int startAreaY, int width, int height, int totalFruits, List<Snake> snakes) {
		while (totalFruits-- > 0) {
			int x = 0;
			int y = 0;
			Position pos = new Position(0, 0);
			while (true) {
				x = new SecureRandom().nextInt(width) + startAreaX;
				y = new SecureRandom().nextInt(height) + startAreaY;
				pos.setValues(x, y);
				if (!containPosition(pos) &&
						!Main.getWalls().stream().map(dot -> dot.getPosition()).collect(Collectors.toList()).contains(pos) &&
						snakes.stream().filter(s -> s.containPosition(pos)).collect(Collectors.toList()).isEmpty())
							break;
			}
			addFruit(x, y);
		}
	}

	public static void addRandomFruit(int startAreaX, int startAreaY, int width, int height, List<Snake> snakes)
		{ addRandomFruits(startAreaX, startAreaY, width, height, 1, snakes); }

	public static int getTotalFruits()
		{ return fruits.size(); }
	
	public Position getPosition()
		{ return dot.getPosition(); }
	
	public void setPosition(Position position)
		{ dot.setPosition(position); }

	public void setPosition(Fruit b)
		{ setPosition(b.getPosition()); }
	
	public void setPosition(int x, int y)
		{ dot.setPosition(x, y); }

	public int getX()
		{ return dot.getX(); }
	
	public int getY()
		{ return dot.getY(); }
	
	public void setX(int x)
		{ dot.setX(x); }
	
	public void setY(int y)
		{ dot.setX(y); }
	
	public static List<Dot> getFruits()
		{ return fruits; }
	
	public static List<Position> getFruitsPositions()
		{ return fruits.stream().map(f -> f.getPosition()).collect(Collectors.toList()); }

	public static Boolean containPosition(Position pos) {
		for (Dot fruit : fruits)
			if (fruit.getPosition().equals(pos))
				return true;
		return false;
	}

}
