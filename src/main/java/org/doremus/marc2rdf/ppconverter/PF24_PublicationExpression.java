package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class PF24_PublicationExpression {

  static Model modelF24;
  static URI uriF24;

  public PF24_PublicationExpression() throws URISyntaxException {
    this.modelF24 = ModelFactory.createDefaultModel();
    this.uriF24 = getURIF24();
  }

  String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
  String frbroo = "http://erlangen-crm.org/efrbroo/";
  String xsd = "http://www.w3.org/2001/XMLSchema#";

  /********************************************************************************************/
  public URI getURIF24() throws URISyntaxException {
    ConstructURI uri = new ConstructURI();
    GenerateUUID uuid = new GenerateUUID();
    uriF24 = uri.getUUID("Publication_Expression", "F24", uuid.get());
    return uriF24;
  }

  /********************************************************************************************/
  public Model getModel() throws URISyntaxException {

    Resource F24 = modelF24.createResource(uriF24.toString());
    F24.addProperty(RDF.type, FRBROO.F24_Publication_Expression);

    /*********** création d'une expression de publication incorporant l'expression **********/
    F24.addProperty(modelF24.createProperty(cidoc + "P165_incorporates"), modelF24.createResource(PF22_SelfContainedExpression.uriF22.toString()));

    return modelF24;
  }
  /********************************************************************************************/
}
