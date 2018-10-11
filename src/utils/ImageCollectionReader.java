package utils;

import searchEngine.ImageInfo;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class provides a Handler to parse text database XML files using a
 * SAX parser. Files must follow this format :
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <imagebase>
 *         <imageinfo>
 *                 <filename>11_foret4.jpg</filename>
 *         </imageinfo>
 *         <imageinfo>
 *                 <filename>1436455635_bb385084ba.jpg</filename>
 *         </imageinfo>
 *         ...
 * </imagebase>
 * }
 * 
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */
public class ImageCollectionReader implements ContentHandler {

	/**
	 * Vector containing the description of the images read from the file
	 */
	Vector<ImageInfo> database;
	
	/**
	 * Description of the image being parsed 
	 */
	ImageInfo currentImageInfo;
	
	/**
	 * Last read data
	 */
	String currentData;
	
	/**
	 * Folder containing the images
	 */
	String fileLocation;
	
	/**
	 * Constructor of a handler for an XML image description file
	 * @param fileLocation The folder where the actual images are stored
	 */
	public ImageCollectionReader( String fileLocation ) {
		this.database = null;
		this.currentData = null;
		this.currentImageInfo = null;
		this.fileLocation = new String( fileLocation );
	}
	
	/**
	 * Returns the database created during the parsing
	 * @return The database created during the parsing
	 */
	public Vector<ImageInfo> getDatabase() {
		return this.database;
	}
	
	/**
	 * Runs a parser to parse the provided file and returns the resulting image database
	 * @param databaseFilePath The XML file to parse
	 * @return The database described by the XML file
	 */
	public static Vector<ImageInfo> readDatabaseFile( String databaseFilePath ) {
		XMLReader parser = null;
		Vector<ImageInfo> database = null;
		File databaseFile = new File( databaseFilePath );
		
		try {
			parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler( new ImageCollectionReader( databaseFile.getParent() + File.separator + "images" ) );
			parser.parse( databaseFile.toURI().toString() );

		} catch ( SAXException saxe ) {
			System.err.println( "Error: cannot parse database file " + databaseFile.getAbsolutePath() + "." );
			saxe.printStackTrace();
		} catch ( IOException ioe ) {
			System.err.println( "Error: cannot open database file " + databaseFile.getAbsolutePath() + "." );
			ioe.printStackTrace();
		}

		if( parser != null ) {
			database = ((ImageCollectionReader)parser.getContentHandler()).getDatabase();
		}
		
		return database;
	}
	
	@Override
	public void setDocumentLocator(Locator locator) {
	}

	@Override
	public void startDocument() throws SAXException {
		this.database = new Vector<ImageInfo>();
	}

	@Override
	public void endDocument() throws SAXException {
		
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		
		if ( qName.equals( "imageinfo" ) ) {
			this.currentImageInfo = new ImageInfo();
			this.currentImageInfo.setFolder( this.fileLocation );
		} else if ( qName.equals( "fileName" ) ) {
			if ( this.currentImageInfo == null ) {
				throw new SAXException( "Error in XML file format: <" + qName + "> markup found outside of <imageinfo> markup.");
			}
			
		}
		return;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if ( qName.equals( "imageinfo" ) ) { //  end of imageinfo element
		
			this.database.add( this.currentImageInfo );
			this.currentImageInfo = null;
			
		} else if ( qName.equals( "filename") ) {

			if ( this.currentImageInfo != null ) {
				this.currentImageInfo.setFileName( this.currentData );
				this.currentData = null;
			} else {
				throw new SAXException( "Error in XML file format: closing </" + qName + "> markup without opening <" + qName + "> markup.");
			}
		}
		
		return;
			
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if ( this.currentData == null ) {
			this.currentData = new String( ch ).substring( start, start + length ).trim();
		} else {
			this.currentData += " " + new String( ch ).substring( start, start + length );
			this.currentData = this.currentData.trim();
		}
		return;
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
	}
	
}
