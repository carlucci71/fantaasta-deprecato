package com.daniele.asta.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Configurazione {

	@Id
	private Integer id;
	private Boolean isATurni;
	private boolean isMantra;
	private Integer budget;
	private Integer durataAsta;
	private Integer numeroAcquisti;
	private Integer numeroMinAcquisti;
	
	private Integer maxP;
	private Integer maxD;
	private Integer maxC;
	private Integer maxA;
	
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
	public boolean isMantra() {
		return isMantra;
	}
	public void setMantra(boolean isMantra) {
		this.isMantra = isMantra;
	}

	public Integer getMaxP() {
		return maxP;
	}
	public void setMaxP(Integer maxP) {
		this.maxP = maxP;
	}
	public Integer getMaxD() {
		return maxD;
	}
	public void setMaxD(Integer maxD) {
		this.maxD = maxD;
	}
	public Integer getMaxC() {
		return maxC;
	}
	public void setMaxC(Integer maxC) {
		this.maxC = maxC;
	}
	public Integer getMaxA() {
		return maxA;
	}
	public void setMaxA(Integer maxA) {
		this.maxA = maxA;
	}
	public Integer getNumeroMinAcquisti() {
		return numeroMinAcquisti;
	}
	public void setNumeroMinAcquisti(Integer numeroMinAcquisti) {
		this.numeroMinAcquisti = numeroMinAcquisti;
	}

	public Integer getDurataAsta() {
		return durataAsta;
	}
	public void setDurataAsta(Integer durataAsta) {
		this.durataAsta = durataAsta;
	}
	@Override
	public String toString() {
		return "Configurazione [id=" + id + ", isATurni=" + isATurni + ", isMantra=" + isMantra + ", budget=" + budget
				+ ", durataAsta=" + durataAsta + ", numeroAcquisti=" + numeroAcquisti
				+ ", numeroMinAcquisti=" + numeroMinAcquisti + ", maxP=" + maxP + ", maxD=" + maxD + ", maxC=" + maxC
				+ ", maxA=" + maxA + ", numeroGiocatori=" + numeroGiocatori + "]";
	}

}
