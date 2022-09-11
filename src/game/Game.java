package game;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import application.Main;
import entities.Fruit;
import entities.Snake;
import enums.Direction;
import enums.Effects;
import gameutil.FPSHandler;
import gameutil.GameTools;
import gameutil.KeyHandler;
import gameutil.Position;
import gui.util.Controller;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class Game {
	
	private static Image sprites = Controller.removeBgColor(new Image("/sprites/sprites.png"), Color.valueOf("#326496"), 0);
	private static Image arena = new Image("/sprites/arenas/01.png");
	private static int dotSize = 20;
	private static List<Position> walls = new ArrayList<>();
	private static List<Snake> snakes = new ArrayList<>();
	private static Menu mainMenu;
	private static FPSHandler fpsHandler = new FPSHandler(60, 2);
	
	public static int getDotSize()
		{ return dotSize; }
	
	public static void setDotSize(int size)
		{ dotSize = size; }
	
	private static void setupGame() {
		getSnakes().add(new Snake(Main.getScreenWidth() / 10 * 7 / dotSize, Main.getScreenHeight() / 3 / dotSize, 3, Direction.DOWN, 30));
		getSnakes().add(new Snake(Main.getScreenWidth() / 10 * 3 / dotSize, Main.getScreenHeight() / 3 / dotSize, 3, Direction.DOWN, 30));
		generateBGCanvas();
		generateFruitCanvas();
	}
	
	private static List<Direction> dirs = Arrays.asList(Direction.LEFT, Direction.UP, Direction.RIGHT, Direction.DOWN);

	private static void onKeyPress(KeyCode keyCode, Boolean isRepeated) {
		//System.out.println("PRESSED: " + keyCode);
		for (Snake snake : getSnakes())
			for (int n = 0; n < 4; n++)
				if (keyCode == snake.getKeys().get(n) && dirs.get(n) != snake.getDirection().getReverseDirection()) {
					snake.setDirection(dirs.get(n));
					break;
				}
	}

	private static void onKeyRelease(KeyCode keyCode) {
		//System.out.println("RELEASED: " + keyCode);
	}

	private static void gameLoop() {
		int snakeId = 0;
		KeyHandler.runItOnMainLoopEveryFrame();
		if (fpsHandler.ableToDraw()) {
			GraphicsContext gc = Main.getSnakeCanvas().getGraphicsContext2D();
			gc.clearRect(0, 0, Main.getScreenWidth(), Main.getScreenHeight());
		}
		
		for (Snake snake : getSnakes()) {
			checkIfSnakeAteAFruit(snake);
			checkIfSnakeColidedWithOtherSnake(snake);
			drawSnake(snakeId);
			snakeId++;
		}

		fpsHandler.fpsCounter(e -> KeyHandler.runItOnMainLoopEveryFrame());
		Main.getMainStage().setTitle("JavaFX Snake - CPS: " + fpsHandler.getCPS() + " FPS: " + fpsHandler.getFPS());
		
		if (Main.windowsIsOpen())
			GameTools.callMethodAgain(e -> gameLoop());
	}

	private static void checkIfSnakeAteAFruit(Snake snake) {
		if (Fruit.getFruitsPositions().contains(snake.getHead().getPosition()))
			for (Fruit fruit : Fruit.getFruits())
				if (fruit.getPosition().equals(snake.getHead().getPosition())) {
					Fruit.getFruits().remove(fruit);
					Main.getGameFruitCanvas().getGraphicsContext2D().clearRect(fruit.getX() * dotSize, fruit.getY() * dotSize, dotSize, dotSize);
					Fruit newFruit = Fruit.addRandomFruit(0 ,0 ,(Main.getScreenWidth() - 20) / dotSize, (Main.getScreenHeight() - 40) / dotSize, getSnakes());
					fruitCanvasDrawFruit(newFruit);
					if (fruit.getEffect() != null) {
						if (fruit.getEffect() == Effects.CLEAR_EFFECTS)
							snake.clearEffects();
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
		Snake randomSnake = snakes.get(new SecureRandom().nextInt(snakes.size()));
		while (snake != null && randomSnake == snake)
			randomSnake = snakes.get(new SecureRandom().nextInt(snakes.size()));
		return randomSnake;
	}

	private static Snake getRandomOpponentSnake()
		{ return getRandomOpponentSnake(null); }
	
	private static void checkIfSnakeColidedWithOtherSnake(Snake snake) {
		if (getWalls().contains(snake.getHead()) ||
				(!snake.isUnderEffect(Effects.CAN_EAT_OTHERS) &&
				(snake.getHeadlessBody().contains(snake.getHead()) ||
				!snakes.stream()
				.filter(s -> s != snake && s.getBody().contains(snake.getHead()))
				.collect(Collectors.toList()).isEmpty()))) {
					snake.setDead(true);
					snake.clearEffects();
		}
		if (snake.isUnderEffect(Effects.CAN_EAT_OTHERS))
			for (Snake opponent : snakes) {
				Boolean itsMe = opponent == snake;
				for (int n = itsMe ? 4 : 0; n < opponent.getBodySize(); n++)
					if (snake.getHead().equals(opponent.getBody().get(n)))
						snake.dropBodyAsWall(n);
			}
	}
	
	private static void drawSnake(int id) {
		Snake snake = getSnakes().get(id);
		if (fpsHandler.ableToDraw()) {
			GraphicsContext gc = Main.getSnakeCanvas().getGraphicsContext2D();
			gc.setImageSmoothing(false);
			Position position = new Position();
			Boolean dropBodyAsWall;
			for (int n = snake.getBody().size() - 1, sprX, sprY; n >= 0; n--) {
				position.setPosition(snake.getBody().get(n).getPosition());
				dropBodyAsWall = snake.isUnderEffect(Effects.DROP_BODY_AS_WALL_AFTER_FEW_STEPS) && 
					fpsHandler.getElapsedFrames() / 6 % 2 == 0 && n > 2;
				sprX = dropBodyAsWall ? 30 : n == 0 ? 15 : 0;
				sprY = dropBodyAsWall ? 0 : snake.getDeadFrames() >= n ? 105 : id * 15;
				gc.drawImage(sprites, sprX, sprY, 15, 15, position.getX() * dotSize, position.getY() * dotSize, dotSize, dotSize);
			}
			int sprX = 30 + (snake.getDirection().getValue() == 0 ? 0 : snake.getDirection().getValue() / 2) * 15;
			int sprY = snake.isUnderEffect(Effects.CAN_EAT_OTHERS) ? (fpsHandler.getElapsedFrames() / 5 % 2 == 0 ? 90 : 105) : 75;
			gc.drawImage(sprites, sprX, sprY, 15, 15, snake.getHead().getX() * dotSize, snake.getHead().getY() * dotSize, dotSize, dotSize);
		}
		snake.move();
	}

	private static void fruitCanvasDrawFruit(Fruit fruit) {
		GraphicsContext gc = Main.getGameFruitCanvas().getGraphicsContext2D();
		Boolean isEffect = fruit.getEffect() != null;
		int sprX = isEffect ? 75 : fruit.getIncSizeBy() < 0 ? 60 : 45;
		int x = fruit.getX() * dotSize;
		int y = fruit.getY() * dotSize;
		gc.setImageSmoothing(false);
		gc.drawImage(sprites, sprX, 0, 15, 15, x, y, dotSize, dotSize);
		gc.setStroke(isEffect ? Color.WHITE : Color.BLACK);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.strokeText((isEffect ? "" + fruit.getEffect().getValue() : "" + Math.abs(fruit.getIncSizeBy())), x + dotSize / 2, y + dotSize * 0.75);
	}

	private static void generateBGCanvas() {
		GraphicsContext gc = Main.getGameBGCanvas().getGraphicsContext2D();
		gc.clearRect(0, 0, Main.getScreenWidth(), Main.getScreenHeight());
		gc.setImageSmoothing(false);
		gc.drawImage(arena, 0, 0, Main.getScreenWidth() - dotSize, Main.getScreenHeight() - dotSize * 2);
		for (int y = 0; y < Main.getScreenHeight() / dotSize - 2; y++) {
			walls.add(new Position(0, y));
			walls.add(new Position(Main.getScreenWidth() / dotSize - 2, y));
		}
		for (int x = 0; x < Main.getScreenWidth() / dotSize - 1; x++) {
			walls.add(new Position(x, 0));
			walls.add(new Position(x, Main.getScreenHeight() / dotSize - 3));
		}
		drawWalls(walls);
	}
	
	public static void drawWalls(List<Position> wallsToAdd) {
		GraphicsContext gc = Main.getGameBGCanvas().getGraphicsContext2D();
		gc.setImageSmoothing(false);
		for (Position wall : wallsToAdd)
			gc.drawImage(sprites, 45, 30, 15, 15, wall.getX() * dotSize, wall.getY() * dotSize, dotSize, dotSize);
		walls.addAll(wallsToAdd);
	}

	private static void generateFruitCanvas() {
//		for (Effects effect : Effects.getListOfAll())
//			Fruit.addEffectToAllowedEffects(effect);
		Fruit.addEffectToAllowedEffects(Effects.MAKE_OTHER_DROP_BODY_AS_WALL_AFTER_FEW_STEPS);
		Fruit.addRandomFruits(0 ,0 ,(Main.getScreenWidth() - 20) / dotSize, (Main.getScreenHeight() - 40) / dotSize, 20, getSnakes())
			.forEach(fruit -> fruitCanvasDrawFruit(fruit));
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
		gc.setImageSmoothing(false);
	
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
		
		fpsHandler.fpsCounter(e -> KeyHandler.runItOnMainLoopEveryFrame());

		if (Main.windowsIsOpen())
			GameTools.callMethodAgain(e -> gameLoopTest());
	}

	public static Menu getMenu()
		{ return mainMenu;	}

	public static List<Position> getWalls()
		{ return walls; }

	public static List<Snake> getSnakes()
		{ return snakes; }

}