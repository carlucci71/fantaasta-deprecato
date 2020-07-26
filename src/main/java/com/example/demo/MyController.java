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
	public MyController() {
		initDb();
	}

	@RequestMapping("/init")
	public Map<String, Object> init() {
		Map<String, Object> m = new HashMap<>();
		String giocatoreLoggato = (String) httpSession.getAttribute("giocatoreLoggato");
		String idLoggato = (String) httpSession.getAttribute("idLoggato");
		if (giocatoreLoggato != null) {
			m.put("giocatoreLoggato", giocatoreLoggato);
			m.put("idLoggato", idLoggato);
		}
		m.put("elencoAllenatori", elencoAllenatori());
		return m;
	}


	@PostMapping("/confermaAsta")
	public void confermaAsta(@RequestBody Map<String, Object> body) throws Exception {
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
		psInserisciCalciaotre.executeUpdate();
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


	private void initDb() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String server ="jdbc:mysql://localhost:3306/asta?user=almaviva&password=almaviva";
			Connection conn = DriverManager.getConnection(server);
			psElencoCalciatori = conn.prepareStatement("select * from giocatori g where not exists (select 1 from fantarose where id=idGiocatore)");
			psInserisciCalciaotre = conn.prepareStatement(" insert into fantarose (idGiocatore , idAllenatore , Costo , sqltime ) values (?,?,?,?);");
			psElencoAllenatori = conn.prepareStatement("select * from allenatori order by id");
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/*
	public HttpSession getSession()
	{
		try
		{
			RequestAttributes parentAttrs = RequestContextHolder.currentRequestAttributes();
			System.out.println(parentAttrs);
			ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
			return attr.getRequest().getSession();
		}
		catch (Exception e)
		{
			System.out.println(httpSession);
			return httpSession;
//			throw new RuntimeException(e);
		}
	}	
		*/

}
