package entities.library;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import entities.Agent;
import entities.SensorPassive;
import geo.Position;
import main.Resources;

public class FloorSensorGlobal extends SensorPassive {

	// Default constructor
	public FloorSensorGlobal()  throws MqttPersistenceException, MqttException {
	}
	
	public void defineDefaultState() {
		state.put("pressurePositions", null);
	}
	
	// Trigger behavior 
	public boolean updateState() {
		ArrayList<Position> agentPositions = new ArrayList<Position>();
		for (Agent agent : Resources.getFloorplan().getAgents()) {
			agentPositions.add(agent.getPosition());
		}
		state.put("pressurePositions", agentPositions);
		return true;

	}
}
