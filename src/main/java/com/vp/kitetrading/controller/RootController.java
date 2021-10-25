package com.vp.kitetrading.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
	
	@Value("${api_key}")
	private String apiKey; 
	
	@RequestMapping(method = RequestMethod.GET,value = "/")
	public String root() {
		return "App is up and running";
	}
	
	@RequestMapping(method = RequestMethod.GET,value = "/key")
	public String key() {
		return apiKey;
	}

}
