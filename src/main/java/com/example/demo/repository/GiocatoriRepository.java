package com.example.demo.repository;


import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.demo.entity.Giocatori;
import com.example.demo.entity.Leghe;


public interface GiocatoriRepository extends CrudRepository<Giocatori, Integer> {

	@Query("select id,squadra,nome,ruolo,quotazione from Giocatori g where not exists (select 1 from Fantarose f where g.id=f.idGiocatore)")
	List<Object[]> getGiocatoriLiberi();

	@Transactional
	@Modifying
	@Query("delete from Giocatori where leghe_id=?1")
	int deleteGiocatoryByLegaId(Integer leghe_id);

	@Query("from Giocatori where leghe_id=?1 and id=?2")
	Giocatori findGiocatoriByLegheAndId(Integer leghe_id, Integer id);


	/*
	@Transactional
	@Modifying
	@Query("delete from Giocatori g where g.leghe.id=:#{#leghe.id}")
	void deleteGiocatoryByLega(@Param("leghe") Leghe leghe);
	*/
}