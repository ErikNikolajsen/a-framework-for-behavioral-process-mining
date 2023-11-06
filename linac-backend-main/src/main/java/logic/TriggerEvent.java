package logic;

import java.time.LocalDateTime;

import entities.Sensor;

public class TriggerEvent {
	Sensor sensor;
	LocalDateTime dateTime;
	
	public TriggerEvent(Sensor sensor, LocalDateTime dateTime) {
		this.sensor = sensor;
		this.dateTime = dateTime;
	}

	public Sensor getSensor() {
		return sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
	
	
}
