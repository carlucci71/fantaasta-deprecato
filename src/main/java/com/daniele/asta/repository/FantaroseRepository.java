package com.daniele.asta.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.daniele.asta.entity.Fantarose;


public interface FantaroseRepository extends CrudRepository<Fantarose, Integer> {
	
	@Query("from Fantarose where id=?1")
	String getFromFantarose(String idGiocatore);
	

}