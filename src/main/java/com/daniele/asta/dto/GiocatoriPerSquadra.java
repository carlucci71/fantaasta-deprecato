package com.daniele.asta.dto;

public class GiocatoriPerSquadra {
	
	private String allenatore;
	private String squadra;
	private String ruolo;
	private String giocatore;
	private int costo;
	private String sqlTime;
	
	public GiocatoriPerSquadra(String allenatore, String squadra, String ruolo, String giocatore, int costo, String sqlTime) {
		super();
		this.allenatore = allenatore;
		this.squadra = squadra;
		this.ruolo = ruolo;
		this.giocatore = giocatore;
		this.costo = costo;
		this.sqlTime = sqlTime;
	}
	public String getAllenatore() {
		return allenatore;
	}
	public void setAllenatore(String allenatore) {
		this.allenatore = allenatore;
	}
	public String getSquadra() {
		return squadra;
	}
	public void setSquadra(String squadra) {
		this.squadra = squadra;
	}
	public String getRuolo() {
		return ruolo;
	}
	public void setRuolo(String ruolo) {
		this.ruolo = ruolo;
	}
	public String getGiocatore() {
		return giocatore;
	}
	public void setGiocatore(String giocatore) {
		this.giocatore = giocatore;
	}
	public int getCosto() {
		return costo;
	}
	public void setCosto(int costo) {
		this.costo = costo;
	}
	public String getSqlTime() {
		return sqlTime;
	}
	public void setSqlTime(String sqlTime) {
		this.sqlTime = sqlTime;
	}
	@Override
	public String toString() {
		return "GiocatoriPerSquadra [allenatore=" + allenatore + ", squadra=" + squadra + ", ruolo=" + ruolo
				+ ", giocatore=" + giocatore + ", costo=" + costo + ", sqlTime=" + sqlTime + "]";
	}

}
