package game;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import application.Main;
import entities.Fruit;
import entities.Hole;
import entities.Snake;
import entities.Wall;
import enums.Direction;
import enums.Effects;
import enums.GameMode;
import gameutil.FPSHandler;
import gameutil.KeyHandler;
import gui.util.ImageUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import objmoveutils.Position;
import util.Misc;

public class Game {
	
	public static Random random = new Random(new SecureRandom().nextInt(Integer.MAX_VALUE));
	private static Image sprites = ImageUtils.removeBgColor(new Image("file:Sprites\\sprites.png"), Color.valueOf("#326496"), 0);
	private static Image arena = new Image("file:Sprites\\arenas\\01.png");
	private static Menu mainMenu;
	private static FPSHandler fpsHandler = new FPSHandler(60);
	private static GameMode gameMode = GameMode.LOCAL_MULTIPLAYER;
	private static Snake mySnake = null;
	private static int framesPerStep = 5; //30
	private static int minSnaekSize = 6;
	private static Font fruitFont = new Font("Arial", 15);
	private static int dotSize = 20;
	
	public static int getDotSize()
		{ return dotSize; }
	
	public static int defaultFramesPerStep()
		{ return framesPerStep; }
	
	public static GameMode getGameMode()
		{ return gameMode; }
	
	private static void setupGame() {
		Snake.getSnakes().add(mySnake = new Snake(Main.getDotSize() * 12, 300, minSnakeSize(), Direction.DOWN));
		Snake.getSnakes().add(new Snake(Main.getDotSize() * 24, 300, minSnakeSize(), Direction.DOWN));
		Snake.getSnakes().add(new Snake(Main.getDotSize() * 36, 300, minSnakeSize(), Direction.DOWN));
		generateBGCanvas();
		generateFruits();
		generateHoles();
	}
	
	private static List<Direction> dirs = Arrays.asList(Direction.LEFT, Direction.UP, Direction.RIGHT, Direction.DOWN);

	private static void onKeyPress(KeyCode keyCode) {
		//System.out.println("PRESSED: " + keyCode);
		if (keyCode == KeyCode.NUMPAD1)
			Main.getGameBGCanvas().setVisible(!Main.getGameBGCanvas().isVisible());
		if (keyCode == KeyCode.NUMPAD2)
			Main.getSnakeCanvas().setVisible(!Main.getSnakeCanvas().isVisible());
		if (keyCode == KeyCode.NUMPAD3)
			Main.getGameFruitCanvas().setVisible(!Main.getGameFruitCanvas().isVisible());
		for (Snake snake : Snake.getSnakes())
			for (int n = 0; n < 4; n++)
				if (keyCode == snake.getKeys().get(n) && snake.canTurnToDirection(dirs.get(n))) {
					snake.setDirection(dirs.get(n));
					break;
				}
	}

	private static void onKeyHold(KeyCode keyCode) {
		//System.out.println("HOLD: " + keyCode);
	}

	private static void onKeyRelease(KeyCode keyCode) {
		//System.out.println("RELEASED: " + keyCode);
	}
	
