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
