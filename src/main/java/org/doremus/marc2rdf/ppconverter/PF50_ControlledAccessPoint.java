package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

/***
 * Correspond à l'attribution d'identifiant pour l'oeuvre
 ***/

public class PF50_ControlledAccessPoint extends DoremusResource{
  public PF50_ControlledAccessPoint(Record record) throws URISyntaxException, UnsupportedEncodingException, NoSuchAlgorithmException {
    super(record);

    this.uri = ConstructURI.build("philharmonie", "F50", "Controlled_Access_Point", record.getIdentifier());


   this.resource = model.createResource(this.uri.toString());
    this.resource.addProperty(RDF.type, FRBROO.F50_Controlled_Access_Point);

    /**************************** Attribution d'identifiant dénomination ********************/
    PM18_DenominationControlledAccessPoint m18 = new PM18_DenominationControlledAccessPoint(this.identifier);
    this.resource.addProperty(FRBROO.R8_consists_of, m18.asResource());
    model.add(m18.getModel());
  }


}
