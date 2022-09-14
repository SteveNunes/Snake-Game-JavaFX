package application;

import game.Game;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
	
	private final static int screenW = 800;
	private final static int screenH = 600;
	private static Canvas gameBGCanvas;
	private static Canvas gameFruitCanvas;
	private static Canvas gameSnakeCanvas;
	private static Stage mainStage;
	private static Scene mainScene;
	private static Boolean windowIsOpen;
	private static int linearFiltering = 1;
	
	public static int getLiearFiltering()
		{ return linearFiltering; }

	public static void setLiearFiltering(int liearFiltering) {
		Main.linearFiltering = liearFiltering;
		gameSnakeCanvas.getGraphicsContext2D().setEffect(new BoxBlur(1, 1, linearFiltering));
		gameBGCanvas.getGraphicsContext2D().setEffect(new BoxBlur(1, 1, linearFiltering));
		gameFruitCanvas.getGraphicsContext2D().setEffect(new BoxBlur(1, 1, linearFiltering));		
	}

	public static Boolean windowsIsOpen()
		{ return windowIsOpen; }
	
	public static Stage getMainStage()
		{ return mainStage; }

	public static Scene getMainScene()
		{ return mainScene; }
	
	public static Canvas getGameBGCanvas()
		{ return gameBGCanvas; }	

	public static Canvas getGameFruitCanvas()
		{ return gameFruitCanvas; }	

	public static Canvas getSnakeCanvas()
		{ return gameSnakeCanvas; }

	public static int getScreenWidth()
		{ return (int)gameSnakeCanvas.getWidth(); }
	
	public static int getScreenHeight()
		{ return (int)gameSnakeCanvas.getHeight(); }

	@Override
	public void start(Stage stage) {
		mainStage = stage;
		VBox vBox = new VBox();
		mainScene = new Scene(vBox);
		stage.setTitle("JavaFX Snake");
		stage.setScene(mainScene);
		stage.setWidth(screenW - 6);
		stage.setHeight(screenH - 2);
		stage.setResizable(false);
		gameSnakeCanvas = new Canvas(screenW, screenH);
		gameBGCanvas = new Canvas(screenW, screenH);
		gameFruitCanvas = new Canvas(screenW, screenH);
		gameSnakeCanvas.getGraphicsContext2D().setImageSmoothing(false);
		gameBGCanvas.getGraphicsContext2D().setImageSmoothing(false);
		gameFruitCanvas.getGraphicsContext2D().setImageSmoothing(false);
		setLiearFiltering(linearFiltering);

		StackPane stackPane = new StackPane();
		stackPane.getChildren().addAll(gameBGCanvas, gameFruitCanvas, gameSnakeCanvas);
		vBox.getChildren().add(stackPane);
		windowIsOpen = true;
		stage.setOnCloseRequest(e -> windowIsOpen = false);
		stage.show();
		Game.init();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
