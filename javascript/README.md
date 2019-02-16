# BlueJeans Meetings Events Javascript SDK

------

This Javascript Software Development Kit (**SDK**) enables an application to receive asynchronous events associated with a BlueJeans meeting.  Events are useful for triggering responses in applications as a result of some change in a meeting.  For example, a meeting ending can trigger an application to update billing.

This api-events-onvideo package contains these examples and information:

- Javascript-based reference application for connecting into the events associated with an active BlueJeans meeting
- Sample Javascript command-line invocation to connect to a given meeting and receive the events.
- Documentation describing the format of the events messages sent from the BlueJeans Event Service in the cloud.



## Introduction

BlueJeans meeting clients are kept up to date by an Event Service in the cloud. This service publishes events related to the meeting, and the events describe conditions like who joined, who left, who muted, who unmuted, and more.

This package encapsulates a event listener into a Javascript object called **EventService().**   The object connects to the cloud-service and receives asynchronous events over a websocket.  Information describing the nature of the event is transmitted from the cloud using a JSON-formatted payload.

### Requirements

To engage an EventService() object, your application environment must first establish these conditions:

1. The meeting from which you are going to receive events must be already running, and you must know its **numeric meeting id**

2. BlueJeans infrastructure consists of service *partitions*.  User accounts are mapped to a partition, and any meeting they schedule will be hosted on the user's partition. You need to know the **partition string name** where your meeting is running.   To determine the meeting's partition name, you must look at the scheduler's profile to see what partition their account maps to.  Partitions are usually named "z1", "z2", and so on.

3. The numeric **user id** for the owner of the meeting.

4. Lastly, you will need a **meeting access token** which allows your application to make API calls to receive events.  Note the meeting access token is different from the application access token.  You generate this meeting access token through making the OAuth authentication API

   ```Javascript
   oauth2/token?Meeting
   ```

------



## Running the EventService

To run the sample reference application, you will need to have installed NodeJS and the npm package manager.

### Install the EventService Package

Download the package files onto your computer and then run the install command to make sure that all required modules are loaded onto your computer.

```javascrpt
npm install
```



### Run the EventService Reference Application

Once the package installation is complete, you invoke the EventService through this command line

```javascript
node event-service.js <partition> <numeric_meeting_id> <user_id> <access_token>
```

So for example if you want to look at John Smith's meeting, 111222333.  Through the get user API, you can find out that John Smith's user id number is 1407819, and the name of his partition is "z4".  Finally, you make the API call to get the meeting access token, "29e15df1eef14b2ca401e778d7a950c8@z4".  (Use the Meeting Grant Type authentication)

Your command line would be:

```javascript
node event-service.js z4 111222333 1407819 29e15df1eef14b2ca401e778d7a950c8@z4
```

------



## About Event Data...

The EventService returns information asynchronously using JSON data objects.  Since events can happen rather quickly, the JSON object uses very short field names.



### JSON Object

Here is a sample JSON event record for when a guest joins meeting "111222333".

```javascript
  "event": "statechange.endpoints.111222333",
  "meetingGuid": "",
  "props": {
    "f": [
      {
        "A1": "1",
        "A2": "0",
        "A3": "0",
        "A4": "Opus 16Khz",
        "A5": "Opus 16Khz",
        "C1": "5",
        "C5": [
          {
            "callGuid": "34c4342a-5ff5-48e6-a38f-7388f443a573",
            "capabilities": [
              "AUDIO",
              "VIDEO",
              "CONTENT"
            ],
            "connectionGuid": "connguid:4e0cffc5-9ab1-4f03-8dad-dc0df9723463",
            "endpoint": "WebRTC"
          }
        ],
        "E1": "seamguid:586e4215-a65c-42d8-9b35-d3711a4b235a",
        "L1": "1",
        "L2": "1",
        "S1": "1",
        "S2": "0",
        "T": "0",
        "V1": "1",
        "V2": "0",
        "V3": "0",
        "V4": "VP8",
        "V5": "720",
        "V6": "1280",
        "V7": "VP8",
        "V8": "180",
        "V9": "320",
        "c": "34c4342a-5ff5-48e6-a38f-7388f443a573",
        "ch": "2e9f2979f40e991e93c18137e6ca04dd",
        "e": "WebRTC",
        "lc": true,
        "m": "111222333:276818-1c55e939-6b20-427e-88cf-91c0c37e2ef8,0",
        "n": "John Smith",
        "sm": false,
        "v": "1"
      }
    ]
  }
}
```

------



### Data Field Definitions

The following list gives the functional meaning of the short JSON field names.



* m - meetingid
* n - name
* C1 - CallQuality
* L1 - Leader
* c - callguid
* e - endpoint
* T - TalkDetected
* C2 - CurrentPresenter
* S1 - SecureCall
* L2 - Layout
* S2 - SubLayout
* V1 - VideoRecv
* V2 - VideoRecvLocalMute
* V3 - VideoRecvRemoteMute
* A1 - AudioRecv
* A2 - AudioRecvLocalMute
* A3 - AudioRecvRemoteMute
* a - alerts
* A4 - AudioRecvCodec
* A5 - AudioSendCodec
* V4 - VideoRecvCodec
* V5 - VideoRecvHeight
* V6 - VideoRecvWidth
* V7 - VideoSendCodec
* V8 - VideoSendHeight
* V9 - VideoSendWidth
* C3 - ContentRecvHeight
* C4 - ContentRecvWidth      
* ch - chatEndpointGuid
* t - type
* v - visibility
* P1 - PinnedGuid
* E1 - endpointGuid
