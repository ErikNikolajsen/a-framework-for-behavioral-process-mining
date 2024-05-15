package pathfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import entities.Agent;
import entities.Entity;
import entities.Floorplan;
import entities.Sensor;
import entities.SensorActive;
import entities.SensorPassive;
import geo.Position;

/**
 * A* grid containing A* nodes.
 *
 * @author Erik Ravn Nikolajsen
 */
public class AStarGrid {

    private AStarLogic logic = new AStarLogic();
    private AStarNode[][] grid;

    /**
     * Constructs A* grid with A* nodes with given width and height.
     * All nodes are initially {@link NodeState#WALKABLE}
     *
     * @param width grid width
     * @param height grid height
     */
    public AStarGrid(int width, int height) {
        if (width < 1 || height < 1)
            throw new IllegalArgumentException("width and height cannot < 1");

        grid = new AStarNode[width][height];
        for (int y = 0; y < grid[0].length; y++) {
            for (int x = 0; x < grid.length; x++) {
                grid[x][y] = new AStarNode(x, y, NodeState.WALKABLE);
            }
        }
    }
    
    /**
     * Default constructor for POST requests
     */
    public AStarGrid() {
	}
    
    /**
     * Constructs A* grid with A* nodes from a given Floorplan
     */
    public AStarGrid(Floorplan floorplan) {
    	// instantiate grid with specified width and height and insert walkable nodes for all tiles
    	grid = new AStarNode[floorplan.getWidth()][floorplan.getHeight()];
        for (int y = 0; y < floorplan.getHeight(); y++) {
            for (int x = 0; x < floorplan.getWidth(); x++) {
                grid[x][y] = new AStarNode(x, y, NodeState.WALKABLE);
            }
        }
        // make nodes of tiles with walls non-walkable
		for (Position i : floorplan.getWalls()) {
			setNodeState(i.getX(), i.getY(), NodeState.NOT_WALKABLE);
		}
		// make nodes of tiles with non-walkable passive sensors not-walkable
		for (Sensor sensor : floorplan.getPassiveSensors()) {
			if (sensor.getWalkable() == false) {
				for (Position position : sensor.getPhysicalArea()) {
					setNodeState(position.getX(), position.getY(), NodeState.NOT_WALKABLE);
				}
			}
		}
		// make nodes of tiles with non-walkable active sensors not-walkable
				for (Sensor sensor : floorplan.getPassiveSensors()) {
					if (sensor.getWalkable() == false) {
						for (Position position : sensor.getPhysicalArea()) {
							setNodeState(position.getX(), position.getY(), NodeState.NOT_WALKABLE);
						}
					}
				}
		// make nodes of tiles with non-walkable entities not-walkable
		for (Entity entity : floorplan.getEntities()) {
			if (entity.getWalkable() == false) {
				for (Position position : entity.getPhysicalArea()) {
					setNodeState(position.getX(), position.getY(), NodeState.NOT_WALKABLE);
				}
			}
		}
		
		// Add interactArea of active and passive sensors to respective nodes
		for (Sensor sensor : floorplan.getPassiveSensors()) {
			for (Position interactPosition : sensor.getInteractArea()) {
				getNode(interactPosition.getX(), interactPosition.getY()).addPassiveTriggers((SensorPassive) sensor);
			}
		}
		for (Sensor sensor : floorplan.getActiveSensors()) {
			for (Position interactPosition : sensor.getInteractArea()) {
				getNode(interactPosition.getX(), interactPosition.getY()).addActiveTriggers((SensorActive) sensor);
			}
		}
	}

    /**
     * @return grid width
     */
    public final int getWidth() {
        return grid.length;
    }

    /**
     * @return grid height
     */
    public final int getHeight() {
        return grid[0].length;
    }

    /**
     *
     * @param x x coord
     * @param y y coord
     * @return true IFF the point is within the grid
     */
    public final boolean isWithin(int x, int y) {
        return x >= 0 && x < getWidth()
                && y >= 0 && y < getHeight();
    }

    /**
     * Convenience method to set state of all nodes to given state.
     *
     * @param state node state
     */
    public final void setStateForAllNodes(NodeState state) {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                getNode(x, y).setState(state);
            }
        }
    }

    /**
     * Set state of the node at x, y.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param state the state
     */
    public final void setNodeState(int x, int y, NodeState state) {
        getNode(x, y).setState(state);
    }

    /**
     * Returns state of the node at a, y.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return  the state
     */
    public final NodeState getNodeState(int x, int y) {
        return getNode(x, y).getState();
    }

    /**
     * Returns a list of A* nodes from start to target.
     * The list will include target.
     * Return an empty list if the path doesn't exist.
     *
     * @param startX start node x
     * @param startY start node y
     * @param targetX target node x
     * @param targetY target node y
     * @return the path
     */
    HashMap<Agent, ArrayList<ArrayList<Position>>> exemptedAreas = new HashMap<Agent, ArrayList<ArrayList<Position>>>();
    public final List<AStarNode> getPath(int startX, int startY, int targetX, int targetY, ArrayList<Position> exempted, Agent agent) {
    	
    	//removes areas in exemptedAreas where the agent is not in anymore. Also makes those areas not-walkable again.
    	if (exemptedAreas.get(agent) != null) {
    		
    		ArrayList<ArrayList<Position>> exemptedAreasClone = new ArrayList<ArrayList<Position>>();
    		for (ArrayList<Position> area : exemptedAreas.get(agent)) {
    			exemptedAreasClone.add(area);
    		}
	    	for (ArrayList<Position> area : exemptedAreasClone) {
	    		if (!area.contains(agent.getPosition())) {
	    			for (Position position : area) {
	    	    		setNodeState(position.getX(), position.getY(), NodeState.NOT_WALKABLE);
	    	    	} 
	    			exemptedAreas.get(agent).remove(area);
	    		}
	    	}
    	}
    	
    	//adds new exempted area to exemptedAreas - and makes exempted area walkable.
    	ArrayList<ArrayList<Position>> list = exemptedAreas.get(agent);
    	if (exemptedAreas.get(agent) == null) {
    		list = new ArrayList<ArrayList<Position>>();
    		list.add(exempted);
	    	exemptedAreas.put(agent, list);
    	} else {
    		list.add(exempted);
    	}
    	for (Position position : exempted) {
    		setNodeState(position.getX(), position.getY(), NodeState.WALKABLE);
    	}
    	
    	//calculates path
    	return logic.getPath(grid, getNode(startX, startY), getNode(targetX, targetY));
    }
    
    public void resetExemptedAreas() {
		exemptedAreas.clear();
	}

    /**
     * Returns a node at x, y. There is no bounds checking.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return A* node at x, y
     */
    public final AStarNode getNode(int x, int y) {
        return grid[x][y];
    }

    /**
     * @return underlying grid of nodes
     */
    public final AStarNode[][] getGrid() {
        return grid;
    }

    /**
     * @return all grid nodes
     */
    public final List<AStarNode> getNodes() {
        List<AStarNode> nodes = new ArrayList<>();

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                nodes.add(getNode(x, y));
            }
        }

        return nodes;
    }

	




    
    
}
