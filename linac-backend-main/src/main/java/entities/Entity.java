package entities;

import java.util.ArrayList;

import geo.Position;

public class Entity {
	
	private String name;
	private ArrayList<Position> physicalArea;
	private ArrayList<Position> interactArea;
	private boolean walkable = true;
	
	//Constructors
	public Entity(String name, ArrayList<Position> physicalArea, ArrayList<Position> interactArea, Boolean walkable) {
		this.name = name;
		this.physicalArea = physicalArea;
		this.interactArea = interactArea;
		this.walkable = walkable;
	}
	
	public Entity() {
	}
	
	//Accessors and Mutators
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ArrayList<Position> getPhysicalArea() {
			return physicalArea;
		}

		public void setPhysicalArea(ArrayList<Position> physicalArea) {
			this.physicalArea = physicalArea;
		}

		public ArrayList<Position> getInteractArea() {
			return interactArea;
		}

		public void setInteractArea(ArrayList<Position> interactArea) {
			this.interactArea = interactArea;
		}

		public boolean getWalkable() {
			return walkable;
		}

		public void setWalkable(boolean walkable) {
			this.walkable = walkable;
		}
		
	
	
}


