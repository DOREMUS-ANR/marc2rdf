package f22_SelfContainedExpression;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import main.Converter;

import org.apache.http.client.utils.URIBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import f14_IndividualWork.F14_IndividualWork;
import f28_ExpressionCreation.F28_ExpressionCreation;
import bnfConverter.ConstructURI;
import bnfConverter.GenerateUUID;
import bnfMarcXML.MarcXmlReader;
import bnfMarcXML.Record;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import f15_ComplexWork.F15_ComplexWork;
import f40_IdentifierAssignment.F40_IdentifierAssignment;

public class F22_SelfContainedExpression {
	
          /*** Correspond à la description développée de l'expression représentative ***/
	
	static Model modelF22 = ModelFactory.createDefaultModel();
	static URI uriF22=null;
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    
	/********************************************************************************************/
    public URI getURIF22() throws URISyntaxException {
    	if (uriF22==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF22 = uri.getUUID("Self_Contained_Expression","F22", uuid.get());
    	}
    	return uriF22;
    }
    
    public Model getModel() throws URISyntaxException, IOException{
    	uriF22 = getURIF22();
    	Resource F22 = modelF22.createResource(uriF22.toString());
    	
    	/**************************** Expression: was created by ********************************/
    	F28_ExpressionCreation F28= new F28_ExpressionCreation();
    	F22.addProperty(modelF22.createProperty(frbroo+ "R17i_was_created_by"), modelF22.createResource(F28.getURIF28().toString()));
    	
    	/**************************** Expression: is representative expression of (Work) ********/
    	F15_ComplexWork F15= new F15_ComplexWork();
    	F22.addProperty(modelF22.createProperty(frbroo+ "R40i_is_representative_expression_of"), modelF22.createResource(F15.getURIF15().toString()));
    	
    	/**************************** Expression: Context for the expression ********************/
    	F22.addProperty(modelF22.createProperty(cidoc+ "P67_refers_to"), getDedicace(Converter.getFile()));
    	
    	/**************************** Expression: Title *****************************************/
    //	String lang = (Converter.getFile());
    //	if (lang.equals("fre"))
    //	F22.addProperty(modelF22.createProperty(cidoc+ "P102_has_title"), getTitle(Converter.getFile()), "http://id.loc.gov/vocabulary/iso639-2/fre");
    	F22.addProperty(modelF22.createProperty(cidoc+ "P102_has_title"), getTitle(Converter.getFile()));
    	/**************************** Expression: Catalogue *************************************/
    	F22.addProperty(modelF22.createProperty(mus+ "U16_has_catalogue_statement"), modelF22.createResource()
    			.addProperty(modelF22.createProperty(cidoc+ "P3_has_note"), getCatalog(Converter.getFile()))
				.addProperty(modelF22.createProperty(cidoc+ "P106_is_composed_of"), getCatalogName(Converter.getFile()))
				.addProperty(modelF22.createProperty(cidoc+ "P106_is_composed_of"), getCatalogNumber(Converter.getFile()))
				);
    	
    	/**************************** Expression: Opus ******************************************/
    	F22.addProperty(modelF22.createProperty(mus+ "U17_has_opus_statement"), modelF22.createResource()
    			.addProperty(modelF22.createProperty(cidoc+ "P3_has_note"), getOpus(Converter.getFile()))
				.addProperty(modelF22.createProperty(cidoc+ "P106_is_composed_of"), getOpusNumber(Converter.getFile()))
				.addProperty(modelF22.createProperty(cidoc+ "P106_is_composed_of"), getOpusSubNumber(Converter.getFile()))
				);
    	
    	/**************************** Expression: ***********************************************/
    	F22.addProperty(modelF22.createProperty(cidoc+ "P3_has_note"), getNote(Converter.getFile()));
    	
    	/**************************** Expression: key *******************************************/
    	F22.addProperty(modelF22.createProperty(mus+ "U11_has_key"), getKey(Converter.getFile()));
    	
    	/**************************** Expression: Genre *****************************************/
    	F22.addProperty(modelF22.createProperty(mus+ "U12_has_genre"), getGenre(Converter.getFile()));
    	
    	/**************************** Expression: Order Number **********************************/
    	F22.addProperty(modelF22.createProperty(mus+ "U10_has_order_number"), getOrderNumber(Converter.getFile()));
    	
    	/**************************** Expression: Casting ***************************************/
    	F22.addProperty(modelF22.createProperty(mus+ "U13_has_casting"), getCasting(Converter.getFile()));
    	
    	/**************************** Expression: Assignation d'identifiant *********************/
    	F40_IdentifierAssignment F40= new F40_IdentifierAssignment();
    	F22.addProperty(modelF22.createProperty(frbroo+ "R45i_was_assigned_by"), modelF22.createResource(F40.getURIF40().toString()));
    	
		return modelF22;
    }
    /********************************************************************************************/

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
    
