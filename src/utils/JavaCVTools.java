package utils;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.WindowConstants;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.KeyPointVector;
import org.bytedeco.javacpp.opencv_core.KeyPointVectorVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;

/**
 * Helper class for image search engine.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
public class JavaCVTools {

	/**
	 * Displays given image in a new window.
	 * @param image The image to display.
	 * @param caption The name of the window to be created.
	 */
	public static void show(Mat image, String caption) {
		CanvasFrame canvas = new CanvasFrame(caption, 1);
		canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		FrameConverter<Mat> converter = new ToMat();
		canvas.showImage(converter.convert(image));
	}
	
	/**
	 * Loads an image.
	 * @param file The file to be loaded.
	 * @param flags Loading flags (typically, image coding).
	 * @return A mat containing the image data.
	 */
	public static Mat load(File file, int flags) {
		Mat image = null;
		try {
			if(!file.exists()) {
				throw new FileNotFoundException("Image file does not exist: " + file.getAbsolutePath());
			}
			image = imread(file.getAbsolutePath(), flags);
			if(image == null || image.empty()) {
				throw new IOException("Couldn't load image: " + file.getAbsolutePath());
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return image;
	}
	
	/**
	 * Prints given (2D)mat to the terminal.
	 * @param m The mat object to display.
	 */
	public static void printMat( Mat m ) {
		Indexer idx = m.createIndexer();
		System.out.print( "[ " );
		for ( int i = 0 ; i < idx.rows() ; i++ ) {
			System.out.print( "[ " );
			for ( int j = 0 ; j < idx.cols() ; j++ ) {
				System.out.print( idx.getDouble( i, j ) + " " );
			}
			System.out.println( "]" );
		}
		System.out.println( "]" );
	}
	
	/**
	 * Saves keypoints computed on a set of images to a folder (one file per image).
	 * @param folderName The path to the output folder.
	 * @param keypoints The keypoints to save.
	 */
	public static void saveKeypoints( String folderName, KeyPointVectorVector keypoints ) {
		new File(folderName).mkdirs();
		FileStorage file;
		
		for ( int i = 0 ; i < keypoints.size() ; i++ ) {
			file = new FileStorage( folderName + File.separator + "keypoints_" + i + ".yml", FileStorage.WRITE );
			opencv_core.write( file, "keypoints", keypoints.get(i) );
			file.close();
		}
	}
	
	/**
	 * Loads keypoints from a folder, previously saved using saveKeypoints. 
	 * @param folderName The folder containing the files (one per image) to load.
	 * @return The loaded keypoints.
	 */
	public static KeyPointVectorVector loadKeypoints( String folderName ) {
		File[] fileList = new File(folderName).listFiles();
		sortFilesByIds( fileList );
		KeyPointVectorVector keypoints = new KeyPointVectorVector( fileList.length );
		KeyPointVector currentKeypoints;
		FileStorage file;

		for ( int i = 0 ; i < fileList.length ; i++ ) {
			file = new FileStorage( fileList[i].getAbsolutePath(), FileStorage.READ );
			currentKeypoints = new KeyPointVector();
			opencv_core.read( file.get("keypoints"), currentKeypoints );
			keypoints.put( i, currentKeypoints );
			file.close();
		}
		
		return keypoints;
	}
	
	/**
	 * Save local descriptors computed on a set of images (one descriptor mat per image). 
	 * @param folderName The folder to save the descriptor files (one per image).
	 * @param descriptors The descriptors to be saved.
	 */
	public static void saveLocalDescriptors( String folderName, MatVector descriptors ) {
		new File(folderName).mkdirs();
		FileStorage file;
		
		for ( int i = 0 ; i < descriptors.size() ; i++ ) {
			file = new FileStorage( folderName + File.separator + "descriptors_" + i + ".yml", FileStorage.WRITE );
			opencv_core.write( file, "descriptors", descriptors.get(i) );
			file.close();
		}
	}
	
	/**
	 * Loads local descriptors from a folder, previously saved using saveLocalDescriptors. 
	 * @param folderName The folder containing the files to load.
	 * @return The loaded descriptors.
	 */
	public static MatVector loadLocalDescriptors( String folderName ) {
		File[] fileList = new File(folderName).listFiles();
		sortFilesByIds( fileList );
		MatVector descriptors = new MatVector( fileList.length );
		Mat currentDescriptors;
		FileStorage file;

		for ( int i = 0 ; i < fileList.length ; i++ ) {
			file = new FileStorage( fileList[i].getAbsolutePath(), FileStorage.READ );
			currentDescriptors = new Mat();
			opencv_core.read( file.get("descriptors"), currentDescriptors );
			descriptors.put( i, currentDescriptors );
			file.close();
		}
		
		return descriptors;
	}
	
	/**
	 * Sorts array of file by filename id.
	 * @param files Files to sort.
	 */
	private static void sortFilesByIds( File[] files ) {
		Arrays.sort(files, 0, files.length, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				int idx1 = Integer.parseInt( o1.getName().split("\\.")[0].split("\\_")[1] );
				int idx2 = Integer.parseInt( o2.getName().split("\\.")[0].split("\\_")[1] );
				int result = 0;
				if ( idx1 < idx2 ) {
					result = -1;
				} else if ( idx1 > idx2 ) {
					result = 1;
				}
				return result;
			}
			
		});

	}
	
	/**
	 * Loads a bag-of-word vocabulary matrix.
	 * @param fileName The file to load.
	 * @return The vocabulary matrix.
	 */
	public static Mat loadVocabulary( String fileName ) {
		Mat vocabulary = new Mat();
		FileStorage file = new FileStorage( fileName, FileStorage.READ );
		opencv_core.read( file.root(), vocabulary );
		file.close();
		return vocabulary;
	}
	
	/**
	 * Computes the joint color histogram of the given image.
	 * @param image Image for which the histogram is computed.
	 * @param nb_bins_per_channel Number of bins per channels (total size: nb_bins_per_channel ^ 3).
	 * @return The histogram as a row vector of doubles.
	 */
	public static Mat computeColorHistogram( Mat image, int nb_bins_per_channel ) {
		long currentBin;
		Mat histogram = new Mat( 1, nb_bins_per_channel * nb_bins_per_channel * nb_bins_per_channel, opencv_core.CV_32F );
		UByteIndexer iidx = (UByteIndexer)image.createIndexer();
		FloatIndexer hidx = (FloatIndexer )histogram.createIndexer();
		
		// set every bin to 0
		for ( int i = 0 ; i < hidx.cols() ; i++ ) {
			hidx.put( 0, i, 0.0f );
		}
		
		// parse image
		for ( int i = 0 ; i < iidx.rows() ; i++ ) {
			for ( int j = 0 ; j < iidx.cols() ; j++ ) {
				currentBin = (int)Math.floor((iidx.get( i, j, 0 ) * (double)nb_bins_per_channel / 256.)) * nb_bins_per_channel * nb_bins_per_channel;
				currentBin += (int)Math.floor((iidx.get( i, j, 1 ) * (double)nb_bins_per_channel / 256.)) * nb_bins_per_channel;
				currentBin += (int)Math.floor((iidx.get( i, j, 2 ) * (double)nb_bins_per_channel / 256.));
				hidx.put( 0, currentBin, hidx.get(0, currentBin) + 1 );
			}
		}
		
		// normalize
		for ( int i = 0 ; i < hidx.cols() ; i++ ) {
			hidx.put( 0, i, (float)Math.ceil( hidx.get(0, i) / (iidx.rows()*iidx.cols()) * 255.0 ) );
		}
		
		return histogram;
	}	

}
