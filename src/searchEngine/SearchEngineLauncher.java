package searchEngine;

import gui.SearchEngineUI;

/**
 * Launcher class to run the search engine GUI.
 * @author Pierre Tirilly - pierre.tirilly@imt-lille-douai.fr
 *
 */	
public class SearchEngineLauncher {
	
	public static void main( String[] args ) {		
		final SearchEngine se = new SearchEngineV1();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SearchEngineUI( se );
            }
        });	
	}
}