    /****************************La langue du titre ************************************/
    public static String getLang(String xmlFile) throws IOException {
    	InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        String langue="";
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
       	 Record s = reader.next();
       	 for (int i=0; i<s.dataFields.size(); i++) {
       		 if (s.dataFields.get(i).getEtiq().equals("144")) {
       			if (s.dataFields.get(i).isCode('w'))
				 {
					 for (int j=0; j<=8; j++){
						 if (j>=6)
       				 langue = langue + s.dataFields.get(i).getSubfield('w').getData().charAt(j);
       			 }
				 }
       		 }
    }
        }
		return langue;
    }
    /*********************************** Le titre **************************************/
    public static String getTitle(String xmlFile) throws IOException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        String title_a = "";
        String title_h = "";
        String title_i = "";
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        					 if (s.dataFields.get(i).isCode('a'))
        						 title_a = s.dataFields.get(i).getSubfield('a').getData();
        						 //buffer.append(s.dataFields.get(i).getSubfield('a').getData());
        					 if (s.dataFields.get(i).isCode('h'))
        						 title_h = s.dataFields.get(i).getSubfield('h').getData();
        					 if (s.dataFields.get(i).isCode('i'))
        						 title_i = s.dataFields.get(i).getSubfield('i').getData();
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
		/*******************************************************************/
        if (trouve==false){
        	buffer.append(title_a);
        	buffer.append(title_h);
        	buffer.append(title_i);
        }
        else buffer.append("");
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
    /*********************************** Le num du catalogue ***********************************/
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
    /***************************************** Le genre *************************************/
    public static String getGenre(String xmlFile) throws IOException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        String codeGenre = "";
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.controlFields.size(); i++) {
        		 if (s.controlFields.get(i).getEtiq().equals("008")) {
        			 	for (int j=18; j<=20; j++){
        			 		codeGenre = codeGenre + s.controlFields.get(i).getData().charAt(j);
        			 		//buffer.append(s.controlFields.get(i).getData().charAt(j));
        			 	}      			 
        		 }
        	 }
        }
        if (((codeGenre.length()==3) && (codeGenre.contains(" ")))||(codeGenre.length()==2)) {
        	codeGenre = codeGenre.replace(" ", "#");
        } 
		/*******************************************************************/
		Boolean trouve = false ; // "trouve=true" si "codeGenre" a �t� trouv� dans le fichier
		 File fichier = new File("Data\\genresIAML.xlsx");
        FileInputStream fis = new FileInputStream(fichier);

        XSSFWorkbook myWorkBook = new XSSFWorkbook (fis); // Trouver l'instance workbook du fichier XLSX
        XSSFSheet mySheet = myWorkBook.getSheetAt(0); // Retourne la 1ere feuille du workbook XLSX
        Iterator<Row> iter = mySheet.iterator(); //It�rateur de toutes les lignes de la feuille courante
       
        while ((iter.hasNext())&&(trouve==false)) { // Traverser chaque ligne du fichier XLSX
       	    Row row = iter.next();
            Iterator<Cell> cellIterator = row.cellIterator(); 
            int numColonne = 0; 
            while (cellIterator.hasNext()) { // Pour chaque ligne, it�rer chaque colonne
                Cell cell = cellIterator.next();
                if (codeGenre.equals(cell.getStringCellValue()) ) trouve = true ; //On a trouv� le code du genre dans le fichier
                
                if ((trouve == true)&&(numColonne>0)) {
                	buffer.append(cell.getStringCellValue());
               	 }
                numColonne ++;
                } 
            }
       		
		/*******************************************************************/
		return buffer.toString();
    }
    
    /*********************************** Casting ***********************************/
    public static String getCasting(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("144")) {
        			 if (s.dataFields.get(i).isCode('b')){
        			 String casting = s.dataFields.get(i).getSubfield('b').getData();
    				 buffer.append(casting);
        		 }
        		 }
        	 }
        }
        return buffer.toString();
    	}
}
