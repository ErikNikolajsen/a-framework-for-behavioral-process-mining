package entities;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import geo.Position;
import main.Main;
import main.Resources;


@JsonTypeInfo(use = Id.CLASS,
include = JsonTypeInfo.As.PROPERTY,
property = "type")
public abstract class Sensor extends Entity{
	
	protected HashMap<String, Object> state = new HashMap<String, Object>();
	ObjectMapper mapper = new ObjectMapper();
	
	public Sensor(String name, ArrayList<Position> physicalArea, ArrayList<Position> interactArea, Boolean walkable) {
		super(name, physicalArea, interactArea, walkable);
	}
	
	public Sensor() {
	}
	
	public void outputSensorReading() throws MqttPersistenceException, MqttException, JsonProcessingException {
		Output output = new Output(Resources.getSimulator().getClock(),getClass().getSimpleName(),getName(),state);
		String json = mapper.writeValueAsString(output);
	    //System.out.println(json);
		
		System.out.println(Resources.getSimulator().getClock().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")).toString()+" : "+getClass().getSimpleName()+" : "+getName()+" : "+state.toString()); //human readable output for console
		if (Main.isWebsocketOutput() == true) {
			Resources.getSimulator().getNotification().notifyToClient(Resources.getSimulator().getClock().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")).toString()+" : "+getClass().getSimpleName()+" : "+getName()+" : "+state.toString());
		}
		//System.out.println("{\"time\":\""+Resources.getSimulator().getClock().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")).toString()+"\",\"type\":\""+getClass().getSimpleName()+"\",\"name\":\""+getName()+"\",\"state\":"+mapper.writeValueAsString(state)+"}"); // JSON format for MQTT output
		// MQTT output
		if (Resources.getSimulator().getMqttOutput() == true) {
			Resources.getMqtt().publish(json);
		}
		
		// CSV output
		if (Resources.getSimulator().getCsvOutput() == true) {
			Resources.getLog().writeToFile(Resources.getSimulator().getClock().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")).toString()+","+getClass().getSimpleName()+","+getName()+","+state.toString());
		}
	}
	
}
