package com.example.util;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String from;
	
	@Value("${spring.mail.displayname}")
	private String displayName;
	
	public void sendEmail(String emailTo, String emailSubject,String emailBody) throws Exception {

		MimeMessage message = mailSender.createMimeMessage();

		message.setFrom(new InternetAddress(from, displayName));

		message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(emailTo));

		message.setSubject(emailSubject);

		// Send the actual HTML message, as big as you like
		message.setContent(emailBody,"text/html");

		mailSender.send(message);
	}
	
	
	

}
