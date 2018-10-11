package indexing.utils;

import java.util.HashSet;

import org.bytedeco.javacpp.opencv_core.DMatch;
import org.bytedeco.javacpp.opencv_core.DMatchVector;

/**
 * Data structure to store the matches found using a radius search.
 * New matches are added only if they are under the specified radius.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
public class EpsSearchResults implements SearchResults {

	private HashSet<DMatch> matches;
	private float eps;
	
	/**
	 * Create a new data structure to store matches.
	 * @param eps The radius used in the radius search.
	 */
	public EpsSearchResults(float eps) {
		this.eps = eps;
		this.matches = new HashSet<DMatch>();
	}
	
	@Override
	public void add(DMatch m) {
		if( m.distance() <= this.eps ) {
			this.matches.add(m);
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
		return this.eps;
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
