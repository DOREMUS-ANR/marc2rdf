package org.doremus.marc2rdf.ppMarcXML;

import java.io.FileInputStream;
import java.io.InputStream;

import org.doremus.marc2rdf.ppMarcXML.MarcXmlReader;
import org.doremus.marc2rdf.ppMarcXML.Record;

public class MainParser {
	
	public static void main(String args[]) throws Exception {
		Record record = null; // Record = notice
        InputStream file = new FileInputStream("Data\\oeuvre-PP.xml"); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        int i=0;
        while (reader.hasNext()) { // Parcourir toutes les notices se trouvant dans le fichier
             record = reader.next();
             System.out.println(record.toString());
        }
}
}