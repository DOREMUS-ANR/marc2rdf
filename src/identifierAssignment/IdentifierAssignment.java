package identifierAssignment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import marcXMLparser.MarcXmlReader;
import marcXMLparser.Record;

public class IdentifierAssignment {
	
	/******** L'agence bibliographique qui a attribué l'identifiant *****************/
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
    /*********************** Le créateur de l'identifiant *************************************/
    public static String getCreator(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("100")) {
        			 	 if (s.dataFields.get(i).isCode('a')){
        			 		 String aSubField = s.dataFields.get(i).getSubfield('a').getData();
        			 		 buffer.append(aSubField+" ");
        			 	 }
        			 	if (s.dataFields.get(i).isCode('m')){
        			 		 String mSubField = s.dataFields.get(i).getSubfield('m').getData();
        			 		 buffer.append(mSubField+" ");
        			 	}
        			 	if (s.dataFields.get(i).isCode('d')){
        			 		 String dSubField = s.dataFields.get(i).getSubfield('d').getData();
        			 		 buffer.append(dSubField+" ");
        			 	}
        			 	if (s.dataFields.get(i).isCode('e')){
        			 		 String eSubField = s.dataFields.get(i).getSubfield('e').getData();
        			 		 buffer.append(eSubField+" ");
        			 	}
        			 	if (s.dataFields.get(i).isCode('h')){
        			 		 String hSubField = s.dataFields.get(i).getSubfield('h').getData();
        			 		 buffer.append(hSubField+" ");
        			 	}
        			 	if (s.dataFields.get(i).isCode('u')){
        			 		 String uSubField = s.dataFields.get(i).getSubfield('u').getData();
        			 		 buffer.append(uSubField+" ");
        			 	}
        		 }
        	 }
        }
        return buffer.toString();
    }
    /*********************************** Le catalogue ***********************************/
    public static String getCatalog(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('k'))
        				 buffer.append(s.dataFields.get(i).getSubfield('k').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** L'opus ***********************************/
    public static String getOpus(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('p'))
        				 buffer.append(s.dataFields.get(i).getSubfield('p').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** La distribution ***********************************/
    public static String getDistribution(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('b'))
        				 buffer.append(s.dataFields.get(i).getSubfield('b').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Le numéro d'ordre ***********************************/
    public static String getOrderNumber(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('n'))
        				 buffer.append(s.dataFields.get(i).getSubfield('n').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Key (Tonalité) ***********************************/
    public static String getKey(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('t'))
        				 buffer.append(s.dataFields.get(i).getSubfield('t').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** La langue ***********************************/
    public static String getLangue(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('f'))
        				 buffer.append(s.dataFields.get(i).getSubfield('f').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Le genre **************************************/
    public static String getGenre(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('e'))
        				 buffer.append(s.dataFields.get(i).getSubfield('e').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** La date (année) **************************************/
    public static String getDateYear(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('j'))
        				 buffer.append(s.dataFields.get(i).getSubfield('j').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** La version **************************************/
    public static String getVersion(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('q'))
        				 buffer.append(s.dataFields.get(i).getSubfield('q').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** La documentation **************************************/
    public static String getDocumentation(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("610")) {
        			 if (s.dataFields.get(i).isCode('a'))
        				 buffer.append(s.dataFields.get(i).getSubfield('a').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
}
