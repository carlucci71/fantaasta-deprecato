package com.example.demo.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.entity.Logger;
public interface LoggerRepository extends CrudRepository<Logger, Integer> 
{
	
}