package com.daniele.asta.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.daniele.asta.dto.GiocatoriPerRuolo;
import com.daniele.asta.dto.GiocatoriPerSquadra;
import com.daniele.asta.dto.SpesoPerRuolo;
import com.daniele.asta.entity.Fantarose;


public interface FantaroseRepository extends CrudRepository<Fantarose, Integer> {
	
	@Query("from Fantarose where id=?1")
	String getFromFantarose(String idGiocatore);
	
	@Query("SELECT new com.daniele.asta.dto.SpesoPerRuolo(a.nome, SUM(f.costo),COUNT(f.costo) as conta) FROM Fantarose f, Allenatori a WHERE  a.id = f.idAllenatore GROUP BY a.nome")
	Iterable<SpesoPerRuolo> spesoPerRuolo();
	
	@Query("SELECT new com.daniele.asta.dto.GiocatoriPerRuolo(COUNT(g.ruolo) as conta,g.ruolo,a.nome) from Fantarose f,Allenatori a,Giocatori g where g.id=f.idGiocatore and a.id = f.idAllenatore group by a.nome,g.ruolo order by a.nome,g.ruolo desc")
	List<GiocatoriPerRuolo> contaGiocatoriPerRuolo();
	
	@Query("SELECT  new com.daniele.asta.dto.GiocatoriPerSquadra(a.nome as allenatore,g.squadra,g.ruolo,g.nome as giocatore,f.costo) from Fantarose f,Giocatori g,Allenatori a where g.id = idGiocatore and a.id = idAllenatore order by a.ordine,ruolo desc,giocatore")
	Iterable<GiocatoriPerSquadra> giocatoriPerSquadra();

}