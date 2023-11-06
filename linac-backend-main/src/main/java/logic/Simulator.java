package logic;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import controllers.NotificationController;

import com.fasterxml.jackson.core.JsonProcessingException;

import entities.Agent;
import entities.Entity;
import entities.Floorplan;
import geo.Position;
import main.Main;
import main.MqttPaho;
import main.Resources;
import pathfinding.AStarGrid;
import pathfinding.AStarNode;
import pathfinding.NodeState;
import utils.Log;
import entities.SensorActive;
import entities.SensorPassive;

public class Simulator {
	
	private LocalDateTime clock; // LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0); 
	private boolean instantSimulation = true;
	private double relativeTime; // how many/few real-time seconds should a simulated second take
	
	private boolean mqttOutput;
	private int qualityOfService;
	private String mqttHost;
	private String mqttPort;
	private String rootTopic;
	
	private boolean csvOutput = false;
	private String csvFileName;
	
	private Long seed;

	private NotificationController notification;

	private Floorplan floorplan = Resources.getFloorplan();
	private AStarGrid grid = Resources.getaStarGrid();
	ArrayList<SensorPassive> passiveSensors = floorplan.getPassiveSensors();
	ArrayList<SensorActive> activeSensors = floorplan.getActiveSensors();
	
	// B-event properties
	ArrayList<BEvent> bEvents;
	LocalDateTime bEventClock;
	
	public Simulator(LocalDateTime clock, boolean instantSimulation, double relativeTime, boolean mqttOutput, int qualityOfService, String mqttHost, String mqttPort, String rootTopic, boolean csvOutput, String csvFileName, Long seed) {
		this.clock = clock;
		this.instantSimulation = instantSimulation;
		this.relativeTime = relativeTime;
		this.mqttOutput = mqttOutput;
		this.qualityOfService = qualityOfService;
		this.mqttHost = mqttHost;
		this.mqttPort = mqttPort;
		this.rootTopic = rootTopic;
		this.csvOutput = csvOutput;
		this.csvFileName = csvFileName;
		this.seed = seed;
	}
	
	public Simulator() throws MqttException { 
	}
	
