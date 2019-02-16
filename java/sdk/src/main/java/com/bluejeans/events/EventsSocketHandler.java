package com.bluejeans.events;

import com.bluejeans.events.jsontemplates;
import com.bluejeans.events.eventtarget;

import org.json.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;



@WebSocket
public class EventsSocketHandler {

    private Logger LOGGER = (Logger)LoggerFactory.getLogger("EventsSocketHandler");

	private final CountDownLatch closeLatch = new CountDownLatch(1);

	//private MeetMeService meetme = new MeetMeService();
	private jsontemplates jst = null;

	private String Task;
	private String Role = "Moderator";
	private String user_id,meeting_id,meeting_passcode,registrationMessage,
				   heartbeat,keepAlive,msgToSend,MeetingAccessToken;
	private eventtarget notifyTarget = null;
	private Session ourSession = null;


	public EventsSocketHandler(String MeetingID,
			             String MeetingPasscode,
						 String accessToken,
						 String UserID,
						 eventtarget notifyThis) {
		this.MeetingAccessToken  = accessToken;
		this.meeting_id			 = MeetingID;
		this.meeting_passcode    = MeetingPasscode;
		this.user_id             = UserID;
		this.jst 				 = new jsontemplates(MeetingAccessToken);
		this.registrationMessage = jst.wsRegisterObject(MeetingID,UserID);
		this.heartbeat           = jst.wsHeartBeat();
		this.msgToSend 			 = "something soon here";
		this.notifyTarget        = notifyThis;
	}
	

	@OnWebSocketConnect //EVENT 1 - OnConnect
	public void onConnect(Session session) {

		LOGGER.info("onConnect() event");
		this.ourSession = session;

		try {
			
			Future<Void> fut;
			
			LOGGER.debug("Sending Event registration:\n" + jst.toString(registrationMessage));

			fut = session.getRemote().sendStringByFuture(registrationMessage);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	@OnWebSocketMessage  //SERVER EVENT 2 - ONMESSAGE
	public void onMessage(Session session, String message) throws IOException,
			Exception, RuntimeException {
		LOGGER.info("onMessage(): message: '" + message.substring(0, Math.min(message.length(),16)) +"...'");
		Future<Void> fut;
		Future<Void> fut3;
		Future<Void> fut4;

		if (!message.equals("o")) {

			if (message.contains("keepalive")) {
				LOGGER.debug("keepalive sending: " + heartbeat.toString());
				fut = session.getRemote().sendStringByFuture(heartbeat);
			}
			
			if (message.contains("meeting.notification.msg") )  // && flag2==0 &&switch1==1)
			{
				// Decoding the notification message melange
				// a[ "[\"meeting.notification.msg\",{\"id\":null,....}]", "[...]"]
				// a[ "[json-string(UberEvent), ..., json-string(UberEvent)]" ]
				// strip off "a","o" operator
				String jsonBlobs        = message.substring(1);
				
				// get array of json-string(s) representing Uber event records
				JSONArray eventBlobs    = new JSONArray(jsonBlobs);
				LOGGER.debug("eventBlobs: \n" + eventBlobs.toString(2));

				// The default case is an single-entry array of JSON
				// representing the Uber event information
				String jsonUberEvent    = eventBlobs.getString(0);

				
				// Now decompose the Uber JSON to get the object
				// UberEvent -->  [ "eventTypeString", {evenDetailsObject} ]
				JSONArray uberEvent     = new JSONArray( jsonUberEvent );
				LOGGER.debug("Uber Event Message:\n" + uberEvent.toString(2) );

				// Separate out the string-name of the Event, and then the
				// actual event details object
				String     eventType    = uberEvent.getString(0);
				JSONObject eventDetails = uberEvent.getJSONObject(1);
				LOGGER.debug("Event Details:\n" + eventDetails.toString(2) );
				
				// The useable BlueJeans event information is contained in the "body"
				// property of the Event Details object
				String bodyJSON         = eventDetails.getString("body");
				JSONObject theEvent     = new JSONObject(bodyJSON);
				LOGGER.debug("Event:\n" + theEvent.toString(2));
				
				if(this.notifyTarget != null) {
					try{
					notifyTarget.onMeetingEvent(bodyJSON);
					} catch(Exception e)
					{
						LOGGER.error("Event callback failed: " + e.getStackTrace().toString());
					}
				}
			}
		}
	}

	@OnWebSocketClose  
	public void onClose(int statusCode, String reason) {
		LOGGER.info("WebSocket Closed. Code:" + statusCode);
	}

	public boolean awaitClose(int duration, TimeUnit unit)
			throws InterruptedException {
		return this.closeLatch.await(duration, unit);
	}
	
	public void await() throws InterruptedException {
		this.closeLatch.await();
	}
	
	public void endSession(){
		LOGGER.info("** Ending Websocket session");
		this.closeLatch.countDown();
	}
	
	
	@OnWebSocketError
	public void onError(Throwable error) {
		LOGGER.info("This is the error => " + error);
	}
	
}