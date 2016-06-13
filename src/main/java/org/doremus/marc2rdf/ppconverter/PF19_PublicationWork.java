package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class PF19_PublicationWork {

  static Model modelF19;
  static URI uriF19;

  public PF19_PublicationWork() throws URISyntaxException {
    this.modelF19 = ModelFactory.createDefaultModel();
    this.uriF19 = getURIF19();
  }

  /********************************************************************************************/
  public URI getURIF19() throws URISyntaxException {
    ConstructURI uri = new ConstructURI();
    GenerateUUID uuid = new GenerateUUID();
    uriF19 = uri.getUUID("Publication_Work", "F19", uuid.get());
    return uriF19;
  }

  /********************************************************************************************/
  public Model getModel() throws URISyntaxException {

    Resource F19 = modelF19.createResource(uriF19.toString());
    F19.addProperty(RDF.type, FRBROO.F19_Publication_Work);

    /**************************** réalisation d'une work ************************************/
    F19.addProperty(FRBROO.R3_is_realised_in, modelF19.createResource(PF24_PublicationExpression.uriF24.toString()));

    return modelF19;
  }
  /********************************************************************************************/
}
