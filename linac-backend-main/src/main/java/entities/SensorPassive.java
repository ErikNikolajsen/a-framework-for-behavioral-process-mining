package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

import geo.Position;

public abstract class SensorPassive extends Sensor {
	
	@JsonIgnore private LocalDateTime lastTriggerTime;
	private long triggerFrequency;

	public SensorPassive(String name, ArrayList<Position> physicalArea, ArrayList<Position> interactArea, Boolean walkable, long triggerFrequency) {
		super(name, physicalArea, interactArea, walkable);
		this.triggerFrequency = triggerFrequency;
	}

	public SensorPassive() {
		defineDefaultState();
	}
	
	// Abstract methods that must be implemented by passive sensors
	public abstract boolean updateState();
	
	public abstract void defineDefaultState();
	
	//Accessors and Mutators
	public LocalDateTime getLastTriggerTime() {
		return lastTriggerTime;
	}

	public void setLastTriggerTime(LocalDateTime lastTriggerTime) {
		this.lastTriggerTime = lastTriggerTime;
	}

	public long getTriggerFrequency() {
		return triggerFrequency;
	}

	public void setTriggerFrequency(long triggerFrequency) {
		this.triggerFrequency = triggerFrequency;
	};
}
