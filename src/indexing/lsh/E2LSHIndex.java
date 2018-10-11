package indexing.lsh;

import java.util.HashSet;
import java.util.Set;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.DMatch;
import org.bytedeco.javacpp.opencv_core.DMatchVector;
import org.bytedeco.javacpp.opencv_core.DMatchVectorVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.RNG;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.ShortIndexer;

import indexing.Index;
import indexing.utils.EpsSearchResults;

/**
 * Implementation of a E2LSH index.
 * 
 * Following the standard LSH procedure, each vector is quantized into hash indices as follows :
 * For each table :
 *   - compute hash values hi(.) by projecting the vector on a random line,
 *   - compute the index as a concatenation of hi(.) values qj(.) = (h1(.), h2(.), ..., hk(.)),
 *   - store the vector on bucket qj(.) of the hashtable.
 * Querying is performed by hashing the query vector and returning the matches in the corresponding
 * buckets of each table.
 * 
 * For the sake of efficiency, qj(.) elements are picked at random from a set of precomputed h(.) values,
 * so the same hi(.) may be used in several qi(.) (but no more than once in each qj(.)).
 * 
 * Data is stored in hashtables using q(.) as key. The hashing method for q(.) is borrowed from P. Indyk's
 * implementation. The size of the actual hashtables has a impact on the efficiency of the index.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
public class E2LSHIndex implements Index {

	/**
	 * Hashtables.
	 */
	private LSHashtable[] tables;
	
	/**
	 * Projection axes (a_i). Each row is an axis.
	 */
	private Mat q1Axes;
	
	/**
	 * Projection shift (b_i).
	 */
	private Mat q1Bias;
	
	/**
	 * q(.) functions of the index. Each row is a qj(.) and contains
	 * the indices (in q1Axes and q1Bias) of the h(.) functions used.
	 */
	private Mat q2subspaces;
	
	/**
	 * Quantification step W.
	 */
	private float w;
	
	/**
	 * Indexed vectors. Each entry corresponds to one collection image
	 * and contains the image descriptors (one per row).
	 */
	private MatVector data;
	
	/**
	 * Random number generator.
	 */
	private RNG rng = new RNG();
	
	/**
	 * Creates a new E2LSH index.
	 * @param d Dimension of the data vectors to be indexed.
	 * @param m Number of random projections h(.) used.
	 * @param k Length of q(.) codes.
	 * @param l Number of hashtables.
	 * @param table_size Size of the actual hashtables storing the data.
	 * @param w Quantization step.
	 */
	public E2LSHIndex( int d, int m, int k, int l, int table_size, float w ) {
		this.q1Axes = this.initQ1Axes( m, d );
		this.q1Bias = this.initQ1Bias( m, w );
		this.q2subspaces = this.initQ2Subspaces( l, k, m );
		this.initTables(l, table_size);
		this.w = w;
		Hasher.setInstance(k, table_size);
		this.data = null;
	}
	
	/**
	 * Initialization of the hashtables.
	 * @param nb_tables Number of hashtables in the index.
	 * @param table_size Size (number of buckets) of the hashtables.
	 */
	private void initTables(int nb_tables, int table_size) {
		this.tables = new LSHashtable[nb_tables];
		for( int i = 0 ; i < this.tables.length ; i++ ) {
			this.tables[i] = new LSHashtable(table_size);
		}
	}
	
	/**
	 * Draws random axes for data projection. Axis components are i.i.d.
	 * samples from a normal distribution. Axes are L2-normalized.
	 * @param rows The number of axes to pick.
	 * @param cols The dimension of the axes.
	 * @return A mat containing one projection axis per row.
	 */
	private Mat initQ1Axes( int rows, int cols ) {
		Mat axes = new Mat( rows, cols, opencv_core.CV_32F );
		FloatIndexer idx = (FloatIndexer)axes.createIndexer();
		
		// generate random vectors with i.i.d. elements
		for ( int i = 0 ; i < idx.rows() ; i++ ) {
			for ( int j = 0 ; j < idx.cols() ; j++ ) {
				idx.put( i, j, (float)this.rng.gaussian(1.) );
			}
		}
		
		// normalize (L2) vectors
		for ( int i = 0 ; i < idx.rows() ; i++ ) {
			opencv_core.normalize(axes.row(i), axes.row(i));
		}
		
		return axes;
	}
	
	/**
	 * Draws a random column vector. Components are i.i.d. samples
	 * in [0; max_value] drawn from a uniform distribution.
	 * @param rows Size of the vector.
	 * @param max_value Maximum value of vector components.
	 * @return The sampled column vector.
	 */
	private Mat initQ1Bias( int rows, float max_value ) {
		Mat bias = new Mat( rows, 1, opencv_core.CV_32F );
		FloatIndexer idx = (FloatIndexer)bias.createIndexer();
		
		for ( int i = 0 ; i < idx.rows() ; i++ ) {
			idx.put( i, this.rng.uniform(0f, max_value) );
		}
		
		return bias;
	}
	
	/**
	 * Initializes the q(.) vector hash functions by drawing h(.) functions at random.
	 * @param nb_subspaces Number of q(.) hash functions.
	 * @param subspace_size Size (number of projections) of the q(.) hash functions.
	 * @param space_size Total number of projections available.
	 * @return A matrix representing the q(.) functions. Each row is a functions and
	 * contains the indices of its h(.) in the q1Axes/q1Bias matrix/vector.
	 */
	private Mat initQ2Subspaces( int nb_subspaces, int subspace_size, int space_size ) {
		Mat subspaces = new Mat( nb_subspaces, subspace_size, opencv_core.CV_16S );
		ShortIndexer idx = (ShortIndexer)subspaces.createIndexer();
		Set<Short> currentIndices = new HashSet<Short>();
		short currentIndex;
		
		for ( int i = 0 ; i < idx.rows() ; i++ ) {
			for ( int j = 0 ; j < idx.cols() ; j++ ) {
				currentIndex = (short)this.rng.uniform(0, space_size);
				while ( currentIndices.contains(currentIndex) ) {
					currentIndex = (short)this.rng.uniform(0, space_size);
				}
				idx.put(i, j, currentIndex);
				currentIndices.add(currentIndex);
			}
			currentIndices.clear();
		}
		
		return subspaces;		
	}
	
	/**
	 * Fill given float matrix with given value.
	 * @param m Float matrix to fill.
	 * @param value Value to fill the matrix with.
	 */
	private void fillCV32FMat(Mat m, float value) {
		FloatIndexer idx = (FloatIndexer)m.createIndexer();
		for( int i = 0 ; i < idx.rows() ; i++ ) {
			for( int j = 0 ; j < idx.cols() ; j++ ) {
				idx.put(i,  j, value);
			}
		}
	}
	
	/**
	 * Projects the given data matrix on the random axes and quantifies the projections.
	 * @param data The data to be quantized.
	 * @return A matrix containing the quantification values h(.). Each row of the matrix
	 * corresponds to the same row in the data matrix.
	 */
	private Mat q1(Mat data) {
		Mat p = new Mat(data.rows(), this.q1Axes.rows(), opencv_core.CV_32F);
		Mat codes = new Mat(data.rows(), this.q1Axes.rows(), opencv_core.CV_16S); 
		FloatIndexer pidx = (FloatIndexer)p.createIndexer();
		ShortIndexer cidx = (ShortIndexer)codes.createIndexer(); 
		
		Mat ones = new Mat(data.rows(), 1, opencv_core.CV_32F);
		this.fillCV32FMat(ones, 1f);
		opencv_core.gemm(ones, this.q1Bias.t().asMat(), 1., p, 0., p);
		opencv_core.gemm(data, this.q1Axes.t().asMat(), 1., p, 1., p);
		
		for ( int i = 0 ; i < pidx.rows() ; i++ ) {
			for ( int j = 0 ; j < pidx.cols() ; j++ ) {
				cidx.put(i,  j, (short)(pidx.get(i, j) / this.w));
			}
		}
		return codes;
	}
	
	/**
	 * Compute the q(.) signatures of one input data vector of h(.) values.
	 * @param code A row vector of h(.) values.
	 * @return A matrix containing the qj(.) values computed from the input vector.
	 * Each row contains one qj(.) signature.
	 */
	private Mat q2(Mat code) {
		Mat qcodes = new Mat(this.q2subspaces.rows(), this.q2subspaces.cols(), opencv_core.CV_16S);
		ShortIndexer qidx = (ShortIndexer)qcodes.createIndexer();
		ShortIndexer cidx = (ShortIndexer)code.createIndexer();
		ShortIndexer sidx = (ShortIndexer)this.q2subspaces.createIndexer();
		
		for( int i = 0 ; i < qidx.rows() ; i++ ) {
			for ( int j = 0 ; j < qidx.cols() ; j++ ) {
				qidx.put(i, j, cidx.get(0, sidx.get(i, j)));
			}
		}
		
		return qcodes;
	}
	
	@Override
	public void index(MatVector collection) {
		this.data = collection;
		for( int i = 0 ; i < collection.size() ; i++ ) {
			this.indexImage(collection.get(i), i);
		}
		
	}
	
	/**
	 * Displays the number of elements stored in the buckets of the hashtables.
	 */
	public void displayHashtableLoads() {
		for( int i = 0 ; i < this.tables.length ; i++ ) {
			System.out.print("[");
			for( int j = 0 ; j < 500 ; j++ ) {
				System.out.print(this.tables[i].table.get(j).size() + " ");
			}
			System.out.println("]");
		}
	}
	
	/**
	 * Indexes on image represented by a matrix of descriptors.
	 * @param data Matrix of descriptors (one descriptor per row).
	 * @param imageIdx The index of the image in the database.
	 */
	private void indexImage(Mat data, int imageIdx) {
		Mat q1Codes = this.q1(data);
		for( int i = 0 ; i < q1Codes.rows() ; i++ ) {
			Mat q2Codes = this.q2(q1Codes.row(i));
			for( int j = 0 ; j < q2Codes.rows() ; j++ ) {
				this.tables[j].put(new LSHEntry(q2Codes.row(j), imageIdx, i));
			}
		}
	}
	
	@Override
	public DMatchVectorVector knnQuery(Mat query, int k) {
		System.err.println("Knn query not available for E2LSH index.");
		return null;
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
		
		Mat imageQ1 = this.q1(query);
		DMatchVectorVector results = new DMatchVectorVector(query.rows());
		
		for( int i = 0 ; i < query.rows() ; i++ ) {
			results.put(i, this.epsSearch(query.row(i), imageQ1.row(i), i, eps));
		}
		
		return results;
	}
		
	/**
	 * Performs a radius search for given query descriptor q with precomputed
	 * h(.) values (q1Code).
	 * @param query The query descriptor as a row vector.
	 * @param q1Code The h(.) values of the descriptors (one per row).
	 * @param qid The id of the query.
	 * @param eps Value of radius for the search.
	 * @return The matches found in the database for this input vector.
	 */
	private DMatchVector epsSearch(Mat query, Mat q1Code, int qid, float eps) {
		EpsSearchResults matches = new EpsSearchResults(eps);
		Mat q2Code = this.q2(q1Code);
		float distance;
		
		for( int i = 0 ; i < q2Code.rows() ; i++ ) {
			for( LSHEntry e : this.tables[i].get(q2Code.row(i)) ) {
				if( !matches.contains(e.getImageIdx(), e.getDescIdx()) ) {
					distance = (float)opencv_core.norm(query, this.data.get(e.getImageIdx()).row(e.getDescIdx()));
					matches.add(new DMatch(qid, e.getDescIdx(), e.getImageIdx(), distance));
				}
			}
		}
		
		return matches.toDMatchVector();
	}
	
}
