package edu.utd.aos.mutex.core;

import edu.utd.aos.mutex.exception.MutexException;
import edu.utd.com.aos.nodes.Host;
import edu.utd.com.aos.nodes.Host.Node;

public class Mutex {
	
	public static void start() throws MutexException {
		Node type = Host.getType();
		
		if(type.equals(Node.MASTER)) {
			Master.start();
		}
		else if(type.equals(Node.SERVER)) {
			Server.start();
		}
		else if (type.equals(Node.CLIENT)){
			Client.start();
		}
		else {
			throw new MutexException("Unidentified node type.");
		}
	}
}
