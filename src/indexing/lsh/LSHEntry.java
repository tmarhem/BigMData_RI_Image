package indexing.lsh;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.indexer.ShortIndexer;

/**
 * Class for LSH entries stored in the hashtables. Each entry contains the
 * image and descriptors ID of the indexed vector and its q(.) signature in the
 * table.
 * 
 * @author Pierre Tirilly  - pierre.tirilly@imt-lille-douai.fr
 *
 */
class LSHEntry {

	/**
	 * q(.) signature of the vector in this table.
	 */
	private Mat code;
	
	/**
	 * Image index of the vector within the indexed data.
	 */
	private int imageIdx;
	
	/**
	 * Descriptor index of the vector within the indexed data.
	 */
	private int descIdx;
	
	/**
	 * Create a new LSH entry with given attributes.
	 * @param code q(.) signature of the entry in this table.
	 * @param imageIdx Image ID of the vector.
	 * @param descIdx Descriptor ID of the vector.
	 */
	public LSHEntry(Mat code, int imageIdx, int descIdx) {
		this.code = code;
		this.imageIdx = imageIdx;
		this.descIdx = descIdx;
	}

	/**
	 * Gets the q(.) signature of the vector.
	 * @return The q(.) signature of the vector.
	 */
	public Mat getCode() {
		return code;
	}

	/**
	 * Gets the image ID of the vector.
	 * @return The image ID of the vector.
	 */
	public int getImageIdx() {
		return imageIdx;
	}

	/**
	 * Gets the descriptor ID of the vector.
	 * @return The descriptor ID of the vector.
	 */
	public int getDescIdx() {
		return descIdx;
	}
	
	@Override
	public boolean equals(Object o) {
		boolean isEqual = false;
		if(o instanceof LSHEntry) {
			LSHEntry e = (LSHEntry)o;
			ShortIndexer cidx = (ShortIndexer)this.code.createIndexer();
			ShortIndexer ecidx = (ShortIndexer)e.code.createIndexer();
			isEqual = true;
			for ( int i = 0 ; i < cidx.rows() && isEqual ; i++ ) {
				isEqual = cidx.get(i) == ecidx.get(i);
			}
		}
		return isEqual;
	}
	
	@Override
	public int hashCode() {
		return Hasher.getInstance().computeHash(this);
	}
	
}
