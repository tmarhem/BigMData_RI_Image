package indexing.kdtree;

import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.DMatch;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import indexing.utils.SearchResults;

/**
 * Best bin first leaf.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
class BestBinFirstLeaf extends Leaf implements BestBinFirstNode {

	public BestBinFirstLeaf(int imageIdx, int descIdx) {
		this.imageIdx = imageIdx;
		this.descIdx = descIdx;
	}
	
	@Override
	public void traverse(Mat q, int qid, SearchResults matches, MatVector data, AtomicInteger visitedLeaves,
			int maxLeaves, BestBinFirstPriorityQueue nodeQueue) {
		if( visitedLeaves.get() > maxLeaves ) {
			return;
		}
		float distance = (float)opencv_core.norm(q, data.get(this.imageIdx).row(this.descIdx));
		matches.add(new DMatch(qid,  this.descIdx,  this.imageIdx, distance));
		visitedLeaves.incrementAndGet();
		if(nodeQueue.hasNext()) {
			nodeQueue.getNext().traverse(q, qid, matches, data, visitedLeaves, maxLeaves, nodeQueue);
		}
	}

	@Override
	public void traverse(Mat q, int qid, SearchResults matches, MatVector data, AtomicInteger visitedLeaves,
			int maxLeaves) {
		this.traverse(q, qid, matches, data, visitedLeaves, maxLeaves, new BestBinFirstPriorityQueue());
		
	}

	@Override
	public void computeAndSetPriorityScore(Mat q) {}

	@Override
	public float getPriorityScore() {
		return 0f;
	}

}
