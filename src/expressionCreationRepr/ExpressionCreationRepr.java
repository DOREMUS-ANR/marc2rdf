package expressionCreationRepr;

import java.io.IOException;

import rdf.DisplayMain;


public class ExpressionCreationRepr {

	public static void execute() throws IOException {
		 
		// F28 : Expression Creation - Creation de l'expression représentative de l'oeuvre musicale
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("DateOfTheWork");
		DisplayMain.tab.add(DateCreation.getDateCreation("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("IsCreatedBy");
		DisplayMain.tab.add(Compositeur.getCompositeur("Data\\1.Document.txt"));
		
	}
}
