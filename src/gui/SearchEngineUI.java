package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import searchEngine.ImageInfo;
import searchEngine.SearchEngine;

/**
 * A GUI for an image retrieval system.
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
public class SearchEngineUI extends JFrame implements ActionListener, ImageChangedListener, ComponentListener {

	
	/* Icon file of the main frame. */
	private static final String SOFTWARE_LOGO_PATH = "data/telecom-lille-transparent.png";
	
	/* Dimensions of the components of the interface. */
	private static final int DEFAULT_FRAME_WIDTH = 1200;
	private static final int DEFAULT_FRAME_HEIGHT = 800;
	private static final int DEFAULT_BROWSER_WIDTH = 500;
	private static final int DEFAULT_BROWSER_HEIGHT = 700;
	private static final int DEFAULT_QUERY_PANEL_WIDTH = 300;
	private static final int DEFAULT_QUERY_PANEL_HEIGHT = 700;
	private static final int TOP_PANEL_HEIGHT = 50;
	private static final Dimension BUTTON_DIMENSION = new Dimension( 180, 30 );
	private static final Dimension COLOR_PICK_BUTTON_DIMENSION = new Dimension( 40, 30 );
	
	/* Texts appearing in the components. */
	private static final String LOAD_BUTTON_TEXT = "Load database";
	private static final String BROWSE_BUTTON_TEXT = "Browse";
	private static final String QUERY_BUTTON_TEXT = "Search";
	private static final String COLOR_FILTER_CHECK_BOX_TEXT = "Color filter";
	private static final String COLOR_CHOOSER_WINDOW_TITLE = "Pick a color for the filter";
	private static final String FRAME_TITLE = "BigMData image search engine v0.2";
	
	/**
	 * The search engine used by the interface.
	 */
	private SearchEngine searchEngine;
	
	/**
	 * The color filter used by the interface.
	 */
	private ColorSearchFilter colorFilter;
	
	/**
	 * The panel showing the query selected by the user
	 */
	private ImagePanel queryPanel;
	
	/**
	 * The panel allowing the user to browse the whole database or the search results.
	 */
	private ImageBrowser imageBrowser;
	
	/**
	 * The button to load the database file.
	 */
	private JButton loadDatabaseButton;
	
	/**
	 * The button to reset the images displayed in the image browser.
	 */
	private JButton browseButton;
	
	/**
	 * The button to query the database using the selected query.
	 */
	private JButton queryButton;
	
	/**
	 * The check box to enable the color filter.
	 */
	private JCheckBox colorFilterCheckBox;
	
	/**
	 * The button to pick the color used by the filter.
	 */
	private JButton pickFilterColorButton;
	
	/**
	 * The file chooser used to load the database file.
	 */
	private JFileChooser fileChooser;
	
	/**
	 * Constructor taking a search engine and color filter as input. 
	 * @param searchEngine The search engine to be used by the system (cannot be null).
	 * @param colorFilter The color filter to be used by the system (optional - use null if not used).
	 */
	public SearchEngineUI( SearchEngine searchEngine, ColorSearchFilter colorFilter ) {
		super( FRAME_TITLE );
		
		this.setIconImage( new ImageIcon( SearchEngineUI.class.getClassLoader().getResource( SOFTWARE_LOGO_PATH ) ).getImage() );
		
		this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		this.addComponentListener( this );
		
		this.fileChooser = new JFileChooser();
		this.searchEngine = searchEngine;
		this.colorFilter = colorFilter;
		
		this.createButtons();
		this.setPanels();
		
		this.setVisible( true );
		this.pack();
	}
	
	/**
	 * Constructor for an interface that does not use a color filter
	 * @param searchEngine The search engine to be used by the interface.
	 */
	public SearchEngineUI( SearchEngine searchEngine ) {
		this( searchEngine, null );
	}

	// creates the buttons of the interface
	private void createButtons() {
		
		// load database button
		this.loadDatabaseButton = new JButton( LOAD_BUTTON_TEXT );
		this.loadDatabaseButton.setPreferredSize( BUTTON_DIMENSION );
		this.loadDatabaseButton.addActionListener( this );
		this.loadDatabaseButton.setEnabled( this.searchEngine != null );
		
		// browse button
		this.browseButton = new JButton( BROWSE_BUTTON_TEXT );
		this.browseButton.setPreferredSize( BUTTON_DIMENSION );
		this.browseButton.addActionListener( this );
		this.browseButton.setEnabled( false );
		
		// query button
		this.queryButton = new JButton( QUERY_BUTTON_TEXT );
		this.queryButton.setPreferredSize( BUTTON_DIMENSION );
		this.queryButton.addActionListener( this );
		this.queryButton.setEnabled( false );
		
		// color filter check box
		this.colorFilterCheckBox = new JCheckBox( COLOR_FILTER_CHECK_BOX_TEXT );
		this.colorFilterCheckBox.setSelected( false );
		this.colorFilterCheckBox.setEnabled( this.colorFilter != null );
		
		// color chooser for color filter
		this.pickFilterColorButton = new JButton();
		this.pickFilterColorButton.setPreferredSize( COLOR_PICK_BUTTON_DIMENSION );
		this.pickFilterColorButton.setBackground( Color.BLACK );
		this.pickFilterColorButton.addActionListener( this );
		this.pickFilterColorButton.setEnabled( this.colorFilter != null );
	}
	
	// lays out the different components of the interface
	private void setPanels() {
		
		JPanel topPanel;
		
		// set general container of the frame and its properties
		this.setContentPane( new JPanel() );
		this.getContentPane().setLayout( new BorderLayout() );
		this.getContentPane().setPreferredSize( new Dimension( DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT ) );
		
		// set top panel with buttons
		topPanel = new JPanel();
		topPanel.setLayout( new BoxLayout( topPanel, BoxLayout.LINE_AXIS ) );
		topPanel.setPreferredSize( new Dimension( DEFAULT_FRAME_WIDTH, TOP_PANEL_HEIGHT ) );
		topPanel.add( Box.createHorizontalStrut( 50 ) );
		topPanel.add( this.loadDatabaseButton );
		topPanel.add( Box.createHorizontalGlue() );
		topPanel.add( this.browseButton );
		topPanel.add( Box.createHorizontalGlue() );
		topPanel.add( this.queryButton );
		topPanel.add( Box.createHorizontalStrut( 10 ) );
		topPanel.add( this.colorFilterCheckBox );
		topPanel.add( this.pickFilterColorButton );
		topPanel.add( Box.createHorizontalStrut( 50 ) );
		this.getContentPane().add( topPanel, BorderLayout.NORTH );
		
		// set query panel
		this.queryPanel = new ImagePanel( DEFAULT_QUERY_PANEL_WIDTH, DEFAULT_QUERY_PANEL_HEIGHT );
		this.getContentPane().add( this.queryPanel, BorderLayout.WEST );
		
		// set browser panel
		this.imageBrowser = new ImageBrowser( new Vector<ImageInfo>(), DEFAULT_BROWSER_WIDTH, DEFAULT_BROWSER_HEIGHT );
		this.imageBrowser.setImageChangedListener( this );
		this.getContentPane().add( this.imageBrowser, BorderLayout.CENTER );	
	}
	
	// loads a collection file in a separate thread and displays a window
	// notifying the user to wait.
	private void loadCollectionFile( final File collectionFile ) {
		
		class OpenFileWorker extends SwingWorker<Void, Void> {

			private File collectionFile;
			private JDialog window;
			
			public OpenFileWorker( JFrame parent, File collectionFile ) {
				super();
				this.collectionFile = collectionFile;
				this.window = new JDialog( parent );
				window.setUndecorated(true);
				window.getContentPane().add( new JLabel( "Opening image database..." ) );
				window.setLocationRelativeTo( parent );
				window.setModal(false);
				window.pack();
				window.setVisible(true);
				
			}
			
			protected Void doInBackground() {
				SearchEngineUI.this.setEnabled( false );
				searchEngine.loadDatabaseFile( this.collectionFile.getAbsolutePath() );
				return null;
			}
			
			protected void done() {
				imageBrowser.setImages( searchEngine.getDatabase() );
				imageBrowser.resetSelectedImage();
				browseButton.setEnabled( searchEngine.getDatabase() != null );
				repaint();
				window.setVisible(false);
				window.dispose();
				SearchEngineUI.this.setEnabled( true );
			}
		}
		
		final OpenFileWorker worker = new OpenFileWorker(this, collectionFile);
		worker.execute();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// load a new database
		if ( e.getSource() == this.loadDatabaseButton ) {
			if ( this.fileChooser.showOpenDialog( this ) == JFileChooser.APPROVE_OPTION ) {
				this.loadCollectionFile( this.fileChooser.getSelectedFile() );
			}
		}
		
		// reset the images to show the whole collection in the image browser
		if ( e.getSource() == this.browseButton ) {
			this.imageBrowser.setImages( this.searchEngine.getDatabase() );
			this.imageBrowser.resetSelectedImage();
			this.queryPanel.setImage( null );
			this.repaint();
		}
		
		// queries the database with the current query selected by the user
		if ( e.getSource() == this.queryButton && this.searchEngine != null && this.searchEngine.getDatabase() != null ) {
			Vector<ImageInfo> searchResults = this.searchEngine.queryDatabase( this.queryPanel.getImage() );
			if ( this.colorFilterCheckBox.isSelected() ) {
				searchResults = this.colorFilter.filter( searchResults, this.pickFilterColorButton.getBackground() ); 
			}
			this.imageBrowser.setImages( searchResults );
		}
		
		// picks a color for the color filter
		if ( e.getSource() == this.pickFilterColorButton ) {
			Color newColor = JColorChooser.showDialog( this, COLOR_CHOOSER_WINDOW_TITLE, this.pickFilterColorButton.getBackground() );
			if ( this.pickFilterColorButton != null ) {
				this.pickFilterColorButton.setBackground( newColor );
				this.pickFilterColorButton.repaint();
			}
		}
	}

	@Override
	public void imageChanged(ImageChangedEvent e) {
		if ( e.getSource() == this.imageBrowser ) {
			this.queryPanel.setImage( e.getImage() );
			this.queryButton.setEnabled( e.getImage() != null );
		}
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if ( e.getSource() == this && e.getID() == ComponentEvent.COMPONENT_RESIZED ) {
			System.out.println( "Component resized to (" + this.getContentPane().getWidth() + ", " + this.getContentPane().getHeight() + ").");
			this.imageBrowser.updatesize( this.getContentPane().getWidth() - DEFAULT_QUERY_PANEL_WIDTH,
					this.getContentPane().getHeight() - TOP_PANEL_HEIGHT );
			this.queryPanel.setPreferredSize( new Dimension( DEFAULT_QUERY_PANEL_WIDTH,
					this.getContentPane().getHeight() - TOP_PANEL_HEIGHT ) );
			this.queryPanel.updateSizes();
			this.repaint();
		}
	}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentHidden(ComponentEvent e) {}
	
}
