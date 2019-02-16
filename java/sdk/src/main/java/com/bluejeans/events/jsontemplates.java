
package com.bluejeans.events;

import org.json.*;


// class jsontemplates
//    Utility class to encapsulate calls that create JSON strings associated with
// Event and Meeting BlueJeans API calls
//
public class jsontemplates
{
	private String accessToken = "";

	
	// jsontemplates constructor
	//   We require an *MEETING ACCESS* token when connecting to the Event Websocket
	// Create this utility bound to a meeting access token.
	//
	public jsontemplates(String at)
	{
		this.accessToken = at;
	}
	
	
	// wsRegisterObject()
	//   Method to create JSON payload for returned upon establishing the
	// websocked connection
	//
	public String wsRegisterObject(String meetingID, String userId)
	{
		String userName = "";
	
		String template = "[\"[\\\"meeting.register\\\","
					+ "{\\\"numeric_id\\\":\\\"" + meetingID + "\\\","
					+ "\\\"access_token\\\":\\\"" + this.accessToken	+ "\\\","
					+ "\\\"user\\\":{"
					+     "\\\"id\\\":\\\"" + userId + "\\\","
					+     "\\\"full_name\\\":\\\"" + userName + "\\\","
					+     "\\\"is_leader\\\":true"
					+     "},"
					+ "\\\"leader_id\\\":" + userId	+","
					+ "\\\"protocol\\\":\\\"2\\\","
					+ "\\\"events\\\":["
					+     "\\\"meeting\\\",\\\"endpoint\\\""
					+     "]"
					+ "}"
					+ "]\"]";
	
		return template;
	}
	
	// wsHeartBeat()
	//   In response to a keep-alive message, the Event client needs to respond
	// with this heartbeat JSON payload
	//
	public String wsHeartBeat()
	{
		String template = "[\"[\\\"heartbeat\\\",{}]\"]";
		return template;
	}
	
	public String toString(String tstr)
	{
		return tstr;
		//JSONObject jobj = new JSONObject(tstr);
		//return jobj.toString(4);
	}
	
	public void test( String mtgId, String uid )
	{
		JSONArray mreg = new JSONArray( this.wsRegisterObject(mtgId,uid) );
		System.out.println( mreg.toString() );
	}
}