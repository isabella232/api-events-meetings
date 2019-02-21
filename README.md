# BlueJeans Meeting Events SDK's



![927](./927.png)

## Introduction

BlueJeans meetings sends realtime updates on the status of participants and the connections.  This data is sent over a Websocket, and the SDK's here provide application developers with simple modules to access this realtime data.



## About Event Data...

The meeting event sends to a listener information asynchronously using JSON data objects.  Since events can happen rather quickly, the JSON object uses very short field names.



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

- "A1": Boolean, if audio is being sent by client at all
- "A2": Boolean, if client is audio local muted
- "A3": Boolean, if client is audio remote muted
- "A4": String, audio codec used by client to send
- "A5": String, audio codec used by server to send
- "C1": Integer from 1-5, current call quality of client
- "C2": String, the callguid of the client who's currently presenting. Null if no one is presenting.
- "C3": Integer, the height of content being sent by this client
- "C4": Integer, the width of content being sent by this client
- "C5":  Array, a list of physical connections that this participant represents. More details later.
- "E1": String, the participant guid of this participant
- "L1": Boolean, if this participant is a moderator
- "L2": Integer, 4 if the client's main stream is just video, 2 if the client is receiving content and video in the same stream (non-dual-stream content)
- "P1": String, callguid of the participant whose video is pinned for this participant
- "R1": String, remote desktop version
- "R2": Boolean, can this client be a RDC controller
- "R3": Boolean, can this client be a RDC controllee
- "S1": Boolean, is this call secure, i.e. encrypted
- "S2": Integer, which video layout the client is using. See reference below.
- "T": Boolean, are we detecting talking from this client
- "V1": Boolean, if video is being sent by client at all
- "V2": Boolean, if client is video local muted
- "V3": Boolean, if client is video remote muted
- "V4": String, video codec used by client to send
- "V5": Integer, the height of video being sent by this client
- "V6": Integer, the width of video being sent by this client
- "V7": String, video codec used by server to send
- "V8": Integer, the height of video being received by this client
- "V9":Integer, the width of video being received by this client
- "a": Array, list of alerts for this client. More on this later.
- "c": String, the callguid of this participant
- "ch": String, the chatguid of this participant
- "crp": Dictionary, arbitrary metadata attached to this participant by any client
- "e": String, the type of this endpoint, e.g Carmel, Blue, WebRTC, Room System, etc.
- "m": String, the meeting id of this meeting
- "n": String, the name of this participant
- "r": Boolean, is this endpoint being remote desktop controlled
- "t": String, always "endpoint" for some reason
- "v": Boolean, should this participant be visible in the roster. Internal entries like the meeting recorder and cascade legs are invisible.

