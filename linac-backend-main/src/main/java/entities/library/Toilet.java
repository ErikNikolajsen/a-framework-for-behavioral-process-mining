package entities.library;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import entities.SensorActive;

public class Toilet extends SensorActive {
	
	public Toilet() throws MqttPersistenceException, MqttException {
	}

	public ArrayList<String> defineCommands() {
		return new ArrayList<String>(Arrays.asList(
				"FLUSH"
		));
	}
	
	public void defineDefaultState() {
		state.put("flush_amount", 0);
		state.put("consumed_water", 0.0); // in Liters
	}
	
	// Sensor behavior
	public void updateState(String command) throws MqttPersistenceException, MqttException {
		// Set power status
		if (command.equals("FLUSH")) {
			state.put("flush_amount", (Integer) state.get("flush_amount") + 1);
			state.put("consumed_water", (Double) state.get("consumed_water") + 3.3);
		}
	}
}
