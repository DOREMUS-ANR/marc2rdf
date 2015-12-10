package main;

import identifierAssignment.IdentifierAssignment;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.apache.jena.vocabulary.RDF;

import com.hp.hpl.jena.rdf.model.Model;

import selfContainedExpression.SelfContainedExpression;
import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import work.Work;
import convert2rdf.ConstructURI;
import convert2rdf.GenerateUUID;
import expressionCreation.ExpressionCreation;


/********************* Query ***********************/
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import virtuoso.jena.driver.*;
/***************************************************/

public class RDFConversion {

	public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
		
		/************* Creer un modele vide **************************/
		Model model = VirtModel.openDatabaseModel("DOREMUS", "jdbc:virtuoso://localhost:1111", "dba", "dba");
        String doremusNS = "http://data.doremus.org/";
        String cidocNS = "http://www.cidoc-crm.org/cidoc-crm/";
        String frbrooNS = "";
        
		/****************************************************************************************/
		Work work = new Work();
		ConstructURI uriWork = new ConstructURI();
		GenerateUUID uuidWork = new GenerateUUID();
		
		model.createResource(uriWork.get("work","F15", uuidWork.get()).toString())
				.addProperty(model.createProperty(cidocNS+ "R45i_was_assigned_by"),
						model.createResource()
							.addProperty(model.createProperty(RDF.type.toString()), doremusNS+"F40_Identifier_Assignment")
							.addProperty(model.createProperty(cidocNS+ "P14_carried_out_by"), work.getBiblioAgency("Data\\XMLFile.xml"))
							.addProperty(model.createProperty(cidocNS+ "R46_assigned"), work.getIdentifier("Data\\XMLFile.xml")));
		
		/****************************************************************************************/
		ExpressionCreation expression = new ExpressionCreation();
		ConstructURI uriCreation = new ConstructURI();
		GenerateUUID uuidCreation = new GenerateUUID();
		
		model.createResource(uriCreation.get("creation","F28", uuidCreation.get()).toString())
				.addProperty(model.createProperty(cidocNS+ "P4_has_time_span"), expression.getDateMachine("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(cidocNS+ "P4_has_time_span"), expression.getDateText("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(cidocNS+ "P3_has_note"), expression.getNote("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(cidocNS+ "P14_carried_out_by"),expression.getComposer("Data\\XMLFile.xml")) ;
		/****************************************************************************************/		
		SelfContainedExpression sce = new SelfContainedExpression();
		ConstructURI uriExpression = new ConstructURI();
		GenerateUUID uuidExpression = new GenerateUUID();
		
		model.createResource(uriExpression.get("expression","F22", uuidExpression.get()).toString())
				.addProperty(model.createProperty(cidocNS+ "P67_refers_to"), sce.getDedicace("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(doremusNS+ "U16_has_catalogue_statement"),
						model.createResource()
							.addProperty(model.createProperty(RDF.type.toString()), doremusNS+"M1_Catalogue_Statement")
							.addProperty(model.createProperty(cidocNS+ "P3_has_note"), sce.getCatalog("Data\\XMLFile.xml"))
							.addProperty(model.createProperty(cidocNS+ "P106_is_composed_of"), sce.getCatalogName("Data\\XMLFile.xml"))
							.addProperty(model.createProperty(cidocNS+ "P106_is_composed_of"), sce.getCatalogNumber("Data\\XMLFile.xml")))
				.addProperty(model.createProperty(doremusNS+ "U17_has_opus_statement"),
						model.createResource()
							.addProperty(model.createProperty(RDF.type.toString()), doremusNS+"M2_Opus_Statement")
							.addProperty(model.createProperty(cidocNS+ "P3_has_note"), sce.getOpus("Data\\XMLFile.xml"))
							.addProperty(model.createProperty(cidocNS+ "P106_is_composed_of"), sce.getOpusNumber("Data\\XMLFile.xml"))
							.addProperty(model.createProperty(cidocNS+ "P106_is_composed_of"), sce.getOpusSubNumber("Data\\XMLFile.xml")))
				.addProperty(model.createProperty(cidocNS+ "P3_has_note"), sce.getNote("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(doremusNS+ "U11_has_key"), sce.getKey("Data\\XMLFile.xml"))			
				.addProperty(model.createProperty(doremusNS+ "U10_has_order_number"), sce.getOrderNumber("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(doremusNS+ "U12_has_genre"), sce.getGenre("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(cidocNS+ "R45i_was_assigned_by"), sce.getBiblioAgency("Data\\XMLFile.xml"))
				;
		/****************************************************************************************/
		IdentifierAssignment ia = new IdentifierAssignment ();
		ConstructURI uriIdentifier = new ConstructURI();
		GenerateUUID uuidIdentifier = new GenerateUUID();
		
		model.createResource(uriIdentifier.get("identifier","F40", uuidIdentifier.get()).toString())
				.addProperty(model.createProperty(cidocNS+ "P14_carried_out_by"), ia.getBiblioAgency("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(cidocNS+ "R46_assigned"), ia.getCreator("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(doremusNS+ "M1_catalogue_statement"), ia.getCatalog("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(doremusNS+ "M2_opus_statement"), ia.getOpus("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(doremusNS+ "M14_medium_of_performance"), ia.getDistribution("Data\\XMLFile.xml"))
				
				.addProperty(model.createProperty(doremusNS+ "M3_order_number"), ia.getOrderNumber("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(doremusNS+ "M4_key"), ia.getKey("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(cidocNS+ "P72_has_langage"), ia.getLangue("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(doremusNS+ "genre"), ia.getGenre("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(doremusNS+ "E50_date"), ia.getDateYear("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(doremusNS+ "E1_crm_entity"), ia.getVersion("Data\\XMLFile.xml"))
				.addProperty(model.createProperty(cidocNS+ "P70i_is_documented_by"), ia.getDocumentation("Data\\XMLFile.xml"))
				;
		/****************************************************************************************/
		
		/**************************************** RDF/XML ***************************************/
        model.setNsPrefix( "doremus", doremusNS );
        model.setNsPrefix( "cidoc-crm", cidocNS );
     // model.write(System.out);
        
        
      /******************************************************************************************/
	/*	Query sparql = QueryFactory.create("SELECT * WHERE { GRAPH ?graph { ?s ?p ?o } } limit 100");
		VirtuosoQueryExecution vqe = (VirtuosoQueryExecution) VirtuosoQueryExecutionFactory.create (sparql, model);

		ResultSet results = vqe.execSelect();
		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
		    RDFNode graph = result.get("graph");
		    RDFNode s = result.get("s");
		    RDFNode p = result.get("p");
		    RDFNode o = result.get("o");
		    System.out.println(graph + " { " + s + " " + p + " " + o + " . }");
		}
      /*******************************************************************************************/
        
	}

}
