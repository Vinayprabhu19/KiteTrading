package com.vp.kitetrading.controller;

import java.io.IOException;
import java.net.URI;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vp.kitetrading.config.Kite;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;

@RestController
public class RootController {
	
	
	
	@RequestMapping(method = RequestMethod.GET,value = "/")
	public String root() {
		return "App is up and running";
	}
	
	@Autowired
	private Kite kite;
	

	@RequestMapping(method = RequestMethod.GET,value = "/login")
	 ResponseEntity<Void> login() {
		String url = kite.getLoginUrl();
		    return ResponseEntity.status(HttpStatus.FOUND)
		        .location(URI.create(url))
		        .build();
		  }
	
	@RequestMapping(method = RequestMethod.GET,value = "/redirectLogin")
	public String redirect_login(@RequestParam("request_token") String request_token) {
		System.out.println("Redirect login "+request_token);
		try {
			User user = kite.generateSession(request_token);
			return user.userName;
		} catch (IOException  | KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	
}
