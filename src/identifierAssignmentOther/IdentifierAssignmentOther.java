package identifierAssignmentOther;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import marcXMLparser.MarcXmlReader;
import marcXMLparser.Record;

public class IdentifierAssignmentOther {
	
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
    /*********************************** Le catalogue ***********************************/
    public static String getCatalog(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('l')){
        				 if (s.dataFields.get(i).getSubfield('l').getData().contains("arr."))
        					 if (s.dataFields.get(i).isCode('k'))
        						 buffer.append(s.dataFields.get(i).getSubfield('k').getData());
        		 }
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
        			 if (s.dataFields.get(i).isCode('l')){
        			 if (s.dataFields.get(i).getSubfield('l').getData().contains("arr."))
        				 if (s.dataFields.get(i).isCode('p'))
        					 buffer.append(s.dataFields.get(i).getSubfield('p').getData());
        		 }
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
        			 if (s.dataFields.get(i).isCode('l')){
        				 if (s.dataFields.get(i).getSubfield('l').getData().contains("arr."))
        					 if (s.dataFields.get(i).isCode('e'))
        						 buffer.append(s.dataFields.get(i).getSubfield('e').getData());
        		 }
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
        			 if (s.dataFields.get(i).isCode('l')){
        				 if (s.dataFields.get(i).getSubfield('l').getData().contains("arr."))
        					 if (s.dataFields.get(i).isCode('b'))
        						 buffer.append(s.dataFields.get(i).getSubfield('b').getData());
        		 }
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Le numero d'ordre ***********************************/
    public static String getOrderNumber(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('l')){
        				 if (s.dataFields.get(i).getSubfield('l').getData().contains("arr."))
        					 if (s.dataFields.get(i).isCode('n'))
        						 buffer.append(s.dataFields.get(i).getSubfield('n').getData());
        		 }
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Key (Tonalite) ***********************************/
    public static String getKey(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('l')){
        				 if (s.dataFields.get(i).getSubfield('l').getData().contains("arr."))
        					 if (s.dataFields.get(i).isCode('t'))
        						 buffer.append(s.dataFields.get(i).getSubfield('t').getData());
        		 }
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
    /*********************************** La date (annee) **************************************/
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
    /*********************************** Type de derivation ***********************************/
    public static String getDerivationType(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('l')) 
        			 	buffer.append(s.dataFields.get(i).getSubfield('l').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
}
