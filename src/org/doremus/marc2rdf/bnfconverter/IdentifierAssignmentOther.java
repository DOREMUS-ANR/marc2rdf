package org.doremus.marc2rdf.bnfconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.apache.http.client.utils.URIBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.doremus.marc2rdf.bnfparser.MarcXmlReader;
import org.doremus.marc2rdf.bnfparser.Record;

public class IdentifierAssignmentOther {
	
	/******** L'agence bibliographique qui a attribue l'identifiant *****************/
	public static URI getBiblioAgency(String xmlFile) throws FileNotFoundException, URISyntaxException {
    	URIBuilder builder = null ;
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        String agence = "";
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.controlFields.size(); i++) {
        		 if (s.controlFields.get(i).getEtiq().equals("001")) {
        			 for (int j=0; j<=4; j++){
        				 agence = agence + s.controlFields.get(i).getData().charAt(j);
        			 }
        		 }
        	 }
        }
        if (agence.equals("FRBNF")) {
        	builder = new URIBuilder().setPath("http://isni.org/isni/0000000121751303");
        }
        URI uri = builder.build();
        return uri;
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
    public static String getGenre(String xmlFile) throws IOException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        String codeGenre = "";
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('l')){
        				 if (s.dataFields.get(i).getSubfield('l').getData().contains("arr."))
        					 if (s.dataFields.get(i).isCode('a'))
        						 codeGenre = s.dataFields.get(i).getSubfield('a').getData();
        						 //buffer.append(s.dataFields.get(i).getSubfield('a').getData());
        		 }
        		 }
        	 }
        }
        
        /*******************************************************************/
		Boolean trouve = false ; // "trouve=true" si "codeGenre" a �t� trouv� dans le fichier
		 File fichier = new File("Data\\GenresBNF.xlsx");
        FileInputStream fis = new FileInputStream(fichier);

        XSSFWorkbook myWorkBook = new XSSFWorkbook (fis); // Trouver l'instance workbook du fichier XLSX
        XSSFSheet mySheet = myWorkBook.getSheetAt(0); // Retourne la 1ere feuille du workbook XLSX
        Iterator<Row> iter = mySheet.iterator(); //It�rateur de toutes les lignes de la feuille courante
       
        while ((iter.hasNext())&&(trouve==false)) { // Traverser chaque ligne du fichier XLSX
        	Boolean correspondance = false ; // correspondance entre 144 $a et le genre courant parcouru
       	    Row row = iter.next();
            Iterator<Cell> cellIterator = row.cellIterator(); 
            int numColonne = 0; 
            while ((cellIterator.hasNext())&&(correspondance==false)) { // Pour chaque ligne, it�rer chaque colonne
                Cell cell = cellIterator.next();
                if (codeGenre.equals(cell.getStringCellValue()) ) trouve = true ; //On a trouv� le code du genre dans le fichier
                if (trouve == true) {
               	 	
               	 		correspondance = true ;
               	 
                } 
                numColonne ++;
            }
        }		
		/*******************************************************************/
        buffer.append(codeGenre);
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
