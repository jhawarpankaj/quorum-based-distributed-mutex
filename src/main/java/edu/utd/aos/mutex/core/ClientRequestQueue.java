package edu.utd.aos.mutex.core;


/**
 * Class to order clients request in queue sorted according to timestamp.
 * @author pankaj
 * 
 */
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