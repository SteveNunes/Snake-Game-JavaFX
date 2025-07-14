package entities;

import java.util.ArrayList;
import java.util.List;

import objmoveutils.Position;

public class Hole extends Position {

	private static List<Hole> holes = new ArrayList<>();

	private Hole hole;

	public Hole(double x, double y) {
		setPosition(x, y);
		hole = null;
	}

	public static void addHole(int x, int y) {
		holes.add(new Hole(x, y));
	}

	public static void addHole(Position position) {
		holes.add(new Hole(position.getX(), position.getY()));
	}

	public static void connectHoles(Hole hole1, Hole hole2) {
		hole1.hole = hole2;
		hole2.hole = hole1;
	}

	public static void connectLastAddedHoles() {
		if (holes.size() == 0)
			throw new RuntimeException("Must have at least 2 holes for call this method");
		if (holes.size() % 2 > 0)
			throw new RuntimeException("The number of holes must be even for call this method");
		holes.get(holes.size() - 1).hole = holes.get(holes.size() - 2);
		holes.get(holes.size() - 2).hole = holes.get(holes.size() - 1);
	}

	public Hole getConnectedHole() {
		return hole;
	}

	public static List<Hole> getHoles() {
		return holes;
	}

	public static Hole getHoleAt(Position position) {
		for (Hole hole : holes)
			if (hole.isOnSameTile(position))
				return hole;
		return null;
	}

}
