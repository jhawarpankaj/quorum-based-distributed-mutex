package edu.utd.aos.mutex.dto;

import lombok.Data;

/**
 * Details of servers.
 * 
 * @author pankaj
 */
@Data
public class ServerDetails {
	
	/**
	 * Name of the server.
	 */
	String name;
	
	/**
	 * Port of the server.
	 */
	String port;
	
	/**
	 * Id of the server.
	 */
	int id;
	
}
