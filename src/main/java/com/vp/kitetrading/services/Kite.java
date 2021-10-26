package com.vp.kitetrading.services;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Margin;
import com.zerodhatech.models.User;

import util.GenericUtils;

@Service
public class Kite {
	
	private String apiKey; 
	
	private String apiSecret; 

	private String userId; 
	
	private KiteConnect kiteSdk;
	
	public KiteConnect getKiteSdk() {
		return kiteSdk;
	}

	private User user;
	
	@Autowired
	public Kite(@Value("${api_key}") String apiKey,@Value("${api_secret}") String apiSecret,@Value("${user_id}") String userId){
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
		kiteSdk.setAccessToken(user.accessToken);
		kiteSdk.setPublicToken(user.publicToken);

		return user;
	}
	
	public User getUser() {
		return user;
	}
	
	public JSONObject getMargins() {
		try {
		Margin margins = kiteSdk.getMargins("equity");
		Double cash= Double.parseDouble(margins.available.cash);
		Double collateral= Double.parseDouble(margins.available.collateral);
		JSONObject margin = new JSONObject();
		margin.put("cash", cash);
		margin.put("collateral", collateral );
		margin.put("total",cash+collateral);
		return margin;
		} catch (JSONException | IOException | KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
		
	}
	
	
}
