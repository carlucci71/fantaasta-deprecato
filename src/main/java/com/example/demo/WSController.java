package com.example.demo;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WSController {

    @MessageMapping("/hello")
    @SendTo("/topic/messaggio")
	public String ricevi(@RequestParam("testo") String nome) {
		return nome;
	}
	
}
