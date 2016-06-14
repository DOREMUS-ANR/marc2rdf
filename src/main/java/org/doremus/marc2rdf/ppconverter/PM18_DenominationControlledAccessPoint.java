package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

public class PM18_DenominationControlledAccessPoint {

  /***
   * Correspond à l'attribution d'identifiant pour l'oeuvre
   ***/

  static Model modelM18;
  static URI uriM18;

  public PM18_DenominationControlledAccessPoint() throws URISyntaxException {
    this.modelM18 = ModelFactory.createDefaultModel();
    this.uriM18 = getURIM18();
  }

  String mus = "http://data.doremus.org/ontology/";
  String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";
  String frbroo = "http://erlangen-crm.org/efrbroo/";
  String xsd = "http://www.w3.org/2001/XMLSchema#";

  /********************************************************************************************/
  public URI getURIM18() throws URISyntaxException {
    ConstructURI uri = new ConstructURI();
    GenerateUUID uuid = new GenerateUUID();
    uriM18 = uri.getUUID("Denomination_Controlled_Access_Point", "M18", uuid.get());
    return uriM18;
  }

  public Model getModel() throws URISyntaxException, FileNotFoundException {

    Resource M18 = modelM18.createResource(uriM18.toString());
    M18.addProperty(RDF.type, modelM18.createResource(mus + "M18_Controlled_Access_Point_Denomination"));

    /**************************** Work: was assigned by *************************************/
    M18.addProperty(FRBROO.R46_assigned, modelM18.createResource(PF50_ControlledAccessPoint.uriF50.toString()));

    return modelM18;
  }
}
