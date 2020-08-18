var app = angular.module('app', [ 'ngResource','ngAnimate', 'ngSanitize', 'ui.bootstrap' ]);
app.run(
		function($rootScope, $resource, $interval){
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
			$rootScope.isLoggato= function(){
				if (!$rootScope.giocatore) return false;
				return $rootScope.giocatore!='';
			};
			$rootScope.confermaCreazioneLega = function (){
				$rootScope.logCrea='';
				$rootScope.logSelezionaLega='';
				$resource('./creaLega',{}).save({'idgiocatore':$rootScope.idgiocatore,'pwdLegaCreata':$rootScope.pwdLegaCreata1,'nomeLegaCreata':$rootScope.nomeLegaCreata, 'numeroUtenti' : $rootScope.numeroUtenti}).$promise.then(function(data) {
						if (data.stato=='OK'){
							$rootScope.logCrea='Lega creata';
							$rootScope.elencoLeghe=data.elencoLeghe;
						}
						else {
							$rootScope.logCrea=data.stato;
						}
				});
			}
			$rootScope.registrazioneUtente = function (){
				$rootScope.logRegistra="";
				$rootScope.logLogin="";
				$resource('./registra',{}).save({'registraUtente':$rootScope.registraUtente, 'registraPwd' : $rootScope.registraPwd1}).$promise.then(function(data) {
						if (data.stato=='OK'){
							$rootScope.logRegistra='Utente registrato';
						}
						else {
							$rootScope.logRegistra=data.stato;
						}
				});
			}
			$rootScope.accessoUtente = function (){
				$rootScope.logRegistra="";
				$rootScope.logLogin="";
				$resource('./login',{}).save({'loginUtente':$rootScope.loginUtente, 'loginPwd' : $rootScope.loginPwd}).$promise.then(function(data) {
						if (data.stato=='OK'){
							$rootScope.nomegiocatore=data.allenatore.nome;
							$rootScope.idgiocatore=data.allenatore.id;
							$rootScope.doConnect();
						}
						else {
							$rootScope.logLogin=data.stato;
						}
				});
			}
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
					$rootScope.sendMsg(JSON.stringify({'legaUtente':$rootScope.legaUtente,'operazione':'connetti', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore, 'tokenUtente':$rootScope.tokenUtente}));
		        }
				$rootScope.giocatore=$rootScope.nomegiocatore;
				$rootScope.calcolaIsAdmin();
//				if ($rootScope.isAdmin) $rootScope.selAllenatoreOperaCome=1 + "@" + "Daniele";
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
				 var f = document.getElementById('file').files[0],
			        r = new FileReader();

			    r.onloadend = function(e) {
			      var data = e.target.result;
					$resource('./caricaFile',{}).save({'legaUtente':$rootScope.legaUtente,'file':data, 'tipo' : tipoFile}).$promise.then(function(ret) {
						
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
			$rootScope.aggiornaUtenti= function() {
				$resource('./aggiornaUtenti',{}).save({'elencoAllenatori':$rootScope.elencoAllenatori,'legaUtente':$rootScope.legaUtente}).$promise.then(function(data) {
					window.location.href = './index.html';
				});
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
			$rootScope.disconnect= function() {
			    if (ws != null) {
			        ws.close();
			    }
			    console.log("Disconnected");
			}
			$rootScope.$watch("legaUtente", function(newValue, oldValue) {
				if ($rootScope.legaUtente){
					$resource('./giocatoriLiberi',{}).query({'legaUtente':$rootScope.legaUtente}).$promise.then(function(data) {
						$rootScope.calciatori=data;
					});
				}
			});
			/*
			$rootScope.aggiornaCronologiaOfferte=function(){
				$resource('./elencoCronologiaOfferte',{}).query().$promise.then(function(data) {
					$rootScope.cronologiaOfferte=data;
				});
			}
			*/
			$rootScope.exitLega=function(){
				$rootScope.legaUtente='';
				$rootScope.nomeLegaUtente='';
				$rootScope.elencoAllenatori=[];
			}
			$rootScope.selezionaLega=function(){
				$rootScope.logCrea='';
				$rootScope.logSelezionaLega='';
				$resource('./selezionaLega',{}).save({'idgiocatore':$rootScope.idgiocatore,'legaUtente':$rootScope.selLega,'legaPwd':$rootScope.legaPwd}).$promise.then(function(data) {
					if (data.stato=='OK'){
						$rootScope.legaUtente=data.legaUtente;
						$rootScope.aliasGiocatore=data.aliasGiocatore;
						$rootScope.nomeLegaUtente=data.nomeLegaUtente;
						$rootScope.elencoAllenatori=data.elencoAllenatori;
					}
					else {
						$rootScope.logSelezionaLega=data.stato;
					}
				});
				
			}
			$rootScope.confermaNumUtenti=function(){
				if ($rootScope.numeroUtenti>0){
				$resource('./aggiornaNumUtenti',{}).save($rootScope.numeroUtenti).$promise.then(function(data) {
					window.location.href = './admin.html';
				});
				}
			}
			$resource('./init',{}).get().$promise.then(function(data) {
//				if (data.DA_CONFIGURARE){
//					$rootScope.config=true;
//				} else {
					$rootScope.connect();
					$rootScope.giocatore=data.giocatoreLoggato;
					$rootScope.legaUtente=data.legaUtente;
					$rootScope.nomeLegaUtente=data.nomeLegaUtente;
					if ($rootScope.giocatore){
						$rootScope.nomegiocatore=$rootScope.giocatore;
						$rootScope.aliasGiocatore=data.aliasGiocatore;
						$rootScope.idgiocatore=data.idLoggato;
						$rootScope.doConnect();
					}

					$rootScope.elencoAllenatori=data.elencoAllenatori;
					$rootScope.elencoLeghe=data.elencoLeghe;
//				}
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
				$rootScope.sendMsg(JSON.stringify({'operazione':'start','legaUtente':$rootScope.legaUtente, 'selCalciatore':$rootScope.selCalciatore, 'nomegiocatoreOperaCome':$rootScope.nomegiocatore, 'idgiocatoreOperaCome':$rootScope.idgiocatore,'nomegiocatore':ng,'idgiocatore':ig, 'bSemaforoAttivo':$rootScope.bSemaforoAttivo, 'durataAsta':$rootScope.durataAsta}));
				$rootScope.selCalciatore="";
			}
			$rootScope.conferma = function(){
				$rootScope.messaggi=[];
				$rootScope.bSemaforoAttivo=true;
				$resource('./confermaAsta',{}).save($rootScope.offertaVincente).$promise.then(function(data) {
					$rootScope.sendMsg(JSON.stringify({'legaUtente':$rootScope.legaUtente,'operazione':'confermaAsta', 'nomegiocatore':$rootScope.nomegiocatore, 'idgiocatore':$rootScope.idgiocatore}));
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
				$rootScope.sendMsg(JSON.stringify({'operazione':'inviaOfferta', 'legaUtente':$rootScope.legaUtente, 'nomegiocatore':ng, 'idgiocatore':ig, 'nomegiocatoreOperaCome':$rootScope.nomegiocatore, 'idgiocatoreOperaCome':$rootScope.idgiocatore, 'offerta':off}));
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
	    	  console.log();
	           return dataArray.filter(function(item){
	              var termInRuolo=true;
	              if($rootScope.filterRuolo) termInRuolo=item.ruolo.toLowerCase().indexOf($rootScope.filterRuolo.toLowerCase()) > -1;
	              var termInNome=true;
	              if($rootScope.filterNome) termInNome = item.nome.toLowerCase().indexOf($rootScope.filterNome.toLowerCase()) > -1;
	              var termInSquadra=true;
	              if($rootScope.filterSquadra) termInSquadra = item.squadra.toLowerCase().indexOf($rootScope.filterSquadra.toLowerCase()) > -1;
	              var termInQuotazione=true;
	              if($rootScope.filterQuotazione) termInQuotazione = item.quotazione >= $rootScope.filterQuotazione;
	              return termInRuolo && termInNome && termInSquadra && termInQuotazione;
	              
	              
	           });
	      } 
	  }    
	});
