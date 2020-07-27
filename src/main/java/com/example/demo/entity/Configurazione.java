package com.example.demo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Configurazione {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}


	public Integer getNumeroGiocatori() {
		return numeroGiocatori;
	}
	public void setNumeroGiocatori(Integer numeroGiocatori) {
		this.numeroGiocatori = numeroGiocatori;
	}


	@Column(nullable = true)
	private Integer numeroGiocatori;
}
