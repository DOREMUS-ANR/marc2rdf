package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;

public class F42_Representative_Expression_Assignment extends DoremusResource {
  public F42_Representative_Expression_Assignment(Record record) throws URISyntaxException {
    super(record);

    this.resource.addProperty(RDF.type, FRBROO.F42_Representative_Expression_Assignment);
    this.resource.addProperty(CIDOC.P14_carried_out_by, this.model.createResource(BNF2RDF.organizationURI));
  }

}
