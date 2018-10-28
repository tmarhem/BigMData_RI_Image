package searchEngine;



import java.util.List;
import java.util.Vector;

import org.bytedeco.javacpp.opencv_core.DMatchVectorVector;
import org.bytedeco.javacpp.opencv_core.KeyPointVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_xfeatures2d.SIFT;

import indexing.LinearSearchIndex;

public class SiftLinear extends SearchEngine {
	
    LinearSearchIndex lsi = new LinearSearchIndex();
 	DMatchVectorVector matches = new DMatchVectorVector();
 	SIFT detector = SIFT.create();
 	
 	
	public SiftLinear(){
		this.database = new  Vector<ImageInfo>();
	}

	@Override
	public void indexDatabase() {
		Mat descriptors = new Mat(); 
		KeyPointVector keypoints = new KeyPointVector(database.size());
		MatVector alldescriptors = new MatVector(database.size());
		int count = 0;
		System.out.println(count);
		for (ImageInfo img : database) {
		detector.detectAndCompute(img.getImage(), new Mat(), keypoints, descriptors, false);
		alldescriptors.put(count, descriptors);
		count++;
		System.out.println(count);
		}
		lsi.index(alldescriptors);
		//int total = (int)keypoints.size();
		//System.out.println(total);
		 
	}

	@Override
	public Vector<ImageInfo> queryDatabase(ImageInfo queryImage) {
		//System.out.println(lsi.toString().length());
		
		Mat descriptorsquery = new Mat(); 
		KeyPointVector keypointsquery = new KeyPointVector();
		detector.detectAndCompute(queryImage.getImage(), new Mat(), keypointsquery, descriptorsquery, false);
		
		System.out.println("hi!");
		matches =lsi.knnQuery(descriptorsquery, 1);	
		//knnQuery tourne à l'infini, on utilise des ressources système sans fin
		System.out.print("sayhi4");

		Vector<ImageInfo> image= new Vector<ImageInfo>();
		int ctn = 0;
		for(int i=0; i< matches.size(); i++) {
			for (int j=0; j<matches.get(i).size(); j++) {
			ctn++;
			System.out.print(ctn);
			int image_index = matches.get(i).get(j).imgIdx();
			image.addElement(this.database.get(image_index));
		}
		}
		return image;
	}

}
