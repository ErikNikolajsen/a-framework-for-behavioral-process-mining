package entities.library;

//import java.time.Duration;
//import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import entities.SensorActive;
//import main.Resources;

public class EntitySensor extends SensorActive {
	
	public EntitySensor() throws MqttPersistenceException, MqttException {
	}

	public ArrayList<String> defineCommands() {
		return new ArrayList<String>(Arrays.asList(
				"activate"
		));
	}
	
	public void defineDefaultState() {
		state.put("activated", "true"); 
	}
	
	// Sensor behavior
	public void updateState(String command) throws MqttPersistenceException, MqttException {
		
	}
}
