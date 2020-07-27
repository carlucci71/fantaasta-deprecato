var app=angular.module("app",["ngResource"]);
var app = angular.module('app', [ 'ngResource' ]);
app.run(
		function($rootScope, $resource, $interval){
			$rootScope.giocatore="";
			$rootScope.offerta=1;
			$rootScope.offertaOC=1;
			$rootScope.durataAsta=10;
			$rootScope.bSemaforoAttivo=true;
			$rootScope.messaggi=[];
			$rootScope.tokenUtente;
			$rootScope.isAdmin=false;
			$rootScope.calciatori=[];
			$rootScope.isLoggato= function(){
				if (!$rootScope.giocatore) return false;
				return $rootScope.giocatore!='';
			};
			$rootScope.callDoConnect = function(nome,id) {
				$rootScope.nomegiocatore=nome;
				$rootScope.idgiocatore=id;
				$rootScope.doConnect();
			}
			$rootScope.doConnect = function() {
		        console.log('Connected');
		        if (!$rootScope.giocatore){
		        	$rootScope.tokenUtente=new Date().getTime();
					$rootScope.sendMsg(JSON.stringify({'operazione':'connetti', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore, 'tokenUtente':$rootScope.tokenUtente}));
		        }
				$rootScope.giocatore=$rootScope.nomegiocatore;
				if ($rootScope.giocatore=="Daniele") $rootScope.isAdmin=true; else $rootScope.isAdmin=false;
				if ($rootScope.isAdmin) $rootScope.selAllenatoreOperaCome=1 + "@" + "Daniele";
			}
			$rootScope.utenteScaduto = function(u){
				var ret = false;
				angular.forEach($rootScope.utentiScaduti, function(value,chiave) {
					if(value == u.nome)
						ret = true;
					});
				return ret;
			}
			
			

			$rootScope.sonoLoggato = function(u){
				var ret = false;
				angular.forEach($rootScope.utenti, function(value,chiave) {
					if($rootScope.giocatore == u.nome)
						ret = true;
					});
				return ret;
			}
			
			$rootScope.utenteCollegato = function(u){
				var ret = false;
				angular.forEach($rootScope.utenti, function(value,chiave) {
					if(value == u.nome)
						ret = true;
					});
				return ret;
			}
			$rootScope.doDisconnect = function() {
				$rootScope.sendMsg(JSON.stringify({'operazione':'disconnetti', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
				$rootScope.giocatore="";
			}
			$rootScope.connect = function() {
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
			$rootScope.disconnect= function() {
			    if (ws != null) {
			        ws.close();
			    }
			    console.log("Disconnected");
			}
			$resource(window.location.pathname + 'elencoCalciatori',{}).query().$promise.then(function(data) {
				$rootScope.calciatori=data;
			});

			$rootScope.aggiornaCronologiaOfferte=function(){
				$resource('./elencoCronologiaOfferte',{}).query().$promise.then(function(data) {
					$rootScope.cronologiaOfferte=data;
				});
			}
			
			
			$resource(window.location.pathname + 'init',{}).get().$promise.then(function(data) {
				$rootScope.connect();
				$rootScope.giocatore=data.giocatoreLoggato;
				if ($rootScope.giocatore){
					$rootScope.nomegiocatore=$rootScope.giocatore;
					$rootScope.idgiocatore=data.idLoggato;
					$rootScope.doConnect();
				}

				$rootScope.elencoAllenatori=data.elencoAllenatori;
			});
			$rootScope.sendMsg=function(s){
				ws.send(s);
			}
			$rootScope.latenza = function(u){
				var ret = -1;
				angular.forEach($rootScope.pingUtenti, function(value,chiave) {
					if(chiave == u.nome)
						ret = value.checkPing;
					});
				return ret;


				
				return u.checkPing;
			}
			$rootScope.getMessaggio = function(message){
				if (message){
					var msg = JSON.parse(message);
//					console.log(msg);
					if (msg.RICHIESTA){
						var t=msg.RICHIESTA + "-" + new Date().getTime();
//						console.log(t);
						$rootScope.RICHIESTA=t;
					}
					if (msg.calciatori){
						$rootScope.calciatori=msg.calciatori;
					}
					if (msg.messaggi){
						//$rootScope.messaggi.push(msg.messaggio);
						$rootScope.messaggi=msg.messaggi;
					}
					if (msg.utenti){
						$rootScope.utenti=msg.utenti;
					}
					if (msg.pingUtenti){
						$rootScope.pingUtenti=msg.pingUtenti;
					}
					if (msg.RESET_UTENTE){
						if (msg.RESET_UTENTE==$rootScope.tokenUtente){
							$rootScope.giocatore="";
							alert("Utente esistente. Riconnettiti!");
						}
					}			
					if (msg.selCalciatore){
						$rootScope.selCalciatore="";
					}
					if (msg.offertaVincente){
						$rootScope.offertaVincente=msg.offertaVincente;
					}
					if (msg.durataAsta){
						$rootScope.durataAsta=msg.durataAsta;
					}
					if (msg.giocatoreDurataAsta){
						$rootScope.giocatoreDurataAsta=msg.giocatoreDurataAsta;
					}
					if (msg.contaTempo){
						if (msg.contaTempo>$rootScope.durataAsta*1000)
							$rootScope.contaTempo=$rootScope.durataAsta*1000;
						else
							$rootScope.contaTempo=msg.contaTempo;
					}
					if (msg.sSemaforoAttivo){
						if (msg.sSemaforoAttivo=='S')
							$rootScope.bSemaforoAttivo=true;
						else
							$rootScope.bSemaforoAttivo=false;
					}
					if (msg.timeStart){
						$rootScope.timeStart=msg.timeStart;
					}
					if (msg.utentiScaduti){
						$rootScope.utentiScaduti=msg.utentiScaduti;
					}
					$rootScope.$apply();
				}
			}
			$rootScope.pinga = function(){
				if ($rootScope.giocatore)
					$rootScope.sendMsg(JSON.stringify({'operazione':'ping', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
			}
			var a = $interval(function() {
				$rootScope.pinga();
	          }, 1000);
			$rootScope.start = function(){
				$rootScope.inizia($rootScope.nomegiocatore,$rootScope.idgiocatore);
//				$rootScope.sendMsg(JSON.stringify({'operazione':'start', 'selCalciatore':$rootScope.selCalciatore,'nomegiocatore':$rootScope.nomegiocatore,'idgiocatore':$rootScope.idgiocatore, 'bSemaforoAttivo':$rootScope.bSemaforoAttivo, 'durataAsta':$rootScope.durataAsta}));
			}
			$rootScope.inizia = function(ng,ig) {
				$rootScope.bSemaforoAttivo=false;
				$rootScope.timeStart=0;
				$rootScope.contaTempo=0;
				$rootScope.sendMsg(JSON.stringify({'operazione':'start', 'selCalciatore':$rootScope.selCalciatore, 'nomegiocatoreOperaCome':$rootScope.nomegiocatore, 'idgiocatoreOperaCome':$rootScope.idgiocatore,'nomegiocatore':ng,'idgiocatore':ig, 'bSemaforoAttivo':$rootScope.bSemaforoAttivo, 'durataAsta':$rootScope.durataAsta}));
				$rootScope.selCalciatore="";
			}
			$rootScope.conferma = function(){
				$rootScope.messaggi=[];
				$rootScope.bSemaforoAttivo=true;
				$resource(window.location.pathname + 'confermaAsta',{}).save($rootScope.offertaVincente).$promise.then(function(data) {
					$rootScope.sendMsg(JSON.stringify({'operazione':'confermaAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
					$rootScope.offertaVincente="";
				});
			}
			$rootScope.annulla = function(){
				$rootScope.messaggi=[];
				$rootScope.bSemaforoAttivo=true;
				$rootScope.sendMsg(JSON.stringify({'operazione':'annullaAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
				$rootScope.offertaVincente="";
			}
			$rootScope.allinea = function(quale){
				if(quale=='OC') {
					$rootScope.offertaOC=$rootScope.offertaVincente.offerta;
				}
				else {
					$rootScope.offerta=$rootScope.offertaVincente.offerta;
				}
			}
			$rootScope.inviaOfferta = function(quale){
				var ng;
				var ig;
				var off;
				if(quale=='OC') {
					ng = $rootScope.nomegiocatoreOperaCome;
					ig = $rootScope.idgiocatoreOperaCome;
					off = $rootScope.offertaOC;
				}
				else {
					ng = $rootScope.nomegiocatore;
					ig = $rootScope.idgiocatore;
					off = $rootScope.offerta;
				}
				$rootScope.sendMsg(JSON.stringify({'operazione':'inviaOfferta', 'nomegiocatore':ng, 'idgiocatore':ig, 'nomegiocatoreOperaCome':$rootScope.nomegiocatore, 'idgiocatoreOperaCome':$rootScope.idgiocatore, 'offerta':off}));
			}
			$rootScope.aggiornaDurataAsta = function(){
				$rootScope.sendMsg(JSON.stringify({'operazione':'aggiornaDurataAsta', 'giocatoreDurataAsta':$rootScope.nomegiocatore, 'durataAsta':$rootScope.durataAsta}));
			}
			$rootScope.cancellaUtente = function(u) {
				$rootScope.sendMsg(JSON.stringify({'operazione':'cancellaUtente', 'nomegiocatore':u.nome, 'idgiocatore':u.id}));
			}
			$rootScope.incrementa = function(inc, quale) {
				if(quale=='OC') {
					$rootScope.offertaOC=$rootScope.offertaOC+inc;
				} else {
					$rootScope.offerta=$rootScope.offerta+inc;
				}
				$rootScope.inviaOfferta(quale);
			}
			$rootScope.terminaAsta= function() {
				$rootScope.sendMsg(JSON.stringify({'operazione':'terminaAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
			}
			$rootScope.$watch("selAllenatore", function(newValue, oldValue) {
				if (newValue){
					var posToken = newValue.indexOf("@");
					$rootScope.idgiocatore=newValue.substr(0,posToken);
					$rootScope.nomegiocatore=newValue.substr(posToken+1);
				}
			});
			
			$rootScope.$watch("selAllenatoreOperaCome", function(newValue, oldValue) {
				if (newValue){
					var posToken = newValue.indexOf("@");
					$rootScope.idgiocatoreOperaCome=newValue.substr(0,posToken);
					$rootScope.nomegiocatoreOperaCome=newValue.substr(posToken+1);
				}
				else {
					$rootScope.idgiocatoreOperaCome="";
					$rootScope.nomegiocatoreOperaCome="";
				}
			});
			
			
	}
)


app.directive('trackProgressBar', [function () {

  return {
    restrict: 'E', // element
    scope: {
      colVal: '@', // bound to 'col-val' attribute, playback progress
      curVal: '@', // bound to 'cur-val' attribute, playback progress
      maxVal: '@'  // bound to 'max-val' attribute, track duration
    },
    template: '<div class="progress-bar-bkgd"><div class="progress-bar-marker"></div></div>',

    link: function ($scope, element, attrs) {
      // grab element references outside the update handler
      var progressBarBkgdElement = angular.element(element[0].querySelector('.progress-bar-bkgd')),
          progressBarMarkerElement = angular.element(element[0].querySelector('.progress-bar-marker'));

      // set the progress-bar-marker width when called
      function updateProgress() {
        var progress = 0,
            currentValue = $scope.curVal,
            maxValue = $scope.maxVal,
            // recompute overall progress bar width inside the handler to adapt to viewport changes
            progressBarWidth = progressBarBkgdElement.prop('clientWidth');

        if ($scope.maxVal) {
          // determine the current progress marker's width in pixels
          progress = Math.min(currentValue, maxValue) / maxValue * progressBarWidth;
        }

        // set the marker's width
        progressBarMarkerElement.css('width', progress + 'px');
        //console.log(currentValue + "-" + maxValue + "-" + progress + "-" +  $scope.colVal);
        if ($scope.colVal<2) progressBarMarkerElement.css('background-color', 'green');
        if ($scope.colVal==2) progressBarMarkerElement.css('background-color', 'yellow');
        if ($scope.colVal==3) progressBarMarkerElement.css('background-color', 'red');
      }

      // curVal changes constantly, maxVal only when a new track is loaded
      $scope.$watch('curVal', updateProgress);
      $scope.$watch('maxVal', updateProgress);
    }
  };
}]);
app.directive('capitalize', function() {
    return {
      require: 'ngModel',
      link: function(scope, element, attrs, modelCtrl) {
        var capitalize = function(inputValue) {
          if (inputValue == undefined) inputValue = '';
          var capitalized = inputValue.toUpperCase();
          if (capitalized !== inputValue) {
            // see where the cursor is before the update so that we can set it back
            var selection = element[0].selectionStart;
            modelCtrl.$setViewValue(capitalized);
            modelCtrl.$render();
            // set back the cursor after rendering
            element[0].selectionStart = selection;
            element[0].selectionEnd = selection;
          }
          return capitalized;
        }
        modelCtrl.$parsers.push(capitalize);
        capitalize(scope[attrs.ngModel]);
      }
    }});
