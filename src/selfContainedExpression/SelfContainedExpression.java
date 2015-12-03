package selfContainedExpression;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import marcXMLparser.MarcXmlReader;
import marcXMLparser.Record;

public class SelfContainedExpression {

	/*********************************** La dedicace ***********************************/
    public static String getDedicace(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("600")) {
        			 if (s.dataFields.get(i).isCode('a')){
        			 String dedicace = s.dataFields.get(i).getSubfield('a').getData();
    			 	 if (dedicace.startsWith("Dédicace")){
    				 buffer.append(dedicace);
    			 	 }
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
    /*********************************** Le nom du catalogue ***********************************/
    public static String getCatalogName(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('k')){
        			 String catalogName= (s.dataFields.get(i).getSubfield('k').getData());
        			 String[] caracter = catalogName.split("");
        				int x = 0 ;
        				catalogName="";
        				while (!(caracter[x].equals(" "))) {
        					catalogName = catalogName + caracter[x];
        					x++;
        				}
        			  buffer.append(catalogName);
        		 }
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Le nom du catalogue ***********************************/
    public static String getCatalogNumber(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('k')){
        			 String catalogNumber= (s.dataFields.get(i).getSubfield('k').getData());
        			 String[] caracter = catalogNumber.split("");
        				boolean t = false ;
        				catalogNumber="";
        				for (int x=0; x<caracter.length; x++){
        					if ((caracter[x].equals(" "))) t = true ;
        					else if (t==true) catalogNumber = catalogNumber + caracter[x];
        				}
        			  buffer.append(catalogNumber);
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
        			 if (s.dataFields.get(i).isCode('p'))
        			 		buffer.append(s.dataFields.get(i).getSubfield('p').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Le numero d'opus ***********************************/
    public static String getOpusNumber(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('p')){
        			 String opusNumber= (s.dataFields.get(i).getSubfield('p').getData());
        			 String[] caracter = opusNumber.split("");
        			 boolean t = false ;
        			 opusNumber="";
     				 for (int x=0; x<caracter.length; x++){
     					if ((caracter[x].equals(","))) t = true ;
     					else if ((t==false)&&(!(caracter[x].equals("O"))&&!(caracter[x].equals("p"))&&!(caracter[x].equals("."))&&!(caracter[x].equals(" ")))) 
     						opusNumber = opusNumber + caracter[x];
     				}
        			  buffer.append(opusNumber);
        		 }
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Le sous-numero d'opus ***********************************/
    public static String getOpusSubNumber(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('p')){
        			 String opusSubNumber= (s.dataFields.get(i).getSubfield('p').getData());
        			 String[] caracter = opusSubNumber.split("");
        			 boolean t = false ;
        			 opusSubNumber = "";
        			 for (int x=0; x<caracter.length; x++){
        					if ((caracter[x].equals(","))) t = true ;
        					else if ((t==true)&&!(caracter[x].equals("n"))&&!(caracter[x].equals("o"))&&!(caracter[x].equals(" ")))
        						opusSubNumber = opusSubNumber + caracter[x];
        			 }
        			  buffer.append(opusSubNumber);
        		 }
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /*********************************** Note Libre ***********************************/
    public static String getNote(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("600")) {
        			 if (s.dataFields.get(i).isCode('a')){
        			 String note = s.dataFields.get(i).getSubfield('a').getData();
    			 	 if (!(note.contains("Date de composition"))&&!(note.contains("Dates de composition"))&&!(note.contains("comp."))
    			 			&&!(note.contains("1re éd."))&&!(note.contains("éd."))&&!(note.contains("édition"))
    			 			&&!(note.contains("1re exécution"))&&!(note.contains("1re représentation"))&&!(note.startsWith("Dédicace"))){
    				 buffer.append(note);
    			 	 }
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
        			 if (s.dataFields.get(i).isCode('t'))
        				 buffer.append(s.dataFields.get(i).getSubfield('t').getData());
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
        			 if (s.dataFields.get(i).isCode('n')){
        			 String orderNumber= (s.dataFields.get(i).getSubfield('n').getData());
        			 String[] caracter = orderNumber.split("");
        			 orderNumber="";
     				 for (int x=0; x<caracter.length; x++){
     					  if (!(caracter[x].equals("N"))&&!(caracter[x].equals("o"))&&!(caracter[x].equals(" ")))
     					    	orderNumber = orderNumber + caracter[x];
     				}
     				buffer.append(orderNumber);
        		 }
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /***************************************** Le genre ***************************************/
    public static String getGenre(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.controlFields.size(); i++) {
        		 if (s.controlFields.get(i).getEtiq().equals("008")) {
        			 	for (int j=18; j<=20; j++){
        			 		buffer.append(s.controlFields.get(i).getData().charAt(j));
        			 	}      			 
        		 }
        	 }
        }
		return buffer.toString();
    }
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
}
