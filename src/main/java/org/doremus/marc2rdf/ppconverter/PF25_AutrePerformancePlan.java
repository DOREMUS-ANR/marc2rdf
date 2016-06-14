package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class PF25_AutrePerformancePlan {

  static Model modelF25;
  static URI uriF25;

  public PF25_AutrePerformancePlan() throws URISyntaxException {
    this.modelF25 = ModelFactory.createDefaultModel();
    this.uriF25 = getURIF25();
  }

  String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
  String frbroo = "http://erlangen-crm.org/efrbroo/";
  String xsd = "http://www.w3.org/2001/XMLSchema#";

  /********************************************************************************************/
  public URI getURIF25() throws URISyntaxException {
    ConstructURI uri = new ConstructURI();
    GenerateUUID uuid = new GenerateUUID();
    uriF25 = uri.getUUID("Performance_Plan", "F25", uuid.get());
    return uriF25;
  }

  /********************************************************************************************/
  public Model getModel() throws URISyntaxException {

    Resource F25 = modelF25.createResource(uriF25.toString());
    F25.addProperty(RDF.type, FRBROO.F25_Performance_Plan);

    /**************************** exécution du plan ******************************************/
    F25.addProperty(modelF25.createProperty(frbroo + "R25i_was_performed_by"), modelF25.createResource(PF31_AutrePerformance.uriF31.toString()));

    /**************************** exécution du plan ******************************************/
    //	PF31_AutrePerformance FA31= new PF31_AutrePerformance();
    //	F25.addProperty(modelF25.createProperty(frbroo+ "R25i_is_performed_by"), modelF25.createResource(F31.getURIF31().toString()));

    return modelF25;
  }
  /********************************************************************************************/
}
