package com.vp.kitetrading.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;

import util.GenericUtils;

@Service
public class Kite {
	
	private String apiKey; 
	
	private String apiSecret; 

	private String userId; 
	
	private KiteConnect kiteSdk;
	
	private User user;
	
	@Autowired
	public Kite(@Value("${api_key}") String apiKey,@Value("${api_secret}") String apiSecret,@Value("${user_id}") String userId){
		System.out.println(apiKey);
		System.out.println(apiSecret);
		System.out.println(userId);
		this.apiKey=apiKey;
		this.apiSecret=apiSecret;
		this.userId=userId;
		kiteSdk= new KiteConnect(apiKey);
	}
	
	
	public String getLoginUrl() {
		kiteSdk.setUserId(this.userId);
		String url = kiteSdk.getLoginURL();
		return url;
	}
	
	public  User generateSession(String requestToken) throws IOException,KiteException {
		String api_text = apiKey+requestToken+apiSecret;
		String hash = GenericUtils.sha256(api_text);
		user = kiteSdk.generateSession(requestToken, apiSecret);
		return user;
	}
	
	public User getUser() {
		return user;
	}
}
