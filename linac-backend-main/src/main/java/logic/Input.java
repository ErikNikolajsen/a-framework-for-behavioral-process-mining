package logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import entities.Agent;
import entities.Entity;
import entities.SensorActive;
import main.Resources;
import pathfinding.NodeState;

public class Input {
	
	private String input;
	HashMap<String, String[]> agentInstructionLists = new HashMap<String, String[]>();
	
	@JsonIgnore private final Pattern agentInstructionListPattern = Pattern.compile("\\s*agent\\((\\w+)\\)\\s*\\{([^}]*)\\}");  //pattern for enclosing each agents instructions list
	
	@JsonIgnore private final Pattern gotoPattern = Pattern.compile("\\s*goto\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)\\s*"); //gotoPattern that accepts coordinates of a tile that is within the boundaries of the specified grid and is walkable
	@JsonIgnore private final Pattern gotoEntityPattern = Pattern.compile("\\s*goto\\(\\s*(\\w+)\\s*\\)\\s*"); //gotoEntityPattern that accepts name of Entity or SensorActive
	@JsonIgnore private final Pattern interactPattern = Pattern.compile("\\s*interact\\(\\s*(\\w+),(\\w+)\\s*\\)\\s*"); //interactPattern that accepts sensorName and command
	@JsonIgnore private final Pattern waitPattern = Pattern.compile("\\s*wait\\(\\s*(\\d+)\\s*\\)\\s*"); //waitPattern that accepts integer
	@JsonIgnore private final Pattern emptyPattern = Pattern.compile("\\s*"); //empty statement and whitespace at end of input string 
	
	@JsonIgnore private final Pattern commentLinePattern = Pattern.compile("//.*");
	@JsonIgnore private final Pattern commentBlockPattern = Pattern.compile("/\\*[\\s\\S]*\\*/");
	
	@JsonIgnore private final Pattern macroDefinePattern = Pattern.compile("\\s*macro\\((\\w+)\\)\\s*\\{([^}]*)\\}"); //\s*let\s+(\w+)\b(?<!\bgoto|wait|interact)\s*\{([^}]*)\}
	//@JsonIgnore private final Pattern macroCallPattern = Pattern.compile("\\s*(\\w+)\\(\\s*\\)\\s*;");
	
	
	
	public Input(String input) {
		this.input = input;
	}
	
	public Input() {	
	}
	
