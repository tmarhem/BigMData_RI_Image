package indexing.lsh;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.indexer.ShortIndexer;
import org.bytedeco.javacpp.opencv_core.RNG;

/**
 * Hash function for LSH hashtables. This hash function computes actual
 * indices in hashtables from the LSH q(.) vector signatures.
 * 
 * Due to the use of random data in the hash function, and for efficiency,
 * this class behaves has a singleton. The class instance must be initialized
 * first by calling setInstance(), then be accessed by calling getInstance().
 * 
 * Hash function borrowed from P. Indyk's implementation of LSH.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
class Hasher {

	/**
	 * Prime number used to limit collisions.
	 */
	private static final long HASH_PRIME = (2l << 31) - 5l;
	
	/**
	 * Instance of the Hasher singleton.
	 */
	private static Hasher instance = null;
	
	/**
	 * Random weights applied to q(.) components.
	 */
	private Mat a;
	
	/**
	 * Size (number of buckets) of the tables the hasher is used on.
	 */
	private int tableSize;
	
	/**
	 * Creates a new buffer for specified q(.) length and table size.
	 * @param codeLength The length of the q(.) codes used in the tables.
	 * @param tableSize The size (number of buckets) of the tables.
	 */
	private Hasher(int codeLength, int tableSize) {
		this.tableSize = tableSize;
		this.a = new Mat(1, codeLength, opencv_core.CV_16S);
		this.init();
	}
	
	/**
	 * Initializes the random weights in a.
	 */
	private void init() {
		RNG rng = new RNG();
		ShortIndexer aidx = (ShortIndexer)a.createIndexer();
		for( int i = 0 ; i < aidx.cols() ; i++ ) {
			aidx.put(0, i, (short)rng.uniform(0, (short)rng.uniform(0, 2 << 16)));
		}
		rng.close();
	}
	
	/**
	 * Returns hash value for given LSHEntry.
	 * @param e LSHEntry to be hashed.
	 * @return The hash (index) of the LSHEntry.
	 */
	public int computeHash(LSHEntry e) {
		long h = 0;
		
		h = (long)this.a.dot(e.getCode());
		h = h % HASH_PRIME;
		h = h % this.tableSize;
		
		return (int)Math.abs(h);
	}
	
	/**
	 * Sets a new instance of the singleton Hasher. Must be called at least
	 * once before accessing the hasher instance.
	 * @param codeLength Length of LSH q(.) codes to be hashed.
	 * @param tableSize Size (number of buckets) of the hash tables.
	 */
	public static void setInstance(int codeLength, int tableSize) {
		if(Hasher.instance == null) {
			Hasher.instance = new Hasher(codeLength, tableSize);
		} else if(Hasher.instance.tableSize != tableSize || Hasher.instance.a.rows() != codeLength) {
			System.err.println("Warning: changing Hasher properties.");
			Hasher.instance = new Hasher(codeLength, tableSize);
		}
	}
	
	/**
	 * Returns current instance of the singleton hasher. Hasher must have been
	 * initialized by calling setInstance() first.
	 * @return The instance of the hasher.
	 */
	public static Hasher getInstance() {
		if(Hasher.instance == null) {
			System.err.println("Cannot return uninstanciated Hasher.");
			return null;
		} else {
			return Hasher.instance;
		}
	}
	
	
	
}
