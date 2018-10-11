package gui;

import java.awt.Color;
import java.util.Vector;

import searchEngine.ImageInfo;

/**
 * Interface describing the functionalities of an image filter based on color.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
interface ColorSearchFilter {

	/**
	 * Filters a vector of images based on the provided color.
	 * Images from the input vector re filtered out if they do not contain
	 * a significant amount of the provided color.
	 * @param images The vector of images to be filtered.
	 * @param color The color that the images must contain not to be filtered.
	 * @return The vector of filtered images.
	 */
	Vector<ImageInfo> filter( Vector<ImageInfo> images, Color color );
	
}
