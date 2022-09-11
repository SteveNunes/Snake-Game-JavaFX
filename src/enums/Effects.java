package enums;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Effects {
	CLEAR_EFFECTS('A'),
	SWAP_2_OPPONENT_POSITIONS('B'),
	TRANSFORM_FRUITS_INTO_WALL('C'),
	REVERSE_CONTROLS('D'),
	CLOCKWISE_CONTROLS('E'),
	REVERSE_CLOCKWISE_CONTROLS('F'),
	MIN_SPEED('G'),
	QUARTER_SPEED('H'),
	HALF_SPEED('I'),
	STOP('J'),
	DOUBLE_SPEED('K'),
	QUAD_SPEED('L'),
	MAX_SPEED('M'),
	CONSTANTLY_CHANGES_SPEED_TO_RANDOM('N'),
	INVISIBLE_TO_MYSELF('O'),
	INVISIBLE_TO_OTHERS('P'),
	CANT_SPEED_UP_HOLDING_KEY('Q'),
	ONLY_MOVE_IF_CONSTANTLY_PRESS('R'),
	DROP_BODY_AS_WALL_AFTER_FEW_STEPS('S'),
	MAKE_OTHER_DROP_BODY_AS_WALL_AFTER_FEW_STEPS('T'),
	CAN_EAT_OTHERS('U');
	
	final char value;
	
	final static List<Effects> listOfAll = Arrays.asList(CLEAR_EFFECTS, SWAP_2_OPPONENT_POSITIONS,
		TRANSFORM_FRUITS_INTO_WALL, REVERSE_CONTROLS, CLOCKWISE_CONTROLS, CAN_EAT_OTHERS,
		REVERSE_CLOCKWISE_CONTROLS, MIN_SPEED, QUARTER_SPEED, HALF_SPEED, STOP,
		DOUBLE_SPEED, QUAD_SPEED, MAX_SPEED, CONSTANTLY_CHANGES_SPEED_TO_RANDOM,
		INVISIBLE_TO_MYSELF, INVISIBLE_TO_OTHERS, CANT_SPEED_UP_HOLDING_KEY,
		ONLY_MOVE_IF_CONSTANTLY_PRESS, DROP_BODY_AS_WALL_AFTER_FEW_STEPS,
		MAKE_OTHER_DROP_BODY_AS_WALL_AFTER_FEW_STEPS);
	
	final static Map<Effects, Integer> effectsDuration = Stream.of(new Object[][] {
		{CLEAR_EFFECTS, 0},
		{SWAP_2_OPPONENT_POSITIONS, 0},
		{TRANSFORM_FRUITS_INTO_WALL, 0},
		{REVERSE_CONTROLS, -60},
		{CLOCKWISE_CONTROLS, -60},
		{REVERSE_CLOCKWISE_CONTROLS, -60},
		{MIN_SPEED, -60},
		{QUARTER_SPEED, -60},
		{HALF_SPEED, -60},
		{STOP, -30},
		{DOUBLE_SPEED, 60},
		{QUAD_SPEED, 60},
		{MAX_SPEED, 60},
		{CONSTANTLY_CHANGES_SPEED_TO_RANDOM, -60},
		{INVISIBLE_TO_MYSELF, -30},
		{INVISIBLE_TO_OTHERS, 30},
		{CANT_SPEED_UP_HOLDING_KEY, -60},
		{CAN_EAT_OTHERS, 30},
		{ONLY_MOVE_IF_CONSTANTLY_PRESS, -60},
		{DROP_BODY_AS_WALL_AFTER_FEW_STEPS, 10},
		{MAKE_OTHER_DROP_BODY_AS_WALL_AFTER_FEW_STEPS, 0}})
			.collect(Collectors.toMap(data -> (Effects) data[0], data -> (Integer) data[1]));
	
	final static Map<Effects, Effects> causeEffectOnOthers = Stream.of(new Object[][] {
		{MAKE_OTHER_DROP_BODY_AS_WALL_AFTER_FEW_STEPS, DROP_BODY_AS_WALL_AFTER_FEW_STEPS}})
			.collect(Collectors.toMap(data -> (Effects) data[0], data -> (Effects) data[1]));
	
	Effects(char val)
		{ value = val; }
	
	public char getValue()
		{ return value; }
	
	public static Boolean isFriendly(Effects effect)
		{ return effectsDuration.get(effect) >= 0; }
	
	public Boolean isFriendly()
		{ return isFriendly(this); }

	public static Effects causeEffectOnOthers(Effects effect)
		{ return !causeEffectOnOthers.containsKey(effect) ? null : causeEffectOnOthers.get(effect); }
	
	public Effects causeEffectOnOthers()
		{ return causeEffectOnOthers(this); }

	public static int getDuration(Effects effect) {
		int val = effectsDuration.get(effect);
		return Math.abs(val);
	}
	
	public int getDuration()
		{ return getDuration(this); }
	
	public static List<Effects> getListOfAll()
		{ return listOfAll; }
	
}
