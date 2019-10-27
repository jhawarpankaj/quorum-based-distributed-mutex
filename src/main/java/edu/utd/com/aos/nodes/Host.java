package edu.utd.com.aos.nodes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tinylog.Logger;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.utd.aos.mutex.dto.ApplicationConfig;
import edu.utd.aos.mutex.dto.ClientDetails;
import edu.utd.aos.mutex.dto.MasterDetails;
import edu.utd.aos.mutex.dto.NodeDetails;
import edu.utd.aos.mutex.dto.ServerDetails;
import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.aos.mutex.references.MutexConfigHolder;

public class Host {
	
	private static String name;
	private static String port;
	private static int id;
	private static String filepath;
	private static String filename;
	public enum Node {
		MASTER, SERVER, CLIENT
	};
	private static Node type;
	private static Table<Integer, String, String> allServerDetails = HashBasedTable.create();
	private static Table<Integer, String, String> allClientDetails = HashBasedTable.create();
	
	/**
	 * Initialization for all host details.
	 * 
	 * @throws MutexException
	 */
	public static void initialize() throws MutexException {
		Logger.info("Initializing all local host related configuration");
		setLocalHostPort();
		createServerList();
		createClientList();
		Logger.info("All localhost initialization complete.");
	}
	
	/**
	 * Set the current host name.
	 * 
	 * @throws MutexException
	 */
	private static void setLocalHostPort() throws MutexException {
		Logger.info("Setting local host and port...");
		InetAddress ip;
		
		ApplicationConfig applicationConfig = MutexConfigHolder.getApplicationConfig();
		NodeDetails nodeDetails = applicationConfig.getNodeDetails();
		MasterDetails masterDetails = nodeDetails.getMaster();
		List<ServerDetails> serverDetails = nodeDetails.getServers();
		List<ClientDetails> clientDetails = nodeDetails.getClients();	
		try {
			ip = InetAddress.getLocalHost();
			name = ip.getHostName().toLowerCase();
		} catch (UnknownHostException e) {
			throw new MutexException("Error while fetching the hostname. " + e);
		}
		boolean flag = false;
		
		if(name.equalsIgnoreCase(masterDetails.getName())){
			type = Node.MASTER;
			port = masterDetails.getPort();
			filepath = masterDetails.getFilepath();
			filename = masterDetails.getFilename();
			return;
		}
		
		for(ServerDetails server: serverDetails) {
			if(name.equalsIgnoreCase(server.getName())) {
				type = Node.SERVER;
				port = server.getPort();
				id = server.getId();
				flag = true;
				break;
			}
		}
		if(!flag) {
			for(ClientDetails client: clientDetails) {
				if(name.equalsIgnoreCase(client.getName())) {
					flag = true;
					type = Node.CLIENT;
					port = client.getPort();
					id = client.getId();
					break;
				}
			}
		}
		if(!flag) {
			throw new MutexException("The code is being run on unkown machines.");
		}
		Logger.info("Local host and port initialized...");
	}
	
	/**
	 * Set all server names list.
	 */
	private static void createServerList() {
		
		Logger.info("Set all servers list.");
		ApplicationConfig applicationConfig = MutexConfigHolder.getApplicationConfig();
		NodeDetails nodeDetails = applicationConfig.getNodeDetails();
		List<ServerDetails> serverDetails = nodeDetails.getServers();
		for(ServerDetails server: serverDetails) {
			allServerDetails.put(server.getId(), server.getName(), server.getPort());
		}
		Logger.info("Initialized server list: " + allServerDetails);
	}
	
	/**
	 * Set all client names list.
	 */
	private static void createClientList() {
		
		Logger.info("Set all clients list.");
		ApplicationConfig applicationConfig = MutexConfigHolder.getApplicationConfig();
		NodeDetails nodeDetails = applicationConfig.getNodeDetails();
		List<ClientDetails> clientDetails = nodeDetails.getClients();
		for(ClientDetails client: clientDetails) {
			allClientDetails.put(client.getId(), client.getName(), client.getPort());
		}
		Logger.info("Initialized server list: " + allClientDetails);
	}
	
	public static Map<String, String> getClientById(int id) {
		Map<String, String> row = allClientDetails.row(id);
		return row;
	}
	
	public static Map<String, String> getServerById(int id) {
		Map<String, String> row = allServerDetails.row(id);
		return row;
	}
	
	public static Integer getServerIdFromName(String name) {
		
		ApplicationConfig applicationConfig = MutexConfigHolder.getApplicationConfig();
		NodeDetails nodeDetails = applicationConfig.getNodeDetails();
		List<ServerDetails> serverDetails = nodeDetails.getServers();
		for(ServerDetails server: serverDetails) {
			if(server.getName().equalsIgnoreCase(name)) {
				return server.getId();
			}
		}
		return null;
	}
	
	public static Integer getClientIdFromName(String name) {
		
		ApplicationConfig applicationConfig = MutexConfigHolder.getApplicationConfig();
		NodeDetails nodeDetails = applicationConfig.getNodeDetails();
		List<ClientDetails> clientDetails = nodeDetails.getClients();
		for(ClientDetails client: clientDetails) {
			if(client.getName().equalsIgnoreCase(name)) {
				return client.getId();
			}
		}
		return null;
	}
	
	public static Integer getClientPortByName(String name) {
		
		ApplicationConfig applicationConfig = MutexConfigHolder.getApplicationConfig();
		NodeDetails nodeDetails = applicationConfig.getNodeDetails();
		List<ClientDetails> clients = nodeDetails.getClients();
		for(ClientDetails clientDetails: clients) {
			if(name.equalsIgnoreCase(clientDetails.getName())) {
				return Integer.valueOf(clientDetails.getPort());
			}
		}
		return null;
	}
	
	/**
	 * @return Name of the host.
	 */
	public static String getname() {
		return name;
	}
	
	/**
	 * @return Port.
	 */
	public static String getPort() {
		return port;
	}
	
	/**
	 * @return Id of the host.
	 */
	public static int getId() {
		return id;
	}
	
	/**
	 * @return Path of the file for master server.
	 */
	public static String getFilePath() {
		return filepath;
	}
	
	/**
	 * @return Name of the file.
	 */
	public static String getFileName() {
		return filename;
	}
	
	/**
	 * @return Type of node.
	 */
	public static Node getType() {
		return type;
	}
	
	/**
	 * @return List of all servers.
	 */
	public static Table<Integer, String, String> getServersList(){
		return allServerDetails;
	}
	
	public static ArrayList<Integer> getSortedServerIds() {
		Set<Integer> rowKeySet = allServerDetails.rowKeySet();
		ArrayList<Integer> idsList = new ArrayList<Integer>(rowKeySet);
		Collections.sort(idsList);
		return idsList;
	}

}