	//Preprocessor
	public String test() {
	
	//Remove comments
	String processedInput = this.input;
	processedInput = commentLinePattern.matcher(processedInput).replaceAll("");
	processedInput = commentBlockPattern.matcher(processedInput).replaceAll("");
	
	
	//Represent macros internally
	HashMap<String, String> macros = new HashMap<String, String>();
	Matcher m = macroDefinePattern.matcher(processedInput);
	while (m.find()) {
	   macros.put(m.group(1), m.group(2));	   
	}
	processedInput = macroDefinePattern.matcher(processedInput).replaceAll("");

	//Expand macros
	for (HashMap.Entry<String, String> entry : macros.entrySet()) {
        processedInput = Pattern.compile("\\s*"+entry.getKey()+"\\(\\s*\\)\\s*;").matcher(processedInput).replaceAll(entry.getValue());
    }
	
	
	
	//Test input
	String[] inputArray;
	
	Matcher listMatcher = agentInstructionListPattern.matcher(processedInput);
	while (listMatcher.find()) {
		
		// Test if instructions for agent has been defined twice
		if (agentInstructionLists.get(listMatcher.group(1)) != null) {
			return "ERROR: instructions have been defined more than once for agent: "+listMatcher.group(1); // returns error-message
		}
		
		inputArray = listMatcher.group(2).split(";");
		for (int i = 0; i < inputArray.length; i++) { 
			if (gotoPattern.matcher(inputArray[i]).matches()) { 
				// parse input
				int x = Integer.parseInt(gotoPattern.matcher(inputArray[i]).replaceAll("$1"));
				int y = Integer.parseInt(gotoPattern.matcher(inputArray[i]).replaceAll("$2"));
				
				// tests if coordinate are within grid boundaries
				if (!Resources.getaStarGrid().isWithin(x, y)) {
					return "ERROR: coordinate is out of bounds in statement "+(i+1)+": "+inputArray[i]; // returns error-message
				}
				// tests if coordinate is walkable
				if (Resources.getaStarGrid().getNodeState(x, y) == NodeState.NOT_WALKABLE) {
					return "ERROR: target coordinate is not walkable in statement "+(i+1)+": "+inputArray[i]; // returns error-message
				}
				continue;
			}
			
			if (interactPattern.matcher(inputArray[i]).matches()) {
				// parse input
				String sensorNameInput = interactPattern.matcher(inputArray[i]).replaceAll("$1");
				String commandInput = interactPattern.matcher(inputArray[i]).replaceAll("$2");
				
				SensorActive sensorInput = null;
				for (SensorActive activeSensor : Resources.getFloorplan().getActiveSensors()) {
					if (activeSensor.getName().equals(sensorNameInput)) {
						sensorInput = activeSensor;
						break;
					}
				}
				
				// test if active sensor name exists in the floorplan
				if (sensorInput == null) {
					return "ERROR: No active sensor exists with the name specified in statement "+(i+1)+": "+inputArray[i]; // returns error-message
				}
				// test if command is applicable for the sensor
				if (!sensorInput.getCommands().contains(commandInput)) {
					return "ERROR: The specified type of sensor does not accept the command in statement "+(i+1)+": "+inputArray[i]; // returns error-message
				}
				continue;
			}
			
			if (waitPattern.matcher(inputArray[i]).matches()) {
				continue;
			}
			
			if (gotoEntityPattern.matcher(inputArray[i]).matches()) {
				// parse input
				String entityNameInput = gotoEntityPattern.matcher(inputArray[i]).replaceAll("$1");
				Entity entityInput = null;
				ArrayList<Entity> gotoableEntities = new ArrayList<Entity>();
				gotoableEntities.addAll(Resources.getFloorplan().getEntities());
				gotoableEntities.addAll(Resources.getFloorplan().getActiveSensors());
				for (Entity entity : gotoableEntities) {
					if (entity.getName().equals(entityNameInput)) {
						entityInput = entity;
						break;
					}
				}
				
				// test if entity exists
				if (entityInput == null) {
					return "ERROR: No entity or active-sensor exists with the name specified in statement "+(i+1)+": "+inputArray[i]; // returns error-message
				}
				
				continue;
			}
			
			if (emptyPattern.matcher(inputArray[i]).matches()) {
				continue;
			}
			
			return "ERROR: syntax error in statement "+(i+1)+": "+inputArray[i]; // returns error-message
			
		}
		
		agentInstructionLists.put(listMatcher.group(1), inputArray);	   
		
	}
	
	/*
	// TEST!!! See input
	for (String name: agentInstructionLists.keySet()) {
	    String key = name.toString();
	    String value = Arrays.toString(agentInstructionLists.get(name));
	    System.out.println("key: " + key + "\n value: " + value);
	}
	*/
	
	//test all agents in the floormap have an agent instruction list assigned to them
	for (Agent agent : Resources.getFloorplan().getAgents()) {
		if (!agentInstructionLists.keySet().contains(agent.getId())) {
			return "ERROR: no agent instructions have been defined for agent: "+agent.getId(); // returns error-message
		}
	}
	
	//test all agent instruction lists corresponds to a defined agent in the floorplan
	ArrayList<String> agentIds = new ArrayList<String>();
	for (Agent agent : Resources.getFloorplan().getAgents()) {
		agentIds.add(agent.getId());
	}
	for (String key : agentInstructionLists.keySet()) {
		if (!agentIds.contains(key)) {
			return "ERROR: no definition has been made in the floormap for agent: "+key; // returns error-message
		}
	}
	
	return "consumed";
	}
	
	//Accessors and Mutators
	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public HashMap<String, String[]> getAgentInstructionLists() {
		return agentInstructionLists;
	}

	public Pattern getGotopattern() {
		return gotoPattern;
	}

	public Pattern getInteractpattern() {
		return interactPattern;
	}

	public Pattern getWaitpattern() {
		return waitPattern;
	}

	public Pattern getGotoentitypattern() {
		return gotoEntityPattern;
	}
	
	
	
}
