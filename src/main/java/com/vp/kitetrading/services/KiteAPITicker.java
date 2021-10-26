package com.vp.kitetrading.services;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnDisconnect;
import com.zerodhatech.ticker.OnOrderUpdate;
import com.zerodhatech.ticker.OnTicks;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.temporal.TemporalAdjusters.next;
@Service
public class KiteAPITicker {

	@Autowired
	private Kite kite;

	private JSONArray instruments;

	private JSONObject nifty50;

	/** Get all instruments that can be traded using kite connect. */
	public JSONArray getAllInstruments(KiteConnect kiteConnect) throws KiteException, IOException {
		List<Instrument> instrumentLists = kiteConnect.getInstruments();

		JSONArray instruments = new JSONArray(instrumentLists);
		JSONArray filtered = new JSONArray();
		for (int i = 0; i < instruments.length(); ++i) {
			JSONObject obj = instruments.getJSONObject(i);
			String ts = obj.getString("tradingsymbol");
			if (!obj.has("name"))
				continue;
			String name = obj.getString("name");
			if (name.equals("NIFTY")) {
				filtered.put(obj);
			}
			if (name.equals("NIFTY 50")) {
				this.nifty50 = obj;
				filtered.put(obj);
			}
		}
		this.instruments = filtered;
		return filtered;
	}

	public String createTicker(ArrayList<Long> tokens) {
		KiteConnect kiteConnect = kite.getKiteSdk();
		if (kiteConnect == null) {
			return "please login";
		}
		try {
			KiteTicker tickerProvider = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());

			tickerProvider.setOnConnectedListener(new OnConnect() {

				@Override
				public void onConnected() {
					// TODO Auto-generated method stub

				}
			});

			tickerProvider.setOnDisconnectedListener(new OnDisconnect() {

				@Override
				public void onDisconnected() {
					// TODO Auto-generated method stub

				}
			});

			/** Set listener to get order updates. */
			tickerProvider.setOnOrderUpdateListener(new OnOrderUpdate() {

				@Override
				public void onOrderUpdate(Order order) {
					// TODO Auto-generated method stub
					System.out.println("order update " + order.orderId);

				}

			});

			tickerProvider.setOnTickerArrivalListener(new OnTicks() {

				@Override
				public void onTicks(ArrayList<Tick> ticks) {
					NumberFormat formatter = new DecimalFormat();
					System.out.println("ticks size " + ticks.size());
					if (ticks.size() > 0) {
						System.out.println("last price " + ticks.get(0).getLastTradedPrice());
						System.out.println("open interest " + formatter.format(ticks.get(0).getOi()));
						System.out.println("day high OI " + formatter.format(ticks.get(0).getOpenInterestDayHigh()));
						System.out.println("day low OI " + formatter.format(ticks.get(0).getOpenInterestDayLow()));
						System.out.println("change " + formatter.format(ticks.get(0).getChange()));
						System.out.println("tick timestamp " + ticks.get(0).getTickTimestamp());
						System.out.println("tick timestamp date " + ticks.get(0).getTickTimestamp());
						System.out.println("last traded time " + ticks.get(0).getLastTradedTime());
						System.out.println(ticks.get(0).getMarketDepth().get("buy").size());
					}

				}
			});

			tickerProvider.setTryReconnection(true);
			// maximum retries and should be greater than 0
			tickerProvider.setMaximumRetries(10);
			// set maximum retry interval in seconds
			tickerProvider.setMaximumRetryInterval(30);

			/**
			 * connects to com.zerodhatech.com.zerodhatech.ticker server for getting live
			 * quotes
			 */
			tickerProvider.connect();

			/**
			 * You can check, if websocket connection is open or not using the following
			 * method.
			 */
			boolean isConnected = tickerProvider.isConnectionOpen();
			System.out.println(isConnected);
			return "Connected to ticker";
		} catch (NullPointerException | KiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public String getToday() {
		return LocalDate.now().toString();
	}

	public String getNextExpiry() {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		
		String[] holidays = getHolidays();
		String nextExpiry = "";
		
		String nextThursday = LocalDate.now().with( next( THURSDAY) ).toString();
		String nextWednesday = LocalDate.now().with( next( WEDNESDAY) ).toString();
		for(int i=0;i<holidays.length;i++) {
			if(nextThursday==holidays[i])
				return nextWednesday;
		}
		return nextThursday;
	}
	public String[] getHolidays() {

		String[] arr = { "2021-11-04", "2021-11-05",
				"2021-11-19" };
		return arr;
	}

	public JSONArray getInstruments() {
		return instruments;
	}



	public JSONObject getNifty50() {
		return nifty50;
	}

	
}
