package org.doremus.marc2rdf.bnfMarcXML;

import java.io.FileInputStream;
import java.io.InputStream;

import org.doremus.marc2rdf.bnfMarcXML.MarcXmlReader;
import org.doremus.marc2rdf.bnfMarcXML.Record;

public class MainParser {
	
	public static void main(String args[]) throws Exception {
		Record record = null; // Record = notice
        InputStream file = new FileInputStream("Data\\BNFFile.xml"); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
             record = reader.next();
             
        }
        System.out.println(record.toString());
		
}
}