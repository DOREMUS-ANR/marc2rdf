package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

/***
 * Correspond à l'attribution d'identifiant pour l'oeuvre
 ***/

public class PF50_ControlledAccessPoint {
  private Model model;
  private URI uriF50;
  private final Resource F50;

  public PF50_ControlledAccessPoint() throws URISyntaxException {
    this.model = ModelFactory.createDefaultModel();
    this.uriF50 = ConstructURI.build("Controlled_Access_Point", "F50");

    F50 = model.createResource(uriF50.toString());
    F50.addProperty(RDF.type, FRBROO.F50_Controlled_Access_Point);

    /**************************** Attribution d'identifiant dénomination ********************/
    PM18_DenominationControlledAccessPoint m18 = new PM18_DenominationControlledAccessPoint();
    F50.addProperty(FRBROO.R8_consists_of, m18.asResource());
    model.add(m18.getModel());
  }


  public Model getModel() {
    return model;
  }

  public Resource asResource() {
    return F50;
  }
}
