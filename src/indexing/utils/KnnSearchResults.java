package indexing.utils;

import java.util.Comparator;
import java.util.PriorityQueue;

import org.bytedeco.javacpp.opencv_core.DMatch;
import org.bytedeco.javacpp.opencv_core.DMatchVector;

/**
 * Data structure to store matches of a knn-search. New Matches are added only if
 * they are closer to the query than existing matches (the furtherst match is
 * therefore removed) or if there are less than k matches in the structure.
 *
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
public class KnnSearchResults implements SearchResults {

	private PriorityQueue<DMatch> matches;
	private int k;
	
	/**
	 * Create a new structure to store k matches.
	 * @param k The maximum number of matches.
	 */
	public KnnSearchResults(int k) {
		this.k = k;
		this.matches = new PriorityQueue<>(k + 1, new Comparator<DMatch>() {
			@Override
			public int compare(DMatch o1, DMatch o2) {
				if( o1.distance() < o2.distance() ) {
					return 1;
				} else if( o1.distance() > o2.distance() ) {
					return -1;
				} else {
					return 0;
				}
			}
		});
	}
	
	@Override
	public void add(DMatch m) {
		if( this.matches.size() < k ) {
			this.matches.add(m);
		} else {
			if( m.distance() < this.matches.peek().distance() ) {
				this.matches.add(m);
				this.matches.remove(this.matches.peek());
			}
		}
	}

	@Override
	public DMatchVector toDMatchVector() {
		DMatchVector v = new DMatchVector(this.matches.size());
		int i = 0;
		
		for( DMatch m : this.matches ) {
			v.put(i, m);
			i++;
		}
		
		return v;
	}

	@Override
	public float getDistanceBound() {
		return this.matches.size() < k ? Float.MAX_VALUE : this.matches.peek().distance();
	}
	
	@Override
	public boolean contains(int imageIdx, int descIdx) {
		for( DMatch m : this.matches ) {
			if( m.imgIdx() == imageIdx && m.trainIdx() == descIdx ) {
				return true;
			}
		}
		return false;
	}
}
