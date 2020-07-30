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
	List<String> utentiLoggati = new ArrayList<>();
	List<String> utentiScaduti=new ArrayList<>();
	Map<String, Map<String, Object>> pingUtenti = new HashMap<>();
	Map<String, Object> offertaVincente = new HashMap<>();
	Calendar calInizioOfferta;
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ssZ");
	int durataAsta;
	String idCalciatore;
	String nomeCalciatore;
	String giocatoreDurataAsta="";
	String sSemaforoAttivo;
	List<String> messaggi=new ArrayList<>();
	@Autowired MyController myController;
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws InterruptedException, IOException {
		HttpSession httpSession = (HttpSession) session.getAttributes().get("HTTPSESSIONID");
//		utentiLoggati.add("GIOC0");//FIXME toglimi
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
			invia(toJson(m));
		}
		if (operazione != null && operazione.equals("connetti")) {
//			messaggi = new ArrayList<>();
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = jsonToMap.get("idgiocatore").toString();
			Long tokenUtente = (Long)jsonToMap.get("tokenUtente");
			Calendar now = Calendar.getInstance();
			Map<String, Object> m = new HashMap<>();
			if (utentiScaduti.contains(nomegiocatore)) {
				utentiLoggati.remove(nomegiocatore);
				utentiScaduti.remove(nomegiocatore);
				pingUtenti.remove(nomegiocatore);
			}
			if (utentiLoggati != null && utentiLoggati.contains(nomegiocatore)) {
//				m.put("RESET_UTENTE",tokenUtente);
				messaggi.add(simpleDateFormat.format(now.getTime()) + " Sessione RUBATA da " + nomegiocatore);
			} 

			httpSession.setAttribute("giocatoreLoggato", nomegiocatore);
//				System.out.println(httpSession.getId() + "-" + httpSession.getAttribute("giocatoreLoggato") + "-" + "connetti");
			httpSession.setAttribute("idLoggato", idgiocatore);
			utentiLoggati.add(nomegiocatore);
			m.put("calciatori", myController.getGiocatoriLiberi());
			m.put("utenti", utentiLoggati);
			messaggi.add(simpleDateFormat.format(now.getTime()) + " Connesso: " + nomegiocatore);
			m.put("messaggi", messaggi);
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("confermaAsta")) {
			sSemaforoAttivo="S";
			offertaVincente = new HashMap<>();
			messaggi = new ArrayList<>();
			Map<String, Object> m = new HashMap<>();
			m.put("calciatori", myController.getGiocatoriLiberi());
			m.put("selCalciatore", "x");
			m.put("messaggi", messaggi);
			invia(toJson(m));
		}		
		else if (operazione != null && operazione.equals("annullaAsta")) {
			sSemaforoAttivo="S";
			offertaVincente = new HashMap<>();
			messaggi = new ArrayList<>();
			Map<String, Object> m = new HashMap<>();
//			m.put("calciatori", myController.getGiocatoriLiberi());
			m.put("selCalciatore", "x");
			m.put("messaggi", messaggi);
			invia(toJson(m));
		}		
		else if (operazione != null && operazione.equals("terminaAsta")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = jsonToMap.get("idgiocatore").toString();
			calInizioOfferta.set(Calendar.YEAR, 1971);
			Calendar now = Calendar.getInstance();
			Map<String, Object> m = new HashMap<>();
			messaggi.add(simpleDateFormat.format(now.getTime()) + " Offerta terminata in anticipo da " + nomegiocatore);
			m.put("messaggi", messaggi);
			invia(toJson(m));
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
			boolean bSemaforoAttivo = (boolean) jsonToMap.get("bSemaforoAttivo");
			if (bSemaforoAttivo)
				sSemaforoAttivo="S";
			else
				sSemaforoAttivo="N";
			calInizioOfferta = Calendar.getInstance();
			offertaVincente = new HashMap<>();
			offertaVincente.put("nomegiocatore", nomegiocatore);
			offertaVincente.put("idgiocatore", idgiocatore);
			offertaVincente.put("offerta", 1);
			offertaVincente.put("nomeCalciatore", nomeCalciatore);
			offertaVincente.put("idCalciatore", idCalciatore);
			Map<String, Object> m = new HashMap<>();
			m.put("offertaVincente", offertaVincente);
			String str = " Offerta avviata da " + nomegiocatore;
			if(!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
				str = str + "(" + nomegiocatoreOperaCome + ")";
			}
			messaggi.add(simpleDateFormat.format(calInizioOfferta.getTime()) + str);
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("disconnetti")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore = jsonToMap.get("idgiocatore").toString();
			utentiLoggati.remove(nomegiocatore);
			utentiScaduti.remove(nomegiocatore);
			pingUtenti.remove(nomegiocatore);
//			System.out.println(httpSession.getId() + "-" + httpSession.getAttribute("giocatoreLoggato") + "-" + "disconnetti");
			httpSession.removeAttribute("giocatoreLoggato");
			httpSession.removeAttribute("idLoggato");
			Map<String, Object> m = new HashMap<>();
			m.put("utenti", utentiLoggati);
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
			String idgiocatore = jsonToMap.get("idgiocatore").toString();
			String nomegiocatoreOperaCome = (String) jsonToMap.get("nomegiocatoreOperaCome");
			String idgiocatoreOperaCome = jsonToMap.get("idgiocatoreOperaCome").toString();
			Integer offerta = (Integer) jsonToMap.get("offerta");
			Integer attOfferta = (Integer) offertaVincente.get("offerta");
			Calendar now = Calendar.getInstance();
			Calendar scadenzaAsta = Calendar.getInstance();
			scadenzaAsta.setTimeInMillis(calInizioOfferta.getTimeInMillis());
			scadenzaAsta.add(Calendar.SECOND, durataAsta);
			Map<String, Object> m = new HashMap<>();
			if (now.after(scadenzaAsta)) {
				String str = " Offerta di " + nomegiocatore + " arrivata dopo : " + (now.getTimeInMillis()-scadenzaAsta.getTimeInMillis()) + "millisecondi da scadenza asta";
				if(!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
					str = str + "(" + nomegiocatoreOperaCome + ")";
				}
				messaggi.add(simpleDateFormat.format(now.getTime()) + str);
			} else {
				String str = " Offerta di " + offerta + " fatta da " + nomegiocatore;
				if(!nomegiocatoreOperaCome.equalsIgnoreCase(nomegiocatore)) {
					str = str + "(" + nomegiocatoreOperaCome + ")";
				}
				if (attOfferta != null && offerta<=attOfferta) {
					messaggi.add(simpleDateFormat.format(now.getTime()) + str + " non superiore all'offerta vincente di " + attOfferta + " fatta da " + offertaVincente.get("nomegiocatore"));
				}
				else {
					calInizioOfferta = Calendar.getInstance();
					offertaVincente.put("nomegiocatore", nomegiocatore);
					offertaVincente.put("idgiocatore", idgiocatore);
					offertaVincente.put("offerta", offerta);
					m.put("offertaVincente", offertaVincente);
					messaggi.add(simpleDateFormat.format(now.getTime()) + str);
				}
			}
			invia(toJson(m));
		}
		else if (operazione != null && operazione.equals("ping")) {
			String nomegiocatore = (String) jsonToMap.get("nomegiocatore");
			String idgiocatore="";
			if(jsonToMap.get("idgiocatore")!=null) {
				idgiocatore = jsonToMap.get("idgiocatore").toString();
			}
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
			m.put("utentiScaduti", utentiScaduti);
			m.put("elencoAllenatori", myController.getAllAllenatori());
			m.put("utenti", utentiLoggati);
			m.put("durataAsta", durataAsta);
			m.put("giocatoreDurataAsta", giocatoreDurataAsta);
			m.put("sSemaforoAttivo", sSemaforoAttivo);
			m.put("offertaVincente", offertaVincente);
			m.put("pingUtenti", pingUtenti);
			m.put("messaggi", messaggi);
			m.put("RICHIESTA", nomegiocatore);
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
		HttpSession httpSession = (HttpSession) session.getAttributes().get("HTTPSESSIONID");
		sessions.add(session);
//		System.out.println(httpSession.getId() + "-" + "sessssionnss");
		
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


