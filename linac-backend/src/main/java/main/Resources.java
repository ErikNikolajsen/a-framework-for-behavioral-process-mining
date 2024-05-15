package main;

import java.util.Random;

import entities.Floorplan;
import logic.Input;
import logic.Simulator;
import pathfinding.AStarGrid;
import utils.Log;

public class Resources {
	private static MqttPaho mqtt;
	private static Log log;
	private static Simulator simulator;
	private static Input input;
	private static AStarGrid aStarGrid;
	private static Floorplan floorplan;
	private static Random random;
	

	//Accessors and Mutators
	public static Log getLog() {
		return log;
	}
	public static void setLog(Log log) {
		Resources.log = log;
	}
	public static Simulator getSimulator() {
		return simulator;
	}
	public static void setSimulator(Simulator simulator) {
		Resources.simulator = simulator;
	}
	public static Input getInput() {
		return input;
	}
	public static void setInput(Input input) {
		Resources.input = input;
	}
	public static AStarGrid getaStarGrid() {
		return aStarGrid;
	}
	public static void setaStarGrid(AStarGrid aStarGrid) {
		Resources.aStarGrid = aStarGrid;
	}
	public static Floorplan getFloorplan() {
		return floorplan;
	}
	public static void setFloorplan(Floorplan floorplan) {
		Resources.floorplan = floorplan;
	}
	public static MqttPaho getMqtt() {
		return mqtt;
	}
	public static void setMqtt(MqttPaho mqtt) {
		Resources.mqtt = mqtt;
	}
	public static Random getRandom() {
		return random;
	}
	public static void setRandom(Random random) {
		Resources.random = random;
	}
	
	
	
	
	
	
	
	
	
}

