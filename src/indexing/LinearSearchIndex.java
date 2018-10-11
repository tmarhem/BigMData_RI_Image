package indexing;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.DMatch;
import org.bytedeco.javacpp.opencv_core.DMatchVector;
import org.bytedeco.javacpp.opencv_core.DMatchVectorVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import indexing.utils.EpsSearchResults;
import indexing.utils.KnnSearchResults;
import indexing.utils.SearchResults;

/**
 * Linear search index.
 * 
 * Performs linear search over a dataset of vectors.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
public class LinearSearchIndex implements Index {

	/**
	 * Indexed data.
	 */
	private MatVector data;
	
	/**
	 * Return a new, empty, linear search index.
	 */
	public LinearSearchIndex() {
		this.data = null;
	}
	
	@Override
	public void index(MatVector data) {
		this.data = data;
	}

	/**
	 * Performs a linear search of matches for query vector q in the collection.
	 * @param q Query row vector.
	 * @param qid ID of the query.
	 * @param matches Empty match container depending on the type of
	 * search performed (k-NN or radius search).
	 * @return The matches of the query vector in the database.
	 */
	private DMatchVector search(Mat q, int qid, SearchResults matches) {
		float distance;
		
		for( int i = 0 ; i < this.data.size() ; i++ ) {
			for( int j = 0 ; j < this.data.get(i).rows() ; j++ ) {
				distance = (float)opencv_core.norm(q, this.data.get(i).row(j));
				matches.add(new DMatch(qid, j, i, distance));
				}
		}
				
		return matches.toDMatchVector();
	}
	
	@Override
	public DMatchVectorVector knnQuery(Mat query, int k) {
		if( this.data == null ) {
			System.err.println("Error: no data indexed. Cannot perform search.");
			return null;
		}
		if( query == null ) {
			System.err.println("Error: null query. Cannot perform search.");
			return null;
		}
		
		DMatchVectorVector results = new DMatchVectorVector(query.rows());
		
		for( int i = 0 ; i < query.rows() ; i++ ) {
			results.put(i, this.search(query.row(i), i, new KnnSearchResults(k)));
		}
		
		return results;
	}
	
	@Override
	public DMatchVectorVector epsQuery(Mat query, float eps) {
		if( this.data == null ) {
			System.err.println("Error: no data indexed. Cannot perform search.");
			return null;
		}
		if( query == null ) {
			System.err.println("Error: null query. Cannot perform search.");
			return null;
		}
		
		DMatchVectorVector results = new DMatchVectorVector(query.rows());
		
		for( int i = 0 ; i < query.rows() ; i++ ) {
			results.put(i, this.search(query.row(i), i, new EpsSearchResults(eps)));
		}
		
		return results;
	}

}
