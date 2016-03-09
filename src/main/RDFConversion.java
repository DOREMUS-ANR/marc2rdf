package main;

import identifierAssignment.IdentifierAssignment;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.vocabulary.RDF;

import selfContainedExpression.SelfContainedExpression;
import convert2rdf.ConstructURI;
import convert2rdf.GenerateUUID;
import expressionCreation.ExpressionCreation;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;
import work.Work;

import javax.swing.*;

/***************************************************/

public class RDFConversion {

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		/****************************File Chooser*****************************/
        FileChooser newFile = new FileChooser();
		JFrame frame = new JFrame("MARC to RDF");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        //Add content to the window.
        frame.add(newFile);
        //Display the window.
        frame.pack();
        frame.setVisible(true);

        synchronized (newFile) {
            while (!newFile.getChoosed())
            	newFile.wait();
        }
        
		String filePath = "Data\\XMLFile.xml"; //Default path
        if(newFile.getDir() != null && (newFile.getDir().contains(".xml")))
        {
        	filePath = newFile.getDir().replace("/", "\\");
        	System.out.println("File Path " + filePath);
        }
		/*********************************************************************/
        
		/************* Creer un modele vide **************************/
		//Model model = VirtModel.openDatabaseModel("DOREMUS", "jdbc:virtuoso://localhost:1111", "dba", "dba");
		Model model = ModelFactory.createDefaultModel();
        String mus = "http://data.doremus.org/ontology/";
        String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
        String frbroo = "http://erlangen-crm.org/efrbroo/";
        String xsd = "http://www.w3.org/2001/XMLSchema#";
		/****************************************************************************************/
		Work work = new Work();
		ConstructURI uriWork = new ConstructURI();
		GenerateUUID uuidWork = new GenerateUUID();
		
		model.createResource(uriWork.getUUID("work","F15", uuidWork.get()).toString())
				.addProperty(model.createProperty(frbroo+ "R45i_was_assigned_by"),
						model.createResource()
							.addProperty(model.createProperty(RDF.type.toString()), model.createResource(getURI(frbroo+"F40_Identifier_Assignment").toString()))
							.addProperty(model.createProperty(cidoc+ "P14_carried_out_by"), model.createResource(work.getBiblioAgency(filePath).toString()))
							.addProperty(model.createProperty(frbroo+ "R46_assigned"), work.getIdentifier(filePath)));
		
		/****************************************************************************************/
		ExpressionCreation expression = new ExpressionCreation();
		ConstructURI uriCreation = new ConstructURI();
		GenerateUUID uuidCreation = new GenerateUUID();
		
		model.createResource(uriCreation.getUUID("creation","F28", uuidCreation.get()).toString())
				.addProperty(model.createProperty(cidoc+ "P4_has_time_span"), l(expression.getDateMachine(filePath),XSDDatatype.XSDgYear))
				.addProperty(model.createProperty(cidoc+ "P4_has_time_span"), l(expression.getDateText(filePath),XSDDatatype.XSDgYear))
				.addProperty(model.createProperty(cidoc+ "P3_has_note"), expression.getNote(filePath))
				.addProperty(model.createProperty(cidoc+ "P14_carried_out_by"),expression.getComposer(filePath));
		/****************************************************************************************/		
		SelfContainedExpression sce = new SelfContainedExpression();
		ConstructURI uriExpression = new ConstructURI();
		GenerateUUID uuidExpression = new GenerateUUID();
		
