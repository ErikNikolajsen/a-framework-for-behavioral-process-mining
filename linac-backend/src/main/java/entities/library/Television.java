package entities.library;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import entities.SensorActive;

public class Television extends SensorActive {
	
	public Television() throws MqttPersistenceException, MqttException {
	}

	public ArrayList<String> defineCommands() {
		return new ArrayList<String>(Arrays.asList(
				"ON",
				"OFF",
				"CHANNEL1",
				"CHANNEL2",
				"CHANNEL3",
				"CHANNEL4",
				"VOLUME_DOWN",
				"VOLUME_UP"
		));
	}
	
	public void defineDefaultState() {
		state.put("power", "OFF");
		state.put("channel", "CHANNEL1");
		state.put("volume", 5);
	}
	
	// Sensor behavior
	public void updateState(String command) throws MqttPersistenceException, MqttException {
		// Set power status
		if (command.equals("ON") || command.equals("OFF")) {
			state.put("power", command);
		// Set channel
		} else if ((command.equals("CHANNEL1") || command.equals("CHANNEL2") || command.equals("CHANNEL3") || command.equals("CHANNEL4")) && state.get("power").equals("ON")) {
			state.put("channel", command);
		// Turn volume down
		} else if (command.equals("VOLUME_DOWN") && state.get("power").equals("ON") && (Integer) state.get("volume")>0) {
			state.put("volume",(Integer) state.get("volume") - 1);
		// Turn volume up
		} else if (command.equals("VOLUME_UP") && state.get("power").equals("ON") && (Integer) state.get("volume")<10) {
			state.put("volume",(Integer) state.get("volume") + 1);
		}
	}
}
