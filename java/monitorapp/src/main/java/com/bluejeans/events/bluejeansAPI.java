package com.bluejeans.events;

import java.net.URL;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// class bluejeansAPI
//   Utility module that handles general API calls to the BlueJeans cloud
//
//
public class bluejeansAPI{
	
	private String apiHost = "https://api.bluejeans.com";
	
	void bluejeansAPI(){
	}
	
	private Logger LOGGER = LoggerFactory.getLogger("bluejeansAPI");

	public String sendPostRequest(String requestUrl, String payload) {
		try {
			URL url = new URL(apiHost+requestUrl);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
			writer.write(payload);
			writer.close();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuffer jsonString = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
					jsonString.append(line);
			}
			br.close();
			connection.disconnect();
			return jsonString.toString();
		} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
		}
	}

	public String sendGetRequest(String requestUrl) {
		try {
			URL url = new URL(apiHost+requestUrl);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuffer jsonString = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
					jsonString.append(line);
			}
			br.close();
			connection.disconnect();
			return jsonString.toString();
		} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
		}
	}	
	
	
	// getMeetingAccessToken()
	//    Routine to call BlueJeans OAuth API for generating a Meeting Access Token
	// Refer to the BlueJeans Developer Site for the required parametric data
	//
	public JSONObject getMeetingAccessToken(String meetingId, String accessCode)
	{
		LOGGER.info("Requesting BlueJeans Meeting Access Token: " + meetingId);
		String url = "";
		String atoken = "";
		String postData = "{\"grant_type\":\"meeting_passcode\","
						 + "\"meetingNumericId\":\"" + meetingId + "\","
						 + "\"meetingPasscode\" :\""  + accessCode + "\"}";
		String results = sendPostRequest("/oauth2/token?Password",postData);	
		JSONObject json = null;
		JSONObject meetingAccessInfo = new JSONObject();
		try
		{
			json = new JSONObject(results);
		}
		catch( Exception e) {
			LOGGER.error("Exception parsing API results:" + results);
		}
		
		LOGGER.debug("Results is: " + json.toString(2) );
		
		try
		{
			// Filter down the API results to return just what is needed
			// access token, meeting ID, Leaders ID, and which partition is hosting
			// the meeting
			meetingAccessInfo.put("access_token",json.get("access_token") );
			JSONObject scope   = json.getJSONObject("scope");
			JSONObject meeting = scope.getJSONObject("meeting");
			meetingAccessInfo.put("id", Integer.toString( meeting.getInt("id") ));
			meetingAccessInfo.put("leaderId", Integer.toString( meeting.getInt("leaderId") ));
			meetingAccessInfo.put("partition", scope.getString("partitionName"));
			
		}
		catch( Exception e)
		{
			LOGGER.error("Exception creating meetingAccessInfo: " + e);
		}
		return meetingAccessInfo;
	}

	
	// ...overload
	public JSONObject getMeetingAccessToken(String meetingId)
	{
		return getMeetingAccessToken(meetingId,"");
	}
}
