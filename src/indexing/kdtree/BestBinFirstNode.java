package indexing.kdtree;

import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import indexing.utils.SearchResults;

/**
 * Interface for best bin first nodes.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
interface BestBinFirstNode {

	/**
	 * Recursive traversal of nodes in best bin first search. Matches are
	 * stored in the matches structure.
	 * @param q Query vector to look for.
	 * @param qid ID of the query vector.
	 * @param matches Matches found so far in the tree.
	 * @param data Indexed data.
	 * @param visitedLeaves Number of leaves visited so far.
	 * @param maxLeaves Maximum number of leaves to visit.
	 * @param nodeQueue Next nodes to visit in best bin first order.
	 */
	public void traverse(Mat q, int qid, SearchResults matches, MatVector data, AtomicInteger visitedLeaves, int maxLeaves, BestBinFirstPriorityQueue nodeQueue);
	
	/**
	 * Computes the priority value of the node.
	 * @param q The current query of the tree.
	 */
	public void computeAndSetPriorityScore(Mat q);
	
	/**
	 * Returns the priority score of the node (distance to query on the split
	 * dimension.
	 * @return The priority score of the node.
	 */
	public float getPriorityScore();
}
