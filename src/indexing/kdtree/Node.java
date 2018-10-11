package indexing.kdtree;

import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import indexing.utils.SearchResults;

/**
 * Interface for kd-tree node.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
interface Node {

	/**
	 * Performs a recursive search based on this node and stores the matches in
	 * matches.
	 * @param q Query vector searched.
	 * @param qid ID of the query vector.
	 * @param matches Matches found in the tree.
	 * @param data Indexed data vectors.
	 * @param visitedLeaves Number of leaves visited so far.
	 * @param maxLeaves Maximum number of leaves to visit.
	 */
	public void traverse(Mat q, int qid, SearchResults matches, MatVector data, AtomicInteger visitedLeaves, int maxLeaves);
	
}
