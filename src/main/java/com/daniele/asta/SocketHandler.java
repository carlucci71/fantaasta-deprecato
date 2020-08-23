package com.daniele.asta;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.daniele.asta.entity.Allenatori;
import com.daniele.asta.entity.Giocatori;
import com.daniele.asta.entity.Logger;
import com.daniele.asta.repository.GiocatoriRepository;
import com.daniele.asta.repository.LoggerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SocketHandler extends TextWebSocketHandler implements WebSocketHandler {
	
	private void creaMessaggio(String messaggio) {
		Long now = System.currentTimeMillis();
		UUID uuid = UUID.randomUUID();
		Map<String, Object> msg = new HashMap<>();
		msg.put("key",uuid.toString());
		msg.put("data",now);
		msg.put("testo", messaggio);
		messaggi.add(msg);
		Logger entity= new Logger();
		entity.setId(now);
		entity.setMessaggio(messaggio);
		loggerRepository.save(entity);
	}
	
	List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
	List<String> utentiLoggati = new ArrayList<>();
	List<String> utentiScaduti=new ArrayList<>();
	Map<String, Map<String, Object>> pingUtenti = new HashMap<>();
	Map<String, Object> offertaVincente = new HashMap<>();
	Calendar calInizioOfferta;
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ssZ");
	int durataAsta;
	String idCalciatore;
	String timeOut="N";
	String nomeCalciatore;
	Long millisFromPausa;
	String giocatoreTimeout;
	String giocatoreDurataAsta="";
	String sSemaforoAttivo;
	
	@Autowired LoggerRepository loggerRepository;
	List<Map<String,Object>> messaggi=new ArrayList<>();
	@Autowired MyController myController;
	@Autowired GiocatoriRepository giocatoriRepository;
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws InterruptedException, IOException {
		HttpSession httpSession = (HttpSession) session.getAttributes().get("HTTPSESSIONID");
		String payload = message.getPayload();
		Map<String, Object> jsonToMap = jsonToMap(payload);
		String operazione = (String) jsonToMap.get("operazione");
		if (operazione != null && operazione.equals("cancellaUtente")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			Integer iIdgiocatore = (Integer) jsonToMap.get("idgiocatore");
			utentiLoggati.remove(nomegiocatore);
			utentiScaduti.remove(nomegiocatore);
			pingUtenti.remove(nomegiocatore);
			Map<String, Object> m = new HashMap<>();
			m.put("utenti", utentiLoggati);
			creaMessaggio("Utente cancellato: " + nomegiocatore);
			m.put("messaggi", messaggi);
			invia(toJson(m));
		}
		if (operazione != null && operazione.equals("azzera")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = jsonToMap.get("idgiocatore").toString();
			creaMessaggio("AZZERATO DA: " + nomegiocatore);
		}
		if (operazione != null && operazione.equals("connetti")) {
//			messaggi = new ArrayList<>();
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = jsonToMap.get("idgiocatore").toString();
			Long tokenUtente = (Long)jsonToMap.get("tokenUtente");
			Map<String, Object> m = new HashMap<>();
			if (utentiScaduti.contains(nomegiocatore)) {
				utentiLoggati.remove(nomegiocatore);
				utentiScaduti.remove(nomegiocatore);
				pingUtenti.remove(nomegiocatore);
			}
			if (utentiLoggati != null && utentiLoggati.contains(nomegiocatore)) {
				creaMessaggio("Sessione RUBATA da " + nomegiocatore);
			} 
			httpSession.setAttribute("nomeGiocatoreLoggato", nomegiocatore);
			httpSession.setAttribute("idLoggato", idgiocatore);
			utentiLoggati.add(nomegiocatore);
			m.put("calciatori", myController.getGiocatoriLiberi());
			m.put("cronologiaOfferte", myController.elencoCronologiaOfferte());
			m.put("utenti", utentiLoggati);
			creaMessaggio("Connesso: " + nomegiocatore);
			m.put("messaggi", messaggi);
			m.put("cronologiaOfferte", myController.elencoCronologiaOfferte());
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("confermaAsta")) {
			sSemaforoAttivo="S";
			messaggi = new ArrayList<>();
			creaMessaggio("Asta confermata per " + offertaVincente.get("nomeCalciatore") + "(" + ((Giocatori)offertaVincente.get("giocatore")).getRuolo()  + ") " + ((Giocatori)offertaVincente.get("giocatore")).getSquadra() + ". Assegnato a " + offertaVincente.get("nomegiocatore") + " per " + offertaVincente.get("offerta"));
			offertaVincente = new HashMap<>();
			Map<String, Object> m = new HashMap<>();
			m.put("calciatori", myController.getGiocatoriLiberi());
			m.put("cronologiaOfferte", myController.elencoCronologiaOfferte());
			m.put("selCalciatore", "x");
			m.put("messaggi", messaggi);
			Integer iTurno=Integer.parseInt(myController.getTurno());
			Iterable<Allenatori> allAllenatori = myController.getAllAllenatori();
			Integer conta=0;
			iTurno++;
			String nomeFirst=null;
			for (Allenatori allenatori : allAllenatori) {
				if(nomeFirst==null) {
					nomeFirst=allenatori.getNome();
				}
				if(allenatori.getOrdine()==iTurno) {
					myController.setNomeGiocatoreTurno(allenatori.getNome());
				}
				conta++;
			}
			if(iTurno>conta-1) {
				iTurno=0;
				myController.setNomeGiocatoreTurno(nomeFirst);
			}
			myController.setTurno(Integer.toString(iTurno));
			m.put("turno", myController.getTurno());
			m.put("nomeGiocatoreTurno", myController.getNomeGiocatoreTurno());
			invia(toJson(m));
		}		
		else if (operazione != null && operazione.equals("annullaAsta")) {
			sSemaforoAttivo="S";
			messaggi = new ArrayList<>();
			creaMessaggio("Asta annullata per:" + offertaVincente.get("nomeCalciatore"));
			offertaVincente = new HashMap<>();
			Map<String, Object> m = new HashMap<>();
			m.put("selCalciatore", "x");
			m.put("messaggi", messaggi);
			invia(toJson(m));
		}		
		else if (operazione != null && operazione.equals("resumeAsta")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			Calendar now = Calendar.getInstance();
			calInizioOfferta.setTimeInMillis(now.getTimeInMillis() - millisFromPausa);
			Map<String, Object> m = new HashMap<>();
			creaMessaggio("Offerta tolta dalla pausa da " + nomegiocatore + " per " + offertaVincente.get("nomeCalciatore") + ". Riparte dopo " + millisFromPausa + " millisecondi");
			timeOut="N";
			m.put("timeout", timeOut);
			m.put("contaTempo", now.getTimeInMillis() - calInizioOfferta.getTimeInMillis());
			m.put("messaggi", messaggi);
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("pausaAsta")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			Calendar now = Calendar.getInstance();
			millisFromPausa=now.getTimeInMillis()-calInizioOfferta.getTimeInMillis();
			calInizioOfferta.set(Calendar.YEAR, 2971);
			Map<String, Object> m = new HashMap<>();
			creaMessaggio("Offerta messa in pausa da da " + nomegiocatore + " per " + offertaVincente.get("nomeCalciatore") + " dopo " + millisFromPausa + " millisecondi");
			timeOut="S";
			m.put("millisFromPausa", millisFromPausa);
			giocatoreTimeout=nomegiocatore;
			m.put("giocatoreTimeout", giocatoreTimeout);
			m.put("timeout", timeOut);
			m.put("messaggi", messaggi);
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("terminaAsta")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = jsonToMap.get("idgiocatore").toString();
			calInizioOfferta.set(Calendar.YEAR, 1971);
			Map<String, Object> m = new HashMap<>();
			creaMessaggio("Offerta terminata in anticipo da " + nomegiocatore + " per " + offertaVincente.get("nomeCalciatore"));
			m.put("messaggi", messaggi);
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("liberaSemaforo")) {
			sSemaforoAttivo="S";
		}
		else if (operazione != null && operazione.equals("start")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = jsonToMap.get("idgiocatore").toString();
			String nomegiocatoreOperaCome = (String) jsonToMap.get("nomegiocatoreOperaCome");
			String idgiocatoreOperaCome = jsonToMap.get("idgiocatoreOperaCome").toString();
			durataAsta = (Integer) jsonToMap.get("durataAsta");
			String selCalciatore = (String)jsonToMap.get("selCalciatore");
			String[] split = selCalciatore.split("@");
			idCalciatore=split[0];
			nomeCalciatore=split[1];
			sSemaforoAttivo="N";
			calInizioOfferta = Calendar.getInstance();
			offertaVincente = new HashMap<>();
			offertaVincente.put("giocatore", giocatoriRepository.findOne(Integer.parseInt(idCalciatore)));
			offertaVincente.put("nomegiocatore", nomegiocatore);
			offertaVincente.put("idgiocatore", idgiocatore);
			offertaVincente.put("offerta", 1);
			offertaVincente.put("nomeCalciatore", nomeCalciatore);
			offertaVincente.put("idCalciatore", idCalciatore);
			
			Map<String, Object> m = new HashMap<>();
			m.put("offertaVincente", offertaVincente);
			String str = "Asta avviata da " + nomegiocatore + " per " + offertaVincente.get("nomeCalciatore") + "(" + ((Giocatori)offertaVincente.get("giocatore")).getRuolo()  + ") " + ((Giocatori)offertaVincente.get("giocatore")).getSquadra();
			if(!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
				str = str + "(" + nomegiocatoreOperaCome + ")";
			}
			messaggi=new ArrayList<>();
			creaMessaggio(str);
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("disconnetti")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = jsonToMap.get("idgiocatore").toString();
			utentiLoggati.remove(nomegiocatore);
			utentiScaduti.remove(nomegiocatore);
			pingUtenti.remove(nomegiocatore);
			httpSession.removeAttribute("nomeGiocatoreLoggato");
			httpSession.removeAttribute("idLoggato");
			Map<String, Object> m = new HashMap<>();
			creaMessaggio("Utente disconnesso: " + nomegiocatore);
			m.put("utenti", utentiLoggati);
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("aggiornaDurataAsta")) {
			giocatoreDurataAsta = (String) jsonToMap.get("giocatoreDurataAsta");
			durataAsta = (Integer) jsonToMap.get("durataAsta");
			Map<String, Object> m = new HashMap<>();
			m.put("durataAsta", durataAsta);
			m.put("giocatoreDurataAsta", giocatoreDurataAsta);
			creaMessaggio("Durata asta modificata da: " + giocatoreDurataAsta);
			invia(toJson(m));
			
		}
		else if (operazione != null && operazione.equals("inviaOfferta")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = jsonToMap.get("idgiocatore").toString();
			String nomegiocatoreOperaCome = (String) jsonToMap.get("nomegiocatoreOperaCome");
			Integer offerta = (Integer) jsonToMap.get("offerta");
			Integer attOfferta = (Integer) offertaVincente.get("offerta");
			Calendar now = Calendar.getInstance();
			Calendar scadenzaAsta = Calendar.getInstance();
			scadenzaAsta.setTimeInMillis(calInizioOfferta.getTimeInMillis());
			scadenzaAsta.add(Calendar.SECOND, durataAsta);
			Map<String, Object> m = new HashMap<>();
			if (now.after(scadenzaAsta)) {
				String str = "Rilancio di " + nomegiocatore + " per " + offertaVincente.get("nomeCalciatore") + "(" + ((Giocatori)offertaVincente.get("giocatore")).getRuolo()  + ") " + ((Giocatori)offertaVincente.get("giocatore")).getSquadra() + " arrivato dopo : " + (now.getTimeInMillis()-scadenzaAsta.getTimeInMillis()) + "millisecondi da scadenza asta";
				if(!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
					str = str + "(" + nomegiocatoreOperaCome + ")";
				}
				creaMessaggio(str);
			} else {
				String str = "Rilancio di " + offerta + " fatto da " + nomegiocatore;
				if(!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
					str = str + "(" + nomegiocatoreOperaCome + ")";
				}
				str = str + " per " + offertaVincente.get("nomeCalciatore") + "(" + ((Giocatori)offertaVincente.get("giocatore")).getRuolo()  + ") " + ((Giocatori)offertaVincente.get("giocatore")).getSquadra();
				if (attOfferta != null && offerta<=attOfferta) {
					creaMessaggio(str + " non superiore all'offerta vincente di " + attOfferta + " fatta da " + offertaVincente.get("nomegiocatore"));
				}
				else {
					calInizioOfferta = Calendar.getInstance();
					offertaVincente.put("nomegiocatore", nomegiocatore);
					offertaVincente.put("idgiocatore", idgiocatore);
					offertaVincente.put("offerta", offerta);
					m.put("offertaVincente", offertaVincente);
					creaMessaggio(str);
				}
			}
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("ping")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			utentiScaduti = new ArrayList<>();
			Calendar now = Calendar.getInstance();
			if (nomegiocatore != null) {
				Map<String, Object> mp = new HashMap<>();
				mp.put("lastPing", now);
				mp.put("checkPing", 0);
				pingUtenti.put(nomegiocatore, mp);
			}
			Map<String, Object> m = new HashMap<>();
			for (String utente : utentiLoggati) {
				Map<String, Object> map = pingUtenti.get(utente);
				if (map!= null)
				{
					Calendar c = (Calendar) map.get("lastPing");
					long checkPing = now.getTimeInMillis() - c.getTimeInMillis();
					map.put("checkPing", checkPing);
					if (checkPing>20000) {
						utentiScaduti.add(utente);
					}
				}
			}
			if (calInizioOfferta != null) m.put("contaTempo", now.getTimeInMillis() - calInizioOfferta.getTimeInMillis());
			m.put("timeout", timeOut);
			m.put("utentiScaduti", utentiScaduti);
//			m.put("elencoAllenatori", myController.getAllAllenatori());
			m.put("utenti", utentiLoggati);
			m.put("durataAsta", durataAsta);
			m.put("giocatoreDurataAsta", giocatoreDurataAsta);
			m.put("sSemaforoAttivo", sSemaforoAttivo);
			m.put("offertaVincente", offertaVincente);
			m.put("pingUtenti", pingUtenti);
			m.put("messaggi", messaggi);
			m.put("giocatoreTimeout", giocatoreTimeout);
			m.put("turno", myController.getTurno());
			m.put("nomeGiocatoreTurno", myController.getNomeGiocatoreTurno());
			m.put("millisFromPausa",millisFromPausa);
			
			m.put("RICHIESTA", nomegiocatore);
			invia(toJson(m));
			aggiorna();
		}
		else {
			invia(payload);
		}
	}
	
	public void notificaCancellaOfferta(Map<String, Object> mapOfferta) throws IOException {
		Map<String, Object> m = new HashMap<>();
		creaMessaggio("Offerta registrata CANCELLATA: " + mapOfferta.get("allenatore") + " per " + mapOfferta.get("giocatore") + "(" + mapOfferta.get("ruolo") 
		+ ") " + mapOfferta.get("squadra") + " vinto a " + mapOfferta.get("costo"));
		m.put("messaggi", messaggi);
		invia(toJson(m));
	}
	public void aggiornaUtenti(Map<String, String> utentiRinominati, Iterable<Allenatori> allAllenatori) throws IOException {

		Iterator<String> iterator = utentiRinominati.keySet().iterator();
		while (iterator.hasNext()) {
			String vecchioNome = (String) iterator.next();
			String nuovoNome=utentiRinominati.get(vecchioNome);
			if (utentiScaduti.contains(vecchioNome)) {
				utentiScaduti.remove(vecchioNome);
				utentiScaduti.add(nuovoNome);
			}
			if (utentiLoggati.contains(vecchioNome)) {
				utentiLoggati.remove(vecchioNome);
				utentiLoggati.add(nuovoNome);
			}
			Map<String, Object> map = pingUtenti.get(vecchioNome);
			if (map != null) {
				pingUtenti.remove(vecchioNome);
				pingUtenti.put(nuovoNome, map);
			}
			if(myController.getNomeGiocatoreTurno().equalsIgnoreCase(vecchioNome)) {
				myController.setNomeGiocatoreTurno(nuovoNome);
			}
		}
		Map<String, Object> m = new HashMap<>();
		creaMessaggio("Utenti rinominati: " + utentiRinominati);
		m.put("messaggi", messaggi);
		m.put("utentiRinominati", utentiRinominati);
		m.put("elencoAllenatori", allAllenatori);
		invia(toJson(m));
	}
	
	private void invia(String payload) throws IOException {
		for (WebSocketSession webSocketSession : sessions) {
			if (webSocketSession.isOpen()) {
				synchronized(webSocketSession) {
					webSocketSession.sendMessage(new TextMessage(payload));
				}
			}
		}
	}
	private void aggiorna() throws IOException {
		Map<String, Object> m = new HashMap<>();
		long l = 0;
		int conta=0;
		if (calInizioOfferta != null) {
			Calendar now = Calendar.getInstance();
			l = (now.getTimeInMillis() - calInizioOfferta.getTimeInMillis())/1000;
			l = 100*l/durataAsta;
			if (l<33) conta = -1;
			else if (l<66) conta = 1;
			else if (l<99) conta = 2;
			else conta = 3;
		}
		m.put("timeStart", conta);
		invia(toJson(m));
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		HttpSession httpSession = (HttpSession) session.getAttributes().get("HTTPSESSIONID");
		sessions.add(session);
		
	}
	private ObjectMapper mapper = new ObjectMapper();
	private Map<String, Object> jsonToMap(String json)
	{
		try
		{
			return mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public String toJson(Object o)
	{
		if (o == null) return null;
		try
		{
			byte[] data = mapper.writeValueAsBytes(o);
			return new String(data);
		} catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		} 
	}
}


