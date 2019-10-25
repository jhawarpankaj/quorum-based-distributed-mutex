package edu.utd.aos.mutex.dto;

import lombok.Data;

@Data
public class MasterDetails {
	
	/**
	 * Name of the server.
	 */
	String name;
	
	/**
	 * Server port.
	 */
	String port;
	
	/**
	 * Path of the file.
	 */
	String filepath;
	
}
