package com.example.demo.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity

@Table(
	    uniqueConstraints=
	        @UniqueConstraint(columnNames={"nome"})
	)
//@Table(name = "allenatori")
public class Allenatori {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	private String nome;
	private String pwd;
//	private Boolean isAdmin;

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
//	public Boolean getIsAdmin() {
//		return isAdmin;
//	}
//	public void setIsAdmin(Boolean isAdmin) {
//		this.isAdmin = isAdmin;
//	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	@OneToMany(mappedBy = "leghe")
	private List<LegheAllenatori> leghe;

	@Override
	public String toString() {
		return "Allenatori [id=" + id + ", nome=" + nome + ", pwd=" + pwd + ", leghe=" + leghe + "]";
	}
	
	
}
