package entities;

import java.util.ArrayList;
import java.util.List;

import objmoveutils.Position;

public class Wall extends Position {

	private static List<Position> walls = new ArrayList<>();

	public Wall(int x, int y) {
		setPosition(x, y);
	}

	public static List<Position> getWalls() {
		return walls;
	}

	public static void addWall(Position position) {
		walls.add(position);
	}

	public static void addAll(List<Position> walls) {
		Wall.walls.addAll(walls);
	}
	
	public static boolean somethingHittedWall(Position pos) {
		for (Position p : walls)
			if (p.isOnSameTile(pos))
				return true;
		return false;
	}

}
