package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Output {
	private LocalDateTime time;
	private String type;
	private String name;
	private HashMap<String, Object> state;
	
	public Output(LocalDateTime time, String type, String name, HashMap<String, Object> state) {
		this.time = time;
		this.name = name;
		this.type = type;
		this.state = state;
	}
	
	public Output() {
		
	}

	public String getTime() {
		return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn")).toString();
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public HashMap<String, Object> getState() {
		return state;
	}

	
}