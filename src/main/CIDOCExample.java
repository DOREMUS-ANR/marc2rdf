package main;

import identifierAssignment.IdentifierAssignment;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;

import selfContainedExpression.SelfContainedExpression;
import work.Work;
import convert2rdf.ConstructURI;
import convert2rdf.GenerateUUID;
import expressionCreation.ExpressionCreation;

public class CIDOCExample {

	public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
		
		/************* Creer un modele vide **************************/
        Model model = ModelFactory.createDefaultModel();
        String doremus = "http://data.doremus.org/";
        
		/****************************************************************************************/
		Work work = new Work();
		ConstructURI uriWork = new ConstructURI();
		GenerateUUID uuidWork = new GenerateUUID();
		
		model.createResource(uriWork.get("work","F15", uuidWork.get()).toString())
				.addProperty(model.createProperty(doremus+ "r45i_was_assigned_by"),
						"Ibrahim");
		
		
		
		/************************* RDF/XML ***************************/
        model.setNsPrefix( "doremus", doremus );
      model.write(System.out);
      /*************************************************************/
	}

}
