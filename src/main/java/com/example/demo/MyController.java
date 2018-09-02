package com.example.demo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/" })
public class MyController {

	@Autowired Environment environment;	
	@Autowired HttpSession httpSession;	
	List<String> utenti = new ArrayList<String>();
	Map<String, Object> pingUtenti = new HashMap<>();
	Map<String, Object> offertaVincente = new HashMap<>();
	Calendar calInizioOfferta;
	int durataAsta;
	@RequestMapping("/init")
	public Map<String, Object> init() {
		Map<String, Object> m = new HashMap<>();
	       for (final String profileName : environment.getActiveProfiles()) {
	            m.put("profilo", profileName);
	        }  
	       String giocatore = (String) httpSession.getAttribute("giocatore");
	       if (giocatore != null) m.put("giocatore", giocatore);
	       m.put("utenti", utenti);
	       return m;
	}

	@RequestMapping(value="/cancellaUtente", method = RequestMethod.POST)
	public Map<String, Object> cancellaUtente(@RequestParam String nomegiocatore) {
		Map<String, Object> ret = new HashMap<>();
		utenti.remove(nomegiocatore);
		pingUtenti.remove(nomegiocatore);
		ret.put("utenti", utenti);
		return ret;
	}
	@RequestMapping("/start")
	public void  start(@RequestParam int durata) {
		durataAsta=durata;
		calInizioOfferta = Calendar.getInstance();
		offertaVincente = new HashMap<>();
	}
	@RequestMapping("/inviaOfferta")
	public void  inviaOfferta(@RequestParam String nomegiocatore,@RequestParam int offerta) {
		synchronized (nomegiocatore) {
			Integer attOfferta = (Integer) offertaVincente.get("offerta");
			Calendar now = Calendar.getInstance();
			now.add(Calendar.SECOND, -durataAsta);
			if (now.after(calInizioOfferta)) throw new RuntimeException("Asta scaduta");
			if (attOfferta != null && offerta<=attOfferta) throw new RuntimeException("Offerta superata");
			calInizioOfferta = Calendar.getInstance();
			offertaVincente.put("nomegiocatore", nomegiocatore);
			offertaVincente.put("offerta", offerta);
			
		}
	}
	@RequestMapping("/aggiorna")
	public Map<String, Object>  aggiorna(@RequestParam(required=false) String nomegiocatore) {
		List<String> utentiScaduti = new ArrayList<String>();
		Calendar instance2 = Calendar.getInstance();
		if (nomegiocatore != null) pingUtenti.put(nomegiocatore, Calendar.getInstance());
		Map<String, Object> m = new HashMap<>();
		instance2.add(Calendar.SECOND, -20);
		for (String utente : utenti) {
			Calendar cal = (Calendar) pingUtenti.get(utente);
			if (instance2.after(cal)) {
				utentiScaduti.add(utente);
			}
		}
	    m.put("utentiScaduti", utentiScaduti);
    	long l = 0;
    	int conta=0;
	    if (calInizioOfferta != null) {
	    	Calendar now = Calendar.getInstance();
	    	l = (now.getTimeInMillis() - calInizioOfferta.getTimeInMillis())/1000;
	    	l = 100*l/durataAsta;
	    	if (l<33) conta = 0;
	    	else if (l<66) conta = 1;
	    	else if (l<99) conta = 2;
	    	else conta = 3;
	    }
		m.put("timeStart", conta);
	    m.put("offertaVincente", offertaVincente);
		return m;
	}
	@RequestMapping(value="/connetti", method = RequestMethod.POST)
	public Map<String, Object> connetti(@RequestParam String nomegiocatore) {
	    if (utenti != null && utenti.contains(nomegiocatore)) throw new RuntimeException("Utente esistente:" + nomegiocatore);
		httpSession.setAttribute("giocatore", nomegiocatore);
		utenti.add(nomegiocatore);
		Map<String, Object> m = new HashMap<>();
		String giocatore = (String) httpSession.getAttribute("giocatore");
		if (giocatore != null) m.put("giocatore", giocatore);
		m.put("utenti", utenti);
		return m;
	}
	@RequestMapping(value="/disconnetti", method = RequestMethod.POST)
	public Map<String, Object>  disconnetti() {
		utenti.remove(httpSession.getAttribute("giocatore"));
		httpSession.removeAttribute("giocatore");
		Map<String, Object> m = new HashMap<>();
		m.put("utenti", utenti);
		return m;
	}
	
}
