package com.vp.kitetrading.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
	
	@RequestMapping(method = RequestMethod.GET,value = "/")
	public String root() {
		return "App is up and running";
	}

}
