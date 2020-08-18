package com.example.demo.entity;

import java.io.Serializable;

public class LegheAllenatoriId implements Serializable {

    private int leghe;
    private int allenatori;
	public int getLeghe() {
		return leghe;
	}
	public void setLeghe(int leghe) {
		this.leghe = leghe;
	}
	public int getAllenatori() {
		return allenatori;
	}
	public void setAllenatori(int allenatori) {
		this.allenatori = allenatori;
	}
	@Override
	public String toString() {
		return "LegheAllenatoriId [leghe=" + leghe + ", allenatori=" + allenatori + "]";
	}

    // getters/setters and most importantly equals() and hashCode()
}