package searchEngine;

import java.util.HashSet;
import java.util.Vector;
import utils.JavaCVTools;

import org.bytedeco.javacpp.opencv_core.DMatch;
import org.bytedeco.javacpp.opencv_core.DMatchVector;
import org.bytedeco.javacpp.opencv_core.DMatchVectorVector;
import org.bytedeco.javacpp.opencv_core.DMatchVectorVector.Iterator;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import indexing.LinearSearchIndex;
import indexing.utils.EpsSearchResults;
import indexing.utils.SearchResults;

public class SearchEngineV1 extends SearchEngine {
	

	LinearSearchIndex LSI = new LinearSearchIndex();
	MatVector descriptors = new MatVector();
	DMatchVectorVector matches = new DMatchVectorVector();
	 
	public SearchEngineV1() {
		this.database = new Vector<ImageInfo>(); 
	}
	
	public void indexDatabase() {
		int count = 0;
		for(ImageInfo e : database) {
			e.loadImage();
			Mat color_hist = JavaCVTools.computeColorHistogram(e.getImage(),16);
			descriptors.put(color_hist);
			count++;
		}
		LSI.index(descriptors);
		System.out.println("Number of descriptors? " + descriptors.toString().length());
		System.out.println("count : " + count);
	}

	@Override
	public Vector<ImageInfo> queryDatabase(ImageInfo queryImage) {
		Vector<ImageInfo> results = new Vector<ImageInfo>();
		Mat queryImage_desc = JavaCVTools.computeColorHistogram(queryImage.getImage(),16);
		matches= LSI.epsQuery(queryImage_desc, 0);
		System.out.println(matches.size());
		for(int i=0; i<matches.size();i++) {
			DMatchVector test = matches.get(i);
			for(int j =0; j<test.size(); j++) {
				DMatch result = test.get(j);
				int image_index = result.imgIdx();
				results.addElement(database.get(image_index));
			}
		}
		return results;
	}
}
