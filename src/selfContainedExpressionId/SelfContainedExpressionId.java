package selfContainedExpressionId;

import java.io.IOException;

import rdf.DisplayMain;


public class SelfContainedExpressionId {

	public static void execute ()  throws IOException {
		 
		// F22 : Self Contained Expression - Assignation d'identifiant pour l'expression représentative
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("Compositor");
		DisplayMain.tab.add(Compositeur.getCompositeur("Data\\1.Document.txt")); 
		
	}
}
