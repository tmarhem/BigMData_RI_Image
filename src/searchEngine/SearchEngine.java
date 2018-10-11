package searchEngine;

import java.util.Vector;

import utils.ImageCollectionReader;

/**
 * Interface describing the basic functionalities of an image search engine.
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
public abstract class SearchEngine {

	/**
	 * Vector containing the image descriptions
	 */
	protected Vector<ImageInfo> database;
	
	/**
	 * Returns the content of the image database indexed by the search engine.
	 * @return A vector of ImageInfo-derived objects representing the images indexed by the search Engine.
	 */
	public Vector<ImageInfo> getDatabase() {
		return database;
	}
	
	/**
	 * Loads the database as described in an XML file.
	 * @param databaseFile The file containing the XML description of the database.
	 */
	public void loadDatabaseFile( String databaseFile ) {
		this.database = ImageCollectionReader.readDatabaseFile( databaseFile );
		this.indexDatabase();
	}
	
	/**
	 * Creates the descriptors and index for the current database.
	 */
	public abstract void indexDatabase();
	
	/**
	 * Searches the database for images that are similar to the query image provided.
	 * The search results are sorted in decreasing order of similarity to the query.
	 * If the query exists in the database, it is not returned as a result.
	 * @param queryImage The query image, described in an ImageInfo object.
	 * @return A vector of images (described as ImageInfo objects) in decreasing order of similarity.
	 */
	public abstract Vector<ImageInfo> queryDatabase( ImageInfo queryImage );
	
}
