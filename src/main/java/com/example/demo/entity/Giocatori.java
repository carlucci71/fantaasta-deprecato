package com.example.demo.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

//@Entity(name = "giocatori")
//@Table(name = "giocatori")
@Entity
public class Giocatori {

	@Id
	private Integer id;
	private String squadra;
	private String nome;
	private String ruolo;
	private Integer quotazione;
	@Override
	public String toString() {
		return "Giocatori [id=" + id + ", squadra=" + squadra + ", nome=" + nome + ", ruolo=" + ruolo + ", quotazione="
				+ quotazione + "]";
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getSquadra() {
		return squadra;
	}
	public void setSquadra(String squadra) {
		this.squadra = squadra;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getRuolo() {
		return ruolo;
	}
	public void setRuolo(String ruolo) {
		this.ruolo = ruolo;
	}
	public Integer getQuotazione() {
		return quotazione;
	}
	public void setQuotazione(Integer quotazione) {
		this.quotazione = quotazione;
	}
}