		model.createResource(uriExpression.getUUID("expression","F22", uuidExpression.get()).toString())
				.addProperty(model.createProperty(cidoc+ "P67_refers_to"), sce.getDedicace(filePath))
				.addProperty(model.createProperty(mus+ "U16_has_catalogue_statement"),
						model.createResource()
							.addProperty(model.createProperty(RDF.type.toString()), model.createResource(getURI(mus+"M1_Catalogue_Statement").toString()))
							.addProperty(model.createProperty(cidoc+ "P3_has_note"), sce.getCatalog(filePath))
							.addProperty(model.createProperty(cidoc+ "P106_is_composed_of"), sce.getCatalogName(filePath))
							.addProperty(model.createProperty(cidoc+ "P106_is_composed_of"), sce.getCatalogNumber(filePath)))
				.addProperty(model.createProperty(mus+ "U17_has_opus_statement"),
						model.createResource()
							.addProperty(model.createProperty(RDF.type.toString()), model.createResource(getURI(mus+"M2_Opus_Statement").toString()))
							.addProperty(model.createProperty(cidoc+ "P3_has_note"), sce.getOpus(filePath))
							.addProperty(model.createProperty(cidoc+ "P106_is_composed_of"), sce.getOpusNumber(filePath))
							.addProperty(model.createProperty(cidoc+ "P106_is_composed_of"), sce.getOpusSubNumber(filePath)))
				.addProperty(model.createProperty(cidoc+ "P3_has_note"), sce.getNote(filePath))
				.addProperty(model.createProperty(mus+ "U11_has_key"), sce.getKey(filePath))			
				.addProperty(model.createProperty(mus+ "U10_has_order_number"), sce.getOrderNumber(filePath))
				.addProperty(model.createProperty(mus+ "U12_has_genre"), sce.getGenre(filePath))
				.addProperty(model.createProperty(frbroo+ "R45i_was_assigned_by"), model.createResource(sce.getBiblioAgency(filePath).toString()))
				;
		/****************************************************************************************/
		IdentifierAssignment ia = new IdentifierAssignment ();
		ConstructURI uriIdentifier = new ConstructURI();
		GenerateUUID uuidIdentifier = new GenerateUUID();
		
		model.createResource(uriIdentifier.getUUID("identifier","F40", uuidIdentifier.get()).toString())
				.addProperty(model.createProperty(cidoc+ "P14_carried_out_by"), model.createResource(ia.getBiblioAgency(filePath).toString()))
				.addProperty(model.createProperty(frbroo+ "R46_assigned"), ia.getCreator(filePath))
				.addProperty(model.createProperty(mus+ "M1_Catalogue_Statement"), ia.getCatalog(filePath))
				.addProperty(model.createProperty(mus+ "M2_Opus_Statement"), ia.getOpus(filePath))
				.addProperty(model.createProperty(mus+ "M14_Medium_of_Performance"), ia.getDistribution(filePath))
				
				.addProperty(model.createProperty(mus+ "M3_Order_Number"), ia.getOrderNumber(filePath))
				.addProperty(model.createProperty(mus+ "M4_Key"), ia.getKey(filePath))
				.addProperty(model.createProperty(cidoc+ "P72_has_langage"), ia.getLangue(filePath))
				.addProperty(model.createProperty(mus+ "genre"), ia.getGenre(filePath))
				.addProperty(model.createProperty(mus+ "E50_date"), ia.getDateYear(filePath))
				.addProperty(model.createProperty(mus+ "E1_Crm_Entity"), ia.getVersion(filePath))
				.addProperty(model.createProperty(cidoc+ "P70i_is_documented_by"), ia.getDocumentation(filePath))
				;
		/************************************ Relations *****************************************/
		
		/****************************************************************************************/
		
		model.setNsPrefix( "mus", mus );
        model.setNsPrefix( "cidoc-crm", cidoc );
        model.setNsPrefix( "frbroo", frbroo );
        model.setNsPrefix( "xsd", xsd );
		/****************************************************************************************/
	    String query = "delete where {?x ?p \"\" }";
	    UpdateAction.parseExecute(query, model);
	    model.write(System.out);
	    
	    /****************************************************************************************/
	    /*String query = "WITH GRAPH  <DOREMUS>"+ 
	    		       "delete where {?x ?p \"\" }";
	    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(query, model);
	    vur.exec();  
		/****************************************************************************************/
	    
	    /***************************Inform the user**************************/
	    newFile.log.setText("Conversion Completed");
	    /********************************************************************/
	}
	
	/**************************************************************************/
	public static URI getURI(String path) throws URISyntaxException  {
		URIBuilder builder = new URIBuilder().setPath(path);
		URI uri = builder.build();
		 return uri;
	}
	/**************************************************************************/
	 private static Literal l ( String lexicalform, RDFDatatype datatype ) {
	        return ResourceFactory.createTypedLiteral ( lexicalform, datatype );
	    }
}
