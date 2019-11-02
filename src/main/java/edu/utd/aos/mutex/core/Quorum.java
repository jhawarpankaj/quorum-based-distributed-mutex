package edu.utd.aos.mutex.core;

import java.util.ArrayList;
import java.util.Random;

import org.tinylog.Logger;

import edu.utd.com.aos.nodes.Host;

/**
 * Logic for selecting a quorum.
 * @author pankaj
 *
 */
public class Quorum {
	
	private static QuorumTree root;
	
	public static class QuorumTree{
		int id;
		QuorumTree left;
		QuorumTree right;
		public QuorumTree(int id) {
			this.id = id;
			left = null;
			right = null;
		}
	}
	
	public static QuorumTree getQuorumRoot() {
		return root;
	}

	public static void initialize() {
		createQuorum();		
	}

	private static void createQuorum() {
		ArrayList<Integer> sortedServerIds = Host.getSortedServerIds();
		int index = 0;
		root = new QuorumTree(sortedServerIds.get(index++));
		root.left = new QuorumTree(sortedServerIds.get(index++));
		root.right = new QuorumTree(sortedServerIds.get(index++));
		root.left.left = new QuorumTree(sortedServerIds.get(index++));
		root.left.right = new QuorumTree(sortedServerIds.get(index++));
		root.right.left = new QuorumTree(sortedServerIds.get(index++));
		root.right.right = new QuorumTree(sortedServerIds.get(index++));
		
		Logger.info("The fixed quorum tree structure: ");
		print("", root, false);
	}
	
	public static void print(String prefix, QuorumTree n, boolean isLeft) {
        if (n != null) {
            Logger.info(prefix + (isLeft ? "|-- " : "\\-- ") + n.id);
            print(prefix + (isLeft ? "|   " : "    "), n.left, true);
            print(prefix + (isLeft ? "|   " : "    "), n.right, false);
        }
    }
	
	private static void generateRandomQuorum(QuorumTree node, ArrayList<Integer> result) {
		if(node == null) return;
		if(node.left == null && node.right == null) {
			result.add(node.id);
			return;
		}
		Random rand = new Random();
		int random = rand.nextInt(3);
		switch(random) {
			case 0:
				generateRandomQuorum(rootLeft(node, result), result);
				break;
			case 1:
				generateRandomQuorum(rootRight(node, result), result);
				break;
			case 2:
				generateRandomQuorum(node.left, result);
				generateRandomQuorum(node.right, result);
				break;
		}
	}

	private static QuorumTree rootRight(QuorumTree node, ArrayList<Integer> result) {
		result.add(node.id);		
		return node.right;
	}

	private static QuorumTree rootLeft(QuorumTree node, ArrayList<Integer> result) {
		result.add(node.id);
		return node.left;
	}

	public static ArrayList<Integer> getRandomQuorum() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		generateRandomQuorum(root, result);
		return result;
	}
	
}
