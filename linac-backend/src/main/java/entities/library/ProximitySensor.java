package entities.library;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import entities.Agent;
import entities.SensorPassive;
import geo.Position;
import main.Resources;

public class ProximitySensor extends SensorPassive {

	// Default constructor
	public ProximitySensor()  throws MqttPersistenceException, MqttException {
	}
	
	public void defineDefaultState() {
		state.put("triggered", false);
		state.put("proximity", null);
	}
	
	// Trigger behavior 
	public boolean updateState() {
		
		if (getPhysicalArea().size() == 0) {
			state.put("triggered", false);
			state.put("proximity", null);
			return false;
		}
		
		Double closestProximity = null;
		
		for (Agent agent : Resources.getFloorplan().getAgents()) {
			if (getInteractArea().contains(agent.getPosition())) {
				
				double x = 0;
				double y = 0;
				for (Position position : getPhysicalArea()) {
					x = x + position.getX();
					y = y + position.getY();
				}
				x = x / getPhysicalArea().size();
				y = y / getPhysicalArea().size();
				Double proximity = Math.hypot(agent.getPosition().getX() - x, agent.getPosition().getY() - y);
				
				if (closestProximity == null || closestProximity > proximity) {
					closestProximity = proximity;
				}
			}
		}
		
		if (closestProximity != null) {
			state.put("triggered", true);
			state.put("proximity", closestProximity);
			return true;
		} else {
			state.put("triggered", false);
			state.put("proximity", null);
			return false;
		}
	}
}
