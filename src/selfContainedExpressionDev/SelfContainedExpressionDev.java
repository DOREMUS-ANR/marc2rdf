package selfContainedExpressionDev;

import java.io.IOException;

import rdf.DisplayMain;


public class SelfContainedExpressionDev {

	public static void execute () throws IOException {
		 
		// F22 : Self Contained Expression - Description développée de l'expression représentative
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("Title");
		DisplayMain.tab.add(Title.getTitle("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("Composer");
		DisplayMain.tab.add(Compositeur.getCompositeur("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("Catalog");
		DisplayMain.tab.add(Catalog.getCatalog("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("CatalogName");
		DisplayMain.tab.add(CatalogName.getCatalogName("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("CatalogNumber");
		DisplayMain.tab.add(CatalogNumber.getCatalogNumber("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("Opus");
		DisplayMain.tab.add(Opus.getOpus("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("OpusNumber");
		DisplayMain.tab.add(OpusNumber.getOpusNumber("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("OpusSubNumber");
		DisplayMain.tab.add(OpusSubNumber.getOpusSubNumber("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("NoteLibre");
		DisplayMain.tab.add(NoteLibre.getNoteLibre("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("Key");
		DisplayMain.tab.add(Key.getKey("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("OrderNumber");
		DisplayMain.tab.add(OrderNumber.getOrderNumber("Data\\1.Document.txt"));
		
		DisplayMain.tab.add(DisplayMain.subject);
		DisplayMain.tab.add("ForeseenMediumOfPerformance");
		DisplayMain.tab.add(Distribution.getDistribution("Data\\1.Document.txt"));
		
		
	}
}