	// next-event time progression discrete-event simulation
	public void startSimulator() throws InterruptedException, MqttPersistenceException, MqttException, JsonProcessingException {
		
		// reset sensors' lastTriggerTime variable
		for (SensorPassive sensor : passiveSensors) {
			sensor.setLastTriggerTime(null);
		}
		
		// reset agents' positions
		for (Agent agent : floorplan.getAgents()) {
			agent.setPosition(agent.getInitialPosition());
		}
		
		// reset exemptedAreas of the grid
		Resources.getaStarGrid().resetExemptedAreas();
		
		// reset B-events list
		bEvents = new ArrayList<BEvent>();
		
		// start MQTT client if appropriate
		if (mqttOutput == true) {
			Resources.setMqtt(new MqttPaho(mqttHost, mqttPort, rootTopic, qualityOfService));
		} else {
			Resources.setMqtt(null);
		}
		
		// instantiate Random object with specified seed. Null or 0 will ensure a random seed.
		if (seed == null || seed == 0) {
			Resources.setRandom(new Random());
		} else {
			Resources.setRandom(new Random(seed));
		}
		
		// instantiate Log object if user wants to generate a CSV file 
		if (csvOutput == true) {
			Resources.setLog(new Log(csvFileName));
			Resources.getLog().createFile();
			Resources.getLog().openFileWriter();
		}
		
		// Converting instructions to B-events
		for (Agent agent : Resources.getFloorplan().getAgents()) {
			
			bEventClock = clock;
			
			for (String statement : Resources.getInput().getAgentInstructionLists().get(agent.getId())) {
				//print("* "+statement+":");
				
				// Add output B-event
				String statementSansWhitespace = statement.replaceAll("\\s+","");
				if (!statementSansWhitespace.equals("")) {
					bEvents.add(new BEvent(BEventType.OUTPUT, bEventClock, "* "+agent.getId()+" - "+statement.replaceAll("\\s+","")+":"));
				}
				
				
				// GOTO
				if (Resources.getInput().getGotopattern().matcher(statement).matches()) {
					Position gotoPosition = new Position(
						Integer.parseInt(Resources.getInput().getGotopattern().matcher(statement).replaceAll("$1")),
						Integer.parseInt(Resources.getInput().getGotopattern().matcher(statement).replaceAll("$2"))
					);
					gotoInstructions(agent, gotoPosition, new ArrayList<Position>());
				
				// WAIT
				} else if (Resources.getInput().getWaitpattern().matcher(statement).matches()) {
					long waitTime = Long.parseLong(Resources.getInput().getWaitpattern().matcher(statement).replaceAll("$1")) * 1000000000; // converts seconds to nanoseconds
					waitInstructions(waitTime);
				
				// INTERACT
				} else if (Resources.getInput().getInteractpattern().matcher(statement).matches()) {
					String sensorName = Resources.getInput().getInteractpattern().matcher(statement).replaceAll("$1");
					String command = Resources.getInput().getInteractpattern().matcher(statement).replaceAll("$2");
					interactInstructions(agent, sensorName, command);
				
				// GOTO ENTITY
				} else if (Resources.getInput().getGotoentitypattern().matcher(statement).matches()) {
					String entityName = Resources.getInput().getGotoentitypattern().matcher(statement).replaceAll("$1");
					gotoEntityInstructions(agent, entityName);
				}
			}
			
			// Add end simulation event (for the scenario where the last instruction is wait, to allow for C-events in waiting period)
			bEvents.add(new BEvent(BEventType.SIMULATION_END, bEventClock));
		}
		
		// Sort B-events by time of event
        bEvents.sort(Comparator.comparing(BEvent::getEventDateTime));
		
		// reset agents' positions
		for (Agent agent : floorplan.getAgents()) {
			agent.setPosition(agent.getInitialPosition());
		}
		
		// Three-phase simulation start
		print("*** Simulation has started ***");
		for (BEvent event : bEvents) {
			
			//Update clock to next B-event
			long diff = ChronoUnit.NANOS.between(clock, event.getEventDateTime());
			
			triggerPassiveSensors(diff);
			
			// Movement event
			if (event.getEventType() == BEventType.MOVEMENT) {
				
				//update agent position
				event.getAgent().setPosition(
						event.getNode().getX(), 
						event.getNode().getY()
						);
				print(clock.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")).toString()+" : "+event.getAgent().getId()+" : "+event.getAgent().getPosition().toString()); // print time & position
			
			// Active sensor activation event
			} else if (event.getEventType() == BEventType.SENSOR_ACTIVATION) {
				event.getSensor().interact(event.getCommand());
			
			// Output event
			} else if (event.getEventType() == BEventType.OUTPUT) {
				print(event.getOutput());
			} 
		}
		print("*** Simulation has ended ***");
		
		// close fileWriter if user wants to generate a CSV file 
		if (csvOutput == true) {
			Resources.getLog().closeFileWriter();
		}
	}

	private void gotoInstructions(Agent agent, Position gotoPosition, ArrayList<Position> exemptedCollisions) throws InterruptedException, MqttPersistenceException, MqttException, JsonProcessingException {
		List<AStarNode> path;
		path = grid.getPath(
				agent.getPosition().getX(), 
				agent.getPosition().getY(), 
				gotoPosition.getX(), 
				gotoPosition.getY(),
				exemptedCollisions,
				agent);
		
		// detects if goto is impossible (HALTING ERROR)
		if (path.isEmpty()) {
			print("ERROR: coordinates are not reachable");
		}
		
		for (AStarNode node : path) {
			double distance = agent.getPosition().distance(new Position(node.getX(),node.getY())) * floorplan.getTileSideLength();
			long time = (long) ((distance / agent.getSpeed()) * 1000000000);
			
			//Agent jumps from start of tile to start of tile
			long halfTime = time/2;
			bEventClock = bEventClock.plusNanos(halfTime);
			bEvents.add(new BEvent(BEventType.MOVEMENT, bEventClock, agent, node));
			bEventClock = bEventClock.plusNanos(halfTime);
			agent.setPosition(new Position(node.getX(),node.getY()));
		}
	}

	private void waitInstructions(long waitTime) throws InterruptedException, MqttPersistenceException, MqttException, JsonProcessingException {
		bEventClock = bEventClock.plusNanos(waitTime);
	}
	
	private void interactInstructions(Agent agent, String sensorName, String command) throws MqttPersistenceException, InterruptedException, MqttException, JsonProcessingException {
		for (SensorActive activeSensor : activeSensors) {
			if (activeSensor.getName().equals(sensorName)) {
				if (!activeSensor.getInteractArea().contains(agent.getPosition())) {
					Position randomInteractPosition = activeSensor.getInteractArea().get(Resources.getRandom().nextInt(activeSensor.getInteractArea().size()));
					//print("randomInteractPosition: "+randomInteractPosition); //test
					//intersection tiles of sensor's physicalArea tiles and interactArea tiles that should become walkable
					ArrayList<Position> intersectionArrayList = new ArrayList<Position>();
			        for (Position t : activeSensor.getPhysicalArea()) {
			            if(activeSensor.getInteractArea().contains(t)) {
			            	intersectionArrayList.add(t);
			            }
			        }
					
					gotoInstructions(agent, randomInteractPosition, intersectionArrayList);
				}
				bEvents.add(new BEvent(BEventType.SENSOR_ACTIVATION, bEventClock, activeSensor, command));
				break;
			}
		}
	}
	
	private void gotoEntityInstructions(Agent agent, String entityName) throws MqttPersistenceException, InterruptedException, MqttException, JsonProcessingException {
		ArrayList<Entity> union = new ArrayList<Entity>();
		union.addAll(floorplan.getEntities());
		union.addAll(activeSensors);
		
		for (Entity entity : union) {
			if (entity.getName().equals(entityName)) {
				if (!entity.getInteractArea().contains(agent.getPosition())) {
					ArrayList<Position> gotoAblePositions = new ArrayList<Position>();
					for (Position position : entity.getInteractArea()) {
						if (grid.getNode(position.getX(), position.getY()).getState() == NodeState.WALKABLE || entity.getPhysicalArea().contains(position)) {
							gotoAblePositions.add(position);
						}
					}
					Position randomInteractPosition = gotoAblePositions.get(Resources.getRandom().nextInt(gotoAblePositions.size()));
					//print("randomInteractPosition: "+randomInteractPosition); //test
					//intersection tiles of entity's physicalArea tiles and interactArea tiles that should become walkable
					ArrayList<Position> intersectionArrayList = new ArrayList<Position>();
			        for (Position t : entity.getPhysicalArea()) {
			            if(entity.getInteractArea().contains(t)) {
			            	intersectionArrayList.add(t);
			            }
			        }
					
					gotoInstructions(agent, randomInteractPosition, intersectionArrayList);
				}
				break;
			}
		}
	}
	
	private void updateTime(long nanos) throws InterruptedException {
		clock = clock.plusNanos(nanos);
		
		if (instantSimulation == false) {
			TimeUnit.NANOSECONDS.sleep( (long) Math.round(nanos * relativeTime));
		}
		
	}
	
	private void triggerPassiveSensors(long time) throws InterruptedException, MqttPersistenceException, MqttException, JsonProcessingException {
		LocalDateTime newTileTime = clock.plusNanos(time);
		ArrayList<TriggerEvent> eventList = new ArrayList<TriggerEvent>();
		for (SensorPassive sensor : passiveSensors) { 
			if (sensor.updateState() == true) {
				long i = 0;
				if (sensor.getLastTriggerTime() != null && sensor.getLastTriggerTime().until(clock,ChronoUnit.NANOS) < sensor.getTriggerFrequency()) {
					i = -sensor.getLastTriggerTime().until(clock,ChronoUnit.NANOS) + sensor.getTriggerFrequency();
				}
				for (; i < time; i = i + sensor.getTriggerFrequency()) {
					eventList.add(new TriggerEvent(sensor,clock.plusNanos(i)));
					sensor.setLastTriggerTime(clock.plusNanos(i));
				}
			}
		}
		eventList.sort(Comparator.comparing(TriggerEvent::getDateTime));
		for (TriggerEvent triggerEvent : eventList) {
			updateTime(clock.until(triggerEvent.getDateTime(),ChronoUnit.NANOS));
			((SensorPassive) triggerEvent.getSensor()).outputSensorReading();
		}
		updateTime(clock.until(newTileTime,ChronoUnit.NANOS));
	}
	
	// Prints to both console and WebSocket - meant for human consumption.
	private void print(String message) {
		System.out.println(message);
		if (Main.isWebsocketOutput() == true) {
			notification.notifyToClient(message);
		}
	}
	/*
	private void triggerPassiveSensors(long time) throws InterruptedException, MqttPersistenceException, MqttException {
		LocalDateTime newTileTime = clock.plusNanos(time);
		ArrayList<TriggerEvent> eventList = new ArrayList<TriggerEvent>();
		for (SensorPassive sensor : grid.getNode(agent.getPosition().getX(), agent.getPosition().getY()).getPassiveTriggers()) { // for all passive sensors in the tile where the agent is present
			long i = 0;
			if (sensor.getLastTriggerTime() != null && sensor.getLastTriggerTime().until(clock,ChronoUnit.NANOS) < sensor.getTriggerFrequency()) {
				i = -sensor.getLastTriggerTime().until(clock,ChronoUnit.NANOS) + sensor.getTriggerFrequency();
			}
			for (; i < time; i = i + sensor.getTriggerFrequency()) {
				eventList.add(new TriggerEvent(sensor,clock.plusNanos(i)));
				sensor.setLastTriggerTime(clock.plusNanos(i));
			}
		}
		eventList.sort(Comparator.comparing(TriggerEvent::getDateTime));
		for (TriggerEvent triggerEvent : eventList) {
			updateTime(clock.until(triggerEvent.getDateTime(),ChronoUnit.NANOS));
			((SensorPassive) triggerEvent.getSensor()).trigger();
		}
		updateTime(clock.until(newTileTime,ChronoUnit.NANOS));
	}
	*/
	
	public String test() {
		//--- relativeTime ---//
		// test that relativeTime is not zero and that it has been instantiated
		if (relativeTime == 0) {
			return "ERROR: \"relativeTime\" has not been defined or has its value defined as zero";
		}
		// test that relativeTime is not negative
		if (relativeTime < 0) {
			return "ERROR: \"relativeTime\" has been defined as a negative integer";
		}
		
		//--- qualityOfService ---//
		if (qualityOfService < 0 || qualityOfService > 2) {
			return "ERROR: \"qualityOfService\" has not been defined as either 0, 1, or 2";
		}
		
		//--- CSV ---//
		// test to ensure that a csvFileName has been given
		if (csvFileName == null || csvFileName == "") {
			return "ERROR: no filename for the CSV output has been set";
		}
		
		return "consumed";
	}
	
	//Accessors and Mutators
	public void setClock(LocalDateTime clock) {
		this.clock = clock;
	}

	public LocalDateTime getClock() {
		return clock;
	}

	public boolean getInstantSimulation() {
		return instantSimulation;
	}

	public void setInstantSimulation(boolean instantSimulation) {
		this.instantSimulation = instantSimulation;
	}

	public double getRelativeTime() {
		return relativeTime;
	}

	public void setRelativeTime(double timeFactor) {
		this.relativeTime = timeFactor;
	}

	public boolean getMqttOutput() {
		return mqttOutput;
	}

	public void setMqttOutput(boolean mqttOutput) {
		this.mqttOutput = mqttOutput;
	}

	public int getQualityOfService() {
		return qualityOfService;
	}

	public void setQualityOfService(int qualityOfService) {
		this.qualityOfService = qualityOfService;
	}

	public String getMqttHost() {
		return mqttHost;
	}

	public void setMqttHost(String mqttHost) {
		this.mqttHost = mqttHost;
	}

	public String getMqttPort() {
		return mqttPort;
	}

	public void setNotification(NotificationController notification){
		this.notification = notification;
	}

	public NotificationController getNotification() {
		return notification;
	}

	public void setMqttPort(String mqttPort) {
		this.mqttPort = mqttPort;
	}

	public String getRootTopic() {
		return rootTopic;
	}

	public void setRootTopic(String rootTopic) {
		this.rootTopic = rootTopic;
	}

	public Long getSeed() {
		return seed;
	}

	public void setSeed(Long seed) {
		this.seed = seed;
	}

	public boolean getCsvOutput() {
		return csvOutput;
	}

	public void setCsvOutput(boolean csvOutput) {
		this.csvOutput = csvOutput;
	}

	public String getCsvFileName() {
		return csvFileName;
	}

	public void setCsvFileName(String csvFileName) {
		this.csvFileName = csvFileName;
	}
	
	
}