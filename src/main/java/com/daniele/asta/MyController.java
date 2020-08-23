package com.daniele.asta;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.daniele.asta.entity.Allenatori;
import com.daniele.asta.entity.Configurazione;
import com.daniele.asta.entity.Fantarose;
import com.daniele.asta.entity.Giocatori;
import com.daniele.asta.entity.Logger;
import com.daniele.asta.repository.AllenatoriRepository;
import com.daniele.asta.repository.ConfigurazioneRepository;
import com.daniele.asta.repository.FantaroseRepository;
import com.daniele.asta.repository.GiocatoriRepository;
import com.daniele.asta.repository.LoggerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RestController
@RequestMapping({ "/" })
public class MyController {

	@Autowired Environment environment;	
	@Autowired HttpSession httpSession;
	@Autowired AllenatoriRepository allenatoriRepository;
	@Autowired FantaroseRepository fantaroseRepository;
	@Autowired GiocatoriRepository giocatoriRepository;
	@Autowired LoggerRepository loggerRepository;
	@Autowired ConfigurazioneRepository configurazioneRepository;
	@Autowired Criptaggio criptaggio; 
	@Autowired EntityManager em;
	@Autowired SocketHandler socketHandler;
	private String turno="0";
	private String nomeGiocatoreTurno="";

