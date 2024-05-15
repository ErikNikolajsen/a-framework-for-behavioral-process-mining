package controllers;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import logic.Input;
import logic.Simulator;
import main.Resources;

@RestController
@RequestMapping("/api/simulation/")
@CrossOrigin
public class SimulationController {
	
	@Autowired
	private NotificationController notificationController;

	@PostMapping("/input") // requires the prior instantiation of a Floorplan object
	public String postInput(@RequestBody String inputString) {
		// Test for prior instantiation of a Floorplan object
		if (Resources.getFloorplan() == null) {
			System.out.println("ERROR: no floorplan has been instantiated");
			return "ERROR: no floorplan has been instantiated";
		}
		
		// Instantiate a Input object
		Input input = new Input();
		input.setInput(inputString);
		
		// Test agent instructions input
		String testResult = input.test();
		if (testResult.equals("consumed")) {
			Resources.setInput(input);
		} else {
			System.out.println(testResult);
		}
		return testResult;
	}
	
	@GetMapping("/Input")
	public @ResponseBody String getInput() {
		return Resources.getInput().getInput(); 
	}
	
	@PostMapping("/simulator")
	public String postSimulator(@RequestBody Simulator simulator) throws MqttPersistenceException, InterruptedException, MqttException, JsonProcessingException {
		// Test for prior instantiation of a Input object
		if (Resources.getInput() == null) {
			System.out.println("ERROR: no instructions input has been instantiated");
			return "ERROR: no instructions input has been instantiated";
		}
		
		// Test simulator input
		String testResult = simulator.test();
		if (testResult.equals("consumed")) {
			Resources.setSimulator(simulator);
			Resources.getSimulator().setNotification(notificationController);
			Resources.getSimulator().startSimulator();
		} else {
			System.out.println(testResult);
		}
		return testResult;
		
	}
	
	@GetMapping("/simulator")
	public @ResponseBody Simulator getSimulator() {
		return Resources.getSimulator(); 
	}
    
}



















/*

// test whole input at once
if (inputString.matches("((\\s*goto\\(\\d+,\\d+\\)\\s*;\\s*)|(\\s*interact\\(\\w+\\)\\s*;\\s*)|(\\s*wait\\(\\d+\\)\\s*;\\s*))+")) { 
	return "consumed"; //Input is correct
} else {
	return "error"; //Input contains error
}

// test input per statement and return statement element with error
String[] inputArray = inputString.split(";"); // inserts statements into an array
for (int i = 0; i < inputArray.length; i++) { 
	if (inputArray[i].matches("(\\s*goto\\(\\s*\\d+\\s*,\\s*\\d+\\s*\\)\\s*)|(\\s*interact\\(\\s*\\w+\\s*\\)\\s*)|(\\s*wait\\(\\s*\\d+\\s*\\)\\s*)")) { 
		inputArray[i] = inputArray[i].replaceAll("\\s",""); // removes whitespace and adds statement to inputArray
	} else {
		return "ERROR: syntax error in statement "+(i+1)+": "+inputArray[i]; // returns error-message
	}
}
return "consumed";
*/