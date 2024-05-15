package entities.library;


import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import entities.Agent;
import entities.SensorPassive;
import main.Resources;

public class PresenceSensor extends SensorPassive {

	// Default constructor
	public PresenceSensor()  throws MqttPersistenceException, MqttException {
		
	}
	
	public void defineDefaultState() {
		state.put("location", false);
	}
	
	// Trigger behavior 
	public boolean updateState() {
		for (Agent agent : Resources.getFloorplan().getAgents()) {
			if (getInteractArea().contains(agent.getPosition())) {
				state.put("location", this.getName());
				return true;
			}
		}
		state.put("location", false);
		return false;
	}
}
