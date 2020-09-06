package com.daniele.asta.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.daniele.asta.dto.GiocatoriPerSquadra;
import com.daniele.asta.dto.SpesoTotale;
import com.daniele.asta.entity.Fantarose;


public interface FantaroseRepository extends CrudRepository<Fantarose, Integer> {
	
	@Query("from Fantarose where id=?1")
	String getFromFantarose(String idGiocatore);
	
	@Query("SELECT new com.daniele.asta.dto.SpesoTotale(a.nome, g.macroRuolo, SUM(f.costo),COUNT(g.macroRuolo) as conta) FROM Fantarose f, Allenatori a, Giocatori g WHERE  a.id = f.idAllenatore AND f.idGiocatore=g.id GROUP BY a.nome, g.macroRuolo")
	Iterable<SpesoTotale> spesoTotale();
	
	@Query("SELECT  new com.daniele.asta.dto.GiocatoriPerSquadra(a.nome as allenatore,g.squadra,g.ruolo,g.macroRuolo,g.nome as giocatore,f.costo) from Fantarose f,Giocatori g,Allenatori a where g.id = idGiocatore and a.id = idAllenatore order by a.ordine,g.macroRuolo desc,g.ruolo desc,giocatore")
	Iterable<GiocatoriPerSquadra> giocatoriPerSquadra();

}