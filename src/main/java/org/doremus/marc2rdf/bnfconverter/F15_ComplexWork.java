package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

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


    parseDerivation();
  }

  private void parseDerivation() throws URISyntaxException {
    // derivation
    DataField derivField = record.getDatafieldByCode("301");
    if (derivField == null || !derivField.isCode('r')) return;

    F15_ComplexWork targetWork = new F15_ComplexWork(derivField.getSubfield('3').getData());

    // derivation type
    String derivHeading = derivField.getSubfield('r').getData();

    if (!derivHeading.endsWith("de") && !derivHeading.endsWith("par")) return;
    String derivType = derivHeading.replaceFirst(" (de|par)$", "").trim().toLowerCase();

    // we know this kind of derivation?
    Resource match = searchDerivationMatch(derivType);
    if (match == null) match = searchDerivationMatch(derivType.split(" ")[0]);
    if (match == null) return;

    this.resource.addProperty(FRBROO.R2_is_derivative_of, targetWork.asResource())
      .addProperty(MUS.U47_has_derivation_type, match);
  }

  private Resource searchDerivationMatch(String str) {
    // depluralize
    str = str.replaceFirst("s$", "");

    if (str.matches("arrang\u00e9e?")) str = "arrangement";
    else if (str.matches("orchestr\u00e9")) str = "orchestration";
    else if (str.matches("parodi\u00e9")) str = "parodie";
    else if (str.matches("transcrit")) str = "transcription";
    else if (str.matches("(version )?transpos\u00e9")) str = "transposition";
    else if (str.matches("(version )?r\u00e9duite")) str = "reduction";

    return VocabularyManager.getVocabulary("derivation").findConcept(str, false);
  }

  private F15_ComplexWork(String identifier) throws URISyntaxException {
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
