package controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import entities.Floorplan;
import main.Resources;
import pathfinding.AStarGrid;

@RestController
@RequestMapping("/api/roomConfig/")
@CrossOrigin
public class RoomConfigController {
	
	@PostMapping("/floorplan")
	public String postFloorplan(@RequestBody Floorplan floorplan) {
		String testResult = floorplan.test();
		if (testResult.equals("consumed")) {
			Resources.setFloorplan(floorplan);
			Resources.setaStarGrid(new AStarGrid(Resources.getFloorplan()));
		} else {
			System.out.println(testResult);
		}
		return testResult;
	}
	
	@GetMapping("/floorplan")
	public @ResponseBody Floorplan getFloorplan() {
		return Resources.getFloorplan(); 
	}

}












































/*
ArrayList<Tile> testList = new ArrayList<Tile>( Arrays.asList( 	
		new Tile(0,4,0), new Tile(1,4,0), new Tile(2,4,0), new Tile(3,4,0), new Tile(4,4,0), 
		new Tile(0,3,0), new Tile(1,3,1), new Tile(2,3,1), new Tile(3,3,1), new Tile(4,3,0), 
		new Tile(0,2,0), new Tile(1,2,0), new Tile(2,2,0), new Tile(3,2,1), new Tile(4,2,0), 
		new Tile(0,1,0), new Tile(1,1,0), new Tile(2,1,0), new Tile(3,1,1), new Tile(4,1,0), 
		new Tile(0,0,0), new Tile(1,0,0), new Tile(2,0,0), new Tile(3,0,0), new Tile(4,0,0)		));

@GetMapping("/test")
public @ResponseBody Grid test() {
	return new Grid(5,5,testList); 
}

// pathfinding2
@GetMapping("/test2")
public @ResponseBody List<AStarNode> test2() {
	AStarGrid grid1 = new AStarGrid(5,5);
	
	grid1.setNodeState(0, 1, NodeState.NOT_WALKABLE);
	grid1.setNodeState(1, 1, NodeState.NOT_WALKABLE);
	grid1.setNodeState(0, 3, NodeState.NOT_WALKABLE);
	grid1.setNodeState(2, 3, NodeState.NOT_WALKABLE);
	grid1.setNodeState(3, 3, NodeState.NOT_WALKABLE);
	grid1.setNodeState(4, 3, NodeState.NOT_WALKABLE);
	
	return grid1.getPath(0, 0, 4, 4);
}


// get a predefined grid tester
@GetMapping("/getGridTest")
public @ResponseBody AStarGrid getGridTest() {
	AStarGrid testGrid = new AStarGrid(5,5);
	return testGrid; 
}


AStarGrid grid2;

// instantiates a grid of @width and @height with tiles being WALKABLE by default.
@PostMapping("/instantiateGrid")
public void instantiateGrid(@RequestBody int width, int height) {
	this.grid2 = new AStarGrid(width,height);
}

@PostMapping("/insertCollisionObjects")
public void insertCollisionObjects(@RequestBody ArrayList<int[]> positions) {
	for (int i = 0; i < positions.size(); i++) {
		this.grid2.setNodeState(positions.get(i)[0], positions.get(i)[1], NodeState.NOT_WALKABLE);
	}
}
*/
