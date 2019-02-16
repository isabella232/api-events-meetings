package com.bluejeans.events;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

interface eventInterface {
	
	// This function is called whenever the Event SDK receives an event
	// from BlueJEans
	public void onMeetingEvent(String jsonEvent);
} 




public class eventtarget implements eventInterface{
	
	private boolean prettyFormat;
	private Logger      LOGGER    = LoggerFactory.getLogger("EVENT");
	
	public eventtarget()
	{
		this.prettyFormat = false;
	}
	
	public eventtarget(boolean doPretty)
	{
		this.prettyFormat = doPretty;
	}
	
	// onMeetingEvent(string)
	//  This member function is called by the BlueJeans Event Websocket SDK upon
	// receiving a Meeting Event.
	//
	public void onMeetingEvent(String json)
	{
		if(this.prettyFormat) {
			JSONObject js = new JSONObject(json);
			LOGGER.info( js.toString(2) );
		}
		else {
			LOGGER.info("(json): " + json );
		}
	}
}