package f30_PublicationEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import f14_IndividualWork.F14_IndividualWork;
import bnfConverter.ConstructURI;
import bnfConverter.GenerateUUID;
import bnfMarcXML.MarcXmlReader;
import bnfMarcXML.Record;
import main.Converter;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import f22_SelfContainedExpression.F22_SelfContainedExpression;

public class F30_PublicationEvent {

	static Model modelF30 = ModelFactory.createDefaultModel();
	static URI uriF30=null;
	
	String mus = "http://data.doremus.org/ontology/";
    String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
    String frbroo = "http://erlangen-crm.org/efrbroo/";
    String xsd = "http://www.w3.org/2001/XMLSchema#";
    
	/********************************************************************************************/
    public URI getURIF30() throws URISyntaxException {
    	if (uriF30==null){
    	ConstructURI uri = new ConstructURI();
    	GenerateUUID uuid = new GenerateUUID();
    	uriF30 = uri.getUUID("Publication_Event","F30", uuid.get());
    	}
    	return uriF30;
    }
    
    public Model getModel() throws URISyntaxException, FileNotFoundException{
    	uriF30 = getURIF30();
    	Resource F30 = modelF30.createResource(uriF30.toString());
    	
    	/**************************** Publication Event: édition princeps ******************/
    	F14_IndividualWork F14= new F14_IndividualWork();
    	F30.addProperty(modelF30.createProperty(mus+ "U4i_is_princeps_publication_of"), modelF30.createResource(F14.getURIF14().toString()));
    	
    	/**************************** Expression: 1ère publication *************************/
    	F30.addProperty(modelF30.createProperty(cidoc+ "P3_has_note"), getNote(Converter.getFile()));
    	
  
        
		return modelF30;
    }
    /********************************************************************************************/
    
    /*********************************** La note ***********************************/
    public static String getNote(String xmlFile) throws FileNotFoundException {
        StringBuilder buffer = new StringBuilder();
        InputStream file = new FileInputStream(xmlFile); //Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(file);
        while (reader.hasNext()) { // Parcourir le fichier MARCXML
        	 Record s = reader.next();
        	 for (int i=0; i<s.dataFields.size(); i++) {
        		 if (s.dataFields.get(i).getEtiq().equals("600")) {
        			 if (s.dataFields.get(i).isCode('a')){
        			 String edition = s.dataFields.get(i).getSubfield('a').getData();
    			 	 if ((edition.contains("éd."))||(edition.contains("édition"))){
    				 buffer.append(edition);
    			 	 }
        		 }
        		 }
        	 }
        }
        return buffer.toString();
    	}
}
