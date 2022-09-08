package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import entities.Snake;
import entities.Dot;
import entities.Fruit;
import enums.Direction;
import game.GameKeyHandler;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
	
	public final static int screenW = 800;
	public final static int screenH = 600;
	private static int gameFps = 30;
	private static int dotSize = Dot.getSnakeDotSize();
	private static long nextFrameAt = System.currentTimeMillis();
	private static Canvas gameCanvas;
	public static List<Snake> snakes = new ArrayList<>();
	private static Timeline timeline;
	private static List<Dot> walls = new ArrayList<>();
	
	@Override
	public void start(Stage stage) {
		Group root = new Group();
		Scene scene = new Scene(root, screenW, screenH, Color.LIGHTYELLOW);
		stage.setTitle("JavaFX Snake Game");
		stage.setScene(scene);
		stage.setWidth(screenW);
		stage.setHeight(screenH);
		stage.setResizable(false);

		gameCanvas = new Canvas(screenW, screenH);
		scene.setOnKeyPressed(e -> GameKeyHandler.keyPressedEventHandler(e, k -> onKeyPress(k)));
		scene.setOnKeyReleased(e -> GameKeyHandler.keyReleasedEventHandler(e, k -> onKeyRelease(k)));
		for (int y = 0; y < screenH / dotSize - 2; y++) {
			walls.add(new Dot(0, y));
			walls.add(new Dot(screenW / dotSize - 2, y));
		}
		for (int x = 0; x < screenW / dotSize - 1; x++) {
			walls.add(new Dot(x, 0));
			walls.add(new Dot(x, screenH / dotSize - 3));
		}
			
		while (snakes.size() < 1)
			snakes.add(new Snake(screenW / 2 / dotSize, screenH / 2 / dotSize, 3, Direction.LEFT, 10));
		Fruit.addRandomFruits(0 ,0 ,(screenW - 20) / dotSize, (screenH - 40) / dotSize, 20, snakes);
		setGameSpeed(500);
		
		root.getChildren().add(gameCanvas);
		stage.show();
		gameLoop();
	}
	
	private void onKeyPress(KeyCode keyCode) {
		System.out.println("PRESSED: " + keyCode);
		List<KeyCode> keys = Arrays.asList(KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT);
		List<Direction> dirs = Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
		List<Direction> rDirs = Arrays.asList(Direction.DOWN, Direction.UP, Direction.RIGHT, Direction.LEFT);
		for (Snake snake : snakes)
			for (int n = 0; n < 4; n++)
				if (keyCode == keys.get(n) && snake.getDirection() != rDirs.get(n)) {
					snake.setDirection(dirs.get(n));
					break;
				}
	}

	private void onKeyRelease(KeyCode keyCode) {
		System.out.println("RELEASED: " + keyCode);
	}

	private void setGameSpeed(long speed) {
		if (timeline != null)
			timeline.stop();
		timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> gameLoop()));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	public int getScreenW()
		{ return screenW; }

	public int getScreenH()
		{ return screenH; }

	public Canvas getGameCanvas()
		{ return gameCanvas; }

	private void gameLoop() {
		GameKeyHandler.run();
		GraphicsContext gc = gameCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, screenW, screenH);
		
		gc.setFill(Color.DARKSLATEGRAY);
		for (Dot wall : walls)
			gc.fillRect(wall.getX() * dotSize, wall.getY() * dotSize, dotSize, dotSize);

		for (Snake snake : snakes) {
			if (Fruit.getFruits().contains(snake.getHead())) {
				for (Dot fruit : Fruit.getFruits())
					if (fruit.equals(snake.getHead())) {
						Fruit.getFruits().remove(fruit);
						Fruit.addRandomFruit(0 ,0 ,(screenW - 20) / dotSize, (screenH - 40) / dotSize, snakes);
						break;
					}
				snake.incBody();
			}

			gc.setFill(snake.isDead() ? Color.GRAY : Color.GREEN);
			for (Dot body : snake.getSnakeBody()) {
				gc.fillRect(body.getX() * dotSize, body.getY() * dotSize, dotSize, dotSize);
				gc.setFill(snake.isDead() ? Color.DARKGRAY : Color.BLUE);
			}
			snake.move();
		}
		gc.setFill(Color.RED);
		for (Dot fruit : Fruit.getFruits())
			gc.fillRect(fruit.getX() * dotSize, fruit.getY() * dotSize, dotSize, dotSize);
		
		while (System.currentTimeMillis() < nextFrameAt)
			GameKeyHandler.run();
		nextFrameAt += 1000 / gameFps;
		
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static List<Dot> getWalls()
		{ return walls; }

}