	@RequestMapping("/init")
	public Map<String, Object> init() {
		Map<String, Object> m = new HashMap<>();
		Configurazione configurazione = getConfigurazione();
		if (configurazione.getNumeroGiocatori()==null) {
			m.put("DA_CONFIGURARE", "x");
		}
		else {
			String giocatoreLoggato = (String) httpSession.getAttribute("nomeGiocatoreLoggato");
			String idLoggato = (String) httpSession.getAttribute("idLoggato");
			if (giocatoreLoggato != null) {
				m.put("giocatoreLoggato", giocatoreLoggato);
				m.put("idLoggato", idLoggato);
			}
			Iterable<Allenatori> allAllenatori = getAllAllenatori();
			for (Allenatori allenatori : allAllenatori) {
				if(allenatori.getOrdine()==Integer.parseInt(getTurno())) {
					setNomeGiocatoreTurno(allenatori.getNome());
				}
			}
			m.put("elencoAllenatori", allAllenatori);
			m.put("nomeGiocatoreTurno", getNomeGiocatoreTurno());
			m.put("turno", getTurno());
		}
		return m;
	}
	@PostMapping("/caricaFile")
	public void caricaFile(@RequestBody Map<String,String> obj) throws Exception {
		String content = obj.get("file");
		String tipoFile = obj.get("tipo");
		giocatoriRepository.deleteAll();
		if("FS".equalsIgnoreCase(tipoFile)) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(content));
			Document parse = builder.parse(is);
			NodeList childNodes = parse.getChildNodes().item(0).getChildNodes();
			for (int i=0;i<childNodes.getLength();i++) {
				if (i>0) {
					Node tr = childNodes.item(i);
					NodeList childNodesTr = tr.getChildNodes();
					String id = childNodesTr.item(0).getTextContent(); 
					String squadra = childNodesTr.item(3).getTextContent(); 
					String nome = childNodesTr.item(2).getTextContent() + " " + childNodesTr.item(1).getTextContent(); 
					String ruolo = childNodesTr.item(4).getTextContent(); 
					String quotazione = childNodesTr.item(6).getTextContent(); 
					System.out.println(id + "-" +squadra + "-" +nome + "-" +ruolo + "-" +quotazione + "-" );
					Giocatori giocatori = new Giocatori();
					giocatori.setId(Integer.parseInt(id));
					giocatori.setNome(nome);
					giocatori.setQuotazione(Integer.parseInt(quotazione));
					giocatori.setRuolo(ruolo);
					giocatori.setSquadra(squadra);
					giocatoriRepository.save(giocatori);
				}
			}
		}
		else if("MANTRA".equalsIgnoreCase(tipoFile)) {
			String[] split = content.split("\n");
			for(int i=1;i<split.length;i++) {
				String riga = split[i];
				String[] colonne = riga.split("\t");
				Giocatori giocatori = new Giocatori();
				giocatori.setId(Integer.parseInt(colonne[0]));
				giocatori.setNome(colonne[2]);
				try
				{
					giocatori.setQuotazione(Integer.parseInt(colonne[6].replace("\r", "")));
				}
				catch (Exception e)
				{	
					giocatori.setQuotazione(-1);
				}
				giocatori.setRuolo(colonne[1].replaceAll("\"", ""));
				giocatori.setSquadra(colonne[3]);
				giocatoriRepository.save(giocatori);
			}
		}
		else {
			throw new RuntimeException("Tipo file non riconoscituo:" + tipoFile);
		}

	}
	@PostMapping("/cancellaOfferta")
	public List<Map<String, Object>>  cancellaOfferta(@RequestBody Map<String, Object> body) throws Exception {
		Map<String, Object> mapOfferta = (Map)body.get("offerta");
		Integer idGiocatore=(Integer) mapOfferta.get("idGiocatore");
		fantaroseRepository.delete(idGiocatore);
		socketHandler.notificaCancellaOfferta(mapOfferta);
		return elencoCronologiaOfferte();
	}

	@PostMapping("/azzera")
	public void azzera() throws Exception {
		giocatoriRepository.deleteAll();
		fantaroseRepository.deleteAll();
		allenatoriRepository.deleteAll();
		Configurazione configurazione = getConfigurazione();
		configurazione.setNumeroGiocatori(null);
		configurazioneRepository.save(configurazione);
	}
	@PostMapping("/aggiornaNumUtenti")
	public void aggiornaNumUtenti(@RequestBody int numUtenti) throws Exception {
		Configurazione configurazione = getConfigurazione();
		configurazione.setNumeroGiocatori(numUtenti);
		configurazioneRepository.save(configurazione);
		for(int i=0;i<numUtenti;i++) {
			Allenatori al = new Allenatori();
			al.setId(i);
			al.setOrdine(i);
			al.setNome("GIOC"+i);
			if (i==0)
				al.setIsAdmin(true);
			else
				al.setIsAdmin(false);
			al.setPwd("");
			allenatoriRepository.save(al);
		}
	}
	
	@PostMapping("aggiornaSessioneNomeUtente")
	public void aggiornaSessioneNomeUtente(@RequestBody Map<String, String> body) {
		httpSession.setAttribute("nomeGiocatoreLoggato", body.get("nuovoNome"));
	}
	@PostMapping("/aggiornaUtenti")
	public  Map<String,String>  aggiornaUtenti(@RequestBody Map<String, Object> body) throws Exception {
		Map <String, String> m = new HashMap<>();
		Map <String, String> utentiRinominati = new HashMap<>();
		int i=0;
		Boolean admin = (Boolean) body.get("admin");
		List<Map<String, Object>> elencoAllenatori = (List<Map<String, Object>>) body.get("elencoAllenatori");
		for (Map<String, Object> map : elencoAllenatori) {
			Allenatori al = allenatoriRepository.findOne((Integer) map.get("id"));
			String nuovoNome = (String) map.get("nuovoNome");
			String vecchioNome=al.getNome();
			String giocatoreLoggato = (String) httpSession.getAttribute("nomeGiocatoreLoggato");
			if (!vecchioNome.equalsIgnoreCase(nuovoNome)) {
				utentiRinominati.put(vecchioNome, nuovoNome);
				if(giocatoreLoggato.equalsIgnoreCase(vecchioNome)) {
					m.put("nuovoNomeLoggato", nuovoNome);
					m.put("vecchioNomeLoggato", vecchioNome);
//					httpSession.setAttribute("nomeGiocatoreLoggato", nuovoNome);
				}
			}
			al.setNome(nuovoNome);
			String pwd = (String) map.get("pwd");
			if (!pwd.equalsIgnoreCase(al.getPwd()))
				al.setPwd(criptaggio.encrypt(pwd,nuovoNome));
			if("true".equalsIgnoreCase(map.get("isAdmin").toString()))
				al.setIsAdmin(true);
			else
				al.setIsAdmin(false);
			if (admin) al.setOrdine((Integer) map.get("ordine"));
			i++;
			allenatoriRepository.save(al);
		}
		socketHandler.aggiornaUtenti(utentiRinominati,getAllAllenatori());
		return m;
	}

	@GetMapping("/cripta")
	public Map<String,String> cripta(@RequestParam(name = "pwd") String pwd,@RequestParam(name = "key") String key) throws Exception {
		Map <String, String> m = new HashMap<>();
		m.put("value", criptaggio.encrypt(pwd, key));
		return m;
	}
