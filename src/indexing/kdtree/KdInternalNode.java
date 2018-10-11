package indexing.kdtree;

import indexing.utils.SearchResults;

import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

/**
 * Internal node of a kd-tree based on the regular branch-and-bound search.
 * 
 * Implementation of the InternalNode abstract class.
 *
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
class KdInternalNode extends InternalNode {

	/**
	 * Left child.
	 */
	private Node leftChild;
	
	/**
	 * Right child.
	 */
	private Node rightChild;
	
	/**
	 * Creates a new node.
	 * @param splitDimension Dimension at which data is split.
	 * @param splitValue Value on splitDimension at which data is split.
	 * @param leftChild Left child of the tree.
	 * @param rightChild RRight child of the tree.
	 */
	public KdInternalNode(int splitDimension, float splitValue, Node leftChild, Node rightChild) {
		this.splitDimension = splitDimension;
		this.splitValue = splitValue;
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}
	
	@Override
	public void traverse(Mat q, int qid, SearchResults matches, MatVector data, AtomicInteger visitedLeaves, int maxLeaves) {
		if( visitedLeaves.get() >= maxLeaves ) {
			return;
		}
		
		float distanceToSplit = this.getDistanceToSplitValue(q);
		Node firstChild, secondChild;
		if( distanceToSplit <= 0 ) {
			firstChild = this.leftChild;
			secondChild = this.rightChild;
			distanceToSplit = -distanceToSplit;
		} else {
			firstChild = this.rightChild;
			secondChild = this.leftChild;
		}
		
		firstChild.traverse(q, qid, matches, data, visitedLeaves, maxLeaves);
		
		if( visitedLeaves.get() < maxLeaves ) {
			if( distanceToSplit < matches.getDistanceBound() ) {
				secondChild.traverse(q, qid, matches, data, visitedLeaves, maxLeaves);
			}
		}
	}
}
