package com.example.demo.dto;

public class AllenatoriDTO {
	private Integer id;
	private String nome;
	private String alias;
	private Boolean isAdmin;
	private Boolean isFittizio;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public Boolean getIsAdmin() {
		return isAdmin;
	}
	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public Boolean getIsFittizio() {
		return isFittizio;
	}
	public void setIsFittizio(Boolean isFittizio) {
		this.isFittizio = isFittizio;
	}
	@Override
	public String toString() {
		return "AllenatoriDTO [id=" + id + ", nome=" + nome + ", alias=" + alias + ", isAdmin=" + isAdmin
				+ ", isFittizio=" + isFittizio + "]";
	}

}
