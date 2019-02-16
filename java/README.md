# BlueJeans Meetings Events Java SDK

------

This Java Software Development Kit (**SDK**) enables a Java application to receive asynchronous events associated with a BlueJeans meeting.  Events are useful for triggering responses in applications as a result of some change in a meeting.  For example, a meeting ending can trigger an application to update billing.

This SDK these examples and information:

- Java reference application that utilizes the SDK and allows you to connect to the events associated with an active BlueJeans meeting
- Sample Window CMD script for invoking the reference application to connect and receive events
- Documentation describing the format of the events messages sent from the BlueJeans Event Service in the cloud.



## Introduction

BlueJeans meeting clients are kept up to date by an Event Service in the cloud. This service publishes events related to the meeting, and the events describe conditions like who joined, who left, who muted, who unmuted, and more.

This package encapsulates a event listener into a Java object called **com.bluejeans.events.eventsSDK().**   The object connects to the cloud-service and receives asynchronous events over a websocket.  Information describing the nature of the event is transmitted from the cloud using a JSON-formatted payload.

### Requirements

To engage an eventsSDK() object, your application environment must first establish these conditions:

- The meeting from which you are going to receive events must be already running, and you must know its **numeric meeting id** and if required, the **meeting passcode**.

------



## Running the Event Reference Application

To run the sample reference application, you will need to have installed the Java Runtime environment as well as Apache Maven.  

### Install the Events SDK

Download the package files onto your computer and then run maven in the top folder of the Java SDK.

```shell
C>\java\Events> mvn package install
```

Additionally, you will need to install the dependencies for the provided Windows shell command to execute the JAR file:

```shell
C>\java\Events> mvn install dependency:copy-dependencies
```


### Run the Event SDK Reference Application

Once the package installation is complete, you invoke the EventService through this command line

```javascript
C>\java\Events> run {meeting id} {passcode}
```

The **run.cmd** shell will invoke Java and launch the monitorapp **jar** file.  It passes in the meeting ID and (optional) passcode to the application.

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
