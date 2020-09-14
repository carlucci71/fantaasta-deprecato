package com.daniele.asta;

public class LogSocket {
	private String id;
	private String hand;
	private String local;
	private String remote;
	private boolean open;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHand() {
		return hand;
	}
	public void setHand(String hand) {
		this.hand = hand;
	}
	public String getLocal() {
		return local;
	}
	public void setLocal(String local) {
		this.local = local;
	}
	public String getRemote() {
		return remote;
	}
	public void setRemote(String remote) {
		this.remote = remote;
	}
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}
	@Override
	public String toString() {
		return "LogSocket [id=" + id + ", hand=" + hand + ", local=" + local + ", remote=" + remote + ", open=" + open
				+ "]";
	}

	

	
	
	
}
