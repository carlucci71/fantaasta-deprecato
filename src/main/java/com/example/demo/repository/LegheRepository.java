package com.example.demo.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.demo.entity.Leghe;

public interface LegheRepository extends CrudRepository<Leghe, Integer> 
{
	@Query("from Leghe where nome=?1")
	Leghe findByNome(String nome);
	
}