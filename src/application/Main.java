package application;

import game.Game;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
	
	private final static int screenW = 800;
	private final static int screenH = 600;
	private static Canvas gameBGCanvas;
	private static Canvas gameGameCanvas;
	private static Stage mainStage;
	private static Scene mainScene;
	private static Boolean windowIsOpen;
	
	public static Boolean windowsIsOpen()
		{ return windowIsOpen; }
	
	public static Stage getMainStage()
		{ return mainStage; }

	public static Scene getMainScene()
		{ return mainScene; }
	
	public static Canvas getGameBGCanvas()
		{ return gameBGCanvas; }	

	public static Canvas getGameCanvas()
		{ return gameGameCanvas; }

	public static int getScreenWidth()
		{ return (int)gameGameCanvas.getWidth(); }
	
	public static int getScreenHeight()
		{ return (int)gameGameCanvas.getHeight(); }

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
		gameGameCanvas = new Canvas(screenW, screenH);
		gameBGCanvas = new Canvas(screenW, screenH);
		StackPane stackPane = new StackPane();
		stackPane.getChildren().addAll(gameBGCanvas, gameGameCanvas);
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
