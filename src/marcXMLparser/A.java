package marcXMLparser;

import java.io.FileInputStream;
import java.io.InputStream;

import marcXMLparser.MarcXmlReader;
import marcXMLparser.Record;

public class A {
	
	public static void main(String args[]) throws Exception {
		Record record = null; // Record = notice
        InputStream file = new FileInputStream("Data\\XMLFile.xml"); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
             record = reader.next();
             
        }
        System.out.println(record.toString());
		
}
}