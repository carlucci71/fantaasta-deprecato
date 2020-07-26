package com.example.demo;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SocketHandler extends TextWebSocketHandler implements WebSocketHandler {
	
	List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
	List<Map<String, String>> utenti = new ArrayList<Map<String, String>>();
	List<Map<String,String>> utentiScaduti=new ArrayList<>();
	Map<Map<String,String>, Map<String, Object>> pingUtenti = new HashMap<>();
	Map<String, Object> offertaVincente = new HashMap<>();
	Calendar calInizioOfferta;
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ssZ");
	int durataAsta;
	String idCalciatore;
	String nomeCalciatore;
	String giocatoreDurataAsta="";
	String sSemaforoAttivo;
	List<String> messaggi=new ArrayList<>();
	@Autowired MyController myController;// = new MyController();
	HttpSession httpSession;
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws InterruptedException, IOException {
		
		String payload = message.getPayload();
		Map<String, Object> jsonToMap = jsonToMap(payload);
		String operazione = (String) jsonToMap.get("operazione");
		if (operazione != null && operazione.equals("cancellaUtente")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			Integer iIdgiocatore = (Integer) jsonToMap.get("idgiocatore");
			Map<String, String> mu = new HashMap<>();
			mu.put("nomegiocatore", nomegiocatore);
			mu.put("idgiocatore", Integer.toString(iIdgiocatore));
			utenti.remove(mu);
			utentiScaduti.remove(mu);
			pingUtenti.remove(mu);
			Map<String, Object> m = new HashMap<>();
			m.put("utenti", utenti);
			invia(toJson(m));
		}
		if (operazione != null && operazione.equals("connetti")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = (String) jsonToMap.get("idgiocatore");
			Map<String, String> mu = new HashMap<>();
			mu.put("nomegiocatore", nomegiocatore);
			mu.put("idgiocatore", idgiocatore);
			Long tokenUtente = (Long)jsonToMap.get("tokenUtente");
			Map<String, Object> m = new HashMap<>();
			if (utentiScaduti.contains(mu)) {
				utenti.remove(mu);
				utentiScaduti.remove(mu);
				pingUtenti.remove(mu);
			}
			if (utenti != null && utenti.contains(mu)) {
				m.put("RESET_UTENTE",tokenUtente);
			} else {
				httpSession.setAttribute("giocatoreLoggato", nomegiocatore);
				httpSession.setAttribute("idLoggato", idgiocatore);
				utenti.add(mu);
				m.put("calciatori", myController.elencoCalciatori());
				m.put("utenti", utenti);
			}
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("confermaAsta")) {
			sSemaforoAttivo="S";
			offertaVincente = new HashMap<>();
			messaggi = new ArrayList<>();
			Map<String, Object> m = new HashMap<>();
			m.put("calciatori", myController.elencoCalciatori());
			m.put("selCalciatore", "x");
			m.put("messaggi", messaggi);
			invia(toJson(m));
		}		
		else if (operazione != null && operazione.equals("annullaAsta")) {
			sSemaforoAttivo="S";
			offertaVincente = new HashMap<>();
			messaggi = new ArrayList<>();
			Map<String, Object> m = new HashMap<>();
//			m.put("calciatori", myController.elencoCalciatori());
			m.put("selCalciatore", "x");
			m.put("messaggi", messaggi);
			invia(toJson(m));
		}		
		else if (operazione != null && operazione.equals("start")) {
			durataAsta = (Integer) jsonToMap.get("durataAsta");
			String selCalciatore = (String)jsonToMap.get("selCalciatore");
			String[] split = selCalciatore.split("@");
			idCalciatore=split[0];
			nomeCalciatore=split[1];
			boolean bSemaforoAttivo = (boolean) jsonToMap.get("bSemaforoAttivo");
			if (bSemaforoAttivo)
				sSemaforoAttivo="S";
			else
				sSemaforoAttivo="N";
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = (String) jsonToMap.get("idgiocatore");
			calInizioOfferta = Calendar.getInstance();
			offertaVincente = new HashMap<>();
			offertaVincente.put("nomegiocatore", nomegiocatore);
			offertaVincente.put("idgiocatore", idgiocatore);
			offertaVincente.put("offerta", 1);
			offertaVincente.put("nomeCalciatore", nomeCalciatore);
			offertaVincente.put("idCalciatore", idCalciatore);
			Map<String, Object> m = new HashMap<>();
			m.put("offertaVincente", offertaVincente);
			messaggi.add(simpleDateFormat.format(calInizioOfferta.getTime()) + " Offerta avviata da " + nomegiocatore);
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("disconnetti")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = (String) jsonToMap.get("idgiocatore");
			Map<String, String> mu = new HashMap<>();
			mu.put("nomegiocatore", nomegiocatore);
			mu.put("idgiocatore", idgiocatore);
			utenti.remove(mu);
			httpSession.removeAttribute("giocatoreLoggato");
			httpSession.removeAttribute("idLoggato");
			Map<String, Object> m = new HashMap<>();
			m.put("utenti", utenti);
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("aggiornaDurataAsta")) {
			giocatoreDurataAsta = (String) jsonToMap.get("giocatoreDurataAsta");
			durataAsta = (Integer) jsonToMap.get("durataAsta");
			Map<String, Object> m = new HashMap<>();
			m.put("durataAsta", durataAsta);
			m.put("giocatoreDurataAsta", giocatoreDurataAsta);
			invia(toJson(m));
			
		}
		else if (operazione != null && operazione.equals("inviaOfferta")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = (String) jsonToMap.get("idgiocatore");
			Integer offerta = (Integer) jsonToMap.get("offerta");
			Integer attOfferta = (Integer) offertaVincente.get("offerta");
			Calendar now = Calendar.getInstance();
			Calendar scadenzaAsta = Calendar.getInstance();
			scadenzaAsta.setTimeInMillis(calInizioOfferta.getTimeInMillis());
			scadenzaAsta.add(Calendar.SECOND, durataAsta);
			Map<String, Object> m = new HashMap<>();
			if (now.after(scadenzaAsta)) {
//				throw new RuntimeException("Asta scaduta");
//				m.put("messaggio", simpleDateFormat.format(now.getTime()) + " Offerta di " + nomegiocatore + " arrivata dopo : " + (now.getTimeInMillis()-scadenzaAsta.getTimeInMillis()) + "millisecondi da scadenza asta");
				messaggi.add(simpleDateFormat.format(now.getTime()) + " Offerta di " + nomegiocatore + " arrivata dopo : " + (now.getTimeInMillis()-scadenzaAsta.getTimeInMillis()) + "millisecondi da scadenza asta");
			} else  if (attOfferta != null && offerta<=attOfferta) {
//				throw new RuntimeException("Offerta superata");
//				m.put("messaggio", simpleDateFormat.format(now.getTime()) + " Offerta di " + offerta + " fatta da " + nomegiocatore + " non superiore all'offerta vincente di " + attOfferta + " fatta da " + offertaVincente.get("nomegiocatore"));
				messaggi.add(simpleDateFormat.format(now.getTime()) + " Offerta di " + offerta + " fatta da " + nomegiocatore + " non superiore all'offerta vincente di " + attOfferta + " fatta da " + offertaVincente.get("nomegiocatore"));
			}
			else {
				calInizioOfferta = Calendar.getInstance();
				offertaVincente.put("nomegiocatore", nomegiocatore);
				offertaVincente.put("idgiocatore", idgiocatore);
				offertaVincente.put("offerta", offerta);
				m.put("offertaVincente", offertaVincente);
				messaggi.add(simpleDateFormat.format(now.getTime()) + " Offerta di " + offerta + " fatta da " + nomegiocatore);
			}
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("ping")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = (String) jsonToMap.get("idgiocatore");
			utentiScaduti = new ArrayList<Map<String,String>>();
			Map<String, String> mu = new HashMap<>();
			mu.put("nomegiocatore", nomegiocatore);
			mu.put("idgiocatore", idgiocatore);
			Calendar now = Calendar.getInstance();
			if (nomegiocatore != null) {
				Map<String, Object> mp = new HashMap<>();
				mp.put("lastPing", now);
				mp.put("checkPing", 0);
				pingUtenti.put(mu, mp);
			}
			Map<String, Object> m = new HashMap<>();
			for (Map<String, String> utente : utenti) {
				Map<String, Object> map = pingUtenti.get(utente);
				Calendar c = (Calendar) map.get("lastPing");
				long checkPing = now.getTimeInMillis() - c.getTimeInMillis();
				map.put("checkPing", checkPing);
				if (checkPing>20000) {
					utentiScaduti.add(utente);
				}
			}
			if (calInizioOfferta != null) m.put("contaTempo", now.getTimeInMillis() - calInizioOfferta.getTimeInMillis());
			m.put("utentiScaduti", utentiScaduti);
			m.put("elencoAllenatori", myController.elencoAllenatori());
			m.put("utenti", utenti);
			m.put("durataAsta", durataAsta);
			m.put("giocatoreDurataAsta", giocatoreDurataAsta);
			m.put("sSemaforoAttivo", sSemaforoAttivo);
			m.put("offertaVincente", offertaVincente);
			m.put("pingUtenti", pingUtenti);
			m.put("messaggi", messaggi);
			invia(toJson(m));
			aggiorna();
		}
		else {
			invia(payload);
		}
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
	
//	private WebSocketSession wsSession;
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//		this.wsSession = session;
		httpSession = (HttpSession) session.getAttributes().get("HTTPSESSIONID");
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


