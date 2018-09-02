var app=angular.module("app",["ngResource"]);
//app.controller('myCtrl', function($scope) {});
var app = angular.module('app', [ 'ngResource' ]);
app.run(
		function($rootScope, $resource, $interval){
			$rootScope.giocatore="";
			$rootScope.offerta=0;
			$rootScope.durataAsta=10;
			$rootScope.isLoggato= function(){
				if (!$rootScope.giocatore) return false;
				return $rootScope.giocatore!='';
			};
			$rootScope.doConnect = function() {
		        console.log('Connected');
		        if (!$rootScope.giocatore){
					$resource(window.location.pathname + 'connetti',{'nomegiocatore':$rootScope.nomegiocatore}).save().$promise.then(function(d) {
						$rootScope.giocatore=d.giocatore;
						$rootScope.sendMsg(JSON.stringify({'utenti': d.utenti}));
					},function(reason) {
						  alert('Failed: ' + reason.data.message);
					});
		        }
			}
			$rootScope.utenteOK = function(u){
				var ret = true;
				angular.forEach($rootScope.utentiScaduti, function(value,chiave) {
					if(value == u)
						ret = false;
					});
				return ret;
			}
			$rootScope.doDisconnect = function() {
				$resource(window.location.pathname + 'disconnetti',{}).save().$promise.then(function(d) {
					$rootScope.sendMsg(JSON.stringify({'utenti': d.utenti}));
					$rootScope.giocatore="";
				});
			}
		    $rootScope.doInvia = function(){
		    }
			$rootScope.connect1 = function() {
			    var socket = new SockJS('/messaggi-websocket');
			    stompClient = Stomp.over(socket);
			    stompClient.connect({}, function (frame) {
			        console.log('Connected: ' + frame);
			        stompClient.subscribe('/topic/messaggio', function (msg) {
			            $rootScope.getMessaggio(msg.body);
			        });
			    });
			}
			$rootScope.disconnect1= function() {
			    if (stompClient !== null) {
			        stompClient.disconnect();
			    }
			    console.log("Disconnected");
			}
			$rootScope.invia1= function(s) {
			    stompClient.send("/app/hello", {}, s);
			    $rootScope.doInvia();
			}
			$rootScope.connect2 = function() {
				 var loc = window.location, new_uri;
	             if (loc.protocol === "https:") {
	                 new_uri = "wss:";
	             } else {
	                 new_uri = "ws:";
	             }
	             new_uri += "//" + loc.host;
	             new_uri += loc.pathname + "messaggi-websocket";
	             ws = new WebSocket(new_uri);
                 ws.onmessage = function(data){
					$rootScope.getMessaggio(data.data);
				}
			}
			$rootScope.disconnect2= function() {
			    if (ws != null) {
			        ws.close();
			    }
			    console.log("Disconnected");
			}
			$rootScope.invia2= function(s) {
				ws.send(s);
			    $rootScope.doInvia();
			}
			$resource(window.location.pathname + 'init',{}).get().$promise.then(function(data) {
				var profilo=data.profilo;
				if (profilo=="PRIMO"){
					$rootScope.invia=$rootScope.invia1;
					$rootScope.disconnect=$rootScope.disconnect1;
					$rootScope.connect=$rootScope.connect1;
				}
				else{
					$rootScope.invia=$rootScope.invia2;
					$rootScope.disconnect=$rootScope.disconnect2;
					$rootScope.connect=$rootScope.connect2;
				}
				$rootScope.connect();
				$rootScope.giocatore=data.giocatore;
				if ($rootScope.giocatore){
					$rootScope.nomegiocatore=$rootScope.giocatore;
					$rootScope.doConnect();
				}
				
				$rootScope.utenti=data.utenti;
			});
			$rootScope.sendMsg=function(s){
				$rootScope.invia(s);
			}
			$rootScope.getMessaggio = function(message){
				if (message){
					var msg = JSON.parse(message);
					if (msg.utenti){
						$rootScope.utenti=msg.utenti;
					}
					if (msg.ping){
						console.log("PING");
					}
					$rootScope.$apply();
				}
			}
			$rootScope.aggiorna = function(){
				$resource(window.location.pathname + 'aggiorna',{'nomegiocatore':$rootScope.giocatore}).get().$promise.then(function(data) {
					$rootScope.utentiScaduti=data.utentiScaduti;
					$rootScope.timeStart=data.timeStart;
					$rootScope.offertaVincente=data.offertaVincente;
				});				
			}
			$rootScope.aggiorna();
			var a = $interval(function() {
				$rootScope.aggiorna();
//				$rootScope.sendMsg(JSON.stringify({'ping': $rootScope.giocatore}));
	          }, 1000);
			$rootScope.start = function(){
				$resource(window.location.pathname + 'start',{'nomegiocatore':$rootScope.giocatore, 'durata':$rootScope.durataAsta}).get();
			}
			$rootScope.inviaOfferta = function(){
				$resource(window.location.pathname + 'inviaOfferta',{'nomegiocatore':$rootScope.giocatore, 'offerta':$rootScope.offerta}).save().$promise.then(function(data){}
				,function(reason) {
					  alert('Failed: ' + reason.data.message);
				});
			}
			$rootScope.cancellaUtente = function(u) {
				$resource(window.location.pathname + 'cancellaUtente',{'nomegiocatore':u}).save().$promise.then(function(d) {
					$rootScope.sendMsg(JSON.stringify({'utenti': d.utenti}));
				})
			}
			$rootScope.inizia = function(u) {
				$resource(window.location.pathname + 'inizia',{'nomegiocatore':u,'durata':$rootScope.durataAsta}).save().$promise.then(function(d) {
				})
			}
			$rootScope.incrementa = function(inc) {
				$resource(window.location.pathname + 'inviaOfferta',{'nomegiocatore':$rootScope.giocatore, 'offerta':$rootScope.offertaVincente.offerta+inc}).save().$promise.then(function(data){}
				,function(reason) {
					  alert('Failed: ' + reason.data.message);
				});
			}
	}
)
