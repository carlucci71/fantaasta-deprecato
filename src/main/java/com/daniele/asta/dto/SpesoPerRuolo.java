package com.daniele.asta.dto;

public class SpesoPerRuolo {
	public SpesoPerRuolo(String nome,Long costo, Long conta) {
		super();
		this.setNome(nome);
		this.setCosto(costo);
		this.setConta(conta);
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public Long getConta() {
		return conta;
	}
	public void setConta(Long conta) {
		this.conta = conta;
	}
	public Long getCosto() {
		return costo;
	}
	public void setCosto(Long costo) {
		this.costo = costo;
	}
	private String nome;
	private Long conta;
	private Long costo;
	
	@Override
	public String toString() {
		return "SpesoPerRuolo [nome=" + nome + ", conta=" + conta + ", costo=" + costo + "]";
	}
	

	
}