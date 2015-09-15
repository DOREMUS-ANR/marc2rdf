package selfContainedExpressionDevAutre;

import java.io.IOException;

import rdf.DisplayMain;


public class SelfContainedExpressiondevAutre {

	public static void execute() throws IOException {
		 
		// F22 : Self Contained Expression - Description développée d'une autre expression de l'oeuvre musicale
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("ForeseenMediumOfPerformance");
		DisplayMain.tab.add(Distribution.getDistribution("Data\\1.Document.txt"));
		
	}
}
