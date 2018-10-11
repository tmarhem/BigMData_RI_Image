package indexing.kdtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.javacpp.opencv_core.DMatchVector;
import org.bytedeco.javacpp.opencv_core.DMatchVectorVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.indexer.FloatIndexer;

import indexing.Index;
import indexing.utils.EpsSearchResults;
import indexing.utils.KnnSearchResults;
import indexing.utils.SearchResults;

/**
 * Kd-tree.
 * 
 * Index structure based on a kd-tree. The kd-tree supports both the regular
 * branch-and-bound search and the best bin first optimization.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
public class KdTree implements Index {

	/**
	 * Root of the tree.
	 */
	private Node root;
	
	/**
	 * Indexed data.
	 */
	private MatVector data;
	
	/**
	 * Maximum number of leaves to visit at each search.
	 */
	private int maxLeaves;
	
	/**
	 * If true, best bin first search is used.
	 */
	private boolean bestBinFirst;
	
	/**
	 * Creates a kd-tree index.
	 * @param maxLeaves Maximum number of leaves visited during search.
	 * @param bestBinFirst If true, best bin first search is used. Otherwise,
	 * the regular branch-and-bound search is performed.
	 */
	public KdTree(int maxLeaves, boolean bestBinFirst) {
		this.root = null;
		this.data = null;
		this.maxLeaves = maxLeaves;
		this.bestBinFirst = bestBinFirst;
	}
	
	/**
	 * Builds the kd-tree for given data to index.
	 * The node for current data is built, and children are recursively built
	 * based on data split at the median of the dimension of largest variance.
	 * Rather than splitting actual data, lists of
	 * @param data Data to index.
	 * @param imageIndices Image indices of the subset of data to be indexed.
	 * @param descIndices Descriptor indices of the subset of data to be indexed.
	 * @return The root of the kd-tree built based on data present in imageIndices/descIndices.
	 */
	private Node buildKdNodes(MatVector data, ArrayList<Integer> imageIndices, ArrayList<Integer> descIndices) {

		float[] means = new float[data.get(0).cols()];
		float[] variances = new float[data.get(0).cols()];
		
		// compute mean and variance
		for( int i = 0 ; i < imageIndices.size() ; i++ ) {
			FloatIndexer idx = (FloatIndexer)data.get(imageIndices.get(i)).createIndexer();
			for( int j = 0 ; j < idx.cols() ; j++ ) {
				means[j] += idx.get(descIndices.get(i), j);
			}
		}
		for( int i = 0 ; i < means.length ; i++ ) {
			means[i] /= (float)imageIndices.size();
		}
		
		for( int i = 0 ; i < imageIndices.size() ; i++ ) {
			FloatIndexer idx = data.get(imageIndices.get(i)).createIndexer();
			for( int j = 0 ; j < idx.cols() ; j++ ) {
				variances[j] += (idx.get(descIndices.get(i), j) - means[j]) * (idx.get(descIndices.get(i), j) - means[j]);
			}
		}
		
		// get max variance dimension
		int maxVarianceDimension = 0;
		for( int i = 1 ; i < variances.length ; i++ ) {
			if( variances[maxVarianceDimension] < variances[i] ) {
				maxVarianceDimension = i;
			}
		}
		
		float splitValue;
		float[] dimValues = new float[imageIndices.size()];
		for( int i = 0 ; i < dimValues.length ; i++ ) {
			dimValues[i] = ((FloatIndexer)data.get(imageIndices.get(i)).createIndexer()).get(descIndices.get(i), maxVarianceDimension);
		}
		Arrays.sort(dimValues);
		if( dimValues.length%2 == 1 ) {
			splitValue = dimValues[dimValues.length/2];
		} else {
			splitValue =(dimValues[dimValues.length/2-1] + dimValues[dimValues.length/2]) / 2f;
		}
		ArrayList<Integer> leftImageIndices = new ArrayList<Integer>();
		ArrayList<Integer> rightImageIndices = new ArrayList<Integer>();
		ArrayList<Integer> leftDescIndices = new ArrayList<Integer>();
		ArrayList<Integer> rightDescIndices = new ArrayList<Integer>();
		for( int i = 0 ; i < imageIndices.size() ; i++ ) {
			if( ((FloatIndexer)data.get(imageIndices.get(i)).createIndexer()).get(descIndices.get(i), maxVarianceDimension) <= splitValue ) {
				leftImageIndices.add(imageIndices.get(i));
				leftDescIndices.add(descIndices.get(i));
			} else {
				rightImageIndices.add(imageIndices.get(i));
				rightDescIndices.add(descIndices.get(i));
			}
		}
		
		if(leftImageIndices.isEmpty()) {
			for( int i = 0 ; i < rightImageIndices.size() && leftImageIndices.isEmpty() ; i++ ) {
				if( ((FloatIndexer)data.get(rightImageIndices.get(i)).createIndexer()).get(rightDescIndices.get(i), maxVarianceDimension) == splitValue ) {
					leftImageIndices.add(rightImageIndices.get(i));
					leftDescIndices.add(rightDescIndices.get(i));
					rightImageIndices.remove(i);
					rightDescIndices.remove(i);
				}
			}
		} else if(rightImageIndices.isEmpty()) {
			for( int i = 0 ; i < leftImageIndices.size() && rightImageIndices.isEmpty() ; i++ ) {
				if( ((FloatIndexer)data.get(leftImageIndices.get(i)).createIndexer()).get(leftDescIndices.get(i), maxVarianceDimension) == splitValue ) {
					rightImageIndices.add(leftImageIndices.get(i));
					rightDescIndices.add(leftDescIndices.get(i));
					leftImageIndices.remove(i);
					leftDescIndices.remove(i);
				}
			}
		}
		
		// build children
		Node leftChild;
		Node rightChild;
		if( leftImageIndices.size() == 0 ) {
			leftChild = null;
		} else if( leftImageIndices.size() == 1 ) {
			if(this.bestBinFirst) {
				leftChild = new BestBinFirstLeaf(leftImageIndices.get(0), leftDescIndices.get(0));
			} else {
				leftChild = new KdLeaf(leftImageIndices.get(0), leftDescIndices.get(0));				
			}
		} else {
			leftChild = buildKdNodes(data, leftImageIndices, leftDescIndices);				
		}
		if( rightImageIndices.size() == 0 ) {
			rightChild = null;
		} else if( rightImageIndices.size() == 1 ) {
			if(this.bestBinFirst) {
				rightChild = new BestBinFirstLeaf(rightImageIndices.get(0), rightDescIndices.get(0));
			} else {
				rightChild = new KdLeaf(rightImageIndices.get(0), rightDescIndices.get(0));				
			}
		} else {
			rightChild = buildKdNodes(data, rightImageIndices, rightDescIndices);
		}
	
		// build node
		Node newNode;
		if(this.bestBinFirst) {
			newNode = new BestBinFirstInternalNode(maxVarianceDimension, splitValue, (BestBinFirstNode)leftChild, (BestBinFirstNode)rightChild);
		} else {
			newNode = new KdInternalNode(maxVarianceDimension, splitValue, leftChild, rightChild);
		}

		return newNode;
	}
	
	@Override
	public void index(MatVector data) {
		ArrayList<Integer> imageIndices = new ArrayList<Integer>();
		ArrayList<Integer> descIndices = new ArrayList<Integer>();
		
		for( int i = 0 ; i < data.size() ; i++ ) {
			for( int j = 0 ; j < data.get(i).rows() ; j++ ) {
				imageIndices.add(i);
				descIndices.add(j);
			}
		}
		
		this.root = this.buildKdNodes(data, imageIndices, descIndices);
		this.data = data;
	}


	/**
	 * Performs a search of matches for query vector q in the collection.
	 * @param q Query row vector.
	 * @param qid ID of the query.
	 * @param matches Empty match container depending on the type of
	 * search performed (k-NN or radius search).
	 * @return The matches of the query vector in the database.
	 */
	private DMatchVector search(Mat q, int qid, SearchResults matches) {
		
		AtomicInteger visitedLeaves = new AtomicInteger(0);
		this.root.traverse(q, qid, matches, this.data, visitedLeaves, this.maxLeaves);
		
		return matches.toDMatchVector();
	}
	
	@Override
	public DMatchVectorVector knnQuery(Mat query, int k) {
		if( this.data == null || this.root == null ) {
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
		if( this.data == null || this.root == null ) {
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
