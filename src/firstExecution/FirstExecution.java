package firstExecution;

import java.io.IOException;

import rdf.DisplayMain;


public class FirstExecution {

	public static void execute() throws IOException {
		 
		// Premi�re ex�cution de l'oeuvre
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("Execution");
		DisplayMain.tab.add(DescripLibre.getDescripLibre("Data\\1.Document.txt"));
		
	}
}
