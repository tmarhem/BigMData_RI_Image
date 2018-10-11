package searchEngine;

import java.util.Vector;
import utils.JavaCVTools;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import indexing.LinearSearchIndex;

public class SearchEngineV1 extends SearchEngine {
	
	
	LinearSearchIndex LSI = new LinearSearchIndex();
	MatVector descriptors = new MatVector();
	
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
		System.out.println("I indexed " + descriptors.toString().length() + " descriptors");
		System.out.println("count : " + count);
	}
	/*
	public Vector<ImageInfo> queryDatabase( ImageInfo queryImage ){
		return database;
	}		*/
}
