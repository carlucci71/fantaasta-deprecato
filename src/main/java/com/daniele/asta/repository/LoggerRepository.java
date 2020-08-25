package com.daniele.asta.repository;

import org.springframework.data.repository.CrudRepository;

import com.daniele.asta.entity.LoggerMessaggi;
public interface LoggerRepository extends CrudRepository<LoggerMessaggi, Integer> 
{
	
}