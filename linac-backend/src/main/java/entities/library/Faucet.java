package entities.library;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import entities.SensorActive;
import main.Resources;

public class Faucet extends SensorActive {
	
	private LocalDateTime lastTurnOnDateTime;
	private double flowRate = 0.06; // Liters per second
	
	public Faucet() throws MqttPersistenceException, MqttException {
	}

	public ArrayList<String> defineCommands() {
		return new ArrayList<String>(Arrays.asList(
				"TURN_ON",
				"TURN_OFF"
		));
	}
	
	public void defineDefaultState() {
		state.put("water_output", "OFF");
		state.put("consumed_water", 0.0); // in Liters
	}
	
	// Sensor behavior
	public void updateState(String command) throws MqttPersistenceException, MqttException {
		// Set power status
		if (command.equals("TURN_ON") && state.get("water_output").equals("OFF")) {
			state.put("water_output", "ON");
			lastTurnOnDateTime = Resources.getSimulator().getClock();
		} else if (command.equals("TURN_OFF") && state.get("water_output").equals("ON")) {
			state.put("water_output", "OFF");
			Duration duration = Duration.between(lastTurnOnDateTime, Resources.getSimulator().getClock());
			Double consumedWater = duration.toSeconds() * flowRate;
			state.put("consumed_water", (Double) state.get("consumed_water") + consumedWater);
		}
	}
}
