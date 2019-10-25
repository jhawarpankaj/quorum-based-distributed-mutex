package edu.utd.aos.mutex.core;


public class ClientRequestQueue implements Comparable<ClientRequestQueue>{
	
	private long timestamp;
	private int id;
	
	public ClientRequestQueue(long time, int clientId) {
		super();
		timestamp = time;
		id = clientId;
	}
	
	@Override
	public int compareTo(ClientRequestQueue obj) {
		return Long.compare(this.getTimeStamp(), obj.getTimeStamp());
	}
	
	public long getTimeStamp() {
		return timestamp;
	}
	
	public int getId() {
		return id;
	}
	
}