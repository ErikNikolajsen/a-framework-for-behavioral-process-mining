package main;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPaho {
	
	private MqttClient client;
	private String rootTopic;
	private int qualityOfService;
	
	public MqttPaho(String host, String port, String rootTopic, int qualityOfService) throws MqttException {
		connect(host, port);
		this.rootTopic = rootTopic;
		this.qualityOfService = qualityOfService;
	}
	
	public void connect(String host, String port) throws MqttException {
		client = new MqttClient( 
				"tcp://"+host+":"+port, //URI //"tcp://broker.hivemq.com:1883" 
			    MqttClient.generateClientId(), //ClientId 
			    new MemoryPersistence()); //Persistence
		
		client.setCallback(new MqttCallback() {
		    @Override
		    public void connectionLost(Throwable cause) { //Called when the client lost the connection to the broker 
		    }

		    @Override
		    public void messageArrived(String topic, MqttMessage message) throws Exception {
		        System.out.println(topic + ": " + new String(message.getPayload()));
		    }

		    @Override
		    public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete 
		    }
		});
		
		MqttConnectOptions options = new MqttConnectOptions();
		options.setAutomaticReconnect(true);
		options.setCleanSession(true); // Whether the client and server should remember state for the client across reconnects (default is true)
		options.setConnectionTimeout(0); // This value, measured in seconds, defines the maximum time interval the client will wait for the network connection to the MQTT server to be established (default is 30 seconds). A value of 0 has the effect of turning off the mechanism.
		options.setKeepAliveInterval(0); // This value, measured in seconds, defines the maximum time interval between messages sent or received (default is 60 seconds). A value of 0 has the effect of turning off the mechanism.
		options.setAutomaticReconnect(true);
		
		client.connect(options);
	}
	
	public boolean isConnected() {
		return client.isConnected();
	}
	
	public void subscribe(String topic) throws MqttException {
		client.subscribe(topic, 2);
	}
	
	
	public void publish(String payload) throws MqttPersistenceException, MqttException {
		client.publish( 
			    rootTopic, // topic 
			    payload.getBytes(), // payload 
			    qualityOfService, // QoS 
			    false); // retained
	}
	
	public void clearRetainedMessage(String topic) throws MqttPersistenceException, MqttException {
		client.publish( 
			    topic, // topic 
			    "".getBytes(), // payload 
			    qualityOfService, // QoS 
			    true); // retained
	}
	
	public void disconnectMqtt() throws MqttException {
		client.disconnect();
	}
}
