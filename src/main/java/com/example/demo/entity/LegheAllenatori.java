package com.example.demo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "leghe_allenatori")
@IdClass(LegheAllenatoriId.class)
public class LegheAllenatori {

    @Id
    @ManyToOne
    @JoinColumn(name = "leghe_id", referencedColumnName = "id")
    private Leghe leghe;

    @Id
    @ManyToOne
    @JoinColumn(name = "allenatori_id", referencedColumnName = "id")
    private Allenatori allenatori;

    @Column(name = "is_admin")
    private boolean isAdmin;
    @Column(name = "is_fittizio")
    private boolean isFittizio;
    @Column(name = "alias")
    private String alias;

	public Leghe getLeghe() {
		return leghe;
	}

	public void setLeghe(Leghe leghe) {
		this.leghe = leghe;
	}

	public Allenatori getAllenatori() {
		return allenatori;
	}

	public void setAllenatori(Allenatori allenatori) {
		this.allenatori = allenatori;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public boolean isFittizio() {
		return isFittizio;
	}

	public void setFittizio(boolean isFittizio) {
		this.isFittizio = isFittizio;
	}
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		return "LegheAllenatori [leghe=" + leghe + ", allenatori=" + allenatori + ", isAdmin=" + isAdmin
				+ ", isFittizio=" + isFittizio + ", alias=" + alias + "]";
	}
}
