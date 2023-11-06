package unitTests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import controllers.RoomConfigController;
import entities.Agent;
import entities.Entity;
import entities.Floorplan;
import entities.SensorActive;
import entities.SensorPassive;
import entities.library.PresenceSensor;
import entities.library.Television;
import geo.Position;
import logic.Input;
import main.Resources;

class InputTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Floorplan floorplan = new Floorplan(
				1,
				5,
				5,
				new ArrayList<Agent>(Arrays.asList(new Agent("John_Doe", new Position(0,0), 1.0))),
				new ArrayList<Position>(Arrays.asList(
					new Position(0,1),
					new Position(0,3),
					new Position(1,1),
					new Position(2,3),
					new Position(3,3),
					new Position(4,3)
				)),
				new ArrayList<SensorActive>(Arrays.asList(
				)),
				new ArrayList<SensorPassive>(Arrays.asList(
				)),
				new ArrayList<Entity>(Arrays.asList(
				))
			);
		
		RoomConfigController roomConfigController = new RoomConfigController();
		roomConfigController.postFloorplan(floorplan);
		
		Television television = new Television();
		television.setName("television");
		television.setPhysicalArea(new ArrayList<Position>(Arrays.asList(new Position(3,2))));
		television.setInteractArea(new ArrayList<Position>(Arrays.asList(new Position(3,1))));
		television.setWalkable(false);
		Resources.getFloorplan().setActiveSensors(new ArrayList<SensorActive>(Arrays.asList(television)));
		
		PresenceSensor doorSensor = new PresenceSensor();
		doorSensor.setName("door");
		doorSensor.setPhysicalArea(new ArrayList<Position>(Arrays.asList(new Position(1,3))));
		doorSensor.setInteractArea(new ArrayList<Position>(Arrays.asList(new Position(1,3))));
		doorSensor.setWalkable(true);
		Resources.getFloorplan().setPassiveSensors(new ArrayList<SensorPassive>(Arrays.asList(doorSensor)));
		
		Entity vestibule = new Entity();
		vestibule.setName("vestibule");
		vestibule.setInteractArea(new ArrayList<Position>(Arrays.asList(new Position(0,0), new Position(1,0))));
		vestibule.setWalkable(true);
		Resources.getFloorplan().setEntities(new ArrayList<Entity>(Arrays.asList(vestibule)));
		
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		Resources.setFloorplan(null);
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		Resources.setInput(null);
	}

	@Test
	void testInputSuccess() {
		String input = "macro(makeTea){goto(4,4);} agent(John_Doe){makeTea();wait(5);;goto(vestibule);interact(television,ON)}";
		Resources.setInput(new Input());
		Resources.getInput().setInput(input);
		assertEquals("consumed",Resources.getInput().test());
	}

	@Test
	void testInputGotoOutOfBounds() {
		String input = "agent(John_Doe){goto(5,5);}";
		Resources.setInput(new Input());
		Resources.getInput().setInput(input);
		assertEquals("ERROR: coordinate is out of bounds in statement 1: goto(5,5)",Resources.getInput().test());
	}
	
	@Test
	void testInputGotoNonWalkable() {
		String input = "agent(John_Doe){goto(0,1);}";
		Resources.setInput(new Input());
		Resources.getInput().setInput(input);
		assertEquals("ERROR: target coordinate is not walkable in statement 1: goto(0,1)",Resources.getInput().test());
	}
	
	@Test
	void testInputInteractNameNonExistent() {
		String input = "agent(John_Doe){interact(radio,TURN_ON);}";
		Resources.setInput(new Input());
		Resources.getInput().setInput(input);
		assertEquals("ERROR: No active sensor exists with the name specified in statement 1: interact(radio,TURN_ON)",Resources.getInput().test());
	}
	
	@Test
	void testInputInteractCommandNonExistent() {
		String input = "agent(John_Doe){interact(television,CHANNEL5);}";
		Resources.setInput(new Input());
		Resources.getInput().setInput(input);
		assertEquals("ERROR: The specified type of sensor does not accept the command in statement 1: interact(television,CHANNEL5)",Resources.getInput().test());
	}
	
	@Test
	void testInputGotoNameNonExistent() {
		String input = "agent(John_Doe){goto(kitchen);}";
		Resources.setInput(new Input());
		Resources.getInput().setInput(input);
		assertEquals("ERROR: No entity or active-sensor exists with the name specified in statement 1: goto(kitchen)",Resources.getInput().test());
	}
	
	@Test
	void testInputSyntaxError() {
		String input = "agent(John_Doe){gote(2,2);}";
		Resources.setInput(new Input());
		Resources.getInput().setInput(input);
		assertEquals("ERROR: syntax error in statement 1: gote(2,2)",Resources.getInput().test());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
