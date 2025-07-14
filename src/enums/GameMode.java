package enums;

import java.util.ArrayList;
import java.util.List;

public enum GameMode {
	SINGLE_PLAYER(1),
	LOCAL_MULTIPLAYER(2),
	ONLINE_MULTIPLAYER(3);
	
	private final int value;

	GameMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static List<GameMode> getListOfAll() {
		List<GameMode> list = new ArrayList<>();
		list.add(SINGLE_PLAYER);
		list.add(LOCAL_MULTIPLAYER);
		list.add(ONLINE_MULTIPLAYER);
		return list;
	}

	public static String getName(GameMode mode) {
		if (mode == SINGLE_PLAYER)
			return "Single Player";
		if (mode == LOCAL_MULTIPLAYER)
			return "Local multiplayer";
		return "Online multiplayer";
	}

	public String getName() {
		return getName(this);
	}

}
