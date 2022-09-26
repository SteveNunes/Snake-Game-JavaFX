package game;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import application.Main;
import entities.Fruit;
import entities.Hole;
import entities.Snake;
import entities.Wall;
import enums.Direction;
import enums.Effects;
import enums.GameMode;
import gameutil.FPSHandler;
import gameutil.GameTools;
import gameutil.KeyHandler;
import gameutil.Position;
import gui.util.Controller;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class Game {
	
	private static Image sprites = Controller.removeBgColor(new Image("/sprites/sprites.png"), Color.valueOf("#326496"), 0);
	private static Image arena = new Image("/sprites/arenas/01.png");
	private static int dotSize = 20;
	private static Menu mainMenu;
	private static FPSHandler fpsHandler = new FPSHandler(60, 2);
	private static GameMode gameMode = GameMode.LOCAL_MULTIPLAYER;
	private static Snake mySnake = null;
	private static int framesPerStep = 30;
	
	public static int defaultFramesPerStep()
		{ return framesPerStep; }
	
	public static GameMode getGameMode()
		{ return gameMode; }
	
	public static int getDotSize()
		{ return dotSize; }
	
	public static void setDotSize(int size)
		{ dotSize = size; }
	
	private static void setupGame() {
		Snake.getSnakes().add(mySnake = new Snake(Main.getScreenWidth() / 10 * 8 / dotSize, Main.getScreenHeight() / 3 / dotSize, 3, Direction.DOWN));
		Snake.getSnakes().add(new Snake(Main.getScreenWidth() / 10 * 5 / dotSize, Main.getScreenHeight() / 3 / dotSize, 3, Direction.DOWN));
		Snake.getSnakes().add(new Snake(Main.getScreenWidth() / 10 * 2 / dotSize, Main.getScreenHeight() / 3 / dotSize, 3, Direction.DOWN));
		generateBGCanvas();
		setupFruits();
		generateHoles();
	}
	
	private static List<Direction> dirs = Arrays.asList(Direction.LEFT, Direction.UP, Direction.RIGHT, Direction.DOWN);

	private static void onKeyPress(KeyCode keyCode, Boolean isRepeated) {
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
		KeyHandler.runItOnMainLoopEveryFrame();
		if (fpsHandler.ableToDraw())
			clearCanvas(Main.getSnakeCanvas());
		
		for (Snake snake : Snake.getSnakes()) {
			checkIfSnakeEnteredInAHole(snake);
			checkIfSnakeAteAFruit(snake);
			checkIfSnakeColidedWithOtherSnake(snake);
			drawSnake(snakeId);
			snakeId++;
		}

		KeyHandler.runItOnMainLoopEveryFrame();
		fpsHandler.fpsCounter();
		Main.getMainStage().setTitle("JavaFX Snake - CPS: " + fpsHandler.getCPS() + " FPS: " + fpsHandler.getFPS());
		
		if (Main.windowsIsOpen())
			GameTools.callMethodAgain(e -> gameLoop());
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
					Fruit.addRandomFruit(0 ,0 ,(Main.getScreenWidth() - 20) / dotSize, (Main.getScreenHeight() - 40) / dotSize, Snake.getSnakes());
					fruitCanvasDrawFruits();
					if (fruit.getEffect() != null) {
						if (fruit.getEffect() == Effects.CLEAR_EFFECTS)
							snake.clearEffects();
						else if (fruit.getEffect() == Effects.TRANSFORM_FRUITS_INTO_WALL) {
							drawBG(Fruit.getFruitsPositions());
							for (Fruit del : Fruit.getFruits())
								Main.getGameFruitCanvas().getGraphicsContext2D().clearRect(del.getX() * dotSize, del.getY() * dotSize, dotSize, dotSize);
							Fruit.getFruits().clear();
							setupFruits();
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
		Snake randomSnake = Snake.getSnakes().get(new SecureRandom().nextInt(Snake.getSnakes().size()));
		while (snake != null && randomSnake == snake)
			randomSnake = Snake.getSnakes().get(new SecureRandom().nextInt(Snake.getSnakes().size()));
		return randomSnake;
	}

	@SuppressWarnings("unused")
	private static Snake getRandomOpponentSnake()
		{ return getRandomOpponentSnake(null); }
	
	private static void checkIfSnakeColidedWithOtherSnake(Snake snake) {
		if (Wall.getWalls().contains(snake.getHead()) ||
				(!snake.isUnderEffect(Effects.CAN_EAT_OTHERS) &&
				(snake.getHeadlessBody().contains(snake.getHead()) ||
				!Snake.getSnakes().stream()
				.filter(s -> s != snake && s.getBody().contains(snake.getHead()))
				.collect(Collectors.toList()).isEmpty()))) {
					snake.setDead(true);
					snake.clearEffects();
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
				fpsHandler.getElapsedFrames() / 6 % 2 == 0 && n > 2;
			sprX = dropBodyAsWall ? 30 : n == 0 ? 15 : 0;
			sprY = dropBodyAsWall ? 0 : snake.getDeadFrames() >= n ? 105 : id * 15;
			if (draw)
				gc.drawImage(sprites, sprX, sprY, 15, 15, position.getX() * dotSize, position.getY() * dotSize, dotSize, dotSize);
		}
		int sprX = 30 + (snake.getDirection().getValue() == 0 ? 0 : snake.getDirection().getValue() / 2) * 15;
		int sprY = snake.isUnderEffect(Effects.CAN_EAT_OTHERS) ? (fpsHandler.getElapsedFrames() / 5 % 2 == 0 ? 90 : 105) : 75;
		if (draw && !snake.isDead())
			gc.drawImage(sprites, sprX, sprY, 15, 15, snake.getHead().getX() * dotSize, snake.getHead().getY() * dotSize, dotSize, dotSize);
		snake.move(-1);
	}

	private static void fruitCanvasDrawFruits() {
		GraphicsContext gc = Main.getGameFruitCanvas().getGraphicsContext2D();
		clearCanvas(Main.getGameFruitCanvas());
		for (Fruit fruit : Fruit.getFruits()) {
			Boolean isEffect = fruit.getEffect() != null;
			int sprX = isEffect ? 75 : fruit.getIncSizeBy() < 0 ? 60 : 45;
			int x = fruit.getX() * dotSize;
			int y = fruit.getY() * dotSize;
			gc.drawImage(sprites, sprX, 0, 15, 15, x, y, dotSize, dotSize);
			gc.setStroke(isEffect ? Color.WHITE : Color.BLACK);
			gc.setTextAlign(TextAlignment.CENTER);
			gc.strokeText((isEffect ? "" + fruit.getEffect().getValue() : "" + Math.abs(fruit.getIncSizeBy())), x + dotSize / 2, y + dotSize * 0.75);
		}
	}

	private static void generateBGCanvas() {
		for (int y = 0; y < Main.getScreenHeight() / dotSize - 2; y++) {
			Wall.addWall(new Position(0, y));
			Wall.addWall(new Position(Main.getScreenWidth() / dotSize - 2, y));
		}
		for (int x = 0; x < Main.getScreenWidth() / dotSize - 1; x++) {
			Wall.addWall(new Position(x, 0));
			Wall.addWall(new Position(x, Main.getScreenHeight() / dotSize - 3));
		}
		drawBG(Wall.getWalls());
	}
	
	private static void generateHoles() {
		while (Hole.getHoles().size() < 10) {
			Hole.addHole(generateNewFreePosition(2, 2, Main.getScreenWidth() / dotSize - 5, Main.getScreenWidth() / dotSize - 16, 1));
			if (Hole.getHoles().size() % 2 == 0)
				Hole.connectLastAddedHoles();
		}
		drawBG(Wall.getWalls());
	}
	
	public static void drawBG(List<Position> wallsToAdd) {
		GraphicsContext gc = Main.getGameBGCanvas().getGraphicsContext2D();
		clearCanvas(Main.getGameBGCanvas());
		gc.drawImage(arena, 0, 0, Main.getScreenWidth() - dotSize, Main.getScreenHeight() - dotSize * 2);
		Wall.addAll(wallsToAdd);
		for (Position wall : Wall.getWalls())
			gc.drawImage(sprites, 45, 30, 15, 15, wall.getX() * dotSize, wall.getY() * dotSize, dotSize, dotSize);
		for (Position holes : Hole.getHoles())
			gc.drawImage(sprites, 60, 30, 20, 20, holes.getX() * dotSize - dotSize * 0.4, holes.getY() * dotSize - dotSize * 0.4, dotSize * 1.8, dotSize * 1.8);
	}
	
	private static void setupFruits() {
		for (Effects effect : Effects.getListOfAll())
			Fruit.addEffectToAllowedEffects(effect);
		Fruit.addEffectToAllowedEffects(Effects.TRANSFORM_FRUITS_INTO_WALL);
		Fruit.addRandomFruits(0 ,0 ,(Main.getScreenWidth() - 20) / dotSize, (Main.getScreenHeight() - 40) / dotSize, 20, Snake.getSnakes());
		fruitCanvasDrawFruits();
	}

	public static void init() {
		mainMenu = new Menu("Game");
		Main.getMainScene().setOnKeyPressed(e -> KeyHandler.keyPressedEventHandler(e, k -> onKeyPress(k, false), k -> onKeyPress(k, true)));
		Main.getMainScene().setOnKeyReleased(e -> KeyHandler.keyReleasedEventHandler(e, k -> onKeyRelease(k)));
		setupGame();
		gameLoop();
	}

	@SuppressWarnings("unused")
	private static void gameLoopTest() {
		GraphicsContext gc = Main.getSnakeCanvas().getGraphicsContext2D();
		gc.clearRect(0, 0, Main.getScreenWidth(), Main.getScreenHeight());
	
		int quad = 20;
		for (int y = 0; y + quad * 2 < Main.getScreenHeight(); y += quad)
			for (int x = 0; x + quad < Main.getScreenWidth(); x += quad) {
				int r = new SecureRandom().nextInt(255);
				int g = new SecureRandom().nextInt(255);
				int b = new SecureRandom().nextInt(255);
				String colorStr = "#" + String.format("%02X", r & 0xFF) + String.format("%02X", g & 0xFF) + String.format("%02X", b & 0xFF);
				Color color = Color.valueOf(colorStr);
				gc.setFill(color);
				gc.fillRect(x, y, quad, quad);
			}
		
		KeyHandler.runItOnMainLoopEveryFrame();
		fpsHandler.fpsCounter();

		if (Main.windowsIsOpen())
			GameTools.callMethodAgain(e -> gameLoopTest());
	}

	public static Menu getMenu()
		{ return mainMenu;	}

	public static Position generateNewFreePosition(int startX, int startY, int width, int height, int range) {
		Position position = new Position(0, 0);
		Boolean ok = false;
		while (!ok) {
			ok = true;
			position.setX(new SecureRandom().nextInt(width) + startX);
			position.setY(new SecureRandom().nextInt(height) + startY);
			for (int y = -range; y <= range; y++)
				for (int x = -range; x <= range; x++)
					if (!positionIsFree(position.getX() + x, position.getY() + y))
						ok = false;
		}
		return position;
	}

	public static Position generateNewFreePosition(int startX, int startY, int width, int height)
		{ return generateNewFreePosition(startX, startY, width, height, 0); }
	
	public static Boolean positionIsFree(Position position) {
		return !Fruit.getFruitsPositions().contains(position) && !Wall.getWalls().contains(position) &&
				Hole.getHoles().stream().filter(hole -> hole.getPosition().equals(position)).collect(Collectors.toList()).isEmpty() &&
				Snake.getSnakes().stream().filter(snake -> snake.getBody().contains(position)).collect(Collectors.toList()).isEmpty();
	}

	public static Boolean positionIsFree(int x, int y)
		{ return positionIsFree(new Position(x, y)); }
	
}