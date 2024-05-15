package entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import geo.Position;

public class Floorplan {
	
	private double tileSideLength; // width and height of a square tile in meters
	private int width; // width of floor plan in tiles
	private int height; // height of floor plan in tiles
	private ArrayList<Agent> agents;
	private ArrayList<Position> walls;
	private ArrayList<SensorActive> activeSensors;
	private ArrayList<SensorPassive> passiveSensors;
	private ArrayList<Entity> entities;
	
	public Floorplan(int tileSideLength, int width, int height, ArrayList<Agent> agents, ArrayList<Position> walls, ArrayList<SensorActive> activeSensors, ArrayList<SensorPassive> passiveSensors, ArrayList<Entity> entities) {
		this.tileSideLength = tileSideLength;
		this.width = width;
		this.height = height;
		this.agents = agents;
		this.walls = walls;
		this.entities = entities;
		this.activeSensors = activeSensors;
		this.passiveSensors = passiveSensors;
	}
	
	public Floorplan() {
	}
	
	public String test() {
		//--- tileSideLength ---//
		// test that tileSideLength is not zero and that it has been instantiated
		if (tileSideLength == 0) {
			return "ERROR: \"tileSideLength\" has not been defined or has its value defined as zero";
		}
		// test that tileSideLength is not negative
		if (tileSideLength < 0) {
			return "ERROR: \"tileSideLength\" has been defined as a negative decimal";
		}
		
		//--- width ---//
		// test that width is not zero and that it has been instantiated
		if (width == 0) {
			return "ERROR: \"width\" has not been defined or has its value defined as zero";
		}
		// test that width is not negative
		if (width < 0) {
			return "ERROR: \"width\" has been defined as a negative integer";
		}
		
		//--- height ---//
		// test that height is not zero and that it has been instantiated
		if (height == 0) {
			return "ERROR: \"height\" has not been defined or has its value defined as zero";
		}
		// test that height is not negative
		if (height < 0) {
			return "ERROR: \"height\" has been defined as a negative integer";
		}
		
		//--- walls ---//
		// test that walls are within the grid
		for (Position wall : walls) {
			if (!isWithin(wall)) {
				return "ERROR: the wall at "+wall+" is not within the grid";
			}
		}

		//--- agent ---//
		// test that agents' initial positions has been instantiated
		for (Agent agent : agents) {
			if (agent.getInitialPosition() == null) {
				return "ERROR: an agent's \"initialPosition\" has not been defined";
			}
		}
		// test that agents' initial positions are within the grid
		for (Agent agent : agents) {
			if (!isWithin(agent.getInitialPosition())) {
				return "ERROR: an agent's \"initialPosition\" is not within the grid";
			}
		}
		// test that agent position is on a walkable surface
		for (Entity entity : getAllEntities()) {
			if (entity.getWalkable() == false) {
				for (Position position : entity.getPhysicalArea()) {
					for (Agent agent : agents) {
						if (position.equals(agent.getInitialPosition())) {
							return "ERROR: an agent's initial position is a non-walkable tile";
						}
					}
				}
			}
		}
		for (Position position : walls) {
			for (Agent agent : agents) {
				if (position.equals(agent.getInitialPosition())) {
					return "ERROR: agent's initial position is a non-walkable tile";
				}
			}
		}
		
		// test that agent speed is not zero and that it has been instantiated
		for (Agent agent : agents) {
			if (agent.getSpeed() == 0) {
				return "ERROR: an agent's \"speed\" has not been defined or has its value defined as zero";
			}
		}
		
		// test that agent speed is not negative
		for (Agent agent : agents) {
			if (agent.getSpeed() < 0) {
				return "ERROR: an agent's \"speed\" has been defined as a negative value";
			}
		}
		
		// test to ensure that agents have unique IDs
		Set<String> uniquesIds = new HashSet<String>();
		for (Agent agent : agents) {
			uniquesIds.add(agent.getId());
		}
		if(uniquesIds.size() < agents.size()){
			return "ERROR: the IDs of the agents are not unique";
		}

		//--- entities ---//
		for (Entity entity : entities) {
			// test that physicalArea is within the grid
			for (Position position : entity.getPhysicalArea()) {
				if (!isWithin(position)) {
					return "ERROR: the physical Area of the entity at "+position+" is not within the grid";
				}
			}
			// test that interactArea is within the grid
			for (Position position : entity.getInteractArea()) {
				if (!isWithin(position)) {
					return "ERROR: the interact Area of the entity at "+position+" is not within the grid";
				}
			}
		}
		
		//--- activeSensors ---//
		for (SensorActive activeSensor : activeSensors) {
			// test that physicalArea is within the grid
			for (Position position : activeSensor.getPhysicalArea()) {
				if (!isWithin(position)) {
					return "ERROR: the physical Area of the active sensor at "+position+" is not within the grid";
				}
			}
			// test that interactArea is within the grid
			for (Position position : activeSensor.getInteractArea()) {
				if (!isWithin(position)) {
					return "ERROR: the interact Area of the active sensor at "+position+" is not within the grid";
				}
			}
		}
		
		//--- passiveSensors ---//
		for (SensorPassive passiveSensor : passiveSensors) {
			// test that physicalArea is within the grid
			for (Position position : passiveSensor.getPhysicalArea()) {
				if (!isWithin(position)) {
					return "ERROR: the physical Area of the passive sensor at "+position+" is not within the grid";
				}
			}
			// test that interactArea is within the grid
			for (Position position : passiveSensor.getInteractArea()) {
				if (!isWithin(position)) {
					return "ERROR: the interact Area of the passive sensor at "+position+" is not within the grid";
				}
			}
			// test that triggerFrequency is not zero and that it has been instantiated
			if (passiveSensor.getTriggerFrequency() == 0) {
				return "ERROR: \"triggerFrequency\" of the passive sensor "+passiveSensor.getName()+" has not been defined or has its value defined as zero";
			}
		}
		return "consumed";
	}
	
