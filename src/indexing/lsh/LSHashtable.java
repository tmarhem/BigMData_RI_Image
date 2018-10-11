package indexing.lsh;

import java.util.ArrayList;

import org.bytedeco.javacpp.opencv_core.Mat;

/**
 * Hashtable for LSH entries.
 * 
 * This hashtable contains LSH entries identified by their q(.) LSH hash value.
 * q(.) values are hashed again to find the bucket in the hashtable that will
 * store the entry. Entries of equal q(.) values will fall into the same bucket
 * (LSH expected collisions). Collisions of different q(.) values are handle by
 * linear search in array lists.
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
class LSHashtable {

	/**
	 * Hash table.
	 */
	ArrayList<ArrayList<LSHEntry>> table;
	
	/**
	 * Create a hashtable.
	 * @param size Size (numbver of buckets) of the hashtable.
	 */
	public LSHashtable(int size) {
		this.table = new ArrayList<ArrayList<LSHEntry>>(size);
		for( int i = 0 ; i < size ; i++ ) {
			this.table.add(new ArrayList<LSHEntry>());
		}
	}
	
	/**
	 * Inserts given LSHEntry into the hashtable.
	 * @param e The entry to insert.
	 */
	public void put(LSHEntry e) {
		this.table.get(e.hashCode()).add(e);
	}
	
	/**
	 * Returns the elements of the hashtable with given q(.) key.
	 * @param key The q(.) to look for.
	 * @return All entries with given q(.) values.
	 */
	public ArrayList<LSHEntry> get(Mat key) {
		return this.get(new LSHEntry(key, 0, 0));
	}
	
	/**
	 * Returns the elements of the hashtable with the same q(.)
	 * values as the given entry.
	 * @param e Entry with searched q(.) value.
	 * @return All entries with the same q(.) values as e.
	 */
	private ArrayList<LSHEntry> get(LSHEntry e) {
		ArrayList<LSHEntry> bucket = this.table.get(e.hashCode());
		ArrayList<LSHEntry> result = new ArrayList<>();
		
		for( LSHEntry ee : bucket ) {
			if( e.equals(ee) ) {
				result.add(ee);
			}
		}
		
		return result;
	}
}
