package unitTests;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import controllers.RoomConfigController;
import controllers.SimulationController;
import controllers.SystemController;
import entities.Agent;
import entities.Entity;
import entities.Floorplan;
import entities.SensorActive;
import entities.SensorPassive;
import geo.Position;
import logic.Simulator;
import main.Main;
import main.Resources;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
class ControllerTest {
	
	RoomConfigController roomConfigController = new RoomConfigController();
	@Autowired
	SimulationController simulationController = new SimulationController();
	SystemController systemController = new SystemController();
	
	@AfterEach
	void tearDown() throws Exception {
		Resources.setaStarGrid(null);
		Resources.setFloorplan(null);
		Resources.setInput(null);
		Resources.setLog(null);
		Resources.setMqtt(null);
		Resources.setRandom(null);
		Resources.setSimulator(null);
	}

	@Test
	void testPostInputLackingDependency() {
		String input = "agent(John_Doe){goto(4,4);}";
		
		assertEquals("ERROR: no floorplan has been instantiated",simulationController.postInput(input));
	}

	@Test
	void testPostSimulationLackingDependency() throws MqttPersistenceException, JsonProcessingException, InterruptedException, MqttException {
		Floorplan floorplan = new Floorplan(
				1,
				5,
				5,
				new ArrayList<Agent>(Arrays.asList(new Agent("John_Doe", new Position(0,0), 1.0))),
				new ArrayList<Position>(Arrays.asList(
				)),
				new ArrayList<SensorActive>(Arrays.asList(
				)),
				new ArrayList<SensorPassive>(Arrays.asList(
				)),
				new ArrayList<Entity>(Arrays.asList(
				))
			);
		roomConfigController.postFloorplan(floorplan);
		
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
		
		assertEquals("ERROR: no instructions input has been instantiated",simulationController.postSimulator(simulator));
	}
	
	@Test
	void testPostFloorplanReturnError() {
		
		Floorplan floorplan = new Floorplan(
				1,
				-5,
				5,
				new ArrayList<Agent>(Arrays.asList(new Agent("John_Doe", new Position(0,0), 1.0))),
				new ArrayList<Position>(Arrays.asList(
				)),
				new ArrayList<SensorActive>(Arrays.asList(
					
				)),
				new ArrayList<SensorPassive>(Arrays.asList(
				)),
				new ArrayList<Entity>(Arrays.asList(
				))
			);
		
		assertEquals("ERROR: \"width\" has been defined as a negative integer",roomConfigController.postFloorplan(floorplan));
	}
	
	@Test
	void testPostFloorplanReturnSuccess() {
		
		Floorplan floorplan = new Floorplan(
				1,
				5,
				5,
				new ArrayList<Agent>(Arrays.asList(new Agent("John_Doe", new Position(0,0), 1.0))),
				new ArrayList<Position>(Arrays.asList(
				)),
				new ArrayList<SensorActive>(Arrays.asList(
					
				)),
				new ArrayList<SensorPassive>(Arrays.asList(
				)),
				new ArrayList<Entity>(Arrays.asList(
				))
			);
		
		assertEquals("consumed",roomConfigController.postFloorplan(floorplan));
	}
	
	@Test
	void testPostInputReturnError() {
		
		Floorplan floorplan = new Floorplan(
				1,
				5,
				5,
				new ArrayList<Agent>(Arrays.asList(new Agent("John_Doe", new Position(0,0), 1.0))),
				new ArrayList<Position>(Arrays.asList(
				)),
				new ArrayList<SensorActive>(Arrays.asList(
					
				)),
				new ArrayList<SensorPassive>(Arrays.asList(
				)),
				new ArrayList<Entity>(Arrays.asList(
				))
			);
		roomConfigController.postFloorplan(floorplan);
		
		String input = "agent(John_Doe){goto(5,5);}";
		
		assertEquals("ERROR: coordinate is out of bounds in statement 1: goto(5,5)",simulationController.postInput(input));
	}
	
	@Test
	void testPostInputReturnSuccess() {
		
		Floorplan floorplan = new Floorplan(
				1,
				5,
				5,
				new ArrayList<Agent>(Arrays.asList(new Agent("John_Doe", new Position(0,0), 1.0))),
				new ArrayList<Position>(Arrays.asList(
				)),
				new ArrayList<SensorActive>(Arrays.asList(
					
				)),
				new ArrayList<SensorPassive>(Arrays.asList(
				)),
				new ArrayList<Entity>(Arrays.asList(
				))
			);
		roomConfigController.postFloorplan(floorplan);
		
		String input = "agent(John_Doe){goto(4,4);}";
		
		assertEquals("consumed",simulationController.postInput(input));
	}
	
