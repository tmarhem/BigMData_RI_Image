package searchEngine;

import java.awt.image.BufferedImage;
import java.io.File;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;

import org.bytedeco.javacpp.opencv_imgcodecs;

/**
 * Class providing basing elements of an image
 * (file name, location, image data). 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 */
public class ImageInfo {

    /**
     * The name of the image file on disk.
     */
    protected String fileName;
    
    /**
     * The folder where the image is stored on disk.
     */
    protected String folder;
    
    /**
     * The image data itself.
     */
	protected Mat image;
    
	/**
	 * Returns the image data. The image is loaded from its file if needed.
	 * The image data should only be accessed in this way, as it is loaded on demand.
	 * @return The image data.
	 */
    public Mat getImage() {
    	
    	if ( this.image == null ) {
    		this.loadImage();
    	}
    	
    	return this.image;
    }
    
    /**
     * Returns the image data as a BufferedImage to display it in Java GUI. 
     * @return The image data as a BufferedImage.
     */
    public BufferedImage getBufferedImage() {
    	 return new Java2DFrameConverter().convert(new ToMat().convert(this.getImage()));
    }
    
    /**
     * Frees the memory of the image buffer.
     */
    public void clearImageBuffer() {
    	this.image.release();
    	this.image = null;
    }
    
	/**
	 * Returns the name of the file containing the image.
	 * @return The name of the image.
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * Sets the file name of the image.
	 * @param fileName The name of the image file.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Returns the path to the folder containing the image file.
	 * @return The path to the folder containing the image file.
	 */
	public String getFolder() {
		return this.folder;
	}

	/**
	 * Sets the path to the folder containing the image file.
	 * @param folder The path to the folder containing the image file.
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * Loads the image data from the image file and stores it the the image buffer.
	 */
	protected void loadImage() {
		try {
			this.image = opencv_imgcodecs.imread( folder + File.separator + this.fileName );
			System.out.println( this.image );
		} catch ( Exception ioe ) {
			System.err.println( "Error: cannot read image file " + folder + File.separator + this.fileName + "." );
			ioe.printStackTrace();
		}
	}
	
}
