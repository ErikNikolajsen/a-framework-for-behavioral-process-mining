package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import geo.Position;

public class Agent {
	
	private String id;
	private Position initialPosition;
	private double speed; // meter per second
	
	@JsonIgnore private Position position;
	
	//Constructors
	public Agent(String id, Position point, double speed) {
		this.id = id;
		this.initialPosition = point;
		this.speed = speed;
	}
	
	public Agent() {
	}
	
	//Accessors and Mutators
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public Position getInitialPosition() {
		return initialPosition;
	}

	public void setInitialPosition(Position initialPosition) {
		this.initialPosition = initialPosition;
	}
	
	public void setPosition(int x, int y) {
		this.position = new Position(x,y);
	}
	
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	

}
