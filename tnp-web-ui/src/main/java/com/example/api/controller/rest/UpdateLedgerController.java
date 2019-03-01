package com.example.api.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.ui.model.UIUpdateLedger;
import com.example.util.io.model.UpdateTriggerIO.TriggerType;
import com.example.util.io.model.UpdateTriggerIO;

@RestController
@RequestMapping("/api/updateledger")
public class UpdateLedgerController {

	@Autowired
	private QueueService queueService;
	
	
	@PostMapping(value = "/initiate", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> initiateDownload(@RequestBody UIUpdateLedger uiUpdateLedger) {

		TriggerType triggerType= null;
        
        if(uiUpdateLedger.getUpdateledgerType().equalsIgnoreCase("UPDATE_CYRO")) {
        	triggerType = TriggerType.UPDATE_CYRO;
        }else if (uiUpdateLedger.getUpdateledgerType().equalsIgnoreCase("UPDATE_FYRO")) {
        	triggerType = TriggerType.UPDATE_FYRO;
        }else if (uiUpdateLedger.getUpdateledgerType().equalsIgnoreCase("UPDATE_MONTHLY_VALUE")) {
        	triggerType = TriggerType.UPDATE_MONTHLY_VALUE;
        }else if (uiUpdateLedger.getUpdateledgerType().equalsIgnoreCase("UPDATE_RESEARCH")) {
        	triggerType = TriggerType.UPDATE_RESEARCH;
        }else if (uiUpdateLedger.getUpdateledgerType().equalsIgnoreCase("RESET_MASTER")) {
        	triggerType = TriggerType.RESET_MASTER;
        }

        UpdateTriggerIO updateTriggerIO = new UpdateTriggerIO(triggerType);
        
        queueService.send(updateTriggerIO, QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE);
        
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
