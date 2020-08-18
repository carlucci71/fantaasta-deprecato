package com.example.demo.entity;

import java.util.ArrayList;
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

public class Leghe {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	private String nome;
	private String pwd;
	private Integer numUtenti;
/*	
	@ManyToMany
	private
    Set<Allenatori> allenatori=new HashSet<>();	
	public Set<Allenatori> getAllenatori() {
		return allenatori;
	}
	public void setAllenatori(Set<Allenatori> allenatori) {
		this.allenatori = allenatori;
	}
*/	
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
	public Integer getNumUtenti() {
		return numUtenti;
	}
	public void setNumUtenti(Integer numUtenti) {
		this.numUtenti = numUtenti;
	}
	@OneToMany(mappedBy = "allenatori")
    private List<LegheAllenatori> allenatori=new ArrayList<LegheAllenatori>();
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	@Override
	public String toString() {
		return "Leghe [id=" + id + ", nome=" + nome + ", pwd=" + pwd + ", numUtenti=" + numUtenti + ", allenatori="
				+ allenatori + "]";
	}

}
