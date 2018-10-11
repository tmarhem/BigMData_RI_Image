package indexing.kdtree;

import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import indexing.utils.SearchResults;

/**
 * Best bin first internal node.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
class BestBinFirstInternalNode extends InternalNode implements BestBinFirstNode, Comparable<BestBinFirstInternalNode> {

	/**
	 * Left child of the node.
	 */
	private BestBinFirstNode leftChild;
	
	/**
	 * Right child of the node.
	 */
	private BestBinFirstNode rightChild;
	
	/**
	 * Priority score of the node.
	 */
	private float priorityScore;
	
	/**
	 * Create new best bin first node.
	 * @param splitDimension Dimension at which data is split.
	 * @param splitValue Value (along the split dimension) at which the data is
	 * split.
	 * @param leftChild Left child of the node.
	 * @param rightChild Right child of the node.
	 */
	public BestBinFirstInternalNode(int splitDimension, float splitValue, BestBinFirstNode leftChild, BestBinFirstNode rightChild) {
		this.splitDimension = splitDimension;
		this.splitValue = splitValue;
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		this.priorityScore = Float.NaN;
	}
		
	@Override
	public void traverse(Mat q, int qid, SearchResults matches, MatVector data, AtomicInteger visitedLeaves,
			int maxLeaves, BestBinFirstPriorityQueue nodeQueue) {
		if( visitedLeaves.get() >= maxLeaves ) {
			return;
		}
		
		float distanceToSplit = this.getDistanceToSplitValue(q);
		BestBinFirstNode firstChild, secondChild;
		if( distanceToSplit <= 0 ) {
			firstChild = this.leftChild;
			secondChild = this.rightChild;
			distanceToSplit = -distanceToSplit;
		} else {
			firstChild = this.rightChild;
			secondChild = this.leftChild;
		}
		
		// traverse the child on the query side
		firstChild.traverse(q, qid, matches, data, visitedLeaves, maxLeaves, nodeQueue);
		
		// enqueue second child
		secondChild.computeAndSetPriorityScore(q);
		if( secondChild.getPriorityScore() < matches.getDistanceBound() ) {
			nodeQueue.addNode(secondChild);
		}

		// get next node to traverse
		if(nodeQueue.hasNext()) {
			nodeQueue.getNext().traverse(q, qid, matches, data, visitedLeaves, maxLeaves, nodeQueue);
		}
		
		// reset priority score for next query
		this.priorityScore = Float.NaN;
	}

	@Override
	public void traverse(Mat q, int qid, SearchResults matches, MatVector data, AtomicInteger visitedLeaves,
			int maxLeaves) {
		this.traverse(q, qid, matches, data, visitedLeaves, maxLeaves, new BestBinFirstPriorityQueue());
		
	}

	@Override
	public int compareTo(BestBinFirstInternalNode n) {
		if( this.priorityScore < n.priorityScore ) {
			return -1;
		}
		if( this.priorityScore > n.priorityScore ) {
			return 1;
		}
		return 0;
	}

	@Override
	public void computeAndSetPriorityScore(Mat q) {
		this.priorityScore = this.getDistanceToSplitValue(q);
		if( this.priorityScore < 0 ) {
			this.priorityScore = -this.priorityScore;
		}
	}

	@Override
	public float getPriorityScore() {
		return this.priorityScore;
	}
	
}
