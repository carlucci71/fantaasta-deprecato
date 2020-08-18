package com.example.demo.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.demo.entity.Allenatori;
import com.example.demo.entity.Leghe;
import com.example.demo.entity.LegheAllenatori;
public interface AllenatoriRepository extends CrudRepository<Allenatori, Integer> 
{
	
	@Query("from Allenatori where nome=?1")
	Allenatori findByNome(String nome);

	@Query("from LegheAllenatori where leghe=?1")
	Iterable<LegheAllenatori> findByLega(Leghe leghe);
	
	
	
}