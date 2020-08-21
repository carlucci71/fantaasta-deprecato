package com.daniele.asta.repository;

import org.springframework.data.repository.CrudRepository;

import com.daniele.asta.entity.Logger;
public interface LoggerRepository extends CrudRepository<Logger, Integer> 
{
	
}