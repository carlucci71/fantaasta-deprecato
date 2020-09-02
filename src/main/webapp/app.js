var app = angular.module('app', [ 'ngResource','ngAnimate', 'ngSanitize', 'ui.bootstrap' ]);
app.run(
		function($rootScope, $resource, $interval){
			$rootScope.sezLinkVisible=true;
			$rootScope.sezUtentiVisible=true;
			$rootScope.sezOperaComeVisible=true;
			$rootScope.sezOfferte=true;
			$rootScope.sezLog=true;
			$rootScope.config=false;
			$rootScope.giocatore="";
			$rootScope.offerta=1;
			$rootScope.offertaOC=1;
			$rootScope.durataAsta=10;
			$rootScope.bSemaforoAttivo=true;
			$rootScope.messaggi=[];
			$rootScope.tokenUtente;
			$rootScope.isAdmin=false;
			$rootScope.calciatori=[];
			$rootScope.numeroUtenti=8;
			$rootScope.turno=0;
			$rootScope.tokenDispositiva=-1;
			$rootScope.isATurni=true;
			$rootScope.caricamentoInCorso=false;
			$rootScope.timePing=1000;
			$rootScope.isLoggato= function(){
				if (!$rootScope.giocatore) return false;
				return $rootScope.giocatore!='';
			};
			$rootScope.callDoConnect = function(nome,id, pwd) {
				var esci=false;
				if (pwd != '') {
					$rootScope.origPwd=pwd;
					$rootScope.tmpNpme=nome;
					$rootScope.tmpId=id;
					$rootScope.open(pwd);
				}
				else {
					$rootScope.nomegiocatore=nome;
					$rootScope.idgiocatore=id;
					$rootScope.doConnect();
				}
			}
			$rootScope.doConnect = function() {
		        console.log('Connected');
		        if (!$rootScope.giocatore){
		        	$rootScope.tokenUtente=new Date().getTime();
					$rootScope.sendMsg(JSON.stringify({'operazione':'connetti', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore, 'tokenUtente':$rootScope.tokenUtente}));
		        }
				$rootScope.giocatore=$rootScope.nomegiocatore;
				$rootScope.calcolaIsAdmin();
			}
			$rootScope.utenteScaduto = function(u){
				var ret = false;
				angular.forEach($rootScope.utentiScaduti, function(value,chiave) {
					if(value == u.nome)
						ret = true;
					});
				return ret;
			}
			$rootScope.caricaFile = function(tipoFile){
				$rootScope.caricamentoInCorso=true;
				var f = document.getElementById('file').files[0], r = new FileReader();
                r.onloadend = function(e) {
			    var data = e.target.result;
				$rootScope.tokenDispositiva=Math.floor(Math.random()*(10000)+1);
				$resource('./caricaFile',{}).save({'file':data, 'tipo' : tipoFile,'idgiocatore':$rootScope.idgiocatore,'tokenDispositiva':$rootScope.tokenDispositiva}).$promise.then(function(data) {
					if(data.esitoDispositiva == 'OK'){
						$rootScope.caricamentoInCorso=false;
					}
					else {
						alert('Errore!')
					}
						
					});
			    }
			    r.readAsBinaryString(f);
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
			$rootScope.ordinaUtente= function(u,verso) {
				angular.forEach($rootScope.elencoAllenatori, function(value,chiave) {
					if (verso == 'D'){
						if(u.ordine==value.ordine){
							value.nuovoOrdine=u.ordine+1;
						} else  if(u.ordine+1==value.ordine){
							value.nuovoOrdine=u.ordine;
						} else {
							value.nuovoOrdine=value.ordine;
						}
					}
					if (verso == 'U'){
						if(u.ordine==value.ordine){
							value.nuovoOrdine=u.ordine-1;
						} else  if(u.ordine-1==value.ordine){
							value.nuovoOrdine=u.ordine;
						} else {
							value.nuovoOrdine=value.ordine;
						}
							
					}
				});
				angular.forEach($rootScope.elencoAllenatori, function(value,chiave) {
					value.ordine=value.nuovoOrdine;
				});
				
			}
			$rootScope.aggiornaConfigLega= function(amministratore) {
				var checkNome=[];
				var ok=true;
				angular.forEach($rootScope.elencoAllenatori, function(value,chiave) {
					if(checkNome.indexOf(value.nuovoNome) !== -1) {
						ok=false;
					}					
					checkNome.push(value.nuovoNome);
				});
				if (!ok) {
					alert("Errore!! Nomi non univoci");
				} else {
					$rootScope.tokenDispositiva=Math.floor(Math.random()*(10000)+1);
					$resource('./aggiornaConfigLega',{}).save({'isATurni':$rootScope.isATurni,'elencoAllenatori':$rootScope.elencoAllenatori,'admin':amministratore,'idgiocatore':$rootScope.idgiocatore,'tokenDispositiva':$rootScope.tokenDispositiva}).$promise.then(function(data) {
						if(data.esitoDispositiva == 'OK'){
							if (data.nuovoNomeLoggato){
								$rootScope.nomegiocatore=data.nuovoNomeLoggato;
								$rootScope.giocatore=data.nuovoNomeLoggato;
							}
							if(data.isATurni=="S")
								$rootScope.isATurni=true;
							else
								$rootScope.isATurni=false;
							window.location.href = './index.html';
						}
						else {
							alert('Errore!')
						}
					});
				}
			}
			$rootScope.doDisconnect = function() {
				$rootScope.sendMsg(JSON.stringify({'operazione':'disconnetti', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
				$rootScope.giocatore="";
				$rootScope.isAdmin=false;
			}
			$rootScope.connect = function() {
				 var loc = window.location, new_uri;
	             if (loc.protocol === "https:") {
	                 new_uri = "wss:";
	             } else {
	                 new_uri = "ws:";
	             }
	             new_uri += "//" + loc.host;
//	             new_uri += loc.pathname + "messaggi-websocket";
	             new_uri += '/' + "messaggi-websocket";
	             ws = new WebSocket(new_uri);
                 ws.onmessage = function(data){
					$rootScope.getMessaggio(data.data);
				}
			}
			$rootScope.forzaTurno= function(turno) {
				$rootScope.sendMsg(JSON.stringify({'operazione':'forzaTurno', 'turno':turno,'idgiocatore':$rootScope.idgiocatore}));
			}
			$rootScope.disconnect= function() {
			    if (ws != null) {
			        ws.close();
			    }
			    console.log("Disconnected");
			}
			$resource('./giocatoriLiberi',{}).query().$promise.then(function(data) {
				$rootScope.calciatori=data;
			});
			$rootScope.aggiornaLoggerMessaggi=function(){
				$resource('./elencoLoggerMessaggi',{}).query().$promise.then(function(data) {
					$rootScope.loggerMessaggi=data;
				});
			}
			$rootScope.selezionaAllenatoreOperaCome=function(allenatore){
				$rootScope.idgiocatoreOperaCome=allenatore.id;
				$rootScope.nomegiocatoreOperaCome=allenatore.nome;
				
			}
			$rootScope.cancellaOfferta=function(offerta){
				if (window.confirm("Cancello offerta di:" + offerta.allenatore + " per " + offerta.giocatore + "(" + offerta.ruolo + ") " + offerta.squadra + " vinto a " + offerta.costo)){
					$rootScope.tokenDispositiva=Math.floor(Math.random()*(10000)+1);
					$resource('./cancellaOfferta',{}).save({'offerta':offerta,'idgiocatore':$rootScope.idgiocatore,'tokenDispositiva':$rootScope.tokenDispositiva}).$promise.then(function(data) {
						if(data.esitoDispositiva == 'OK'){
							$rootScope.cronologiaOfferte=data.ret;
						}
						else {
							alert('Errore!')
						}
					});
				}
			}
			$rootScope.aggiornaCronologiaOfferte=function(){
				$resource('./elencoCronologiaOfferte',{}).query().$promise.then(function(data) {
					$rootScope.cronologiaOfferte=data;
				});
			}
			$rootScope.selezionaCalciatore=function(calciatore){
				$rootScope.selCalciatore=calciatore.id+'@'+calciatore.nome;
				$rootScope.selCalciatoreId=calciatore.id;
				$rootScope.selCalciatoreRuolo=calciatore.ruolo;
				$rootScope.selCalciatoreNome=calciatore.nome;
				$rootScope.selCalciatoreSquadra=calciatore.squadra;
			}
			$rootScope.confermaConfigIniziale=function(){
				if ($rootScope.numeroUtenti>0){
					$resource('./inizializzaLega',{}).save({'numUtenti':$rootScope.numeroUtenti,'isATurni':$rootScope.isATurni}).$promise.then(function(data) {
						if(data.esitoDispositiva == 'OK'){
							$rootScope.inizializza(false);
							if(data.isATurni=="S")
								$rootScope.isATurni=true;
							else
								$rootScope.isATurni=false;
						}
						else {
							alert('Errore!')
						}
					});
				}
			}
			$rootScope.ritornaIndex=function(){
				window.location.href = './index.html';
			}
			$rootScope.caricaAdmin=function(){
				window.location.href = './admin.html';
			}
			$rootScope.azzera=function(){
				if (window.confirm("Sicuro??????????? CANCELLERAI TUTTO IL DB")){
					$rootScope.tokenDispositiva=Math.floor(Math.random()*(10000)+1);
					$resource('./azzera',{}).save({'conferma':'S','idgiocatore':$rootScope.idgiocatore,'tokenDispositiva':$rootScope.tokenDispositiva}).$promise.then(function(data) {
						if(data.esitoDispositiva == 'OK'){
							$rootScope.sendMsg(JSON.stringify({'operazione':'azzera', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
							$rootScope.inizializza(true);
						}
						else {
							alert('Errore!')
						}

					});
				}
			}
			$rootScope.inizializza=function(chiudi){
				$resource('./init',{}).get().$promise.then(function(data) {
					if (data.DA_CONFIGURARE){
						$rootScope.config=true;
						if(chiudi){
							window.location.href = './index.html';
						}
					} else {
						$rootScope.config=false;
						$rootScope.connect();
						$rootScope.giocatore=data.giocatoreLoggato;
						if ($rootScope.giocatore){
							$rootScope.nomegiocatore=$rootScope.giocatore;
							$rootScope.idgiocatore=data.idLoggato;
							$rootScope.doConnect();
							$rootScope.pinga();
						}
						if(data.isATurni=="S")
							$rootScope.isATurni=true;
						else
							$rootScope.isATurni=false;
						$rootScope.turno=data.turno;
						$rootScope.nomeGiocatoreTurno=data.nomeGiocatoreTurno;
						$rootScope.elencoAllenatori=data.elencoAllenatori;
						$rootScope.aggiornaTimePing();
					}
				});
			}
			$rootScope.inizializza(false);
			$rootScope.sendMsgORIG=function(s){
				try {
					ws.send(s);
	            } catch (error) {
	                	console.log("Errore invio messaggio:" + s);
	            }				
			}
			/* */
			$rootScope.sendMsg=function (message, callback) {
				$rootScope.waitForConnection(function () {
					ws.send(message);
			        if (typeof callback !== 'undefined') {
			          callback();
			        }
			    }, 1000);
			};
			$rootScope.waitForConnection = function (callback, interval) {
			    if (ws.readyState === 1) {
			        callback();
			    } else {
			        var that = this;
			        // optional: implement backoff for interval here
			        setTimeout(function () {
			            that.waitForConnection(callback, interval);
			        }, interval);
			    }
			};			
			/* */
			
			
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
					if (msg.azzera){
						$resource('./cancellaSessioneNomeUtente',{}).save().$promise.then(function(data) {
							$rootScope.nomegiocatore="";
							$rootScope.giocatore="";
							$rootScope.elencoAllenatori=[];
							
						});
					}
					if (msg.isATurni){
						if (msg.isATurni=="S")
							$rootScope.isATurni=true;
						else
							$rootScope.isATurni=false;
					}
					if (msg.elencoAllenatori){
						$rootScope.elencoAllenatori=msg.elencoAllenatori;
					}
					if (msg.utentiRinominati){
						angular.forEach(msg.utentiRinominati, function(nuovoNome,vecchioNome) {
							if ($rootScope.nomegiocatore==vecchioNome){
								$rootScope.nomegiocatore=nuovoNome;
								$rootScope.giocatore=nuovoNome;
								$resource('./aggiornaSessioneNomeUtente',{}).save({'nuovoNome':nuovoNome}).$promise.then(function(data) {
								});
								
							}
						});
					}
					if (msg.verificaDispositiva){
						if (msg.verificaDispositiva==$rootScope.idgiocatore){
							if($rootScope.tokenDispositiva>=0){
								$rootScope.sendMsg(JSON.stringify({'operazione':'verificaDispositiva', 'tokenDispositiva':$rootScope.tokenDispositiva, 'idgiocatore':$rootScope.idgiocatore}));
								$rootScope.tokenDispositiva=-1;
							}
						}
					}
					if (msg.calciatori){
						$rootScope.calciatori=msg.calciatori;
					}
					if (msg.cronologiaOfferte){
						$rootScope.cronologiaOfferte=msg.cronologiaOfferte;
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
					if (msg.clearOfferta){
						$rootScope.clearOfferta();
					}
					if (msg.giocatoreTimeout){
						$rootScope.giocatoreTimeout=msg.giocatoreTimeout;
					}
					if (msg.millisFromPausa){
						$rootScope.millisFromPausa=msg.millisFromPausa;
					}
					if (msg.timeout){
						if (msg.timeout=='N') $rootScope.timeout=null; else $rootScope.timeout=msg.timeout;
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
					if(msg.turno){
						$rootScope.turno=msg.turno;
					}
					if(msg.nomeGiocatoreTurno){
						$rootScope.nomeGiocatoreTurno=msg.nomeGiocatoreTurno;
					}
					if (msg.utentiScaduti){
						$rootScope.utentiScaduti=msg.utentiScaduti;
					}
					$rootScope.$apply();
				}
			}
			$rootScope.pinga = function(){
				console.log("PING:" + $rootScope.timePing);
				if ($rootScope.giocatore)
					$rootScope.sendMsg(JSON.stringify({'operazione':'ping', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
			}
			$rootScope.aggiornaTimePing= function() {
				console.log("AGGIORNO");
				$interval.cancel(a);
				a=$interval(function() {$rootScope.pinga();}, $rootScope.timePing);
			}
			var a=$interval(function() {$rootScope.pinga();}, $rootScope.timePing);
			
			$rootScope.start = function(){
				$rootScope.inizia($rootScope.nomegiocatore,$rootScope.idgiocatore);
			}
			$rootScope.liberaSemaforo = function() {
				$rootScope.bSemaforoAttivo=true;
				$rootScope.sendMsg(JSON.stringify({'operazione':'liberaSemaforo'}));
			}
			$rootScope.inizia = function(ng,ig) {
				$rootScope.bSemaforoAttivo=false;
				$rootScope.timeStart=0;
				$rootScope.contaTempo=0;
				$rootScope.sendMsg(JSON.stringify({'operazione':'start', 'selCalciatore':$rootScope.selCalciatore, 'nomegiocatoreOperaCome':$rootScope.nomegiocatore, 'idgiocatoreOperaCome':$rootScope.idgiocatore,'nomegiocatore':ng,'idgiocatore':ig, 'durataAsta':$rootScope.durataAsta}));
				$rootScope.selCalciatore="";
			}
			$rootScope.azzeraTempo=function(){
						$rootScope.sendMsg(JSON.stringify({'operazione':'azzeraTempo', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
			};
			$rootScope.conferma = function(){
				$rootScope.messaggi=[];
				$rootScope.bSemaforoAttivo=true;
				$rootScope.tokenDispositiva=Math.floor(Math.random()*(10000)+1);
				$resource('./confermaAsta',{}).save({'offerta':$rootScope.offertaVincente,'idgiocatore':$rootScope.idgiocatore,'tokenDispositiva':$rootScope.tokenDispositiva}).$promise.then(function(data) {
					if(data.esitoDispositiva == 'OK'){
						$rootScope.sendMsg(JSON.stringify({'operazione':'confermaAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
					}
					else {
						alert('Errore!')
					}
				});
			}
			$rootScope.clearOfferta=function(){
				$rootScope.offertaVincente="";
				$rootScope.filterRuolo="";
				$rootScope.filterNome="";
				$rootScope.filterSquadra="";
				$rootScope.filterQuotazione="";
				$rootScope.selCalciatore="";
				$rootScope.selCalciatoreId="";
				$rootScope.selCalciatoreRuolo="";
				$rootScope.selCalciatoreNome="";
				$rootScope.selCalciatoreSquadra="";
			}
			$rootScope.annulla = function(){
				if (window.confirm("Annullo offerta di:" + $rootScope.offertaVincente.nomegiocatore + " per " + $rootScope.offertaVincente.giocatore.nome + "(" + $rootScope.offertaVincente.giocatore.ruolo + ") " + $rootScope.offertaVincente.giocatore.squadra + " vinto a " + $rootScope.offertaVincente.offerta)){
					$rootScope.messaggi=[];
					$rootScope.bSemaforoAttivo=true;
					$rootScope.sendMsg(JSON.stringify({'operazione':'annullaAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
				}
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
			$rootScope.resumeAsta= function() {
				$rootScope.sendMsg(JSON.stringify({'operazione':'resumeAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
			}
			$rootScope.pausaAsta= function() {
				$rootScope.sendMsg(JSON.stringify({'operazione':'pausaAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
			}
			
			$rootScope.calcolaIsAdmin= function() {
				$rootScope.isAdmin=false;
				angular.forEach($rootScope.elencoAllenatori, function(value,chiave) {
					if(value.id == $rootScope.idgiocatore)
						if(value.isAdmin) $rootScope.isAdmin=true;
					});
			}
			$rootScope.$watch("elencoAllenatori", function(newValue, oldValue) {
				$rootScope.calcolaIsAdmin();
			});
			
			$rootScope.sort = {
				    column: 'Ruolo',
				    descending: false
				};
			$rootScope.selectedCls = function(column) {
				    return column == $rootScope.sort.column && 'sort-' + $rootScope.sort.descending;
				};

				$rootScope.changeSorting = function(column) {
				    var sort = $rootScope.sort;
				    if (sort.column == column) {
				        sort.descending = !sort.descending;
				    } else {
				        sort.column = column;
				        sort.descending = false;
				    }
				};			
			
			
			
			
/*			
			$rootScope.$watch("selAllenatore", function(newValue, oldValue) {
				if (newValue){
					var posToken = newValue.indexOf("@");
					$rootScope.idgiocatore=newValue.substr(0,posToken);
					$rootScope.nomegiocatore=newValue.substr(posToken+1);
				}
			});
			*/
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
app.controller('ModalDemoCtrl', function ($uibModal, $log, $rootScope) {
	  var pc = this;
	  pc.data = "..."; 

	  $rootScope.open = function (size,pwd) {
	    var modalInstance = $uibModal.open({
	      animation: true,
	      ariaLabelledBy: 'modal-title',
	      ariaDescribedBy: 'modal-body',
	      templateUrl: 'modale.html',
	      controller: 'ModalInstanceCtrl',
	      controllerAs: 'pc',
	      size: size,
	      resolve: {
	        data: function () {
	          return pwd;
	        }
	      }
	    });
		pc.data=pwd;

	    modalInstance.result.then(function () {
//	      alert("now I'll close the modal");
	    });
	  };
	});

app.controller('ModalInstanceCtrl', function ($uibModalInstance, data, $rootScope, $resource) {
	  var pc = this;
	  pc.data = data;
	  
	  pc.ok = function (modalePwd) {
		  
			$resource('./cripta',{}).get({'pwd':modalePwd,'key':$rootScope.tmpNpme}).$promise.then(function(data) {
				pc.data="CONTROLLO PASSWORD...";
				if (data.value==$rootScope.origPwd){
					$rootScope.nomegiocatore=$rootScope.tmpNpme;
					$rootScope.idgiocatore=$rootScope.tmpId;
					$rootScope.doConnect();
				    $uibModalInstance.close();
		        }
				else{
					pc.data="PASSWORD ERRATA";
				}
			});
		  
		  
	  };

	  pc.cancel = function () {
	    //{...}
//	    alert("You clicked the cancel button."); 
	    $uibModalInstance.dismiss('cancel');
	  };
	});

app.filter('myTableFilter', function($rootScope){
	  return function(dataArray) {
	      if (!dataArray) {
	          return;
	      }
	      else {
	           return dataArray.filter(function(item){
	              var termInRuolo=true;
	              if($rootScope.filterRuolo) termInRuolo=item.ruolo.toLowerCase().indexOf($rootScope.filterRuolo.toLowerCase()) > -1;
	              var termInNome=true;
	              if($rootScope.filterNome) termInNome = item.nome.toLowerCase().indexOf($rootScope.filterNome.toLowerCase()) > -1;
	              var termInSquadra=true;
	              if($rootScope.filterSquadra) termInSquadra = item.squadra.toLowerCase().indexOf($rootScope.filterSquadra.toLowerCase()) > -1;
	              var termInQuotazione=true;
	              if($rootScope.filterQuotazione >0) termInQuotazione = item.quotazione >= $rootScope.filterQuotazione;
	              if($rootScope.filterQuotazione <0){
	            	  var tmp=-$rootScope.filterQuotazione;
	            	  termInQuotazione = item.quotazione <= tmp;
	              }
	              return termInRuolo && termInNome && termInSquadra && termInQuotazione;
	              
	              
	           });
	      } 
	  }    
	});
