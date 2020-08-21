package com.daniele.asta.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
//@Table(name = "allenatori")
public class Allenatori {

	@Id
	private Integer id;
	private String nuovoNome;
	private String nome;
	private String pwd;
	private Boolean isAdmin;
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
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	@Override
	public String toString() {
		return "Allenatori [id=" + id + ", nome=" + nome + ", pwd=" + pwd + ", isAdmin=" + isAdmin + "]";
	}
	public String getNuovoNome() {
		return nuovoNome;
	}
	public void setNuovoNome(String nuovoNome) {
		this.nuovoNome = nuovoNome;
	}
}
