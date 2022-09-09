package entities;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import enums.Effects;
import game.Game;
import gameutil.Position;

public class Fruit extends Position {

	private static List<Fruit> fruits = new ArrayList<>();
	private static int incSizeFruitProc = 70; // Se proc falhar, gera uma fruta que diminui ao invés de aumentar
	private static int effectFruitProc = 20; // Se proc passar, gera uma fruta de efeito ao invés da fruta de aumentar/diminuir o tamanho
	private static List<Effects> allowedEffects = new ArrayList<>();
	

	private int incSizeBy;
	private Effects effect;
	
	public Fruit(int x, int y) {
		super(x, y);
		incSizeBy = 0;
		effect = null;
	}

	public Fruit(int x, int y, int incSizeBy) {
		this(x, y);
		this.incSizeBy = incSizeBy;
	}
	
	public Fruit(int x, int y, Effects causeEffect) {
		this(x, y);
		effect = causeEffect;
	}

	public static void addFruit(Fruit fruit)
		{ fruits.add(fruit); }
	
	public static List<Fruit> addRandomFruits(int startAreaX, int startAreaY, int width, int height, int totalFruits, List<Snake> snakes) {
		List<Fruit> addedFruitsPosition = new ArrayList<>();
		while (totalFruits-- > 0) {
			int x = 0;
			int y = 0;
			Position pos = new Position(0, 0);
			while (true) {
				x = new SecureRandom().nextInt(width) + startAreaX;
				y = new SecureRandom().nextInt(height) + startAreaY;
				pos.setPosition(x, y);
				if (!Fruit.getFruitsPositions().contains(pos) && !Game.getWalls().contains(pos) &&
						snakes.stream().filter(snake -> snake.getBody().contains(pos)).collect(Collectors.toList()).isEmpty())
							break;
			}
			Fruit fruit = new Fruit(x, y);
			if (!allowedEffects.isEmpty() && new SecureRandom().nextInt(100) + 1 <= effectFruitProc)
				fruit.setEffect(allowedEffects.get(new SecureRandom().nextInt(allowedEffects.size())));
			else
				fruit.setIncSizeBy(new SecureRandom().nextInt(9) + 1 * (new SecureRandom().nextInt(100) + 1 <= incSizeFruitProc ? 1 : -1));
			addFruit(fruit);
			addedFruitsPosition.add(fruit);
		}
		return addedFruitsPosition;
	}

	public static Fruit addRandomFruit(int startAreaX, int startAreaY, int width, int height, List<Snake> snakes)
		{ return addRandomFruits(startAreaX, startAreaY, width, height, 1, snakes).get(0); }
	
	public Effects getEffect()
		{ return effect; }
	
	public void setEffect(Effects effect)
		{ this.effect = effect; }
	
	public int getIncSizeBy()
		{ return incSizeBy; }
	
	public void setIncSizeBy(int incSizeBy)
		{ this.incSizeBy = incSizeBy; }
	
	public static int getTotalFruits()
		{ return fruits.size(); }
	
	public static List<Fruit> getFruits()
		{ return fruits; }
	
	public static List<Position> getFruitsPositions()
		{ return fruits.stream().map(fruit -> fruit.getPosition()).collect(Collectors.toList()); }

	public static int getIncSizeFruitProc()
		{ return incSizeFruitProc; }

	public static void setIncSizeFruitProc(int incSizeFruitProc)
		{ Fruit.incSizeFruitProc = incSizeFruitProc; }

	public static int getEffectFruitProc()
		{ return effectFruitProc; }

	public static void setEffectFruitProc(int effectFruitProc)
		{ Fruit.effectFruitProc = effectFruitProc; }

	public static List<Effects> getAllowedEffects()
		{ return allowedEffects; }
	
	public static void addEffectToAllowedEffects(Effects effect) {
		if (!allowedEffects.contains(effect))
			allowedEffects.add(effect);
	}

	public static void removeEffectToAllowedEffects(Effects effect) {
		if (allowedEffects.contains(effect))
			allowedEffects.remove(effect);
	}

}
