package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

/***
 * Correspond à l'oeuvre musicale
 ***/
public class PF15_ComplexWork extends DoremusResource {

  public PF15_ComplexWork(Record record, String identifier) {
    super(record, identifier);
    this.resource.addProperty(RDF.type, FRBROO.F15_Complex_Work);
    this.resource.addProperty(DC.identifier, identifier);
    this.resource.addProperty(OWL.sameAs, model.createResource("http://digital.philharmoniedeparis.fr/doc/CIMU/" + identifier));
  }

  public PF15_ComplexWork(String identifier) {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F15_Complex_Work);
  }

  public PF15_ComplexWork add(PF22_SelfContainedExpression f22) {
    if (record.isType("UNI:100"))
      this.resource.addProperty(MUS.U38_has_descriptive_expression, f22.asResource());
    return this;
  }

  public PF15_ComplexWork add(PF14_IndividualWork f14) {
    this.resource.addProperty(FRBROO.R10_has_member, f14.asResource());
    return this;
  }

  public PF15_ComplexWork add(PM42_PerformedExpressionCreation performance) {
    this.resource.addProperty(FRBROO.R10_has_member, performance.getWork())
      .addProperty(FRBROO.R13_is_realised_in, performance.getExpression().asResource());
    return this;
  }
}
