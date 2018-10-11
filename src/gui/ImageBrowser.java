package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import searchEngine.ImageInfo;

/**
 * An image browsing interface that provides to users a list of images and allows
 * them to click on one image to select it. Listeners are notified of the image
 * selection. 
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
class ImageBrowser extends JPanel implements ActionListener, MouseListener {

	private static final int IB_BUTTON_WIDTH = 100;
	private static final int IB_BUTTON_HEIGHT = 30;
	private static final int IMAGE_MARGIN = 25;
	private static final int INTER_IMAGE_MARGIN = 15;
	
	private static final int STANDARD_LINE_THICKNESS = 1;
	private static final int SELECTED_LINE_THICKNESS = 3;
	
	private static final int THUMBNAIL_DEFAULT_WIDTH = 150;
	private static final int THUMBNAIL_DEFAULT_HEIGHT = 150;
	
	
	public static final String IB_TITLE = "Browser";
	public static final String COUNT_STRING = " image(s)";
	
	private static final Color IMAGE_FRAME_COLOR = Color.BLACK;
	
	private Vector<ImageInfo> images;
	private LinkedList<ImageInfo> displayedImages;
	private ImageInfo selectedImage;
	private int colNumber;
	private int rowNumber;
	
	private int currentFirstImageIndex;
	
	private ImageChangedListener imageChangedListener;
	
	private JButton upButton;
	private JButton downButton;
	private JPanel imagePanel;
	private JLabel countLabel;
	
	/**
	 * Builds a new ImageBrowser object with given image collection and container dimensions
	 * @param images The image collection
	 * @param width The container's width
	 * @param height The container's height
	 */
	public ImageBrowser( Vector<ImageInfo> images, int width, int height ) {
		super();
		
		this.images = images;
		this.selectedImage = null;
		
		this.displayedImages = new LinkedList<ImageInfo>();
	
		this.currentFirstImageIndex = 0;
		this.imageChangedListener = null;
		
		this.createButtons();
		this.setPanel(width, height);
		this.setColumnsAndRows();
	}
	
	/**
	 * Return the ImageInfo object of the image clicked by the user.
	 * @return The ImageInfo object of the image clicked by the user.
	 */
	public ImageInfo getSelectedImage() {
		return this.selectedImage;
	}
	
	/**
	 * Sets the selected image to null
	 */
	public void resetSelectedImage() {
		this.selectedImage = null;
		this.imageChangedListener.imageChanged( new ImageChangedEvent( this, 
				   													   9999,
				   													   ImagePanel.IMAGE_CHANGE_CLEAR_OPTION,
				   													   this.selectedImage ) );
	}
	
	/**
	 * Loads the image collection to be browsed
	 * @param images The image collection
	 */
	public void setImages( Vector<ImageInfo> images ) {
		this.images = images;
		this.currentFirstImageIndex = 0;
		this.displayedImages.clear();
		if ( images != null ) {
			this.setColumnsAndRows();
//			for(int i=0 ; i<colNumber*rowNumber && i<images.size() ; i++) {
//			displayedImages.addLast(images.get(i));
//		}
			this.countLabel.setText( images.size() + COUNT_STRING );
		} else {
			displayedImages.clear();
			this.countLabel.setText( " " );
		}
//		this.selectedImage = null;
//		this.imageChangedListener.imageChanged(new ImageChangedEvent(this, 9998, ImagePanel.IMAGE_CHANGE_CLEAR_OPTION, null));
		
		this.repaint();
	}
	
	/**
	 * Set the listener to whom notifications of change of selected image should be sent.
	 * @param l The listener
	 */
	public void setImageChangedListener( ImageChangedListener l ) {
		this.imageChangedListener = l;
	}
	
	/**
	 * Creates the panels of the browser
	 * @param width the initial width of the container
	 * @param height the initial height of the container
	 */
	private void setPanel( int width, int height ) {
		
		this.setPreferredSize( new Dimension( width, height ) );
		this.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ), ImageBrowser.IB_TITLE ) );

		// create the image panel
		imagePanel = new JPanel() {
			public void paint(Graphics g) {
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage imageBuffer;
				if(images != null) {
					g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
					int currentX = IMAGE_MARGIN;
					int shiftX = 0;
					int currentY = IMAGE_MARGIN;
					int shiftY = 0;
					ImageInfo currentImage = null;
					for(int i=0 ; i<rowNumber ; i++) {
						for(int j=0 ; j<colNumber ; j++) {
							if(displayedImages.size() <= i*colNumber+j)
								break;
							currentImage = displayedImages.get(i*colNumber+j);
							imageBuffer = currentImage.getBufferedImage();
							shiftX = (int)((THUMBNAIL_DEFAULT_WIDTH
									        - getScaledImage( imageBuffer, THUMBNAIL_DEFAULT_WIDTH, THUMBNAIL_DEFAULT_HEIGHT ).getWidth() ) / 2.0 );
							shiftY = (int)((THUMBNAIL_DEFAULT_HEIGHT
							        		- getScaledImage( imageBuffer, THUMBNAIL_DEFAULT_WIDTH, THUMBNAIL_DEFAULT_HEIGHT ).getHeight() ) / 2.0);
							g2d.drawImage( getScaledImage( imageBuffer, THUMBNAIL_DEFAULT_WIDTH, THUMBNAIL_DEFAULT_HEIGHT ),
										  null,
										  currentX+shiftX,
										  currentY+shiftY);
							g2d.setColor(IMAGE_FRAME_COLOR);
							if ( displayedImages.get( i * colNumber + j ) == selectedImage ) {
								g2d.setStroke( new BasicStroke( SELECTED_LINE_THICKNESS ) );
							} else {
								g2d.setStroke( new BasicStroke( STANDARD_LINE_THICKNESS ) );
							}
							g2d.drawRect( currentX,
										  currentY,
										  THUMBNAIL_DEFAULT_WIDTH,
										  THUMBNAIL_DEFAULT_HEIGHT );
							currentX += THUMBNAIL_DEFAULT_WIDTH + INTER_IMAGE_MARGIN;
						}
						currentX = IMAGE_MARGIN;
						currentY += THUMBNAIL_DEFAULT_HEIGHT + INTER_IMAGE_MARGIN;
					}
				}
			}
		};
		this.imagePanel.setPreferredSize( new Dimension( width, height - 2 * IB_BUTTON_HEIGHT ) );
		this.imagePanel.setAlignmentY( Component.CENTER_ALIGNMENT );
		this.imagePanel.addMouseListener( this );

		//create the button margin panel
		
		// create the label component
		countLabel = new JLabel( " " );
		countLabel.setAlignmentX( CENTER_ALIGNMENT );
		
		// adds the buttons and image panel
		this.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
		this.add( this.countLabel );
		this.add( Box.createVerticalStrut( INTER_IMAGE_MARGIN ) );
		this.add( this.upButton );
		this.add( this.imagePanel );
		this.add( this.downButton );
	}
	
	/**
	 * Sets the buttons of the panel
	 */
	private void createButtons() {
		this.upButton = new JButton( "UP" );
		this.upButton.addActionListener( this );
		this.upButton.setPreferredSize( new Dimension( IB_BUTTON_WIDTH, IB_BUTTON_HEIGHT ) );
		this.upButton.setAlignmentX( Component.CENTER_ALIGNMENT );
		
		this.downButton = new JButton( "DOWN" );
		this.downButton.addActionListener( this );
		this.downButton.setPreferredSize( new Dimension( IB_BUTTON_WIDTH, IB_BUTTON_HEIGHT ) );
		this.downButton.setAlignmentX( Component.CENTER_ALIGNMENT );
	}

	/**
	 * Updates the view when going up in the image list
	 */
	private void goUp() {
		
		if ( images == null || this.currentFirstImageIndex == 0 ) {
			return;
		}

		this.currentFirstImageIndex -= this.rowNumber*this.colNumber;
		if ( this.currentFirstImageIndex < 0 ) {
			this.currentFirstImageIndex = 0;
		}
		
		while( !this.displayedImages.isEmpty() ) {
			this.displayedImages.pop();
		}
		
		for ( int i = 0 ; i < this.rowNumber * this.colNumber ; i++ ) {
			this.displayedImages.addLast( this.images.get( this.currentFirstImageIndex + i ) );
		}
		this.repaint();
	}
	
	/**
	 * Updates the view when going down in the image list
	 */
	private void goDown() {
		if ( images == null || this.currentFirstImageIndex + this.colNumber * this.rowNumber >= images.size() ) {
			return;
		}
			
		while ( !this.displayedImages.isEmpty() ) {
			this.displayedImages.pop();
		}
		
		this.currentFirstImageIndex += this.rowNumber * this.colNumber;
		if ( this.currentFirstImageIndex > this.images.size() - this.rowNumber * this.colNumber ) {
			this.currentFirstImageIndex = this.images.size() - this.rowNumber * this.colNumber;
		}
		
		for ( int i = 0 ; i < this.rowNumber * this.colNumber ; i++ ) {
			this.displayedImages.addLast( this.images.get( this.currentFirstImageIndex + i ) );
		}
		this.repaint();
	}

	/**
	 * Select the image located at pixel (x,y) in the imagePanel coordinates
	 * @param x x coordinate of the pixel
	 * @param y y coordinate of the pixel
	 */
	public void selectImageAt( int x, int y ) {
		int col = (int)( ( x - IMAGE_MARGIN ) / ( THUMBNAIL_DEFAULT_WIDTH + INTER_IMAGE_MARGIN ) );
		int xRest = ( ( x - IMAGE_MARGIN ) % ( THUMBNAIL_DEFAULT_WIDTH + INTER_IMAGE_MARGIN) );
		int row = (int)( ( y - IMAGE_MARGIN ) / ( THUMBNAIL_DEFAULT_HEIGHT + INTER_IMAGE_MARGIN ) );
		int yRest = ( ( y - IMAGE_MARGIN ) % ( THUMBNAIL_DEFAULT_HEIGHT + INTER_IMAGE_MARGIN ) );
		
		if ( xRest <= THUMBNAIL_DEFAULT_WIDTH 
				&& yRest <= THUMBNAIL_DEFAULT_HEIGHT
				&& col < this.colNumber
				&& row < this.rowNumber
				&& this.displayedImages.size() > row*colNumber+col ) {
			this.selectedImage = this.displayedImages.get( row * colNumber + col );
			this.imageChangedListener.imageChanged( new ImageChangedEvent( this, 
																		   9999,
																		   ImagePanel.IMAGE_CHANGE_CLEAR_OPTION,
																		   this.selectedImage ) );
			this.repaint();
		}
	}
	
	/**
	 * Updates the size of the container
	 * @param width the new width
	 * @param height the new height
	 */
	public void updatesize( int width, int height ) {
		this.setPreferredSize( new Dimension( width, height ) );
		this.setColumnsAndRows();
		this.repaint();
	}
	
	/**
	 * Sets the rows and columns according to the size of the browser panel
	 */
	private void setColumnsAndRows() {
		this.colNumber = ( this.imagePanel.getWidth() + INTER_IMAGE_MARGIN - 2 * IMAGE_MARGIN )
														/ (INTER_IMAGE_MARGIN + THUMBNAIL_DEFAULT_WIDTH );
		this.rowNumber = ( this.imagePanel.getHeight() + INTER_IMAGE_MARGIN - 2 * IMAGE_MARGIN )
														/ ( INTER_IMAGE_MARGIN + THUMBNAIL_DEFAULT_HEIGHT );
		while ( this.colNumber * this.rowNumber < displayedImages.size() ) {
			displayedImages.removeLast();
		}
		
		while ( this.images != null && this.colNumber*this.rowNumber > displayedImages.size()
				&& this.currentFirstImageIndex + this.displayedImages.size() < this.images.size() ) {
			displayedImages.addLast( images.get( currentFirstImageIndex + displayedImages.size() ) );	
		}
	}
	
	/**
	 * Returns a version of the image scaled to the given maximum dimensions.
	 * The ratio of the image is preserved.
	 * @param image The image to be scaled
	 * @param maxWidth The maximum width of the scaled image.
	 * @param maxHeight The maximum height of the scaled image.
	 * @return The scaled image data.
	 */
	public static BufferedImage getScaledImage( BufferedImage image, int maxWidth, int maxHeight ) {
		BufferedImage scaledImage = null;
		double scaleFactor;
		int scaledImageWidth;
		int scaledImageHeight;
		Graphics2D g2d;
		
		if ( image == null ) {
			return null;
		}
		
		// compute the scale factor to best fit the original image size 
		// without exceeding requested width and height
		scaleFactor = 0.0;	
		if ( image.getHeight() > image.getWidth() ) {
			scaleFactor = (double)maxHeight / (double)image.getHeight();
		} else {
			scaleFactor = (double)maxWidth / (double)image.getWidth();
		}
		scaledImageWidth = (int)( image.getWidth() * scaleFactor );
		scaledImageHeight = (int)( image.getHeight() * scaleFactor );

		// create the scaled image
		scaledImage = new BufferedImage( scaledImageWidth, scaledImageHeight, image.getType() );
		g2d = scaledImage.createGraphics();
		g2d.drawImage( image, 0, 0, scaledImageWidth, scaledImageHeight, null );
		g2d.dispose();
		
		return scaledImage;
	}
	
	/* ***************************************************************************************************/
	/* ********************************** LISTENER METHODS ***********************************************/
	/* ***************************************************************************************************/
	
	@Override
	public void actionPerformed( ActionEvent e ) {
		if ( e.getSource() == this.upButton ) {
			this.goUp();
			return;
		}
		
		if ( e.getSource() == this.downButton ) {
			this.goDown();
			return;
		}
	}
	
	@Override
	public void mouseClicked( MouseEvent e ) {
		if ( e.getButton() == MouseEvent.BUTTON1 ) {
			this.selectImageAt( e.getX(), e.getY() );
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {}
	
	@Override
	public void mouseReleased(MouseEvent e) {}
	
	@Override
	public void mouseEntered(MouseEvent e) {}
	
	@Override
	public void mouseExited(MouseEvent e) {}

}
