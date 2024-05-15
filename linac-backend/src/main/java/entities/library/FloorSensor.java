package entities.library;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import entities.Agent;
import entities.SensorPassive;
import main.Resources;

public class FloorSensor extends SensorPassive {

	// Default constructor
	public FloorSensor()  throws MqttPersistenceException, MqttException {
	}
	
	public void defineDefaultState() {
		state.put("pressurePosition", null);
	}
	
	// Trigger behavior 
	public boolean updateState() {
		for (Agent agent : Resources.getFloorplan().getAgents()) {
			if (getInteractArea().contains(agent.getPosition())) {
				state.put("pressurePosition", agent.getPosition());
				return true;
			}
		}
		state.put("pressurePosition", null);
		return false;
	}
}
