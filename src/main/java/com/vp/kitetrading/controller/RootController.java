package com.vp.kitetrading.controller;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vp.kitetrading.services.Kite;
import com.vp.kitetrading.services.KiteAPITicker;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.CombinedMarginData;
import com.zerodhatech.models.MarginCalculationParams;
import com.zerodhatech.models.Quote;
import com.zerodhatech.models.User;

@Controller
public class RootController {

	@Autowired
	private Kite kite;

	@Autowired
	private KiteAPITicker kiteTicker;
	
	@Value("${api_key}") 
	private String apiKey;

	Logger logger = LoggerFactory.getLogger(RootController.class);
	

	@RequestMapping(method = RequestMethod.GET, value = "/")
	public ResponseEntity<Void> root() {
		String url = kite.getLoginUrl();
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/login")
	public ResponseEntity<Void> login() {
		String url = kite.getLoginUrl();
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/redirectLogin")
	public ResponseEntity<String> redirect_login(@RequestParam("request_token") String request_token) {
		try {
			User user = kite.generateSession(request_token);
			KiteConnect kiteConnect = kite.getKiteSdk();
			JSONArray inst = kiteTicker.getAllInstruments(kiteConnect);
			return new ResponseEntity<>("<h1>Logged in as " + user.userName+"</h1>",HttpStatus.OK);
	
		} catch (IOException | KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getMargins", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String getMargins(HttpServletResponse httpResponse) {
		try {
			JSONObject margin = kite.getMargins();
			if (margin == null) {
				httpResponse.sendRedirect("/");
				return null;
			}
			return margin.toString();
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}

	}

	@RequestMapping(method = RequestMethod.GET, value = "/getInstruments", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getInstruments(HttpServletResponse httpResponse) {
		try {
			KiteConnect kiteConnect = kite.getKiteSdk();
			if (kiteConnect == null) {
				httpResponse.sendRedirect("/");
				return null;
			}
			JSONArray inst = kiteTicker.getAllInstruments(kiteConnect);
			return new ResponseEntity<>(inst.toString(),HttpStatus.OK);

		} catch (JSONException | IOException | KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(method = RequestMethod.GET, value = "/order")
	public String order(Model model) {
		try {
			KiteConnect kiteConnect = kite.getKiteSdk();
			JSONObject nifty50 = kiteTicker.getNifty50();
			if (kiteConnect == null || nifty50 == null) {
				return "redirect:/";
			}

			String[] niftyIns = { nifty50.get("instrument_token").toString() };
			Map<String, Quote> quotes = kiteConnect.getQuote(niftyIns);
			double niftyLTP = quotes.get(nifty50.get("instrument_token").toString()).lastPrice;
			System.out.println("Nifty Last traded price - " + niftyLTP);
			int atm;
			int d = ((int) niftyLTP) % 100;
			int r = d;
			if (r > 26 && r < 75)
				atm = (int) niftyLTP - ((int) niftyLTP) % 100 + 50;
			else if (r > 75)
				atm = ((int) niftyLTP) % 100 + 100;
			else
				atm = (int) niftyLTP - ((int) niftyLTP) % 100;
			System.out.println("At the money strike - " + atm);

			JSONArray straddleList = prepareStraddle(kiteConnect, atm);
			JSONArray order = placeStraddleOrder(kiteConnect,straddleList);
			model.addAttribute("api_key",apiKey);
			model.addAttribute("data",order.toString());
			return "publisher";
//			return html(body(form(input().withType("hidden").withName("api_key").withValue(apiKey),
//					input().withType("hidden").withId("basket").withName("data").withValue(order.toString())
//
//					).withMethod("post").withId("basket-form").withAction("https://kite.zerodha.com/connect/basket"),
//							script(rawHtml(
//									" document.getElementById(\"basket\").value = your_basket;document.getElementById(\"basket-form\").submit();"))))
//											.render();

		} catch (JSONException | IOException | KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getStackTrace().toString();
		}
	}
	
	public JSONArray placeStraddleOrder(KiteConnect kiteConnect,JSONArray straddle) {
		JSONArray data = new JSONArray();
		for(int i=straddle.length()-1;i>=0;i--) {
			JSONObject ins = straddle.getJSONObject(i);
			JSONObject order= new JSONObject();
			
			order.put("quantiy", 250);
			order.put("transaction_type", ins.getString("order_type"));
			order.put("order_type", Constants.ORDER_TYPE_MARKET);
			order.put("tradingsymbol", ins.getString("tradingsymbol"));
			order.put("variety", Constants.VARIETY_REGULAR);
			order.put("product", Constants.PRODUCT_MIS);
			order.put("exchange",  Constants.EXCHANGE_NFO);
			data.put(order);

		}
		return data;
		
	}

	public JSONArray prepareStraddle(KiteConnect kiteConnect, int atmStrike) {

		JSONArray straddle = new JSONArray();
		try {
			JSONArray instruments = getNextExpiryInstruments();
			int spotIndex = 0;
			// Get at the money strikes
			for (int i = 0; i < instruments.length() && straddle.length() != 2; i++) {
				JSONObject ins = instruments.getJSONObject(i);
				String strike = ins.getString("strike");
				String type = ins.getString("instrument_type");
				if (strike.equals(Integer.toString(atmStrike)) && type.equals("CE")) {
					ins.put("order_type", "SELL");
					straddle.put(ins);
					spotIndex = i;
				}
				if (strike.equals(Integer.toString(atmStrike)) && type.equals("PE")) {
					straddle.put(ins);
					ins.put("order_type", "SELL");
				}

			}

			// Prepare hedges
			JSONObject margins = kite.getMargins();
			int requiredMargins = 999999999;
			double available = margins.getDouble("total");
			int counter = 30;
			int startIndex = 0;
			int endIndex = instruments.length() - 1;

			while (requiredMargins > available && startIndex < endIndex) {
				startIndex = spotIndex - counter;
				endIndex = spotIndex + counter + 1;
				JSONArray temp = new JSONArray(straddle.toString());
				counter--;
				if (startIndex < 0 || endIndex >= instruments.length())
					continue;
				JSONObject peHedge = instruments.getJSONObject(startIndex);
				JSONObject ceHedge = instruments.getJSONObject(endIndex);
				int strike1 = Integer.parseInt(peHedge.getString("strike"));
				int strike2 = Integer.parseInt(ceHedge.getString("strike"));
				String type1 = peHedge.getString("instrument_type");
				String type2 = ceHedge.getString("instrument_type");
				
				if ((strike1 < atmStrike && type1.equals("CE")) || (strike2 > atmStrike && type2.equals("PE")) || (strike1==strike2)) {
					continue;
				}
				
				peHedge.put("order_type", "BUY");
				ceHedge.put("order_type", "BUY");
				temp.put(peHedge);
				temp.put(ceHedge);
				double margin = getCombinedMarginCalculation(kiteConnect, temp);
				if (margin < available) {
					straddle.put(peHedge);
					straddle.put(ceHedge);
					break;
				}
			}

		} catch (JSONException | ParseException | KiteException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return straddle;
	}

	public double getCombinedMarginCalculation(KiteConnect kiteConnect, JSONArray instruments)
			throws IOException, KiteException {
		List<MarginCalculationParams> params = new ArrayList();

		for (int i = instruments.length() - 1; i >= 0; i--) {
			JSONObject ins = instruments.getJSONObject(i);
			MarginCalculationParams param = new MarginCalculationParams();
			param.exchange = "NFO";
			param.tradingSymbol = ins.getString("tradingsymbol");
			param.orderType = "MARKET";
			param.quantity = 250;
			param.product = "MIS";
			param.variety = "regular";
			param.transactionType = ins.getString("order_type");

//			System.out.println(ins.getString("tradingsymbol") + " - " + ins.getString("order_type"));
			params.add(param);
		}

		CombinedMarginData combinedMarginData = kiteConnect.getCombinedMarginCalculation(params, true, false);
		System.out.println("Total margin req - " + combinedMarginData.initialMargin.total);
		return combinedMarginData.initialMargin.total;
	}

	public JSONArray getNextExpiryInstruments() throws JSONException, ParseException {
		JSONArray instruments = kiteTicker.getInstruments();
		JSONArray filtered = new JSONArray();
		String expiry = kiteTicker.getNextExpiry();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getTimeZone("IST"));
		Date date1 = sdf.parse(expiry);
		for (int i = 0; i < instruments.length(); ++i) {
			JSONObject obj = instruments.getJSONObject(i);
			if (!obj.has("expiry"))
				continue;
			String expiryDate = obj.getString("expiry");
			if (expiryDate.equals(date1.toString())) {
				filtered.put(obj);
			}
		}

		return filtered;

	}

}
