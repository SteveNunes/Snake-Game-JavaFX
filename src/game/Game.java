package game;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import application.Main;
import entities.Fruit;
import entities.Snake;
import enums.Direction;
import enums.Effects;
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
	private static int gameFps = 30;
	private static int dotSize = 20;
	private static long nextFrameAt = System.currentTimeMillis();
	private static List<Position> walls = new ArrayList<>();
	private static Menu mainMenu;
	private static int fps = 0;
	private static long fpsTimer = System.currentTimeMillis();
	
	public static int getDotSize()
		{ return dotSize; }
	
	public static void setDotSize(int size)
		{ dotSize = size; }
	
	private static void setupGame() {
		while (Snake.getSnakes().size() < 1)
			Snake.getSnakes().add(new Snake(Main.getScreenWidth() / 2 / dotSize, Main.getScreenHeight() / 2 / dotSize, 3, Direction.LEFT, 30));
		setGameFps(60);
		generateBGCanvas();
		generateFruitCanvas();
	}
	
	public static void setGameFps(int fps)
		{ gameFps = fps; }
	
	private static List<KeyCode> keys = Arrays.asList(KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT);
	private static List<Direction> dirs = Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
	private static List<Direction> rDirs = Arrays.asList(Direction.DOWN, Direction.UP, Direction.RIGHT, Direction.LEFT);

	private static void onKeyPress(KeyCode keyCode) {
		//System.out.println("PRESSED: " + keyCode);
		for (Snake snake : Snake.getSnakes())
			for (int n = 0; n < 4; n++)
				if (keyCode == keys.get(n) && snake.getDirection() != rDirs.get(n)) {
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
		GraphicsContext gc = Main.getGameSnakeCanvas().getGraphicsContext2D();
		gc.clearRect(0, 0, Main.getScreenWidth(), Main.getScreenHeight());
	
		for (Snake snake : Snake.getSnakes()) {
			checkIfSnakeAteAFruit(snake);
			drawSnake(snakeId);
			snakeId++;
		}
		
		fpsCounter();

		if (Main.windowsIsOpen())
			GameTools.callMethodAgain(e -> gameLoop());
	}

	private static void checkIfSnakeAteAFruit(Snake snake) {
		if (Fruit.getFruitsPositions().contains(snake.getHead().getPosition())) {
			for (Fruit fruit : Fruit.getFruits())
				if (fruit.getPosition().equals(snake.getHead().getPosition())) {
					Fruit.getFruits().remove(fruit);
					Fruit newFruit = Fruit.addRandomFruit(0 ,0 ,(Main.getScreenWidth() - 20) / dotSize, (Main.getScreenHeight() - 40) / dotSize, Snake.getSnakes());
					fruitCanvasReplaceFruit(fruit, newFruit);
					if (fruit.getEffect() != null)
						snake.addEffect(fruit.getEffect());
					else
						snake.incBody(fruit.getIncSizeBy());
					break;
				}
		}
	}

	private static void fpsCounter() {
		if (gameFps > 0) {
			while (System.currentTimeMillis() < nextFrameAt)
				KeyHandler.runItOnMainLoopEveryFrame();
			nextFrameAt += 1000 / gameFps;
		}
		fps++;
		if (System.currentTimeMillis() >= fpsTimer) {
			fpsTimer += 1000;
			Main.getMainStage().setTitle("JavaFX Snake - FPS: " + fps);
			fps = 0;
		}
	}

	private static void drawSnake(int id) {
		GraphicsContext gc = Main.getGameSnakeCanvas().getGraphicsContext2D();
		Snake snake = Snake.getSnakes().get(id);
		Position position = new Position();
		for (int n = snake.getBody().size() - 1, sprX, sprY; n >= 0; n--) {
			position.setPosition(snake.getBody().get(n).getPosition());
			sprX = n == 0 ? 15 : 0;
			sprY = snake.getDeadFrames() >= n ? 105 : id * 15;
			gc.drawImage(sprites, sprX, sprY, 15, 15, position.getX() * dotSize, position.getY() * dotSize, dotSize, dotSize);
		}
		snake.move();
	}

	private static void fruitCanvasReplaceFruit(Fruit removeFruit, Fruit addFruit) {
		GraphicsContext gc = Main.getGameFruitCanvas().getGraphicsContext2D();
		gc.clearRect(removeFruit.getX() * dotSize, removeFruit.getY() * dotSize, dotSize, dotSize);
		fruitCanvasDrawFruit(addFruit);
	}

	private static void fruitCanvasDrawFruit(Fruit fruit) {
		GraphicsContext gc = Main.getGameFruitCanvas().getGraphicsContext2D();
		Boolean isEffect = fruit.getEffect() != null;
		int sprX = isEffect ? 75 : fruit.getIncSizeBy() < 0 ? 60 : 45;
		int x = fruit.getX() * dotSize;
		int y = fruit.getY() * dotSize;
		gc.drawImage(sprites, sprX, 0, 15, 15, x, y, dotSize, dotSize);
		gc.setStroke(isEffect ? Color.WHITE : Color.BLACK);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.strokeText("" + (isEffect ? fruit.getEffect().getValue() : Math.abs(fruit.getIncSizeBy())), x + dotSize / 2, y + dotSize * 0.75);
	}

	private static void generateBGCanvas() {
		GraphicsContext gc = Main.getGameBGCanvas().getGraphicsContext2D();
		gc.clearRect(0, 0, Main.getScreenWidth(), Main.getScreenHeight());
		gc.drawImage(arena, 0, 0, Main.getScreenWidth() - dotSize, Main.getScreenHeight() - dotSize * 2);
		for (int y = 0; y < Main.getScreenHeight() / dotSize - 2; y++) {
			walls.add(new Position(0, y));
			walls.add(new Position(Main.getScreenWidth() / dotSize - 2, y));
		}
		for (int x = 0; x < Main.getScreenWidth() / dotSize - 1; x++) {
			walls.add(new Position(x, 0));
			walls.add(new Position(x, Main.getScreenHeight() / dotSize - 3));
		}
		for (Position wall : walls)
			gc.drawImage(sprites, 45, 30, 15, 15, wall.getX() * dotSize, wall.getY() * dotSize, dotSize, dotSize);
	}

	private static void generateFruitCanvas() {
		Main.getGameFruitCanvas().getGraphicsContext2D().clearRect(0, 0, Main.getScreenWidth(), Main.getScreenHeight());
		for (Effects effect : Effects.getListOfAll())
			Fruit.addEffectToAllowedEffects(effect);
		Fruit.addRandomFruits(0 ,0 ,(Main.getScreenWidth() - 20) / dotSize, (Main.getScreenHeight() - 40) / dotSize, 20, Snake.getSnakes())
			.forEach(f -> fruitCanvasDrawFruit(f));
	}

	public static void init() {
		mainMenu = new Menu("Game");
		Main.getMainScene().setOnKeyPressed(e -> KeyHandler.keyPressedEventHandler(e, k -> onKeyPress(k)));
		Main.getMainScene().setOnKeyReleased(e -> KeyHandler.keyReleasedEventHandler(e, k -> onKeyRelease(k)));
		setupGame();
		gameLoop();
	}

	private static void gameLoopTest() {
		GraphicsContext gc = Main.getGameFruitCanvas().getGraphicsContext2D();
		gc.clearRect(0, 0, Main.getScreenWidth(), Main.getScreenHeight());
	
		int quad = 20;
		int quads = 0;
		for (int y = 0; y + quad * 2 < Main.getScreenHeight(); y += quad)
			for (int x = 0; x + quad < Main.getScreenWidth(); x += quad, quads++) {
				int r = new SecureRandom().nextInt(255);
				int g = new SecureRandom().nextInt(255);
				int b = new SecureRandom().nextInt(255);
				String colorStr = "#" + String.format("%02X", r & 0xFF) + String.format("%02X", g & 0xFF) + String.format("%02X", b & 0xFF);
				Color color = Color.valueOf(colorStr);
				gc.setFill(color);
				gc.fillRect(x, y, quad, quad);
			}
		
		fps++;
		if (System.currentTimeMillis() >= fpsTimer) {
			fpsTimer += 1000;
			Main.getMainStage().setTitle("JavaFX Snake - FPS: " + fps + " - Quads: " + quads);
			fps = 0;
		}

		if (Main.windowsIsOpen())
			GameTools.callMethodAgain(e -> gameLoopTest());
	}

	public static Menu getMenu()
		{ return mainMenu;	}

	public static List<Position> getWalls()
		{ return walls; }

}
