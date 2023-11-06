package logic;

import java.time.LocalDateTime;

import entities.Agent;
import entities.SensorActive;
import pathfinding.AStarNode;

public class BEvent {
	
	private BEventType eventType;
	private LocalDateTime eventDateTime;
	private Agent agent;
	private AStarNode node;
	private SensorActive sensor;
	private String command;
	private String output;
	
	// Movement constructor
	public BEvent(BEventType eventType, LocalDateTime eventDateTime, Agent agent, AStarNode node) {
		this.eventType = eventType;
		this.eventDateTime = eventDateTime;
		this.agent = agent;
		this.node = node;
	}
	
	// Sensor activation constructor
	public BEvent(BEventType eventType, LocalDateTime eventDateTime, SensorActive sensor, String command) {
		this.eventType = eventType;
		this.eventDateTime = eventDateTime;
		this.sensor = sensor;
		this.command = command;
	}
	
	// Output constructor
	public BEvent(BEventType eventType, LocalDateTime eventDateTime, String output) {
		this.eventType = eventType;
		this.eventDateTime = eventDateTime;
		this.output = output;
	}
	
	// Output constructor
	public BEvent(BEventType eventType, LocalDateTime eventDateTime) {
		this.eventType = eventType;
		this.eventDateTime = eventDateTime;
	}
	
	// Default constructor
	public BEvent() {
	}

	public BEventType getEventType() {
		return eventType;
	}

	public void setEventType(BEventType eventType) {
		this.eventType = eventType;
	}

	public LocalDateTime getEventDateTime() {
		return eventDateTime;
	}

	public void setEventDateTime(LocalDateTime eventDateTime) {
		this.eventDateTime = eventDateTime;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public AStarNode getNode() {
		return node;
	}

	public void setNode(AStarNode node) {
		this.node = node;
	}

	public SensorActive getSensor() {
		return sensor;
	}

	public void setSensor(SensorActive sensor) {
		this.sensor = sensor;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}
	
	

}
