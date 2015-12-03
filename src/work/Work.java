package work;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import marcXMLparser.MarcXmlReader;
import marcXMLparser.Record;

public class Work {
	
	/******** L'agence bibliographique qui a attribue l'identifiant *****************/
    public static String getBiblioAgency(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.controlFields.size(); i++) {
        		 if (s.controlFields.get(i).getEtiq().equals("001")) {
        			 for (int j=0; j<=4; j++){
        				 buffer.append(s.controlFields.get(i).getData().charAt(j));
        			 }
        		 }
        	 }
        }
        return buffer.toString();
    }
    
    /******** L'identifiant assigne par l'agence bibliographique ******************************/
    public static String getIdentifier (String xmlFile) throws FileNotFoundException {
    	StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.controlFields.size(); i++) {
        		 if (s.controlFields.get(i).getEtiq().equals("003")) {
        				 buffer.append(s.controlFields.get(i).getData());
        		 }
        	 }
        }
        return buffer.toString();
    }
    /*******************************************************************************************/
}
