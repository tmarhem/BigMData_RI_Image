package evaluation;

import java.util.TreeMap;
import java.util.Vector;

import searchEngine.ImageInfo;
import searchEngine.SearchEngine;
import utils.ImageCollectionReader;

/**
 * A simple search engine evaluator computing (recall, precision) points for a single query.
 * It is assumed that the search engine would return results and that there are expected results for all queries.
 * 
 * @author Pierre Tirilly  - pierre.tirilly@imt-lille-douai.fr
 * 
 */
public class SearchEngineEvaluator {

	/**
	 * Inner class to store the computed (recall, precision) couples.
	 * @author Pierre Tirilly
	 */
	private class RecallPrecisionPoint {
		double recall;
		double precision;
		
		/**
		 * Constructor.
		 * @param recall The recall value of the point.
		 * @param precision The precision value of the point.
		 */
		private RecallPrecisionPoint( double recall, double precision ) {
			this.recall = recall;
			this.precision = precision;
		}
		
		@Override
		public String toString() {
			return this.recall + " " + this.precision;
		}
	}
	
	/**
	 * The search engine to be evaluated
	 */
	private SearchEngine searchEngine;
	
	/**
	 * Constructor for a search engine evaluator.
	 * @param searchEngine The search engine to be evaluated.
	 */
	public SearchEngineEvaluator( SearchEngine searchEngine ) {
		this.searchEngine = searchEngine;
	}
	
	/**
	 * Computes precision-recall points for the given query.
	 * @param query The query image info instance.
	 * @return A vector of (precision, recall) points, or null
	 * if no groundtruth exists for the query.
	 */
	public Vector<RecallPrecisionPoint> evaluateRecallPrecisionPoints( ImageInfo query ) {
		return this.evaluate(query, false);		
	}

	/**
	 * Computes precision-recall for the given query using 11-pt interpolation.
	 * @param query The query image info instance.
	 * @return A vector of the 11 (precision, recall) points, or null
	 * if no groundtruth exists for the query.
	 */
	public Vector<RecallPrecisionPoint> evaluate11pt( ImageInfo query ) {
		return this.evaluate(query, true);
	}

	
	/**
	 * Outputs precision-recall points for the given query.
	 * @param query The query to be used for evaluation.
	 * @param use11ptInterpolation If true, (recall, precision) points are
	 * interpolated at 11 recall points. If false, regular interpolation at
	 * all known recall points in used.
	 * @return A vector of (recall, precision) points, or null if no
	 * groundtruth exists for this query.
	 */
	public Vector<RecallPrecisionPoint> evaluate( ImageInfo query, boolean use11ptInterpolation ) {
		
		int truePositives;
		int groundTruthImageNumber;
		Vector<ImageInfo> searchResults;
		Vector<RecallPrecisionPoint> recallPrecisionPoints = null;
		
		// get the search results for this query
		searchResults = this.searchEngine.queryDatabase( query );

		// get the number of relevant images (for recall computation)
		groundTruthImageNumber = this.countSceneImages( query );

		if ( groundTruthImageNumber != 0 ) { // if there exists some groundtruth
			recallPrecisionPoints = new Vector<>();
			if ( searchResults.size() == 0 ) {
				recallPrecisionPoints.add( new RecallPrecisionPoint(0.0, 0.0) );
			} else {

				// compute recall and precision points for each DCV
				truePositives = 0;
				for ( int i = 0 ; i < searchResults.size() ; i++ ) {
					if ( this.getSceneId(query) == this.getSceneId(searchResults.get(i)) ) {
						truePositives++;
					}
					recallPrecisionPoints.add( new RecallPrecisionPoint( (double)truePositives / (double)groundTruthImageNumber,
							(double)truePositives / (double)( i + 1) ) );
				}
				recallPrecisionPoints = interpolate( recallPrecisionPoints );
			}
			
			if( use11ptInterpolation ) {
				recallPrecisionPoints = this.to11pt(recallPrecisionPoints);
			}
			
		}
		
		return recallPrecisionPoints;
	}
	
	// interpolation of recall-precision curve
	private Vector<RecallPrecisionPoint> interpolate( Vector<RecallPrecisionPoint> points ) {
		TreeMap< Double, RecallPrecisionPoint > interpolatedPoints = new TreeMap<Double, SearchEngineEvaluator.RecallPrecisionPoint>();
		
		for ( RecallPrecisionPoint rpp : points ) {
			if  ( interpolatedPoints.containsKey( rpp.recall ) ) {
				if ( interpolatedPoints.get( rpp.recall ).precision < rpp.precision ) {
					interpolatedPoints.put( rpp.recall, rpp );
				}
			} else {
				interpolatedPoints.put( rpp.recall, rpp );
			}
		}
		
		return new Vector<RecallPrecisionPoint>( interpolatedPoints.values() );
	}
	
