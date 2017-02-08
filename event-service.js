if (process.argv.length != 6)
{
    console.log('Usage: node event-service.js <partition> <numeric_meeting_id> <user_id> <access_token>');
    process.exit(1);
}

var partition = process.argv[2];
var meeting_id = process.argv[3];
var user_id = process.argv[4];
var access_token = process.argv[5];

// Libs
var _ = require('underscore');
var my = require('myclass');
var sockjs = require('sockjs-client');

function x(_, my, sockjs)
{
    var invokeIfImplemented = function(collection, methodName, arg)
    {
        return _.invoke(_.filter(collection, function (item)
        {
            return item[methodName] !== undefined;
        }), methodName, arg);
    };

    var EventService = my.Class(
    {
        events: function()
        {
            return {
                "guid_assigned": this.guidAssigned,
                "remoteclose": this.remoteclose,
                "pairingError": this.pairingError,
                "kicked": this.kicked
            };
        },

        maxReconnects: 10,

        reconnects: 0,

        reconnectBackoff: 1000,

        constructor: function()
        {
            this.handlers = {};
        },

        registerHandler: function(handler, namespace, customOpts)
        {
            this.handlers[namespace] = handler;
        },

        setUpSocket: function(options, reconnect_count)
        {
            var self = this;
            var sock_url = options.eventServiceUrl || '';

            if (self.sock)
            {
                delete self.connected;
                if (self.joinTimeout)
                {
                    clearTimeout(self.joinTimeout);
                    delete self.joinTimeout;
                }

                self.sock.onclose = function()
                {
                    // Dummy function to avoid reconnect in the onclose method
                    // of previous socket connection.
                };
            }

            self.close(); //prevent multiple connections

            self.options = options;
            self.meetingAccessToken = options.access_token;

            var sockjs_protocols = [
                    'websocket', 'xdr-streaming', 'xhr-streaming',
                    'xdr-polling', 'xhr-polling', 'iframe-xhr-polling',
                    'jsonp-polling'
            ];
            
            var sock = self.sock = new sockjs(sock_url, {},
            {
                cookie: true,
                transports: sockjs_protocols
            });

            if (self.joinTimeout)
            {
                clearTimeout(self.joinTimeout);
                delete self.joinTimeout;
            }

            sock.onopen = function()
            {
                sock._selfclosed = false;
                sock._remoteclosed = false;

                if(self._crashed){
                    sock.close();
                    return;
                }
                 
                options.events = ['meeting', 'endpoint'];
                self.sendEvent('meeting.register', options);
                invokeIfImplemented(_.values(self.handlers), "onOpen", self.meetingAccessToken);
                self.reconnects = 0;
                if (reconnect_count && reconnect_count > 0)
                {
                    //window.Notifications.trigger('socket:reconnected');
                }

                self.joinTimeout = setTimeout(function()
                {
                   if(!self.connected)
                   {
                       self.sock.close();
                   }
                   delete self.joinTimeout;
                },10000);
            };

            sock.onmessage = function(_e)
            {
                try
                {
                    var msg = JSON.parse(_e.data);
                    if (msg.length == 2 && typeof msg[1] === 'object')
                    {
                        var evt = msg[0];
                        switch(evt)
                        {
                            case 'keepalive':
                                self.sendEvent("heartbeat");
                                break;
                            default:
                              var evt_data = msg[1];
                              if(evt_data && evt_data.reqId && self.reqCallbacks[evt_data.reqId])
                              {
                                var cb = self.reqCallbacks[evt_data.reqId];
                                delete self.reqCallbacks[evt_data.reqId];
                                cb(evt_data.error,evt_data.data);
                                break;
                              }

                              var protocolEvent = evt.match("([^.]*)$")[0];
                              if (protocolEvent in self.events())
                              {
                                  //self.events()[protocolEvent](evt_data);
                                  var c = self.events()[protocolEvent];
                                  c.call(self, evt_data);
                              }
                              else
                              {
                                  var namespaces = _.keys(self.handlers);
                                  var eventNamespace = _.find(namespaces, function (namespace)
                                  {
                                      return evt.match("^"+namespace);
                                  });

                                  self.handlers[eventNamespace].onMessage(evt, evt_data);
                              }

                              break;
                        }
                    }
                    else
                    {
                        console.log("JSON Received but not valid event: " + (msg[0] || ""));
                    }
                }
                catch (e)
                {
                    console.log("ERROR: " + e)
                    //invalid json, discarding
                    console.log("Invalid JSON from SockJS - " + msg);
                }
            };

            sock.onclose = function()
            {
                delete self.connected;

                if (self.joinTimeout)
                {
                    clearTimeout(self.joinTimeout);
                    delete self.joinTimeout;
                }
                if (
                    !self.sock._selfclosed &&
                    !self.sock._remoteclosed &&
                    !self._crashed &&

                    !self._timeoutClosed &&
                    !self._kicked

                    ) {
                    invokeIfImplemented(_.values(self.handlers), "onClosedUnexpectedly", {});
                    self.reconnect();
                }
                else
                {
                    invokeIfImplemented(_.values(self.handlers), "onClose", {});
                    //window.Notifications.trigger('socket:closed');
                }
                //Logger.warn("SockJS connection closed");
            };
            sock.onerror = function(e) {
                //Logger.warn("SockJS error occured");
                invokeIfImplemented(_.values(self.handlers), "onError", {});
            };
        },

        guidAssigned: function(event)
        {
            console.log("Connected to event service. Endpoint guid: " + event.seamGuid + ", chat guid: " + event.guid);
            this.connected = true;
            //cofa.skinny.instances.selfParticipant.set({id: event.seamGuid});
            //invokeIfImplemented(_.values(this.handlers), "onConnect");
            //window.Notifications.trigger('socket:connected');
        },

        close: function()
        {
            this.connected = false;
            if (this.sock)
            {
                invokeIfImplemented(_.values(this.handlers), "onClose", {});
                //Logger.info("Closing SockJS connection");
                this.sock._selfclosed = true;
                this.sock.close();
            }
        },

        reconnect: function()
        {
            console.log("Reconnect!")
            var self = this;
            console.log("--- oops4");
            this.connected = false;
            if (self.sock._remoteclosed) return;
            if (self.sock._kicked) return;
            if (self._timeoutClosed) return;
            if (self.reconnects < self.maxReconnects && self.meetingAccessToken && !self._reconnecting)
            {
                //window.Notifications.trigger('socket:reconnecting');
                self._reconnecting = true;
                setTimeout(function()
                {
                    console.log("Reconnecting");
                    self.setUpSocket(self.options, self.reconnects);
                    self._reconnecting = false;
                    self.reconnects++;
                }, self.reconnectBackoff * (self.reconnects > 10 ? 10 : self.reconnects));
            }
        },

        remoteclose: function()
        {
            console.log("remotecolose")
            var self = this;
            self.sock._remoteclosed = true;
            invokeIfImplemented(_.values(self.handlers), "remoteclose");
        },

        pairingError: function(error)
        {
            var self = this;
            console.log("Error Pairing Meeting");
            console.log(JSON.stringify(error));
            setTimeout(function()
            {
                self.sock.close();
            }, 200);
        },

        isDisconnected: function()
        {
            return !this.isConnected();
        },

        isConnected: function()
        {
            return this.sock && this.connected;
        },

        isJoinEvent: function(eventName)
        {
            return eventName === 'meeting.register';
        },

        sendEvent: function(event_name, event_data)
        {
            if (event_name === 'heartbeat' || this.isJoinEvent(event_name) || this.isConnected())
            {
                this.sock.send(JSON.stringify([event_name, event_data || {}]));
            }
            else
            {
                console.log("Cant send event yet -- sock or guid not ready");
            }
        },

        sendRequest: function(event_name, event_data, callback)
        {
          if(this.isConnected())
          {
            if(!this.reqId)
            {
              this.reqId = 1;
            }
            else 
            {
              this.reqId++;
            }
            if(!this.reqCallbacks)
            {
              this.reqCallbacks = {};
            }
            this.reqCallbacks[this.reqId] = callback;
            this.sock.send(JSON.stringify([event_name, {reqId: this.reqId, data: (event_data || {})}]));
          } else {
            callback({error: {message: "Sending request while not connected."}});
          }
        },

        kicked: function(event)
        {
            console.log("Kicked");
            this.sock._remoteclosed = true;
            this.sock._kicked = true;
            this.sock.close();
        },

        crashed: function()
        {
            console.log("Crashed");
            this._crashed = true;
            this._idleTimeout();
        }
    });

    return new EventService();
}

