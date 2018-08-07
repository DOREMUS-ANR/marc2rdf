package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.ontology.MUS;

/***
 * Correspond à l'attribution d'identifiant pour l'oeuvre
 ***/
public class PM18_DenominationControlledAccessPoint extends DoremusResource {
  public PM18_DenominationControlledAccessPoint(String identifier) {
    super(identifier);

    this.resource.addProperty(RDF.type, MUS.M18_Controlled_Access_Point_Denomination);
  }
}
