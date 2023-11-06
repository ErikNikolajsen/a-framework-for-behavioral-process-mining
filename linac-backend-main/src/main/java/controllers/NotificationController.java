package controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@ComponentScan({"config"})
@Controller
public class NotificationController {

    @Autowired
	private SimpMessagingTemplate template;
	
	public void notifyToClient(String notification) {
		template.convertAndSend("/SimulationStatus", notification);
	}
}