/*
	@GetMapping("/decripta")
	public String decripta(@RequestParam(name = "pwd") String pwd,@RequestParam(name = "key") String key) throws Exception {
		return criptaggio.decrypt(pwd, key);
	}
*/	
	@PostMapping("/confermaAsta")
	public void confermaAsta(@RequestBody Map<String, Object> body) throws Exception {
		//		String nomegiocatore = (String) body.get("nomegiocatore");
		//		String nomeCalciatore = (String) body.get("nomeCalciatore");
		String idgiocatore =  body.get("idgiocatore").toString();
		Integer offerta = (Integer) body.get("offerta");
		String idCalciatore = body.get("idCalciatore").toString();
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String stm = sdf.format(c.getTime());
		Fantarose fantarosa = new Fantarose();
		fantarosa.setCosto(offerta);
		fantarosa.setIdAllenatore(Integer.parseInt(idgiocatore));
		fantarosa.setIdGiocatore(Integer.parseInt(idCalciatore));
		fantarosa.setSqlTime(stm);
		fantaroseRepository.save(fantarosa);
	}


	@RequestMapping("/spesoAllenatori")
	public List<Map<String, Object>>  spesoAllenatori() {
		try {
			String sql = "select sum(costo) costo, a.nome from fantarose f, allenatori a where a.id = idAllenatore group by a.nome";
			Query qy = em.createNativeQuery(sql);
			List<Object[]> resultList = qy.getResultList();
			List<Map<String, Object>> ret = new ArrayList<>();
			for (Object[] row : resultList) {
				Map<String, Object> m = new HashMap<>();
				m.put("costo",  row[0]);
				m.put("nome",row[1]);
				ret.add(m);
			}
			return ret;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}


	@RequestMapping("/elencoCronologiaOfferte")
	public List<Map<String, Object>>  elencoCronologiaOfferte() {
		try {
			String sql = "select a.Nome allenatore, g.Squadra, g.Ruolo, g.nome giocatore, Costo, sqlTime, idGiocatore, idAllenatore   from  fantarose f, " + 
					"giocatori g, allenatori a  where g.id = idGiocatore and a.id = idAllenatore order by sqlTime desc";
			Query qy = em.createNativeQuery(sql);
			List<Object[]> resultList = qy.getResultList();
			List<Map<String, Object>> ret = new ArrayList<>();
			for (Object[] row : resultList) {
				Map<String, Object> m = new HashMap<>();
				m.put("allenatore",  row[0]);
				m.put("squadra", row[1]);
				m.put("ruolo",  row[2]);
				m.put("giocatore",  row[3]);
				m.put("costo",  row[4]);
				m.put("sqlTime",row[5]);
				m.put("idGiocatore",  row[6]);
				m.put("idAllenatore", row[7]);
				ret.add(m);
			}
			return ret;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@RequestMapping("/elencoOfferte")
	public List<Map<String, Object>>  elencoOfferte() {
		try {
			String sql = "select a.Nome allenatore, g.Squadra, g.Ruolo, g.nome giocatore, Costo, sqlTime from fantarose f, giocatori g, " + 
					"allenatori a where g.id = idGiocatore and a.id = idAllenatore order by allenatore, ruolo desc, giocatore";
			Query qy = em.createNativeQuery(sql);
			List<Object[]> resultList = qy.getResultList();
			List<Map<String, Object>> ret = new ArrayList<>();
			for (Object[] row : resultList) {
				Map<String, Object> m = new HashMap<>();
				m.put("allenatore", row[0]);
				m.put("squadra", row[1]);
				m.put("ruolo", row[2]);
				m.put("giocatore", row[3]);
				m.put("costo", row[4]);
				m.put("sqlTime", row[5]);
				ret.add(m);
			}
			return ret;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private ObjectMapper mapper = new ObjectMapper();
	public String toJson(Object o)
	{
		try
		{
			byte[] data = mapper.writeValueAsBytes(o);
			return new String(data);//, Charsets.ISO_8859_1
		} catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		} 
	}
	public List<Map<String, Object>> jsonToList(String json)
	{
		try
		{
			return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	@RequestMapping("/riepilogoAllenatori")
	public List<Map<String, Object>>  riepilogoAllenatori() {
		try {
			String sql = "select count(ruolo) conta, ruolo, a.nome nome from fantarose f, allenatori a, giocatori g where g.id=idGiocatore " + 
					"and a.id = idAllenatore group by a.nome ,ruolo order by a.nome, ruolo desc";
			Query qy = em.createNativeQuery(sql);
			List<Object[]> resultList = qy.getResultList();
			List<Map<String, Object>> ret = new ArrayList<>();
			for (Object[] row : resultList) {
				Map<String, Object> m = new HashMap<>();
				m.put("conta",  row[0]);
				m.put("ruolo",row[1]);
				m.put("nome",row[2]);
				ret.add(m);
			}
			return ret;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Cacheable(cacheNames = "allenatori")
	@GetMapping(path="/allAllenatori")
	public @ResponseBody Iterable<Allenatori> getAllAllenatori() {
		Iterable<Allenatori> findAll = allenatoriRepository.getAllenatoriOrderByOrdine();
		for (Allenatori allenatori : findAll) {
			allenatori.setNuovoNome(allenatori.getNome());
		}
		return findAll;
	}	

	@GetMapping(path="/allFantarose")
	public @ResponseBody Iterable<Fantarose> getAllFantarose() {
		return fantaroseRepository.findAll();
	}	

	@GetMapping(path="/allGiocatori")
	public @ResponseBody Iterable<Giocatori> getAllGiocatori() {
		return giocatoriRepository.findAll();
	}	
	@GetMapping(path="/elencoLogger")
	public @ResponseBody Iterable<Logger> elencoLogger() {
		return loggerRepository.findAll();
	}	
	
	@GetMapping(path="/configurazione")
	public @ResponseBody Configurazione getConfigurazione() {
		return configurazioneRepository.findOne(1);
	}	
	
	@GetMapping(path="/giocatoriLiberi")
	public @ResponseBody List<Map<String, Object>> getGiocatoriLiberi() {
//		Iterable<Giocatori> giocatoriLiberi = giocatoriRepository.getGiocatoriLiberi();
		List<Object[]> resultList = giocatoriRepository.getGiocatoriLiberi();
		List<Map<String, Object>> ret = new ArrayList<>();
//		for (Giocatori giocatori : giocatoriLiberi) {
//			System.out.println(giocatori);
//		}
		for (Object[] row : resultList) {
			Map<String, Object> m = new HashMap<>();
			m.put("id",  row[0]);
			m.put("squadra",  row[1]);
			m.put("nome",  row[2]);
			m.put("ruolo",  row[3]);
			m.put("quotazione",  row[4]);
			ret.add(m);
		}
		return ret;
	}
	public String getNomeGiocatoreTurno() {
		return nomeGiocatoreTurno;
	}
	public void setNomeGiocatoreTurno(String nomeGiocatoreTurno) {
		this.nomeGiocatoreTurno = nomeGiocatoreTurno;
	}
	public String getTurno() {
		return turno;
	}
	public void setTurno(String turno) {
		this.turno = turno;
	}

}
