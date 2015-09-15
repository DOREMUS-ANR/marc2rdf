package publicationEvent;

import java.io.IOException;

import rdf.DisplayMain;


public class PublicationEvent {

	public static void execute() throws IOException {
		 
		// F30 : Publication Event - 1ere publication de l'oeuvre
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("Edition");
		DisplayMain.tab.add(Edition.getEdition("Data\\1.Document.txt")); 
		
	}
}
