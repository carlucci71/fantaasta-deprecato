package com.example.demo.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.demo.entity.LegheAllenatori;
import com.example.demo.entity.LegheAllenatoriId;

public interface LegheAllenatoriRepository extends CrudRepository<LegheAllenatori, Integer> 
{
	@Query("from LegheAllenatori where id=?1")
	LegheAllenatori findById(LegheAllenatoriId legheAllenatoriId);
	
	

}