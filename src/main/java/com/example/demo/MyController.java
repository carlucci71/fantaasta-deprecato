package com.example.demo;

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

import com.example.demo.dto.AllenatoriDTO;
import com.example.demo.entity.Allenatori;
import com.example.demo.entity.Configurazione;
import com.example.demo.entity.Fantarose;
import com.example.demo.entity.Giocatori;
import com.example.demo.entity.Leghe;
import com.example.demo.entity.LegheAllenatori;
import com.example.demo.entity.LegheAllenatoriId;
import com.example.demo.repository.AllenatoriRepository;
import com.example.demo.repository.ConfigurazioneRepository;
import com.example.demo.repository.FantaroseRepository;
import com.example.demo.repository.GiocatoriRepository;
import com.example.demo.repository.LegheAllenatoriRepository;
import com.example.demo.repository.LegheRepository;
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
	@Autowired LegheRepository legheRepository;
	@Autowired LegheAllenatoriRepository legheAllenatoriRepository;
	@Autowired ConfigurazioneRepository configurazioneRepository;
	@Autowired Criptaggio criptaggio; 
	@Autowired EntityManager em;

	@RequestMapping("/init")
	public Map<String, Object> init() {
		Map<String, Object> m = new HashMap<>();
//		Configurazione configurazione = getConfigurazione();
//		if (configurazione.getNumeroGiocatori()==null) {
//			m.put("DA_CONFIGURARE", "x");
//		}
//		else {
		String giocatoreLoggato = (String) httpSession.getAttribute("giocatoreLoggato");
		//		System.out.println(httpSession.getId() + "-" + giocatoreLoggato + "-" + "init");
		String idLoggato = (String) httpSession.getAttribute("idLoggato");
		if (giocatoreLoggato != null) {
			m.put("giocatoreLoggato", giocatoreLoggato);
			m.put("idLoggato", idLoggato);
		}
		String legaUtente = (String) httpSession.getAttribute("legaUtente");
		if (legaUtente != null) {
			m.put("legaUtente", legaUtente);
			LegheAllenatoriId legheAllenatoriId = new LegheAllenatoriId();
			legheAllenatoriId.setAllenatori( Integer.parseInt(idLoggato));
			legheAllenatoriId.setLeghe(Integer.parseInt(legaUtente));
			LegheAllenatori findById = legheAllenatoriRepository.findById(legheAllenatoriId);
			m.put("aliasGiocatore",findById.getAlias());
			m.put("nomeLegaUtente",findById.getLeghe().getNome());
			m.put("elencoAllenatori", getAllenatoriByLega(legheRepository.findOne(Integer.parseInt(legaUtente))));
		}
		m.put("elencoLeghe", getAllLeghe());
//		}
		return m;
	}

	@PostMapping("/caricaFile")
	public void caricaFile(@RequestBody Map<String,String> obj) throws Exception {
		String content = obj.get("file");
		String legaUtente = obj.get("legaUtente");
		String tipoFile = obj.get("tipo");
		Leghe findOneLega = legheRepository.findOne(Integer.parseInt(legaUtente));
		giocatoriRepository.deleteGiocatoryByLegaId(findOneLega.getId());
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
					giocatori.setLegheId(findOneLega.getId());
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
				giocatori.setLegheId(findOneLega.getId());
				giocatoriRepository.save(giocatori);
			}
		}
		else {
			throw new RuntimeException("Tipo file non riconoscituo:" + tipoFile);
		}

	}
	
	@PostMapping("/aggiornaNumUtenti")
	public void aggiornaNumUtenti(@RequestBody int numUtenti) throws Exception {
		Configurazione configurazione = getConfigurazione();
		configurazione.setNumeroGiocatori(numUtenti);
		configurazioneRepository.save(configurazione);
		for(int i=0;i<numUtenti;i++) {
			Allenatori al = new Allenatori();
			al.setId(i);
			al.setNome("GIOC"+i);
//			if (i==0)
//				al.setIsAdmin(true);
//			else
//				al.setIsAdmin(false);
			al.setPwd("");
			allenatoriRepository.save(al);
		}
	}
	@PostMapping("/aggiornaUtenti")
	public void aggiornaUtenti(@RequestBody Map<String, Object> body) throws Exception {
		String legaUtente = body.get("legaUtente").toString();
		List<Map<String, Object>> elencoAllenatori = (List<Map<String, Object>>) body.get("elencoAllenatori");
		for (Map<String, Object> map : elencoAllenatori) {
			LegheAllenatoriId legheAllenatoriId = new LegheAllenatoriId();
			legheAllenatoriId.setAllenatori( (int) map.get("id"));
			legheAllenatoriId.setLeghe(Integer.parseInt(legaUtente));
			LegheAllenatori findById = legheAllenatoriRepository.findById(legheAllenatoriId);
			if("true".equalsIgnoreCase(map.get("isAdmin").toString()))
				findById.setAdmin(true);
			else
				findById.setAdmin(false);
			String alias = (String) map.get("alias");
			findById.setAlias(alias);
			legheAllenatoriRepository.save(findById);
		}
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
	@PostMapping("/selezionaLega")
	public Map<String,Object>  selezionaLega(@RequestBody Map<String, Object> body) throws Exception {
		String idgiocatore =  body.get("idgiocatore").toString();
		String legaUtente = (String) body.get("legaUtente");
		String legaPwd = (String) body.get("legaPwd");
		Map<String,Object> ret = new HashMap<>();
		Leghe findLega = legheRepository.findOne(Integer.parseInt(legaUtente));
		if (findLega != null && cripta(legaPwd,findLega.getNome()).get("value").equals(findLega.getPwd())) {
			httpSession.setAttribute("legaUtente",legaUtente);
			ret.put("stato", "OK");
			ret.put("nomeLegaUtente", findLega.getNome());
			ret.put("legaUtente", findLega.getId());
			LegheAllenatoriId legheAllenatoriId = new LegheAllenatoriId();
			legheAllenatoriId.setAllenatori( Integer.parseInt(idgiocatore));
			legheAllenatoriId.setLeghe(Integer.parseInt(legaUtente));
			LegheAllenatori findById = legheAllenatoriRepository.findById(legheAllenatoriId);
			ret.put("aliasGiocatore",findById.getAlias());
			ret.put("elencoAllenatori", getAllenatoriByLega(legheRepository.findOne(Integer.parseInt(legaUtente))));
		} else {
			ret.put("stato", "Errore!! Password di lega errata");
		}
		return ret;
	}
	@PostMapping("/creaLega")
	public Map<String,Object>  creaLega(@RequestBody Map<String, Object> body) throws Exception {
		String nomeLegaCreata = (String) body.get("nomeLegaCreata");
		String pwdLegaCreata = (String) body.get("pwdLegaCreata");
		Integer numeroUtenti = (Integer) body.get("numeroUtenti");
		String idgiocatore =  body.get("idgiocatore").toString();

		Map<String,Object> ret = new HashMap<>();
		Leghe findByNome = legheRepository.findByNome(nomeLegaCreata);
		if (findByNome == null) {
			Leghe leghe = new Leghe();
			leghe.setPwd(criptaggio.encrypt(pwdLegaCreata,nomeLegaCreata));
			leghe.setNome(nomeLegaCreata);
			leghe.setNumUtenti(numeroUtenti);
			legheRepository.save(leghe);
			LegheAllenatori legheAllenatori = new LegheAllenatori();
			Allenatori findOne = allenatoriRepository.findOne(Integer.parseInt(idgiocatore));
			legheAllenatori.setAllenatori(findOne);
			legheAllenatori.setLeghe(leghe);
			legheAllenatori.setAlias(findOne.getNome());
			legheAllenatori.setAdmin(true);
			legheAllenatori.setFittizio(false);
			legheAllenatoriRepository.save(legheAllenatori);

			for (int i=1;i<numeroUtenti;i++) {
				legheAllenatori = new LegheAllenatori();
				Allenatori a=new Allenatori();
				a.setNome("GIOC_" + nomeLegaCreata + "_" + i);
				allenatoriRepository.save(a);
				legheAllenatori.setAllenatori(a);
				legheAllenatori.setAlias(a.getNome());
				legheAllenatori.setLeghe(leghe);
				legheAllenatori.setAdmin(false);
				legheAllenatori.setFittizio(true);
				legheAllenatoriRepository.save(legheAllenatori);

			}
			ret.put("stato", "OK");
			ret.put("elencoLeghe", getAllLeghe());
		}
		else {
			ret.put("stato", "Errore!! Lega esistente");
		}
		return ret;
	}
	@PostMapping("/registra")
	public Map<String,Object>  registra(@RequestBody Map<String, String> body) throws Exception {
		String registraUtente = body.get("registraUtente");
		String registraPwd = body.get("registraPwd");
		Map<String,Object> ret = new HashMap<>();
		Allenatori findByNome = allenatoriRepository.findByNome(registraUtente);
		if (findByNome == null) {
			Allenatori allenatori = new Allenatori();
			//		allenatori.setIsAdmin(false);
			allenatori.setNome(registraUtente);
			allenatori.setPwd(criptaggio.encrypt(registraPwd,registraUtente));
			allenatoriRepository.save(allenatori);
			ret.put("stato", "OK");
		}
		else {
			ret.put("stato", "Errore!! Utente esistente");
		}
		return ret;
	}
	@PostMapping("/login")
	public Map<String,Object>  login(@RequestBody Map<String, String> body) throws Exception {
		String loginUtente = body.get("loginUtente");
		String loginPwd = body.get("loginPwd");
		if (loginPwd==null) loginPwd="";
		Allenatori findByNome = allenatoriRepository.findByNome(loginUtente);
		Map<String,Object> ret = new HashMap<>();
		String stato = "Utente non trovato";
		if (findByNome != null ) stato = "Password errata";
		if (findByNome != null && cripta(loginPwd,loginUtente).get("value").equals(findByNome.getPwd())) {
			ret.put("allenatore", findByNome);
			stato="OK";
		}
		ret.put("stato", stato);
		return ret;
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
		return allenatoriRepository.findAll();
	}	
	@GetMapping(path="/getAllenatoriByLega")
	public @ResponseBody Iterable<AllenatoriDTO> getAllenatoriByLega(Leghe lega) {
		List<AllenatoriDTO> ret = new ArrayList<>();
		Iterable<LegheAllenatori> findByLega = allenatoriRepository.findByLega(lega);
		for (LegheAllenatori legheAllenatori : findByLega) {
			AllenatoriDTO a = new AllenatoriDTO();
			a.setId(legheAllenatori.getAllenatori().getId());
			a.setNome(legheAllenatori.getAllenatori().getNome());
			a.setAlias(legheAllenatori.getAlias());
			a.setIsAdmin(legheAllenatori.isAdmin());
			a.setIsFittizio(legheAllenatori.isFittizio());
			ret.add(a);
		}
		return ret;
	}	
	@GetMapping(path="/allLeghe")
	public @ResponseBody Iterable<Leghe> getAllLeghe() {
		return legheRepository.findAll();
	}	
	
	
	@GetMapping(path="/allFantarose")
	public @ResponseBody Iterable<Fantarose> getAllFantarose() {
		return fantaroseRepository.findAll();
	}	

	@GetMapping(path="/allGiocatori")
	public @ResponseBody Iterable<Giocatori> getAllGiocatori() {
		return giocatoriRepository.findAll();
	}	

	@GetMapping(path="/configurazione")
	public @ResponseBody Configurazione getConfigurazione() {
		return configurazioneRepository.findOne(1);
	}	
	
	@GetMapping(path="/giocatoriLiberi")
	public @ResponseBody List<Map<String, Object>> getGiocatoriLiberi(@RequestParam(name = "legaUtente") String legaUtente) {

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

}
