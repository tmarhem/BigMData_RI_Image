package indexing.kdtree;

import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Node priority queue for best bin first search.
 * 
 * Nodes are organized according to the proximity of the query to their split
 * line. Leaves have highest priority.
 * 
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
class BestBinFirstPriorityQueue {
	
	/**
	 * Priority queue of nodes.
	 */
	private PriorityQueue<BestBinFirstNode> nodes;
	
	/**
	 * List of leaves in their expected visit order.
	 */
	private LinkedList<BestBinFirstNode> leaves;
	
	/**
	 * Creates a new priority queue.
	 */
	public BestBinFirstPriorityQueue() {
		this.nodes = new PriorityQueue<>();
		this.leaves = new LinkedList<>();
	}
	
	/**
	 * Get next node (leaf or internal node) to visit.
	 * @return The next node to visit.
	 */
	public BestBinFirstNode getNext() {
		BestBinFirstNode next = null;
		if( !this.leaves.isEmpty() ) {
			next = this.leaves.poll();
		} else {
			next = this.nodes.poll();
		}
		return next;
	}
	
	/**
	 * Add node to the queue.
	 * @param n Node to be added to the queue.
	 */
	public void addNode(BestBinFirstNode n) {
		if(n instanceof BestBinFirstLeaf) {
			this.leaves.add(n);
		} else {
			this.nodes.add(n);
		}
	}

	/**
	 * Checks whether there are nodes left to visit.
	 * @return True if there are nodes let to visit, false otherwise.
	 */
	public boolean hasNext() {
		return !this.leaves.isEmpty() || !this.nodes.isEmpty();
	}
	
}
