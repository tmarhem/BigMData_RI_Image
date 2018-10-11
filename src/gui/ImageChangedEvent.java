package gui;

import java.awt.event.ActionEvent;

import searchEngine.ImageInfo;

/**
 * An event to notify listening components that the selected image in the image browser
 * has changed.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
class ImageChangedEvent extends ActionEvent {

	/**
	 * The new image selected in the browser.
	 */
	private ImageInfo image;
	
	/**
	 * Constructor
	 * @param source The Component generating the event.
	 * @param id The Id of the event.
	 * @param action The action associated with the event.
	 * @param image The new image.
	 */
	public ImageChangedEvent(Object source, int id, String action, ImageInfo image) {
		super(source, id, action);
		this.image = image;
	}
	
	/**
	 * Returns the image transfered through the event.
	 * @return The image.
	 */
	public ImageInfo getImage() {
		return this.image;
	}
	
}
