package com.daniele.asta.entity;

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
	private Boolean isATurni;
	private Integer budget;
	private Integer numeroAcquisti;
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


	public Boolean getIsATurni() {
		return isATurni;
	}
	public void setIsATurni(Boolean isATurni) {
		this.isATurni = isATurni;
	}

	@Column(nullable = true)
	private Integer numeroGiocatori;
	public Integer getBudget() {
		return budget;
	}
	public void setBudget(Integer budget) {
		this.budget = budget;
	}
	public Integer getNumeroAcquisti() {
		return numeroAcquisti;
	}
	public void setNumeroAcquisti(Integer numeroAcquisti) {
		this.numeroAcquisti = numeroAcquisti;
	}
	@Override
	public String toString() {
		return "Configurazione [id=" + id + ", isATurni=" + isATurni + ", budget=" + budget + ", numeroAcquisti="
				+ numeroAcquisti + ", numeroGiocatori=" + numeroGiocatori + "]";
	}

}