	@Test
	void testPostSimulationReturnError() throws MqttPersistenceException, JsonProcessingException, InterruptedException, MqttException {
		
		Floorplan floorplan = new Floorplan(
				1,
				5,
				5,
				new ArrayList<Agent>(Arrays.asList(new Agent("John_Doe", new Position(0,0), 1.0))),
				new ArrayList<Position>(Arrays.asList(
				)),
				new ArrayList<SensorActive>(Arrays.asList(
					
				)),
				new ArrayList<SensorPassive>(Arrays.asList(
				)),
				new ArrayList<Entity>(Arrays.asList(
				))
			);
		roomConfigController.postFloorplan(floorplan);
		
		String input = "agent(John_Doe){goto(4,4);}";
		
		simulationController.postInput(input);
		
		Simulator simulator = new Simulator(
				LocalDateTime.parse("2020-01-01T00:00:00.000000000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")),
				true,
				0,
				false,
				0,
				"broker.hivemq.com",
				"1883",
				"smarthome",
				false,
				"test_simulation",
				7357L
				);
		
		assertEquals("ERROR: \"relativeTime\" has not been defined or has its value defined as zero",simulationController.postSimulator(simulator));
	}
	
	@Test
	void testPostSimulationReturnSuccess() throws MqttPersistenceException, JsonProcessingException, InterruptedException, MqttException {
		
		Floorplan floorplan = new Floorplan(
				1,
				5,
				5,
				new ArrayList<Agent>(Arrays.asList(new Agent("John_Doe", new Position(0,0), 1.0))),
				new ArrayList<Position>(Arrays.asList(
				)),
				new ArrayList<SensorActive>(Arrays.asList(
					
				)),
				new ArrayList<SensorPassive>(Arrays.asList(
				)),
				new ArrayList<Entity>(Arrays.asList(
				))
			);
		roomConfigController.postFloorplan(floorplan);
		
		String input = "agent(John_Doe){goto(4,4);}";
		
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
		
		assertEquals("consumed",simulationController.postSimulator(simulator));
	}
	
	@Test
	void testGetSimulation() throws MqttPersistenceException, JsonProcessingException, InterruptedException, MqttException {
		
		Floorplan floorplan = new Floorplan(
				1,
				5,
				5,
				new ArrayList<Agent>(Arrays.asList(new Agent("John_Doe", new Position(0,0), 1.0))),
				new ArrayList<Position>(Arrays.asList(
				)),
				new ArrayList<SensorActive>(Arrays.asList(
					
				)),
				new ArrayList<SensorPassive>(Arrays.asList(
				)),
				new ArrayList<Entity>(Arrays.asList(
				))
			);
		roomConfigController.postFloorplan(floorplan);
		
		String input = "agent(John_Doe){goto(4,4);}";
		
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
		
		assertEquals(simulator,simulationController.getSimulator());
	}
	
	@Test
	void testGetInput() {
		
		Floorplan floorplan = new Floorplan(
				1,
				5,
				5,
				new ArrayList<Agent>(Arrays.asList(new Agent("John_Doe", new Position(0,0), 1.0))),
				new ArrayList<Position>(Arrays.asList(
				)),
				new ArrayList<SensorActive>(Arrays.asList(
					
				)),
				new ArrayList<SensorPassive>(Arrays.asList(
				)),
				new ArrayList<Entity>(Arrays.asList(
				))
			);
		roomConfigController.postFloorplan(floorplan);
		
		String input = "agent(John_Doe){goto(4,4);}";
		
		simulationController.postInput(input);
		
		assertEquals(input,simulationController.getInput());
	}
	
	@Test
	void testFloorplan() {
		
		Floorplan floorplan = new Floorplan(
				1,
				5,
				5,
				new ArrayList<Agent>(Arrays.asList(new Agent("John_Doe", new Position(0,0), 1.0))),
				new ArrayList<Position>(Arrays.asList(
				)),
				new ArrayList<SensorActive>(Arrays.asList(
				)),
				new ArrayList<SensorPassive>(Arrays.asList(
				)),
				new ArrayList<Entity>(Arrays.asList(
				))
			);
		roomConfigController.postFloorplan(floorplan);
		
		assertEquals(floorplan,roomConfigController.getFloorplan());
	}
	
	@Test
	void testPing() {
		assertEquals("pong",systemController.ping());
	}
	
	@Test
	void testGetActiveSensors() throws ClassNotFoundException {
		assertEquals(new ArrayList<String>(Arrays.asList("entities.library.Faucet","entities.library.Television","entities.library.Toilet")),systemController.getActiveSensors());
	}
	
	@Test
	void testGetPassiveSensors() throws ClassNotFoundException {
		assertEquals(new ArrayList<String>(Arrays.asList("entities.library.FloorSensor","entities.library.FloorSensorGlobal","entities.library.PresenceSensor","entities.library.ProximitySensor")),systemController.getPassiveSensors());
	}

}
