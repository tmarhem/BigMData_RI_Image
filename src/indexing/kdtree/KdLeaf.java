package indexing.kdtree;

import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.DMatch;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import indexing.utils.SearchResults;

/**
 * Leaf for a kd-tree with regular branch-and-bound search.
 * 
 * Implementation of the Leaf  abstract class.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
class KdLeaf extends Leaf {
	
	/**
	 * Creates a leaf.
	 * @param imageIdx Image ID of the vector stored in the leaf.
	 * @param descIdx Descriptor ID of the vector stored in the leaf.
	 */
	public KdLeaf(int imageIdx, int descIdx) {
		this.imageIdx = imageIdx;
		this.descIdx = descIdx;
	}

	@Override
	public void traverse(Mat q, int qid, SearchResults matches, MatVector data, AtomicInteger visitedLeaves, int maxLeaves) {
		float distance = (float)opencv_core.norm(q, data.get(this.imageIdx).row(this.descIdx));
		matches.add(new DMatch(qid, this.descIdx, this.imageIdx, distance));
		visitedLeaves.incrementAndGet();
	}
	
}
