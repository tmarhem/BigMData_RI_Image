package indexing.kdtree;

import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import indexing.utils.SearchResults;

/**
 * Abstract class for a kd-tree leaf. The leaf stores data vectors identified
 * by their (image ID, descriptor ID) indices in the original data matrix.
 * 
 * This is a specialization of the node interface.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
abstract class Leaf implements Node {

	/**
	 * Image ID of the vector stored in the leaf.
	 */
	protected int imageIdx;
	
	/**
	 * Descriptor ID of the vector stored in the leaf.
	 */
	protected int descIdx;
	
	/**
	 * Gets the image ID of the vector stored in the leaf.
	 * @return The Image ID of the vector stored in the leaf.
	 */
	protected int getImageIdx() {
		return imageIdx;
	}

	/**
	 * Get the descriptor ID of the vector stored in the leaf.
	 * @return The descriptor ID of the vector stored in the leaf.
	 */
	protected int getDescIdx() {
		return descIdx;
	}
	
	@Override
	public abstract void traverse(Mat q, int qid, SearchResults matches, MatVector data, AtomicInteger visitedLeaves,
			int maxLeaves);

}
