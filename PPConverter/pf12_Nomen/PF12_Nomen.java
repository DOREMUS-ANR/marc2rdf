package pf12_Nomen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import main.Converter;
import pf50_ControlledAccessPoint.PF50_ControlledAccessPoint;
import ppConverter.ConstructURI;
import ppConverter.GenerateUUID;
import ppMarcXML.MarcXmlReader;
import ppMarcXML.Record;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class PF12_Nomen {

	static Model modelF12 = ModelFactory.createDefaultModel();
	static URI uriF12=null;
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    
	/********************************************************************************************/
    public URI getURIF12() throws URISyntaxException {
    	if (uriF12==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF12 = uri.getUUID("Nomen","F12", uuid.get());
    	}
    	return uriF12;
    }
    
    public Model getModel() throws URISyntaxException, IOException{
    	uriF12 = getURIF12();
    	Resource F12 = modelF12.createResource(uriF12.toString());
    	
    	/**************************** Expression: Title *****************************************/
    	if (getTypeNotice(Converter.getFile()).equals("UNI:100")){ // Si c'est une notice d'oeuvre
    		F12.addProperty(modelF12.createProperty(cidoc+ "P106_is_composed_of"), getTitle200(Converter.getFile()));
    	}
    	
    	if (getTypeNotice(Converter.getFile()).equals("AIC:14")) { // Si c'est une notice TUM
    		F12.addProperty(modelF12.createProperty(cidoc+ "P106_is_composed_of"), getTitle(Converter.getFile(), "144"));
    		F12.addProperty(modelF12.createProperty(cidoc+ "P106_is_composed_of"), getTitle(Converter.getFile(), "444"));
    	}
    
    	/**************************** Order Number **********************************/
    	F12.addProperty(modelF12.createProperty(cidoc+ "P106_is_composed_of"), getOrderNumber(Converter.getFile(), "144"));
    	F12.addProperty(modelF12.createProperty(cidoc+ "P106_is_composed_of"), getOrderNumber(Converter.getFile(), "444"));
    	
    	return modelF12;
    }
    
    /************************** Type de notice  *********************************************/
	 public static String getTypeNotice (String xmlFile) throws FileNotFoundException {
	    	StringBuilder buffer = new StringBuilder();
	        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
	        MarcXmlReader reader = new MarcXmlReader(file);
	        String typeNotice="";
	        while (reader.hasNext()) { // Parcourir le fichier MARCXML
	        	 Record s = reader.next();
	        	 typeNotice = s.getType();
	        }
	        buffer.append(typeNotice);
	        return buffer.toString();
	    }
	 
	 /*********************************** Le titre **************************************/
	    public static String getTitle200(String xmlFile) throws IOException {
	    	
	    	StringBuilder buffer = new StringBuilder(); 
	    	InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
	        MarcXmlReader reader = new MarcXmlReader(file);
	        String title = "";
	        while (reader.hasNext()) { // Parcourir le fichier MARCXML
	          	 Record s = reader.next();
	           	 for (int i=0; i<s.dataFields.size(); i++) {
	           		 if (s.dataFields.get(i).getEtiq().equals("200")) {
	        			 if (s.dataFields.get(i).isCode('a'))
	          				 title = s.dataFields.get(i).getSubfield('a').getData();
	           		 }
	           	 }
	        }
	        buffer.append(title);
	    	return buffer.toString();
	    }
 /*********************************** Le titre **************************************/
 public static String getTitle(String xmlFile, String etiq) throws IOException {
 	
 	StringBuilder buffer = new StringBuilder(); 
 	InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
 	MarcXmlReader reader = new MarcXmlReader(file);
 	String title_a= ""; 
 	String title_h= "";
 	String title_i= "";
 	String title_g= "";
 	String title_c= "";
 		
 	while (reader.hasNext()) { // Parcourir le fichier MARCXML
 		Record s = reader.next();
 		for (int i=0; i<s.dataFields.size(); i++) {
 			if (s.dataFields.get(i).getEtiq().equals(etiq)) {
 				if (s.dataFields.get(i).isCode('a'))
 					title_a = s.dataFields.get(i).getSubfield('a').getData();
 				if (s.dataFields.get(i).isCode('h'))
 					title_h = s.dataFields.get(i).getSubfield('h').getData();
 				if (s.dataFields.get(i).isCode('i'))
 					title_i = s.dataFields.get(i).getSubfield('i').getData();
 				if (s.dataFields.get(i).isCode('g'))
 					title_g = s.dataFields.get(i).getSubfield('g').getData();
 				if (s.dataFields.get(i).isCode('c'))
 					title_c = s.dataFields.get(i).getSubfield('c').getData();
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
 			if (title_a.equals(cell.getStringCellValue()) ) trouve = true ; //On a trouv� le code du genre dans le fichier
 			if (trouve == true) {
        	 		correspondance = true ;
 			} 
 			numColonne ++;
 		}
 	}
 	if ((trouve==false)&&((title_g).equals(""))&&((title_c).equals(""))){ //si 144 ne contient ni la sous-zone $g ni la sous-zone $c 
 		buffer.append(title_a);
 		buffer.append(",");
 		buffer.append(title_h);
 		buffer.append(",");
 		buffer.append(title_i);
 		buffer.append(".");
 	}
 	else buffer.append("");
 		/*******************************************************************/
     return buffer.toString();
 	}
 
 public static String getOrderNumber(String xmlFile, String etiq) throws FileNotFoundException {
     StringBuilder buffer = new StringBuilder();
     InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
     MarcXmlReader reader = new MarcXmlReader(file);
     String orderNumber="";
     while (reader.hasNext()) { // Parcourir le fichier MARCXML
     	 Record s = reader.next();
     	 for (int i=0; i<s.dataFields.size(); i++) {
     		 if (s.dataFields.get(i).getEtiq().equals(etiq)) {
     			 if (s.dataFields.get(i).isCode('n')){
     				 orderNumber= (s.dataFields.get(i).getSubfield('n').getData());
     			 }
     		 }
     	 }
     }
     buffer.append(orderNumber);
     return buffer.toString();
 	}
    }