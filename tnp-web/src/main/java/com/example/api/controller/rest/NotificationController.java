package com.example.api.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ui.model.UINotification;
import com.example.util.io.model.NotificationTriggerIO;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
	
	@PostMapping(value = "/initiate", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> initiateNotification(@RequestBody UINotification uiNotification) {

        
		System.out.println(uiNotification);
		NotificationTriggerIO notificationTriggerIO = null;
        if(uiNotification.getNotificationType().equalsIgnoreCase("PORTFOLIO")) {
        	notificationTriggerIO = new NotificationTriggerIO(NotificationTriggerIO.TriggerType.PORTFOLIO);
    		
        }else if (uiNotification.getNotificationType().equalsIgnoreCase("RESEARCH")) {
        	notificationTriggerIO = new NotificationTriggerIO(NotificationTriggerIO.TriggerType.RESEARCH);
    		
        }

		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
