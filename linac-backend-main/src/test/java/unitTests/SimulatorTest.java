package unitTests;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import controllers.SimulationController;
import entities.Agent;
import entities.Entity;
import entities.Floorplan;
import entities.SensorActive;
import entities.SensorPassive;
import entities.library.PresenceSensor;
import entities.library.Television;
import geo.Position;
import logic.Simulator;
import main.Main;
import main.Resources;
import pathfinding.AStarGrid;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
class SimulatorTest {
	
	@Autowired
	SimulationController simulationController = new SimulationController();
	
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
		
		Resources.setFloorplan(floorplan);
		
		Television television2 = new Television();
		television2.setName("television2");
		television2.setPhysicalArea(new ArrayList<Position>(Arrays.asList(new Position(4,2))));
		television2.setInteractArea(new ArrayList<Position>(Arrays.asList(new Position(4,2))));
		television2.setWalkable(false);
		
		Television television = new Television();
		television.setName("television");
		television.setPhysicalArea(new ArrayList<Position>(Arrays.asList(new Position(3,2))));
		television.setInteractArea(new ArrayList<Position>(Arrays.asList(new Position(3,1))));
		television.setWalkable(false);
		Resources.getFloorplan().setActiveSensors(new ArrayList<SensorActive>(Arrays.asList(television2, television)));
		
		PresenceSensor doorSensor = new PresenceSensor();
		doorSensor.setName("door");
		doorSensor.setPhysicalArea(new ArrayList<Position>(Arrays.asList(new Position(1,3))));
		doorSensor.setInteractArea(new ArrayList<Position>(Arrays.asList(new Position(1,3))));
		doorSensor.setWalkable(true);
		doorSensor.setTriggerFrequency(1000000000L);
		Resources.getFloorplan().setPassiveSensors(new ArrayList<SensorPassive>(Arrays.asList(doorSensor)));
		
		Entity window = new Entity();
		window.setName("window");
		window.setPhysicalArea(new ArrayList<Position>(Arrays.asList(new Position(0,3))));
		window.setInteractArea(new ArrayList<Position>(Arrays.asList(new Position(0,2),new Position(0,4))));
		window.setWalkable(false);
		
		Entity chair = new Entity();
		chair.setName("chair");
		chair.setPhysicalArea(new ArrayList<Position>(Arrays.asList(new Position(3,1))));
		chair.setInteractArea(new ArrayList<Position>(Arrays.asList(new Position(3,1))));
		chair.setWalkable(false);
		
		Entity vestibule = new Entity();
		vestibule.setName("vestibule");
		vestibule.setPhysicalArea(new ArrayList<Position>(Arrays.asList(new Position(0,0), new Position(1,0))));
		vestibule.setInteractArea(new ArrayList<Position>(Arrays.asList(new Position(0,0), new Position(1,0))));
		vestibule.setWalkable(true);
		Resources.getFloorplan().setEntities(new ArrayList<Entity>(Arrays.asList(vestibule, window, chair)));
		
		Resources.setaStarGrid(new AStarGrid(Resources.getFloorplan()));
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		Resources.setaStarGrid(null);
		Resources.setFloorplan(null);
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		Resources.setInput(null);
		Resources.setLog(null);
		Resources.setMqtt(null);
		Resources.setRandom(null);
		Resources.setSimulator(null);
	}

	@Test
	void testSimulationSuccess() throws MqttPersistenceException, JsonProcessingException, InterruptedException, MqttException {
		
		String input = "agent(John_Doe){goto(4,4);wait(5);goto(0,0);goto(vestibule);interact(television,ON);interact(television,CHANNEL1);interact(television2,ON);goto(window);goto(chair)}";
		simulationController.postInput(input);
		
		Simulator simulator = new Simulator(
			LocalDateTime.parse("2020-01-01T00:00:00.000000000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")),
			true,
			1.0,
			false,
			0,
			"broker.hivemq.com",
			"1883",
			"smarthome",
			false,
			"test_simulation",
			7357L
		);
		
		simulationController.postSimulator(simulator);	
	}
	

	@Test
	void testSimulationSuccess2() throws MqttPersistenceException, JsonProcessingException, InterruptedException, MqttException {
		
		String input = "agent(John_Doe){goto(1,3);}";
		simulationController.postInput(input);
		
		Simulator simulator = new Simulator(
			LocalDateTime.parse("2020-01-01T00:00:00.000000000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")),
			false,
			0.1,
			true,
			0,
			"broker.hivemq.com",
			"1883",
			"smarthome",
			false,
			"test_simulation",
			null
		);
		simulationController.postSimulator(simulator);	
	}
	
	@Test
	void testSimulationRelativeTimeZero() throws MqttPersistenceException, JsonProcessingException, InterruptedException, MqttException {
		
		String input = "agent(John_Doe){goto(1,3);}";
		simulationController.postInput(input);
		
		Simulator simulator = new Simulator(
			LocalDateTime.parse("2020-01-01T00:00:00.000000000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")),
			false,
			0.0,
			true,
			0,
			"broker.hivemq.com",
			"1883",
			"smarthome",
			false,
			"test_simulation",
			null
		);
		assertEquals("ERROR: \"relativeTime\" has not been defined or has its value defined as zero",simulationController.postSimulator(simulator));
	}
	
	@Test
	void testSimulationRelativeTimeNegative() throws MqttPersistenceException, JsonProcessingException, InterruptedException, MqttException {
		
		String input = "agent(John_Doe){goto(1,3);}";
		simulationController.postInput(input);
		
		Simulator simulator = new Simulator(
			LocalDateTime.parse("2020-01-01T00:00:00.000000000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")),
			false,
			-1.0,
			true,
			0,
			"broker.hivemq.com",
			"1883",
			"smarthome",
			false,
			"test_simulation",
			null
		);
		assertEquals("ERROR: \"relativeTime\" has been defined as a negative integer",simulationController.postSimulator(simulator));
	}
	
	@Test
	void testSimulationQualityOfServiceError() throws MqttPersistenceException, JsonProcessingException, InterruptedException, MqttException {
		
		String input = "agent(John_Doe){goto(1,3);}";
		simulationController.postInput(input);
		
		Simulator simulator = new Simulator(
			LocalDateTime.parse("2020-01-01T00:00:00.000000000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")),
			false,
			1.0,
			true,
			3,
			"broker.hivemq.com",
			"1883",
			"smarthome",
			false,
			"test_simulation",
			null
		);
		assertEquals("ERROR: \"qualityOfService\" has not been defined as either 0, 1, or 2",simulationController.postSimulator(simulator));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
