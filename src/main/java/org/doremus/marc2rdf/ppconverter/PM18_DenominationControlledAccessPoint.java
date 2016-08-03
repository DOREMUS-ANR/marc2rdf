package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.ontology.MUS;

import java.net.URI;
import java.net.URISyntaxException;

public class PM18_DenominationControlledAccessPoint {

  /***
   * Correspond à l'attribution d'identifiant pour l'oeuvre
   ***/

  private Model model;
  private URI uriM18;
  private final Resource M18;

  public PM18_DenominationControlledAccessPoint() throws URISyntaxException {
    this.model = ModelFactory.createDefaultModel();
    this.uriM18 = ConstructURI.build("Denomination_Controlled_Access_Point", "M18");

    M18 = model.createResource(uriM18.toString());
    M18.addProperty(RDF.type, MUS.M18_Controlled_Access_Point_Denomination);
  }

  public Model getModel() {
    return model;
  }

  public Resource asResource() {
    return M18;
  }
}
