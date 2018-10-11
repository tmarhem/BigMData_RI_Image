package gui;

/**
 * Listener for components that require to listen to image selection changes
 * in ImageBrowser. 
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
interface ImageChangedListener {

	/**
	 * The method called when an ImageChangedEvent event occurs.
	 * @param e The event.
	 */
	public void imageChanged(ImageChangedEvent e);
	
}
