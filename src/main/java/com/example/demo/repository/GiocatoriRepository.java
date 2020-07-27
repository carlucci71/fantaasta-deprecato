package com.example.demo.repository;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.demo.entity.Giocatori;


public interface GiocatoriRepository extends CrudRepository<Giocatori, Integer> {
	
	@Query("select id,squadra,nome,ruolo,quotazione from Giocatori g where not exists (select 1 from Fantarose f where g.id=f.idGiocatore)")
	List<Object[]> getGiocatoriLiberi();

//	Iterable<Giocatori>  getGiocatoriLiberi();
	
}