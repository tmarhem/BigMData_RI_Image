package indexing.kdtree;

import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import indexing.utils.SearchResults;

/**
 * Abstract class for the internal nodes of a kd-tree.
 * Each node contains the dimension and value on this dimension at which the
 * data is split.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 * 
 */
abstract class InternalNode implements Node {

	/**
	 * Dimension at which the data is split.
	 */
	protected int splitDimension;
	
	/**
	 * Value (on the split dimension) at which the data is split.
	 */
	protected float splitValue;
	
	/**
	 * Computed the signed distance along the split dimension of a query vector
	 * to the split line.
	 * @param query Query vector.
	 * @return Signed distance of the query to the split line on the split
	 * dimension.
	 */
	public float getDistanceToSplitValue(Mat query) {
		return ((FloatIndexer)query.createIndexer()).get(0, this.splitDimension) - this.splitValue;
	}
	
	@Override
	public abstract void traverse(Mat q, int qid, SearchResults matches, MatVector data, AtomicInteger visitedLeaves,
			int maxLeaves);
	
	
}
