package expressionCreation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import marcXMLparser.MarcXmlReader;
import marcXMLparser.Record;

public class ExpressionCreation {
	
	/************* Date de creation de l'expression (Format texte) ***********************/
    public static String getDateMachine(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.controlFields.size(); i++) {
        		 
        		 /***************************** Cas 1 *********************************/
        		 if (s.controlFields.get(i).getEtiq().equals("008")) {
        			 if (((s.controlFields.get(i).getData().charAt(36))==((" ").charAt(0)))&&
        			 ((s.controlFields.get(i).getData().charAt(46))==((" ").charAt(0)))){
        			 	for (int j=28; j<=31; j++){
        			 		buffer.append(s.controlFields.get(i).getData().charAt(j));
        			 	}
        			 	String st = "";
        			 	boolean nothing = true;
        			 	for (int j=38; j<=41; j++){
        			 		char character = s.controlFields.get(i).getData().charAt(j);
        			 		st = st+ character;
        			 		if (!(character==(' '))) {nothing = false;}
        			 	}
        			 	if (nothing==false) {
        			 		buffer.append("/");
        			 		buffer.append(st);
        			 	}
        			 }
        			 /***************************** Cas 2 *********************************/
        			 else if (((s.controlFields.get(i).getData().charAt(36))==(("?").charAt(0)))||
                			 ((s.controlFields.get(i).getData().charAt(46))==(("?").charAt(0)))||
                			 ((s.controlFields.get(i).getData().charAt(30))==((".").charAt(0)))||
                			 ((s.controlFields.get(i).getData().charAt(31))==((".").charAt(0)))||
                			 ((s.controlFields.get(i).getData().charAt(40))==((".").charAt(0)))||
                			 ((s.controlFields.get(i).getData().charAt(41))==((".").charAt(0)))){
        				 /******************** Sous cas 2.1 ********************************/
        				 if (((s.controlFields.get(i).getData().charAt(30))==((".").charAt(0)))&&
        						 ((s.controlFields.get(i).getData().charAt(31))==((".").charAt(0)))){
        				 String dt ="";
        				 for (int j=28; j<=29; j++){
         			 		buffer.append(s.controlFields.get(i).getData().charAt(j));
         			 		dt = dt+s.controlFields.get(i).getData().charAt(j);
         			 	}
        				 buffer.append("00/"+dt+"99");
        				 }
        				 /******************** Sous cas 2.2 ********************************/
        				 if (!((s.controlFields.get(i).getData().charAt(30))==((".").charAt(0)))&&
        						 ((s.controlFields.get(i).getData().charAt(31))==((".").charAt(0)))){
        				 String dt ="";
        				 for (int j=28; j<=30; j++){
         			 		buffer.append(s.controlFields.get(i).getData().charAt(j));
         			 		dt = dt+s.controlFields.get(i).getData().charAt(j);
         			 	}
        				 buffer.append("0/"+dt+"9");
        				 }
        				 /******************** Sous cas 2.3 ********************************/
        				 if (((s.controlFields.get(i).getData().charAt(40))==((".").charAt(0)))&&
        						 ((s.controlFields.get(i).getData().charAt(41))==((".").charAt(0)))){
        				 String dt ="";
        				 for (int j=38; j<=39; j++){
         			 		buffer.append(s.controlFields.get(i).getData().charAt(j));
         			 		dt = dt+s.controlFields.get(i).getData().charAt(j);
         			 	}
        				 buffer.append("00/"+dt+"99");
        				 }
        				 /******************** Sous cas 2.4 ********************************/
        				 if (!((s.controlFields.get(i).getData().charAt(40))==((".").charAt(0)))&&
        						 ((s.controlFields.get(i).getData().charAt(41))==((".").charAt(0)))){
        				 String dt ="";
        				 for (int j=38; j<=40; j++){
         			 		buffer.append(s.controlFields.get(i).getData().charAt(j));
         			 		dt = dt+s.controlFields.get(i).getData().charAt(j);
         			 	}
        				 buffer.append("0/"+dt+"9");
        				 }
        			 }
        			 /******************************************************************************/
        		 }
        	 }
        }
        return buffer.toString();
    }
	/************* Date de création de l'expression (Format texte) ***********************/
    public static String getDateText(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("600")) {
        			 if (s.dataFields.get(i).isCode('a')){
        			 	 String date = s.dataFields.get(i).getSubfield('a').getData();
        			 	 if ((date.contains("comp."))||(date.contains("Date de composition"))||(date.contains("Dates de composition")))
        				 buffer.append(date);
        			 }
        		 }
        	 }
        }
        return buffer.toString();
    }
    /*********************************** La note ***********************************/
    public static String getNote(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("600")) {
        			 if (s.dataFields.get(i).isCode('a'))
        				 buffer.append(s.dataFields.get(i).getSubfield('a').getData());
        		 }
        	 }
        }
        return buffer.toString();
    	}
    /***************** Le compositeur qui a crée l'oeuvre ******************************/
    public static String getComposer(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("100")) {
        			/* 	 if (s.dataFields.get(i).isCode('w')){
        			 		 String wSubField = s.dataFields.get(i).getSubfield('w').getData();
        			 		 buffer.append(wSubField+" ");
        			 	 } */
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
}
