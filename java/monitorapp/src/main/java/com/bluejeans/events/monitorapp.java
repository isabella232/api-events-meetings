package com.bluejeans.events;
import com.bluejeans.events.bluejeansAPI;
import com.bluejeans.events.eventtarget;
import com.bluejeans.events.eventsSDK;


import java.util.Random;
import java.util.Scanner;
import java.net.URI;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.json.*;

//
// Reference Application to connect into a BlueJeans Meeting Events' Stream
//  Glenn Inn,  BlueJeans 
//  Jan 2019
//


public class monitorapp
{

	private static void doHelp()
	{
		System.out.println("\nUsage: monitorapp  {meetingid} {access_Code}");
		System.out.println("  Test utility to connect to a BlueJeans Meeting Event Stream");
		System.out.println("Where:");
		System.out.println("  meetingId : the BlueJeans numeric Meeting ID of interest");
		System.out.println("  access_Code : (optional) Access code for the meeting");
		System.exit(0);		
	}

	
	
	public static void main( String[] args )
	{
		Logger LOGGER       = (Logger) LoggerFactory.getLogger("wsClient");
		Logger levelSet     = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		/*		
		  public static final ch.qos.logback.classic.Level definitions:
		  OFF; ERROR; WARN; INFO; DEBUG; TRACE; ALL;
		*/
		levelSet.setLevel( Level.OFF );
		
		String prompt = "Monitorapp ('q' to quit)> ";
		
		// Local event handler class.
		class myEventTarget extends eventtarget
		{
			private String prompt = "";
			
			public myEventTarget(String prompt)
			{
				this.prompt = prompt;
			}
			
			@Override
			public void onMeetingEvent(String json)
			{
				JSONObject js = new JSONObject(json);
				System.out.println( js.toString(2) );
				System.out.print(prompt );
			}
		}
		
		
		//----------------------------------------------
		// Example:  Creating a reference target to receive the asynchronous
		// event callbacks from the BlueJeans meeting. 
		//    Normally you would replace this object with your application's
		//    event handler
		//----------------------------------------------
		myEventTarget  eventsGoHere  = new myEventTarget(prompt);

		LOGGER.info("Monitorapp -- Reference Application That connects to BlueJeans' Events websocket");
		
		//
		// Parse the Command line for BlueJeans meeting ID, and passcode
		//
		if(args.length < 1)
			doHelp();

		String meetingID = args[0];
		String passcode  = (args.length>1 ? args[1] : "");
		
		//
		// Now make API call to get a Meeting Access Token so that we can connect
		// to the BlueJeans event stream
		//
		bluejeansAPI theApi      = new bluejeansAPI();
		JSONObject mtgAccessInfo = theApi.getMeetingAccessToken(meetingID,passcode);
		LOGGER.debug("Meeting Access info:\n" + mtgAccessInfo.toString(2));
		String accessToken       = mtgAccessInfo.getString("access_token");
		String leaderId          = mtgAccessInfo.getString("leaderId");
		String Task              = "meeting.register";
		String partition         = mtgAccessInfo.getString("partition");

		//
		// Create a separate thread to handle Websocket events - this allows
		// this thread to have User Interaction
		//
		eventsSDK eventCapture = new eventsSDK();

		// for user I/O
		Scanner in = new Scanner(System.in);

	    try
		{
/* ---------------------------------------------------------------------
			// Use the standalone entry if you want to block and receive
			// events in the current thread.
			//
			LOGGER.info("Calling startStandalone()");
			eventCapture.startStandalone(
					meetingID, passcode,leaderId,partition,
					accessToken,eventsGoHere);
   --------------------------------------------------------------------- */

/* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			// Use the Threaded entry to receive events in separate thread
			// and remain unblocked to do other things (in this example we
			// are handling user Input)
			//
   ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */   
			LOGGER.info("Calling startInThread()");
			eventCapture.startInThread(
					meetingID, passcode,leaderId,partition,
					accessToken,eventsGoHere);

		    String cmd = "";
			do{
				System.out.print(prompt );
				cmd = in.nextLine();
				cmd = cmd.trim();
				System.out.println("Command is: '" + cmd + "'");
			} while(!(cmd.equals("q")));
			
			System.out.println("\n*** Done, ending session ***");
			eventCapture.endSession();
		}
		catch(Exception e)
		{
			LOGGER.info( e.getStackTrace().toString() );			
			System.exit(-1);
		}
	}	
	
}