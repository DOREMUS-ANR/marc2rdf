package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;

/***
 * Correspond à l'oeuvre musicale
 ***/
public class F15_ComplexWork extends DoremusResource {

  public F15_ComplexWork(Record record) throws URISyntaxException {
    super(record);

    this.resource.addProperty(RDF.type, FRBROO.F15_Complex_Work);
    this.resource.addProperty(DCTerms.identifier, record.getIdentifier());
    String ark = record.getAttrByName("IDPerenne").getData();
    if (ark != null) this.resource.addProperty(OWL.sameAs, model.createResource("http://data.bnf.fr/" + ark));


    // derivation
    DataField derivField = record.getDatafieldByCode("301");
    if (derivField != null) {
      F15_ComplexWork targetWork = new F15_ComplexWork(derivField.getSubfield('3').getData());
      this.resource.addProperty(FRBROO.R2_is_derivative_of, targetWork.asResource());

      // derivation type
      if (derivField.isCode('r')) {
        String derivHeading = derivField.getSubfield('r').getData();
        System.out.println("DERIVATION: " + derivHeading);

        // TODO check
        String derivType = derivHeading.replaceFirst(" de$", "").trim().toLowerCase();
        this.resource.addProperty(MUS.U47_has_derivation_type, model.createLiteral(derivType, "fr"));

      }
    }
  }

  public F15_ComplexWork(String identifier) throws URISyntaxException {
    super(identifier);
  }

  public F15_ComplexWork add(F22_SelfContainedExpression f22) {
    this.resource.addProperty(MUS.U38_has_descriptive_expression, f22.asResource());
    return this;
  }

  public F15_ComplexWork add(F14_IndividualWork f14) {
    this.resource.addProperty(FRBROO.R10_has_member, f14.asResource());
    return this;
  }

  public F15_ComplexWork add(M42_PerformedExpressionCreation performance) {
    this.resource.addProperty(FRBROO.R10_has_member, performance.getWork())
      .addProperty(FRBROO.R13_is_realised_in, performance.getExpression());
    return this;
  }
}
