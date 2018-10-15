package searchEngine;

import java.util.Vector;

import indexing.kdtree.KdTree;

public class SearchEnginev2 extends SearchEngine {

	
	KdTree index = new KdTree(0, false);
	
	
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
