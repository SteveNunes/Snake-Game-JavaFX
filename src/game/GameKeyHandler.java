package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class GameKeyHandler {
	
	private static List<KeyCode> pressedKeys = new ArrayList<>();
	private static Map<KeyEvent, Integer> repeatKeys = new HashMap<>();
	private static int repeatDelay = -1;
	private static Consumer<KeyCode> onPressedKeyCall;
	
	public static void setRepatKeyDelay(int delay)
		{ repeatDelay = delay; }
	
	public static void keyPressedEventHandler(KeyEvent e, Consumer<KeyCode> onKeyPressEvent) {
		KeyCode keyCode = e.getCode();
		if (repeatDelay != -1)
			repeatKeys.put(e, 0);
		pressedKeys.add(keyCode);
		onPressedKeyCall = onKeyPressEvent;
		onKeyPressEvent.accept(keyCode);
	}

	public static void keyReleasedEventHandler(KeyEvent e, Consumer<KeyCode> onKeyReleaseEvent) {
		KeyCode keyCode = e.getCode();
		if (repeatDelay != -1)
			repeatKeys.remove(e);
		pressedKeys.remove(keyCode);
		onKeyReleaseEvent.accept(keyCode);
	}
	
	public static Boolean keyIsPressed(KeyCode keyCode)
		{ return pressedKeys.contains(keyCode); }

	public static void run() {
		if (repeatDelay == -1)
			return;
		for (KeyEvent e : repeatKeys.keySet()) {
			if (repeatKeys.get(e) >= repeatDelay)
				onPressedKeyCall.accept(e.getCode());
			else
				repeatKeys.put(e, repeatKeys.get(e) + 1);
		}
		
	}

}
