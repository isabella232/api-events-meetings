# api-events-onvideo

BlueJeans meetings are kept up to date by and Event Service in the cloud. The meeting clients connect to this to find out what is going on in a meeting.  Various events such as who joined and left, who muted and unmuted, etc. are published by this service.

This project provides some sample code that can connect to a given meeting and receive the events.  The code uses a socket to connect to the service in order to receive the events.  A JSON payload is received when events occur.
