package com.example.demo.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;

//@Entity(name = "giocatori")
//@Table(name = "giocatori")
@Entity
@IdClass(GiocatoriLeghe.class)
public class Giocatori {

	@Id
	private Integer id;
	private String squadra;
	private String nome;
	private String ruolo;
	private Integer quotazione;
//	@ManyToOne
	@Id
	private Integer legheId;
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
	public Integer getLegheId() {
		return legheId;
	}
	public void setLegheId(Integer legheId) {
		this.legheId = legheId;
	}
	@Override
	public String toString() {
		return "Giocatori [id=" + id + ", squadra=" + squadra + ", nome=" + nome + ", ruolo=" + ruolo + ", quotazione="
				+ quotazione + ", legheId=" + legheId + "]";
	}
}
