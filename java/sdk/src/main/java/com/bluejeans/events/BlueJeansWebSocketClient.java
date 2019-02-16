package com.bluejeans.events;
import com.bluejeans.events.eventtarget;
import com.bluejeans.events.EventsSocketHandler;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import org.json.*;
import java.util.Random;



public class BlueJeansWebSocketClient
{
	private String      MeetingID, MeetingPasscode, access_token, UserID;
	private String      partition = "";
	private Logger      LOGGER    = LoggerFactory.getLogger("BlueJeansWebSocketClient");
	private eventtarget notifyMe = null;
	private URI		    eventUri = null;
	private EventsSocketHandler socket = null;
	private WebSocketClient client = null;
	
	
	
	// BlueJeansWebSocketClient constructor 
	//   The events come from the meeting entity which is instantiated on a specific
	// partition in the BlueJeans cloud.
	//
	public BlueJeansWebSocketClient(String whichPartition)
	{
		this.partition = whichPartition;
	}
	

	// BlueJeansWebSocketClient constructor 
	//   The events come from the meeting entity which is instantiated on a specific
	// partition in the BlueJeans cloud.  Accommodate a user-specified event handler
	//
	public BlueJeansWebSocketClient(String whichPartition, eventtarget receiver)
	{
		this.partition = whichPartition;
		this.notifyMe  = receiver;
	}
	

	// BlueJeans Events Websocket URI Format
	//  wss://bluejeans.com/z5/evt/v1/4159908751/512/9sdn2iro/websocket
	private URI createUri(String meetingID)
	{
		Random rand         = new Random();
		int    num          = rand.nextInt((999 - 100) + 1) + 100;
		URI    websocketURI = null;
		String WebsocketStr;

		WebsocketStr = "wss://bluejeans.com/" 
					   + this.partition 
					   + "/evt/v1/"
					   + meetingID
					   + "/" + num + "/eventjdk/websocket";
					   
		LOGGER.debug("URI for web socket: " + WebsocketStr);
				
		try{
			websocketURI = new URI(WebsocketStr);
		} catch (Exception e)
		{
			LOGGER.error("Exception creating websocket URI: " + e);
		}
		
		return websocketURI;
	}
	
	
    public  void init(String websocketMeetingId,
					  String webSocketPasscode,
					  String accessToken,
					  String userId
					  ) throws IOException, URISyntaxException 
    {
        
    	// Copy to Object local variables
    	MeetingID       = websocketMeetingId;
    	MeetingPasscode = webSocketPasscode;
    	UserID          = userId;
		access_token    = accessToken;
    	
	
		LOGGER.info("Creating Websocket URI");
    	eventUri = createUri(MeetingID);

		 
		LOGGER.info("Creating SslContextFactory()");
        SslContextFactory sec    = new SslContextFactory();
        sec.setValidateCerts(false);
        client = new WebSocketClient(sec);
        
		LOGGER.info("Creating MyWebSocket(MeetingID:" 
					+ MeetingID + ", Passcode=" + MeetingPasscode +")");
        socket = new EventsSocketHandler(MeetingID,MeetingPasscode,
										 access_token,
										 UserID,
										 notifyMe);
       
        // Client starts
        try 
        {
            client.start();
            LOGGER.info("CLIENT [start]ed");
        }        
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        // Connecting to server
        try 
        {
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			LOGGER.info("CLIENT [connect]ed: " + eventUri.toString());
            client.connect(socket, eventUri, request);
        } 
        catch (Throwable t) 
        {
            t.printStackTrace();
        }

        
        //Waiting for the socket to close.
        try 
        {
			LOGGER.info("CLIENT [wait]ing for close");
			socket.await();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        //Stopping the client.
        finally 
        {
            try 
            {
                client.stop();
            }
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
    }
	
	
	// endTheThread()
	//    Call this routine if this object is connecting in a multi-threaded
	// instance, and you are ending the event capture session
	//	
	public void endTheThread()
	{
		try{
			client.stop();
			socket.endSession();
		}catch (Exception e)
		{
			LOGGER.debug("Exception when ending thread");
		}
	}
	
}