	private static void clearCanvas(Canvas canvas) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setEffect(null);
		gc.clearRect(0, 0, Main.getScreenWidth(), Main.getScreenHeight());
		gc.setEffect(new BoxBlur(1, 1, Main.getLiearFiltering()));
	}

	private static void gameLoop() {
		int snakeId = 0;
		KeyHandler.holdKeyPoll();
		if (fpsHandler.ableToDraw())
			clearCanvas(Main.getSnakeCanvas());
		
		for (Snake snake : Snake.getSnakes()) {
			checkIfSnakeEnteredInAHole(snake);
			checkIfSnakeAteAFruit(snake);
			checkIfSnakeColidedWithOtherSnake(snake);
			drawSnake(snakeId);
			snakeId++;
		}

		fpsHandler.fpsCounter();
		Main.getMainStage().setTitle("JavaFX Snake - CPS: " + fpsHandler.getCPS() + " FPS: " + fpsHandler.getFPS());
		
		if (Main.windowsIsOpen())
			Misc.runLater(() -> gameLoop());
	}
	
	private static int aliveSnakes()
		{ return (int)Snake.getSnakes().stream().filter(s -> !s.isDead()).count(); }

	private static void checkIfSnakeEnteredInAHole(Snake snake) {
		Hole hole = Hole.getHoleAt(snake.getHead());
		Hole hole2 = hole == null ? null : hole.getConnectedHole();
		if (hole != null && !snake.getHeadlessBody().contains(hole2.getPosition()))
			snake.teleportHeadTo(hole2.getPosition());
	}
	
	private static void checkIfSnakeAteAFruit(Snake snake) {
		if (Fruit.getFruitsPositions().contains(snake.getHead().getPosition()))
			for (Fruit fruit : Fruit.getFruits())
				if (fruit.getPosition().equals(snake.getHead().getPosition())) {
					Fruit.getFruits().remove(fruit);
					Fruit.addRandomFruits(1 ,1 ,Main.getMaxWidth() / Main.getDotSize() - 1, Main.getMaxHeight() / Main.getDotSize() - 1, 1, Snake.getSnakes());
					fruitCanvasDrawFruits();
					if (fruit.getEffect() != null) {
						if (fruit.getEffect() == Effects.CLEAR_EFFECTS)
							snake.clearEffects();
						else if (fruit.getEffect() == Effects.TRANSFORM_FRUITS_INTO_WALL) {
							drawBG(Fruit.getFruitsPositions());
							for (Fruit del : Fruit.getFruits())
								Main.getGameFruitCanvas().getGraphicsContext2D().clearRect(del.getX(), del.getY(), Main.getDotSize(), Main.getDotSize());
							Fruit.getFruits().clear();
							generateFruits();
						}
						else if (fruit.getEffect() == Effects.SWAP_2_OPPONENT_POSITIONS) {
							Snake opponent1 = aliveSnakes() == 2 ? snake : getRandomOpponentSnake(snake);
							Snake opponent2 = getRandomOpponentSnake(opponent1);
							while (opponent2 == snake)
								opponent2 = getRandomOpponentSnake(opponent1);
							opponent1.swapPositon(opponent2);
						}
						else if (fruit.getEffect().causeEffectOnOthers() != null)
							getRandomOpponentSnake(snake).addEffect(fruit.getEffect().causeEffectOnOthers());
						else if (fruit.getEffect().isFriendly())
							snake.addEffect(fruit.getEffect());
						else
							getRandomOpponentSnake(snake).addEffect(fruit.getEffect());
					}
					else
						snake.incBody(fruit.getIncSizeBy());
					break;
				}
	}
	
	private static Snake getRandomOpponentSnake(Snake snake) {
		Snake randomSnake = Snake.getSnakes().get(random.nextInt(Snake.getSnakes().size()));
		while (snake != null && randomSnake == snake)
			randomSnake = Snake.getSnakes().get(random.nextInt(Snake.getSnakes().size()));
		return randomSnake;
	}

	@SuppressWarnings("unused")
	private static Snake getRandomOpponentSnake()
		{ return getRandomOpponentSnake(null); }
	
	private static void checkIfSnakeColidedWithOtherSnake(Snake snake) {
		if (Wall.getWalls().contains(snake.getHead()) ||
				(!snake.isUnderEffect(Effects.CAN_EAT_OTHERS) &&
				(snake.anotherSnakeColidedWithMe(snake) ||
				Snake.getSnakes().stream()
					.filter(s -> s != snake && s.anotherSnakeColidedWithMe(snake)).count() != 0))) {
						snake.setDead(true);
						snake.clearEffects();
						if (Snake.getSnakes().stream().filter(s -> !s.isDead()).count() <= 1) {
							// Se restou 1 ou nenhuma cobra viva
						}
		}
		if (snake.isUnderEffect(Effects.CAN_EAT_OTHERS))
			for (Snake opponent : Snake.getSnakes()) {
				Boolean itsMe = opponent == snake;
				for (int n = itsMe ? 4 : 0; n < opponent.getBodySize(); n++)
					if (snake.getHead().equals(opponent.getBody().get(n)))
						snake.dropBodyAsWall(n);
			}
	}
	
	private static void drawSnake(int id) {
		Snake snake = Snake.getSnakes().get(id);
		Boolean draw = fpsHandler.ableToDraw() && (!snake.isUnderEffect(Effects.INVISIBLE_TO_MYSELF) ||
			mySnake != null && snake == mySnake);
		GraphicsContext gc = Main.getSnakeCanvas().getGraphicsContext2D();
		Position position = new Position();
		Boolean dropBodyAsWall;
		for (int n = snake.getBody().size() - 1, sprX, sprY; n >= 0; n--) {
			position.setPosition(snake.getBody().get(n).getPosition());
			dropBodyAsWall = snake.isUnderEffect(Effects.DROP_BODY_AS_WALL_AFTER_FEW_STEPS) && 
				fpsHandler.getElapsedFrames() / 6 % 2 == 0 && n > minSnakeSize();
			sprX = dropBodyAsWall ? 30 : n == 0 ? 15 : 0;
			sprY = dropBodyAsWall ? 0 : snake.getDeadFrames() >= n ? 105 : id * 15;
			if (draw)
				gc.drawImage(sprites, sprX, sprY, 15, 15, position.getX(), position.getY(), Main.getDotSize(), Main.getDotSize());
		}
		int sprX = 30 + (snake.getDirection().getValue() == 0 ? 0 : snake.getDirection().getValue() / 2) * 15;
		int sprY = snake.isUnderEffect(Effects.CAN_EAT_OTHERS) ? (fpsHandler.getElapsedFrames() / 5 % 2 == 0 ? 90 : 105) : 75;
		if (draw && !snake.isDead())
			gc.drawImage(sprites, sprX, sprY, 15, 15, snake.getHead().getX(), snake.getHead().getY(), Main.getDotSize(), Main.getDotSize());
		snake.move(-1);
	}

	public static int minSnakeSize()
		{ return minSnaekSize; }

	private static void fruitCanvasDrawFruits() {
		GraphicsContext gc = Main.getGameFruitCanvas().getGraphicsContext2D();
		clearCanvas(Main.getGameFruitCanvas());
		for (Fruit fruit : Fruit.getFruits()) {
			Boolean isEffect = fruit.getEffect() != null;
			int sprX = isEffect ? 75 : fruit.getIncSizeBy() < 0 ? 60 : 45;
			double x = fruit.getX();
			double y = fruit.getY();
			gc.drawImage(sprites, sprX, 0, 15, 15, x + Main.getDotSize() * 0.10, y + Main.getDotSize() * 0.10, Main.getDotSize() * 0.8, Main.getDotSize() * 0.8);
			gc.setStroke(isEffect ? Color.WHITE : Color.BLACK);
			gc.setTextAlign(TextAlignment.CENTER);
			gc.setFont(fruitFont );
			gc.strokeText((isEffect ? "" + fruit.getEffect().getValue() : "" + Math.abs(fruit.getIncSizeBy())), x + Main.getDotSize() / 2, y + Main.getDotSize() * 0.67);
		}
	}

	private static void generateBGCanvas() {
		for (int y = 0; y <= Main.getMaxHeight(); y += Main.getDotSize()) {
			Wall.addWall(new Position(0, y));
			Wall.addWall(new Position(Main.getMaxWidth(), y));
		}
		for (int x = 0; x <= Main.getMaxWidth(); x += Main.getDotSize()) {
			Wall.addWall(new Position(x, 0));
			Wall.addWall(new Position(x, Main.getMaxHeight()));
		}
		drawBG(Wall.getWalls());
	}
	
	private static void generateHoles() {
		while (Hole.getHoles().size() < 10) {
			Hole.addHole(generateNewFreePosition(2, 2, Main.getMaxWidth() / Main.getDotSize() - 3, Main.getMaxHeight() / Main.getDotSize() - 3, 1));
			if (Hole.getHoles().size() % 2 == 0)
				Hole.connectLastAddedHoles();
		}
		drawBG(Wall.getWalls());
	}
	
	public static void drawBG(List<Position> wallsToAdd) {
		GraphicsContext gc = Main.getGameBGCanvas().getGraphicsContext2D();
		clearCanvas(Main.getGameBGCanvas());
		gc.drawImage(arena, 0, 0, Main.getScreenWidth(), Main.getScreenHeight());
		Wall.addAll(wallsToAdd);
		for (Position wall : Wall.getWalls())
			gc.drawImage(sprites, 45, 30, 15, 15, wall.getX(), wall.getY(), Main.getDotSize(), Main.getDotSize());
		for (Position holes : Hole.getHoles())
			gc.drawImage(sprites, 120, 0, 20, 20, holes.getX() - Main.getDotSize() * 0.4, holes.getY() - Main.getDotSize() * 0.4, Main.getDotSize() * 1.8, Main.getDotSize() * 1.8);
	}
	
	private static void generateFruits() {
		for (Effects effect : Effects.getListOfAll())
			Fruit.addEffectToAllowedEffects(effect);
		Fruit.addEffectToAllowedEffects(Effects.TRANSFORM_FRUITS_INTO_WALL);
		Fruit.addRandomFruits(1 ,1 ,Main.getMaxWidth() / Main.getDotSize() - 1, Main.getMaxHeight() / Main.getDotSize() - 1, 20, Snake.getSnakes());
		fruitCanvasDrawFruits();
	}

	public static void init() {
		mainMenu = new Menu("Game");
		KeyHandler.setOnKeyPressEvent(Main.getMainScene(), k -> onKeyPress(k));
		KeyHandler.setOnKeyHoldEvent(k -> onKeyHold(k));
		KeyHandler.setOnKeyReleaseEvent(Main.getMainScene(), k -> onKeyRelease(k));
		setupGame();
		gameLoop();
	}

	public static Menu getMenu()
		{ return mainMenu;	}

	public static Position generateNewFreePosition(int startX, int startY, int width, int height, int range) {
		Position position = new Position(0, 0);
		Boolean ok = false;
		while (!ok) {
			ok = true;
			position.setX((random.nextInt(width) + startX) * Main.getDotSize());
			position.setY((random.nextInt(height) + startY) * Main.getDotSize());
			for (int y = -range; y <= range; y++)
				for (int x = -range; x <= range; x++)
					if (!positionIsFree(position.getX() + x * Main.getDotSize(), position.getY() + y * Main.getDotSize()))
						ok = false;
		}
		return position;
	}

	public static Position generateNewFreePosition(int startX, int startY, int width, int height)
		{ return generateNewFreePosition(startX, startY, width, height, 0); }
	
	public static Boolean positionIsFree(Position position) {
		return !Fruit.getFruitsPositions().contains(position) && !Wall.getWalls().contains(position) &&
				Hole.getHoles().stream().filter(hole -> hole.getPosition().equals(position)).count() == 0 &&
				Snake.getSnakes().stream().filter(snake -> snake.somethingColidedWithMe(position)).count() == 0;
	}

	public static Boolean positionIsFree(double x, double y)
		{ return positionIsFree(new Position(x, y)); }
	
	public static Boolean positionColidedWithAnotherPosition(Position position1, Position position2, int centerX, int centerY) {
		int x = (int)(position1.getX() + centerX);
		int y = (int)(position1.getY() + centerY);
		if (x >= position2.getX() && x <= position2.getX() + Game.getDotSize() &&
				y > position2.getY() && y < position2.getY() + Game.getDotSize())
					return true;
		return false;
	}
	
	public static Boolean positionColidedWithAnotherPosition(Position position1, Position position2)
		{ return positionColidedWithAnotherPosition(position1, position2, getDotSize() / 2, getDotSize() / 2); }
	
	public static Boolean positionColidedWithAnotherPosition(Position position, List<Position> positions, int centerX, int centerY) {
		for (Position position2 : positions)
			if (positionColidedWithAnotherPosition(position, position2, centerX, centerY))
				return true;
		return false;
	}

	public static Boolean positionColidedWithAnotherPosition(Position position, List<Position> positions)
		{ return positionColidedWithAnotherPosition(position, positions, getDotSize() / 2, getDotSize() / 2); }
	
}