	public final boolean isWithin(Position position) {
        return position.getX() >= 0 && position.getX() < width && position.getY() >= 0 && position.getY() < height;
    }
	
	//Accessors and Mutators
	public ArrayList<Sensor> getAllSensors() {
		ArrayList<Sensor> allSensors = new ArrayList<Sensor>();
		allSensors.addAll(activeSensors);
		allSensors.addAll(passiveSensors);
		return allSensors;
	}
	
	public ArrayList<Entity> getAllEntities() {
		ArrayList<Entity> allEntities = new ArrayList<Entity>();
		allEntities.addAll(activeSensors);
		allEntities.addAll(passiveSensors);
		allEntities.addAll(entities);
		return allEntities;
	}
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public ArrayList<Agent> getAgents() {
		return agents;
	}

	public void setAgents(ArrayList<Agent> agents) {
		this.agents = agents;
	}

	public ArrayList<Position> getWalls() {
		return walls;
	}

	public void setWalls(ArrayList<Position> walls) {
		this.walls = walls;
	}

	public double getTileSideLength() {
		return tileSideLength;
	}

	public void setTileSideLength(double tileSideLength) {
		this.tileSideLength = tileSideLength;
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public void setEntities(ArrayList<Entity> entities) {
		this.entities = entities;
	}

	public ArrayList<SensorActive> getActiveSensors() {
		return activeSensors;
	}

	public void setActiveSensors(ArrayList<SensorActive> activeSensors) {
		this.activeSensors = activeSensors;
	}

	public ArrayList<SensorPassive> getPassiveSensors() {
		return passiveSensors;
	}

	public void setPassiveSensors(ArrayList<SensorPassive> passiveSensors) {
		this.passiveSensors = passiveSensors;
	}
	
	
	
}
