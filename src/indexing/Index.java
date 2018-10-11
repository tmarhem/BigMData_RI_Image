package indexing;

import org.bytedeco.javacpp.opencv_core.DMatchVectorVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

/**
 * Interface of an index for high-dimensional nearest neighbor search.
 * 
 * The interface offers signatures for the three fundamental operations
 * on indices: indexing data, knn-search and radius search.

 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
public interface Index {

	/**
	 * Stores the given image descriptors in the index.
	 * Data previously stored in the index is removed.
	 * @param data The descriptors to index. Each entry is a matrix containing
	 * image descriptors as rows.
	 */
	public void index(MatVector data);
	
	/**
	 * Performs a k-nearest neighbors query in the index.
	 * @param query Query descriptors. Each matrix row is a descriptor.
	 * @param k Number of nearest neighbors to retrieve for each query descriptor.
	 * @return Matches found in the index. Each entry is a match vector containing
	 * all the matches of the corresponding query vector. Returns null if no data is
	 * indexed or if the query is null.
	 */
	public DMatchVectorVector knnQuery(Mat query, int k);

	/**
	 * Performs an epsilon-search query in the index.
	 * @param query Query descriptors. Each matrix row is a descriptor.
	 * @param eps Radius of the search.
	 * @return Matches found in the index, i.e. descriptors within the specified radius for each query.
	 * Each entry is a match vector containing all the matches of the corresponding query vector. 
	 * Returns null if no data is indexed or if the query is null.
	 */
	public DMatchVectorVector epsQuery(Mat query, float eps);

}
