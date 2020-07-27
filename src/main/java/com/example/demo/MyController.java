package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@RequestMapping({ "/" })
public class MyController {

	@Autowired Environment environment;	
	@Autowired HttpSession httpSession;

	PreparedStatement psElencoCalciatori;
	PreparedStatement psInserisciCalciaotre;
	PreparedStatement psElencoAllenatori;
	PreparedStatement psElencoOffertePerAllenatore;
	PreparedStatement psElencoCronologiaOfferte;
	PreparedStatement psRiepilogoAllenatori;
	PreparedStatement psSpesoAllenatori;
	
	public MyController() {
		initDb();
	}

	@RequestMapping("/init")
	public Map<String, Object> init() {
		Map<String, Object> m = new HashMap<>();
		String giocatoreLoggato = (String) httpSession.getAttribute("giocatoreLoggato");
//		System.out.println(httpSession.getId() + "-" + giocatoreLoggato + "-" + "init");
		String idLoggato = (String) httpSession.getAttribute("idLoggato");
		if (giocatoreLoggato != null) {
			m.put("giocatoreLoggato", giocatoreLoggato);
			m.put("idLoggato", idLoggato);
		}
		m.put("elencoAllenatori", elencoAllenatori());
		return m;
	}


	@PostMapping("/confermaAsta")
	public int confermaAsta(@RequestBody Map<String, Object> body) throws Exception {
		String nomegiocatore = (String) body.get("nomegiocatore");
		String idgiocatore = (String) body.get("idgiocatore");
		Integer offerta = (Integer) body.get("offerta");
		String nomeCalciatore = (String) body.get("nomeCalciatore");
		String idCalciatore = (String) body.get("idCalciatore");
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String stm = sdf.format(c.getTime());
		psInserisciCalciaotre.setString(1, idCalciatore);
		psInserisciCalciaotre.setString(2, idgiocatore);
		psInserisciCalciaotre.setLong(3, offerta);
		psInserisciCalciaotre.setString(4, stm);
		int executeUpdate = psInserisciCalciaotre.executeUpdate();
		return executeUpdate;
	}

	@RequestMapping("/elencoCalciatori")
	public List<Map<String, Object>>  elencoCalciatori() {
		try {
			ResultSet rs = psElencoCalciatori.executeQuery();
			List<Map<String, Object>> l = new ArrayList<>();
			while (rs.next()) {
				Map<String, Object> m = new HashMap<>();
				m.put("id", rs.getLong("Id"));
				m.put("squadra", rs.getString("Squadra"));
				m.put("nome", rs.getString("Nome"));
				m.put("ruolo", rs.getString("Ruolo"));
				m.put("quotazione", rs.getLong("quotazione"));
				l.add(m);
			}
			return l;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@RequestMapping("/elencoOfferte")
	public List<Map<String, Object>>  elencoOfferte() {
		try {
			ResultSet rs = psElencoOffertePerAllenatore.executeQuery();
			List<Map<String, Object>> l = new ArrayList<>();
			while (rs.next()) {
				Map<String, Object> m = new HashMap<>();
				m.put("allenatore", rs.getString("allenatore"));
				m.put("squadra", rs.getString("Squadra"));
				m.put("ruolo", rs.getString("Ruolo"));
				m.put("giocatore", rs.getString("giocatore"));
				m.put("costo", rs.getLong("Costo"));
				m.put("sqlTime", rs.getString("sqlTime"));
				l.add(m);
			}
			return l;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@RequestMapping("/riepilogoAllenatori")
	public List<Map<String, Object>>  riepilogoAllenatori() {
		try {
			ResultSet rs = psRiepilogoAllenatori.executeQuery();
			List<Map<String, Object>> l = new ArrayList<>();
			while (rs.next()) {
				Map<String, Object> m = new HashMap<>();
				m.put("conta", rs.getLong("conta"));
				m.put("ruolo", rs.getString("ruolo"));
				m.put("nome", rs.getString("nome"));
				l.add(m);
			}
			return l;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@RequestMapping("/spesoAllenatori")
	public List<Map<String, Object>>  spesoAllenatori() {
		try {
			ResultSet rs = psSpesoAllenatori.executeQuery();
			List<Map<String, Object>> l = new ArrayList<>();
			while (rs.next()) {
				Map<String, Object> m = new HashMap<>();
				m.put("costo", rs.getLong("costo"));
				m.put("nome", rs.getString("nome"));
				l.add(m);
			}
			return l;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static List<Map<String, Object>> elencoAllenatoriCache=null;
	
	@RequestMapping("/elencoAllenatori")
	public List<Map<String, Object>>  elencoAllenatori() {
		if (elencoAllenatoriCache != null) return elencoAllenatoriCache;
		try {
			ResultSet rs = psElencoAllenatori.executeQuery();
			List<Map<String, Object>> l = new ArrayList<>();
			while (rs.next()) {
				Map<String, Object> m = new HashMap<>();
				m.put("id", rs.getLong("Id"));
				m.put("nome", rs.getString("Nome"));
				l.add(m);
			}
			elencoAllenatoriCache=l;
			return l;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@RequestMapping("/elencoCronologiaOfferte")
	public List<Map<String, Object>>  elencoCronologiaOfferte() {
		try {
			ResultSet rs = psElencoCronologiaOfferte.executeQuery();
			List<Map<String, Object>> l = new ArrayList<>();
			while (rs.next()) {
				Map<String, Object> m = new HashMap<>();
				m.put("allenatore", rs.getString("allenatore"));
				m.put("squadra", rs.getString("Squadra"));
				m.put("ruolo", rs.getString("Ruolo"));
				m.put("giocatore", rs.getString("giocatore"));
				m.put("costo", rs.getLong("Costo"));
				m.put("sqlTime", rs.getString("sqlTime"));
				m.put("idGiocatore", rs.getString("idGiocatore"));
				m.put("idAllenatore", rs.getString("idAllenatore"));
				l.add(m);
			}
			return l;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private void initDb() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String server ="jdbc:mysql://localhost:3306/asta?user=almaviva&password=almaviva";
			Connection conn = DriverManager.getConnection(server);
			psElencoCalciatori = conn.prepareStatement("select * from giocatori g where not exists (select 1 from fantarose where id=idGiocatore)");
			psInserisciCalciaotre = conn.prepareStatement(" insert into fantarose (idGiocatore , idAllenatore , Costo , sqltime ) values (?,?,?,?);");
			psElencoAllenatori = conn.prepareStatement("select * from allenatori order by id");
			psElencoOffertePerAllenatore = conn.prepareStatement("select a.Nome allenatore, g.Squadra, g.Ruolo, g.nome giocatore, Costo, sqlTime from fantarose f, giocatori g, allenatori a where g.id = idGiocatore and a.id = idAllenatore order by allenatore, ruolo desc, giocatore");
			psRiepilogoAllenatori = conn.prepareStatement("select count(ruolo) conta, ruolo, a.nome nome from fantarose f, allenatori a, giocatori g where g.id=idGiocatore and a.id = idAllenatore group by a.nome ,ruolo order by a.nome, ruolo desc");
			psSpesoAllenatori = conn.prepareStatement("select sum(costo) costo, a.nome from fantarose f, allenatori a where a.id = idAllenatore group by a.nome");
			psElencoCronologiaOfferte = conn.prepareStatement("select a.Nome allenatore, g.Squadra, g.Ruolo, g.nome giocatore, Costo, sqlTime, idGiocatore, idAllenatore   from  fantarose f, giocatori g, allenatori a  where g.id = idGiocatore and a.id = idAllenatore order by sqlTime desc");
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
