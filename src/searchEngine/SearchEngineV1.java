package searchEngine;

import java.util.Vector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import indexing.LinearSearchIndex;

public class SearchEngineV1 extends SearchEngine {
	
	
	LinearSearchIndex LSI = new LinearSearchIndex();
	MatVector descriptors;
	
	public SearchEngineV1() {
		this.database = new Vector<ImageInfo>(); 
	}
	
	public void indexDatabase() {
		int count = 0;
		for(ImageInfo e : database) {
			System.out.println("index");
			Mat color_hist = utils.JavaCVTools.computeColorHistogram(e.getImage(),0);
			descriptors.put(color_hist);
			count++;
			System.out.println(count);
		}
		LSI.index(descriptors);
		System.out.println("I indexed " + count + "descriptors");
	}
	
	public Vector<ImageInfo> queryDatabase( ImageInfo queryImage ){
		return database;
		
	}
	}
