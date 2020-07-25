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
	@RequestMapping("/init")
	public Map<String, Object> init() {
		Map<String, Object> m = new HashMap<>();
	       String giocatore = (String) httpSession.getAttribute("giocatore");
	       if (giocatore != null) m.put("giocatore", giocatore);
//	       m.put("utenti", utenti);
	       return m;
	}
/*
	@RequestMapping(value="/cancellaUtente", method = RequestMethod.POST)
	public Map<String, Object> cancellaUtente(@RequestParam String nomegiocatore) {
		Map<String, Object> ret = new HashMap<>();
		utenti.remove(nomegiocatore);
		pingUtenti.remove(nomegiocatore);
		ret.put("utenti", utenti);
		return ret;
	}
	*/
	/*
	@RequestMapping("/inizia")
	public void  inizia(@RequestParam String nomegiocatore,@RequestParam int durata) {
		durataAsta=durata;
		calInizioOfferta = Calendar.getInstance();
		offertaVincente = new HashMap<>();
		offertaVincente.put("nomegiocatore", nomegiocatore);
		offertaVincente.put("offerta", 1);
	}
	*/
	@RequestMapping("/aggiorna")
	public Map<String, Object>  aggiorna(@RequestParam(required=false) String nomegiocatore) {
		Map<String, Object> m = new HashMap<>();
	    /*
		List<String> utentiScaduti = new ArrayList<String>();
		Calendar instance2 = Calendar.getInstance();
		if (nomegiocatore != null) pingUtenti.put(nomegiocatore, Calendar.getInstance());
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
		*/
		return m;
	}

	
}
