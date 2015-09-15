package expressionCreationAutre;

import java.io.IOException;

import rdf.DisplayMain;


public class ExpressionCreationAutre {

	public static void execute () throws IOException {
		 
		// F28 : Expression Creation - Creation d'une autre expression de l'oeuvre musicale
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("DateOfTheCreation");
		DisplayMain.tab.add(DateCreationH.getDateCreation("Data\\1.Document.txt")); 
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("DateOfTheCreation");
		DisplayMain.tab.add(DateCreationM.getDateCreationM("Data\\1.Document.txt")); 
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("IsCreatedBy");
		DisplayMain.tab.add(Compositeur.getCompositeur("Data\\1.Document.txt")); 
		
	}
}