var roster_names = {};

var handler =
{
    onMessage: function(event, eventData)
    {
        if (event === 'meeting.register.error')
        {
            console.log('Authentication Error: You probably have a bad access token or the meeting does not exist.');
            process.exit(1);
            return;
        }

        var self = this;
        var eventJson = JSON.parse(eventData.body);
        var eventType = eventJson.event;

        console.log("+++ HANDLER " + eventType + ": " + JSON.stringify(eventJson));
        console.log("");

        if (eventType.startsWith('statechange.livemeeting'))
        {
            console.log('MEETING ' + eventJson.props.meetingId + ": " +  eventJson.props.audioEndpointCount + " on audio and " + eventJson.props.videoEndpointCount + " on video");
            console.log('MEETING ' + eventJson.props.meetingId + ": locked = " + eventJson.props.locked);
            console.log('MEETING ' + eventJson.props.meetingId + ": status = " + eventJson.props.status);
        }
        else if (eventType.startsWith('statechange.endpoints'))
        {
            if (eventJson.props)
            {
                // Current state
                if (eventJson.props.f)
                {
                    eventJson.props.f.forEach(function (item)
                    {
                        console.log("PARTICIPANT: " + item.n + " via " + item.e + " (" + item.c + ")");
                        roster_names[item.c] = item.n;
                    });
                }
                // Add
                else if (eventJson.props.a)
                {
                    eventJson.props.a.forEach(function (item)
                    {
                        console.log("PARTICIPANT: " + item.n + " via " + item.e + " (" + item.c + ")");
                        roster_names[item.c] = item.n;
                    });
                }
                // Delete?
                else if (eventJson.props.d)
                {
                    eventJson.props.d.forEach(function (item)
                    {
                        console.log("LEFT MEETING: " + item.n);
                    });
                }
                // Modify?
                else if (eventJson.props.m)
                {
                    eventJson.props.m.forEach(function (item)
                    {
                        if (item.V2)
                        {
                            if (item.V2 == '1')
                            {
                                console.log("VIDEO MUTE IS ON FOR " + roster_names[item.c]);
                            }
                            else if (item.V2 == '0')
                            {
                                console.log("VIDEO MUTE IS OFF FOR " + roster_names[item.c]);
                            }
                        }

                        if (item.V8 && item.V9)
                        {
                            console.log("VIDEO SEND SIZE IS " + item.V9 + "x" + item.V8 + " FOR " + roster_names[item.c]);
                        }

                        if (item.V5 && item.V6)
                        {
                            console.log("VIDEO RECV SIZE IS " + item.V6 + "x" + item.V5 + " FOR " + roster_names[item.c]);
                        }

                        if (item.A2)
                        {
                            if (item.A2 == '1')
                            {
                                console.log("AUDIO MUTE IS ON FOR " + roster_names[item.c]);
                            }
                            else if (item.A2 == '0')
                            {
                                console.log("AUDIO MUTE IS OFF FOR " + roster_names[item.c]);
                            }
                        }

                        if (item.C1)
                        {
                            console.log("CALL QUALITY CHANGED TO " + item.C1 + " FOR " + roster_names[item.c]);
                        }

                        if (item.T)
                        {
                            if (item.T == '1')
                            {
                                console.log("TALKING YES FOR " + roster_names[item.c]);
                            }
                            else if (item.T == '0')
                            {
                                console.log("TALKING NO FOR " + roster_names[item.c]);
                            }
                        }
                    });
                }
            }
        }

        console.log("");
    }
};

var eventService = x(_, my, sockjs);
if (eventService)
{
     var opts =
     {
        'numeric_id': meeting_id,
        'access_token': access_token,
        'user' : {
            'full_name': '',
            'is_leader': true
        },
        'leader_id': user_id,
        'protocol': '2',
        'endpointType': 'commandCenter',
        'eventServiceUrl': 'https://bluejeans.com/' + partition + '/evt/v1/' + meeting_id
    };

    eventService.setUpSocket(opts);
    eventService.registerHandler(handler, 'meeting.register.error');   
    eventService.registerHandler(handler, 'meeting.notification');   
}