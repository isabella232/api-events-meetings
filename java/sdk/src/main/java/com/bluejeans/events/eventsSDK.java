package com.bluejeans.events;

import com.bluejeans.events.BlueJeansWebSocketClient;
import com.bluejeans.events.eventtarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// class eventsSDK
//     This is the BlueJeans SDK for connecting into the asynchronous Event
// stream.   Note that the connection is over a websocket, and Apache Jetty
// websocket client library is blocking.
//
//   This SDK provides a blocking and child-threaded implementation.  The user
//   has the choice to run in a blocking or non-blocking response to the calling
//   application
//
public class eventsSDK extends Thread
{
	private Logger LOGGER = (Logger) LoggerFactory.getLogger("EventThread");
	private String meetingID,passcode,accessToken,leaderId;
	private Thread iAm;
	private BlueJeansWebSocketClient eventWebSocket;
	private String partition;
	private eventtarget eventTarget;
	
	
	void sdk()
	{
	}
	
	// startInThread()
	// Capture the asynchronous events in a separate thread to prevent
	// blocking the calling application's thread.
	public void startInThread(String meetingID, String passcode,
							  String leaderId,
							  String partition,
							  String accessToken, 
							  eventtarget eventsGoHere) 
	{				
		LOGGER.info("startInThread()");
		this.meetingID   = meetingID;
		this.passcode    = passcode;
		this.accessToken = "";
		this.leaderId    = leaderId;
		this.partition   = partition;
		this.eventTarget = null;		
		this.accessToken = accessToken;
		this.eventTarget = eventsGoHere;
		this.eventWebSocket     = new BlueJeansWebSocketClient(this.partition,this.eventTarget);
		LOGGER.debug("starting thread");
		this.start();
	}
	
	// startStandalone()
	// Capture the asynchronous events in the caller's thread.  This
	// method is blocking.
	public void startStandalone(String meetingID, String passcode,
								String leaderId,
								String partition,
								String accessToken, 
								eventtarget eventsGoHere) 
	{
		this.meetingID   = meetingID;
		this.passcode    = passcode;
		this.accessToken = "";
		this.leaderId    = leaderId;
		this.partition   = partition;
		this.eventTarget = null;		
		this.accessToken = accessToken;
		this.eventTarget = eventsGoHere;
		this.eventWebSocket     = new BlueJeansWebSocketClient(this.partition,this.eventTarget);
		this.run();					 
	 }
	
	
	//
	// Local instances of Thread control methods
	//	
	public void run() {
		
		try{
			this.eventWebSocket.init(this.meetingID, this.passcode,
							  this.accessToken,
							  this.leaderId);
		} catch(Exception e){
			LOGGER.warn(e.getStackTrace().toString());	
		}
	}
	
	public void start(){
	  if (iAm == null) {
		 iAm = new Thread (this, "EventsThread");
		 iAm.start ();
	  }
	}
	
	
	// endSession()
	//   Application should call this method to cleanup the web socket connection
	// when done.
	public void endSession() {
		this.eventWebSocket.endTheThread();
	}
	
}
