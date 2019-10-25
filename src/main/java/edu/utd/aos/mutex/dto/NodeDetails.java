package edu.utd.aos.mutex.dto;

import java.util.List;

import lombok.Data;

/**
 * Details of all nodes.
 * 
 * @author pankaj
 */
@Data
public class NodeDetails {
	
	/**
	 * Master Server Details.
	 */
	MasterDetails master;
	
	/**
	 * Server Details.
	 */
	List<ServerDetails> servers;
	
	/**
	 * Client Details.
	 */
	List<ClientDetails> clients;
}
