package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;

public class F14_IndividualWork extends DoremusResource {
  public F14_IndividualWork(Record record) throws URISyntaxException {
    super(record);

    this.resource.addProperty(RDF.type, FRBROO.F14_Individual_Work);
  }


  public F14_IndividualWork addPremiere(F31_Performance premiere) {
    /**************************** Work: had premiere ****************************************/
    this.resource.addProperty(MUS.U5_had_premiere, premiere.asResource());

    return this;
  }

  public F14_IndividualWork add(F22_SelfContainedExpression expression) {
    /**************************** Work: is realised through (expression) ********************/
    this.resource.addProperty(FRBROO.R9_is_realised_in, expression.asResource());
    return this;
  }

  public F14_IndividualWork add(F30_PublicationEvent publication) {
    /**************************** Work: had princeps publication ****************************/
    this.resource.addProperty(MUS.U4_had_princeps_publication, publication.asResource());
//    publication.asResource().addProperty(MUS.U4i_was_princeps_publication_of, F14);
    return this;
  }

}