	// compute 11-pt recall precision points from sorted, interpolated regular recall precision points
	private Vector<RecallPrecisionPoint> to11pt( Vector<RecallPrecisionPoint> points ) {
		Vector<RecallPrecisionPoint> points11 = new Vector<RecallPrecisionPoint>();
		RecallPrecisionPoint newPoint;

		for ( double cut = 0.0 ; cut <= 1.0 ; cut += 0.1 ) {
			newPoint = null;
			for ( RecallPrecisionPoint p : points ) {
				if( p.recall >= cut ) {
					newPoint = new RecallPrecisionPoint(cut, p.precision);
					break;
				}
			}
			if ( newPoint == null ) {
				newPoint = new RecallPrecisionPoint(cut, 0.0);
			}
			points11.add(newPoint);
		}

		return points11;
	}

	/**
	 * Computes average precision for the given query.
	 * @param query The query image.
	 * @return The average precision for the given query. Returns -1.0
	 * if no groundtruth exists for the query.
	 */
	public double evaluateAveragePrecision( ImageInfo query ) {
		int truePositives;
		int groundTruthImageNumber;
		Vector<ImageInfo> searchResults;
		double ap;

		// get the search results for this query
		searchResults = this.searchEngine.queryDatabase( query );

		// get the number of relevant images (for recall computation)
		groundTruthImageNumber = this.countSceneImages( query );

		// evaluate
		ap = 0.0;
		if ( groundTruthImageNumber <= 0 ) { // no documents to retrieve
			ap = Double.NaN; // skip query
		} else if ( searchResults.size() != 0 ){
			// compute precision point for each relevant document returned
			truePositives = 0;
			for ( int i = 0 ; i < searchResults.size() ; i++ ) {
				if ( this.getSceneId(query) == this.getSceneId(searchResults.get(i)) ) {
					truePositives++;
					ap += (double)truePositives / (double)(i + 1); 
				}
			}
			ap /= groundTruthImageNumber;
		}

		return ap;
	}

		
	// Gets the expected number of images for the query
	private int countSceneImages( ImageInfo query ) {
		
		int imageNumber = 0;
		
		for ( ImageInfo ii : this.searchEngine.getDatabase() ) {
			if ( this.getSceneId(ii) == this.getSceneId(query) ) {
				imageNumber++;
			}
		}
		
		return imageNumber;
	}
	
	// Gets the ImageInfo object from the file name
	private static ImageInfo getImageFromFilename( String filename, Vector<ImageInfo> queries ) {
		
		ImageInfo result = null;
		
		for ( ImageInfo ii : queries ) {
			if ( ii.getFileName().equals( filename ) ) {
				result = ii;
				break;
			}
		}
		
		return result;
	}
	
	// gets the scene id from the file name
	private int getSceneId( ImageInfo image ) {
		return Integer.parseInt(image.getFileName().split("\\.")[0])/100;
	}
	
	// Main method
	// Use: java SearchEngineEvaluator database_file query_name
	public static void main( String[] args ) {
		
		// check argument number
		if ( args.length != 3 ) {
			System.err.println("Use:\n\tjava SearchEngineEvaluator database_file query_file query_name " );
			return;
		}
		
		// get arguments
		String databaseFilePath = args[0];
		String queryFilePath = args[1];
		String queryName = args[2];
		
		SearchEngine se = null;
		SearchEngineEvaluator see = null;
		Vector<RecallPrecisionPoint> rpps = null;
		
		// TODO: initialize search engine object
		se = null;
		
		// load query file and get query
		Vector<ImageInfo> queries = ImageCollectionReader.readDatabaseFile( queryFilePath );
		ImageInfo query = getImageFromFilename( queryName, queries );
		if ( query == null ) {
			System.err.println( "Error: unknown query name" );
			return;
		}
		
		// load database file
		se.loadDatabaseFile( databaseFilePath );
		
		// run evaluation
		see = new SearchEngineEvaluator( se );
		rpps = see.evaluateRecallPrecisionPoints( query );
		
		// print out results
		if ( rpps != null ) {
			for ( RecallPrecisionPoint rpp : rpps ) {
				System.out.println( rpp );
			}
		} else {
			System.err.println( "Error: no (precision, recall) points could be computed." );
		}
		
		System.out.println( "Average precision: " + see.evaluateAveragePrecision(query) );
		
	}
	
}
