package indexing.utils;

import org.bytedeco.javacpp.opencv_core.DMatch;
import org.bytedeco.javacpp.opencv_core.DMatchVector;

/**
 * Interface for data structure storing matches.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
public interface SearchResults {

	/**
	 * Adds an element to the search results.
	 * @param m The match to be added.
	 */
	public void add(DMatch m);
	
	/**
	 * Converts the search result container to a DMatchVector.
	 * @return The DMatchVector containing the search results.
	 */
	public DMatchVector toDMatchVector();
	
	/**
	 * Computes the maximum distance of current matches to the query.
	 * @return The maximum distance of a match to the current query.
	 */
	public float getDistanceBound();
	
	/**
	 * Checks whether a descriptor identified by its image index and
	 * descriptor index is already in the result set.
	 * @param imageIdx Image index of the descriptor tested.
	 * @param descIdx Descriptor index (within the image) of the
	 * descriptor tested.
	 * @return True if the descriptor is already in the match set,
	 * false otherwise.
	 */
	public boolean contains(int imageIdx, int descIdx);
}
