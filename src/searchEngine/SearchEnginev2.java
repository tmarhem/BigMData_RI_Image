package searchEngine;

import java.util.ArrayList;
import java.util.Vector;

import org.bytedeco.javacpp.opencv_core.DMatchVectorVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import indexing.kdtree.KdTree;
import utils.JavaCVTools;

public class SearchEnginev2 extends SearchEngine {

	DMatchVectorVector matches = new DMatchVectorVector();
	KdTree kdTree = new KdTree(6000, false);
	
	@Override
	public void indexDatabase() {
		MatVector descriptors = new MatVector(database.size());
		System.out.println(database.size());
		int count = 0;
		for(ImageInfo e : database) {
			e.loadImage();
			Mat color_hist = JavaCVTools.computeColorHistogram(e.getImage(),16);
			descriptors.put(count,color_hist);
			
			count++;
		}
		kdTree.index(descriptors);
	}

	@Override
	public Vector<ImageInfo> queryDatabase(ImageInfo queryImage) {
		Vector<ImageInfo> results = new Vector<ImageInfo>();
		queryImage.loadImage();
		Mat queryImage_desc = JavaCVTools.computeColorHistogram(queryImage.getImage(),16);
		matches = kdTree.epsQuery(queryImage_desc, 30);
		for(int i=0; i<matches.size();i++) {
			for(int j=0; j<matches.get(i).size();j++) {
				int image_index = matches.get(i).get(j).imgIdx();
				System.out.println(image_index);
				results.addElement(database.get(image_index));
				}
		}
		return results;
	}
}
