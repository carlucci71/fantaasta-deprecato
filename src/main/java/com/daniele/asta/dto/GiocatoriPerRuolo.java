package com.daniele.asta.dto;

public class GiocatoriPerRuolo {
	public GiocatoriPerRuolo(Long conta, String ruolo, String nome) {
		super();
		this.conta = conta;
		this.ruolo = ruolo;
		this.nome = nome;
	}
	private Long conta;
	private String ruolo;
	private String nome;
	
	public Long getConta() {
		return conta;
	}
	public void setConta(Long conta) {
		this.conta = conta;
	}
	public String getRuolo() {
		return ruolo;
	}
	public void setRuolo(String ruolo) {
		this.ruolo = ruolo;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	@Override
	public String toString() {
		return "GiocatoriPerRuolo [conta=" + conta + ", ruolo=" + ruolo + ", nome=" + nome + "]";
	}

	
}