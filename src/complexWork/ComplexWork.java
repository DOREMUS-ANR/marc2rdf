package complexWork;

import java.io.IOException;

import rdf.DisplayMain;


public class ComplexWork {

	public static void execute () throws IOException {
		 
				// F15 : Complex Work - Description minimale non ambigue
		
				//DisplayMain.tab.add(DisplayMain.subject);
				//DisplayMain.tab.add("TitleOfTheWork");
				//DisplayMain.tab.add(Title.getTitle("Data\\1.Document.txt")); 
				
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
				DisplayMain.tab.add("Catalog");
				DisplayMain.tab.add(Catalog.getCatalog("Data\\1.Document.txt"));
				
				DisplayMain.tab.add(DisplayMain.subject);
				DisplayMain.tab.add("CatalogName");
				DisplayMain.tab.add(CatalogName.getCatalogName("Data\\1.Document.txt"));
				
				DisplayMain.tab.add(DisplayMain.subject);
				DisplayMain.tab.add("CatalogNumber");
				DisplayMain.tab.add(CatalogNumber.getCatalogNumber("Data\\1.Document.txt"));
				
				DisplayMain.tab.add(DisplayMain.subject);
				DisplayMain.tab.add("Compositeur");
				DisplayMain.tab.add(Compositeur.getCompositeur("Data\\1.Document.txt"));
	}
}
