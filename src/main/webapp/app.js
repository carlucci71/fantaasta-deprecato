var app=angular.module("app",["ngResource"]);
var app = angular.module('app', [ 'ngResource' ]);
app.run(
		function($rootScope, $resource, $interval){
			$rootScope.giocatore="";
			$rootScope.offerta=1;
			$rootScope.durataAsta=10;
			$rootScope.bSemaforoAttivo=true;
			$rootScope.messaggi=[];
			$rootScope.tokenUtente;
			$rootScope.isLoggato= function(){
				if (!$rootScope.giocatore) return false;
				return $rootScope.giocatore!='';
			};
			$rootScope.doConnect = function() {
		        console.log('Connected');
		        if (!$rootScope.giocatore){
		        	$rootScope.tokenUtente=new Date().getTime();
					$rootScope.sendMsg(JSON.stringify({'operazione':'connetti', 'nomegiocatore':$rootScope.nomegiocatore, 'tokenUtente':$rootScope.tokenUtente}));
					$rootScope.giocatore=$rootScope.nomegiocatore;
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
				$rootScope.sendMsg(JSON.stringify({'operazione':'disconnetti', 'nomegiocatore':$rootScope.nomegiocatore}));
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
			$resource(window.location.pathname + 'init',{}).get().$promise.then(function(data) {
				$rootScope.connect();
				$rootScope.giocatore=data.giocatore;
				if ($rootScope.giocatore){
					$rootScope.nomegiocatore=$rootScope.giocatore;
					$rootScope.doConnect();
				}

				$rootScope.utenti=data.utenti;
			});
			$rootScope.sendMsg=function(s){
				ws.send(s);
			}
			$rootScope.getMessaggio = function(message){
				if (message){
					var msg = JSON.parse(message);
					console.log(msg);
					if (msg.messaggi){
						//$rootScope.messaggi.push(msg.messaggio);
						$rootScope.messaggi=msg.messaggi;
					}
					if (msg.utenti){
						$rootScope.utenti=msg.utenti;
					}
					if (msg.RESET_UTENTE){
						if (msg.RESET_UTENTE==$rootScope.tokenUtente){
							$rootScope.giocatore="";
							alert("Utente esistente. Riconnettiti!");
						}
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
					if($rootScope.timeStart==3 && $rootScope.offertaVincente.nomegiocatore==$rootScope.giocatore) $rootScope.stileScritta='finitoVincente';
					if($rootScope.timeStart==3 && $rootScope.offertaVincente.nomegiocatore!=$rootScope.giocatore) $rootScope.stileScritta='finitoPerdente';
					if($rootScope.timeStart<3 && $rootScope.offertaVincente.nomegiocatore==$rootScope.giocatore) $rootScope.stileScritta='correnteVincente';
					if($rootScope.timeStart<3 && $rootScope.offertaVincente.nomegiocatore!=$rootScope.giocatore) $rootScope.stileScritta='correntePerdente';
				}
			}
			$rootScope.pinga = function(){
				$rootScope.sendMsg(JSON.stringify({'operazione':'ping', 'nomegiocatore':$rootScope.nomegiocatore}));
			}
			var a = $interval(function() {
				$rootScope.pinga();
	          }, 1000);
			$rootScope.start = function(){
				$rootScope.bSemaforoAttivo=false;
				$rootScope.timeStart=0;
				$rootScope.contaTempo=0;
				$rootScope.sendMsg(JSON.stringify({'operazione':'start', 'nomegiocatore':$rootScope.nomegiocatore, 'bSemaforoAttivo':$rootScope.bSemaforoAttivo, 'durataAsta':$rootScope.durataAsta}));
			}
			$rootScope.conferma = function(){
				$rootScope.messaggi=[];
				$rootScope.bSemaforoAttivo=true;
				$rootScope.offertaVincente="";
				$rootScope.sendMsg(JSON.stringify({'operazione':'ripristinaSemaforoAttivo', 'nomegiocatore':$rootScope.nomegiocatore}));
			}
			$rootScope.allinea = function(){
				$rootScope.offerta=$rootScope.offertaVincente.offerta;
			}
			$rootScope.inviaOfferta = function(){
				$rootScope.sendMsg(JSON.stringify({'operazione':'inviaOfferta', 'nomegiocatore':$rootScope.nomegiocatore, 'offerta':$rootScope.offerta}));
			}
			$rootScope.aggiornaDurataAsta = function(){
				$rootScope.sendMsg(JSON.stringify({'operazione':'aggiornaDurataAsta', 'giocatoreDurataAsta':$rootScope.nomegiocatore, 'durataAsta':$rootScope.durataAsta}));
			}
			$rootScope.cancellaUtente = function(u) {
				$rootScope.sendMsg(JSON.stringify({'operazione':'cancellaUtente', 'nomegiocatore':u}));
			}
			$rootScope.inizia = function(u) {
				$rootScope.sendMsg(JSON.stringify({'operazione':'start', 'nomegiocatore':u, 'durataAsta':$rootScope.durataAsta}));
			}
			$rootScope.incrementa = function(inc) {
				$rootScope.offerta=$rootScope.offerta+inc;
				$rootScope.inviaOfferta();
//				$rootScope.sendMsg(JSON.stringify({'operazione':'inviaOfferta', 'nomegiocatore':$rootScope.nomegiocatore, 'offerta':$rootScope.offertaVincente.offerta+inc}));
			}
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
        capitalize(scope[attrs.ngModel]); // capitalize initial value
      }
    }});
$rootScope.pinga();
