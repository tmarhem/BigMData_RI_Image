package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import searchEngine.ImageInfo;

/**
 * JPanel that displays an image and its properties as stored in an ImageInfo
 * object (name, description, date of creation). 
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
class ImagePanel extends JPanel {
	
	// message to be transmitted by ImageChanged events.
	public static final String IMAGE_CHANGE_CLEAR_OPTION = "clear";

	// dimensions to lay out the components.
	public static final int IMAGE_VERTICAL_MARGIN = 25;
	public static final int IMAGE_HORIZONTAL_MARGIN = 25;
	public static final int TITLE_DEFAULT_HEIGHT = 40;
	
	// Text appearing on the interface.
	public static final String IMAGE_PANEL_TITLE = "Query image";
	public static final String IMAGE_NAME_TITLE = "Image name";
	public static final String IMAGE_DESCRIPTION_TITLE = "Image description";
	public static final String IMAGE_DATE_TITLE = "Image date";
	
	/**
	 * Image to be displayed in the panel
	 */
	private ImageInfo image;
	
	/**
	 * Width of the image as displayed on the panel.
	 */
	private int width;
	
	/**
	 * Height of the image as displayed on the panel.
	 */
	private int height;
	
	/**
	 * Panel displaying the image.
	 */
	private JPanel imagePanel;
	
	/**
	 * Area displaying the name of the image.
	 */
	private JTextArea titleArea;
	
	/**
	 * Area displaying the description of the image. 
	 */
	private JTextArea descriptionArea;
	
	/**
	 * Area displaying the date of the image.
	 */
	private JTextArea dateArea;
	
	/**
	 * Constructor to create an ImagePanel with the provided dimensions.
	 * @param width The maximum display width of the image.
	 * @param height The maximum display height of the image.
	 */
	public ImagePanel( int width, int height ) {
		super();
		this.width = width;
		this.height = height;
		this.image = null;
		
		this.setBorder( BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ), 
				IMAGE_PANEL_TITLE ) );

		setPanels();
	}
	
	// lays out the components of the ImagePanel.
	private void setPanels() {
		// main panel
		this.setPreferredSize( new Dimension( this.width, this.height ) );
		this.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
		
		// image panel
		this.imagePanel = new JPanel() {
			
			public void paint( Graphics g ) {
			
				if ( image != null ) {
					Graphics2D g2d = (Graphics2D)g;
					BufferedImage imageBuffer = image.getBufferedImage();
					double xScaleFactor = ( (double)this.getWidth() - 2 * (double)IMAGE_HORIZONTAL_MARGIN )
										  / (double)imageBuffer.getWidth();
					double yScaleFactor = ( (double)this.getHeight() - 2 * (double)IMAGE_VERTICAL_MARGIN) 
										  / (double)imageBuffer.getHeight();
					double scaleFactor = Math.min( xScaleFactor, yScaleFactor );
					
					int imageWidth = (int)( imageBuffer.getWidth() * scaleFactor );
					int imageHeight = (int)( imageBuffer.getHeight() * scaleFactor );
					int shiftX = ( this.getWidth() - imageWidth - 2 * IMAGE_HORIZONTAL_MARGIN ) / 2;
					int shiftY = ( this.getHeight() - imageHeight - 2 * IMAGE_VERTICAL_MARGIN ) / 2;
					g2d.drawImage( imageBuffer,
							       IMAGE_HORIZONTAL_MARGIN + shiftX,
							       IMAGE_VERTICAL_MARGIN + shiftY,
							       imageWidth,
							       imageHeight,
							       null);
				}
			}
		};
		this.imagePanel.setAlignmentX( Component.CENTER_ALIGNMENT );
		this.add( this.imagePanel );
		
		// text areas
		this.titleArea = new JTextArea( 1, 1 );
		this.titleArea.setEditable( false );
		this.titleArea.setBorder( BorderFactory.createTitledBorder(
												BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ),
												IMAGE_NAME_TITLE ) );
		this.titleArea.setAlignmentX( Component.CENTER_ALIGNMENT );
		this.titleArea.setBackground( this.getBackground() );
		this.add( this.titleArea );
		
		// description area
		this.descriptionArea = new JTextArea( 1, 1 );
		this.descriptionArea.setEditable( false );
		this.descriptionArea.setBorder( BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ), 
				IMAGE_DESCRIPTION_TITLE ) );
		this.descriptionArea.setAlignmentX( Component.CENTER_ALIGNMENT );
		this.descriptionArea.setBackground( this.getBackground() );
		this.add( this.descriptionArea );
		
		// date area
		this.dateArea = new JTextArea( 1, 1 );
		this.dateArea.setEditable( false );
		this.dateArea.setBorder( BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ), 
				IMAGE_DATE_TITLE ) );
		this.dateArea.setAlignmentX( Component.CENTER_ALIGNMENT );
		this.dateArea.setBackground( this.getBackground() );
		this.add( this.dateArea );
		
		this.updateSizes();
	}
	
	// updates the text in the areas when the image changes.
	private void updateText() {
		
		if ( this.image != null ) {
			this.titleArea.setText( this.image.getFileName() );
		}
		
	}
	
	/**
	 * Updates the size of the subcomponents according to the current size of the ImagePanel component. 
	 */
	public void updateSizes() {
		
		this.revalidate();
		
		this.imagePanel.setPreferredSize( new Dimension( this.getWidth(), this.getHeight() * 2 / 3 ) );
		this.imagePanel.setMinimumSize( new Dimension( this.getWidth(), this.getHeight() * 2 / 3 ) );
		this.imagePanel.setMaximumSize( new Dimension( this.getWidth(), this.getHeight() * 2 / 3 ) );
		
		this.titleArea.setPreferredSize( new Dimension( this.getWidth(), TITLE_DEFAULT_HEIGHT ) );
		this.titleArea.setMaximumSize( new Dimension( this.getWidth(), TITLE_DEFAULT_HEIGHT ) );
		this.titleArea.setMinimumSize( new Dimension( this.getWidth(), TITLE_DEFAULT_HEIGHT ) );
		
		this.dateArea.setPreferredSize( new Dimension( this.getWidth(), TITLE_DEFAULT_HEIGHT ) );
		this.dateArea.setMaximumSize( new Dimension( this.getWidth(), TITLE_DEFAULT_HEIGHT ) );
		this.dateArea.setMinimumSize( new Dimension( this.getWidth(), TITLE_DEFAULT_HEIGHT ) );
		
		this.revalidate();
		this.updateText();
		this.repaint();
		
	}
	
	/**
	 * Sets the image displayed in the panel.
	 * @param image The new image to display.
	 */
	public void setImage( ImageInfo image ) {
		this.image = image;
		this.updateSizes();
	}
	
	/**
	 * Returns the image currently displayed in the panel.
	 * @return The image currently displayed in the panel.
	 */
	public ImageInfo getImage() {
		return this.image;
	}
	
}
