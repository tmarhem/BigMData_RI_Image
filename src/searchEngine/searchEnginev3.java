/**
 * 
 */
package searchEngine;

import java.util.Vector;

import indexing.lsh.E2LSHIndex;

/**
 * @author Bloum
 *
 */
public class searchEnginev3 extends SearchEngine {

	E2LSHIndex index = new E2LSHIndex(0, 0, 0, 0, 0, 0);
	
	@Override
	public void indexDatabase() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vector<ImageInfo> queryDatabase(ImageInfo queryImage) {
		// TODO Auto-generated method stub
		return null;
	}

}
