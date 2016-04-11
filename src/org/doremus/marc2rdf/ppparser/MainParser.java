package org.doremus.marc2rdf.ppparser;

import java.io.FileInputStream;
import java.io.InputStream;

import org.doremus.marc2rdf.ppparser.MarcXmlReader;
import org.doremus.marc2rdf.ppparser.Record;

public class MainParser {
	
	public static void main(String args[]) throws Exception {
		Record record = null; // Record = notice
        InputStream file = new FileInputStream("data\\oeuvre-PP.xml"); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        int i=0;
        while (reader.hasNext()) { // Parcourir toutes les notices se trouvant dans le fichier
             record = reader.next();
             System.out.println(record.toString());
        }
}
}