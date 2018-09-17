package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.OWL;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.util.Arrays;
import java.util.List;

public class F15_ComplexWork extends DoremusResource {
  public F15_ComplexWork(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F15_Complex_Work);
  }

  public F15_ComplexWork(Record record) {
    super(record);
    this.setClass(FRBROO.F15_Complex_Work);

    if (record.isTUM()) {
      this.addProperty(DC.identifier, record.getIdentifier());
      String ark = record.getAttrByName("IDPerenne").getData();
      if (ark != null) this.addProperty(OWL.sameAs, model.createResource("http://data.bnf.fr/" + ark));
      parseDerivation();
    }
  }

  private void parseDerivation() {
    // derivation
    DataField derivField = record.getDatafieldByCode(301);
    if (derivField == null || derivField.getIndicator1() == '7' || !derivField.isCode('r')) return;

    F15_ComplexWork targetWork = new F15_ComplexWork(derivField.getSubfield('3').getData());

    // derivation type
    String derivHeading = derivField.getSubfield('r').getData();
    String derivType = null;

    if (derivHeading.isEmpty())
      derivType = record.getDatafieldByCode(144).getSubfield('a').getData();

    if (derivType == null) {
      if (!derivHeading.endsWith("de") && !derivHeading.endsWith("par"))
        return;
    }

    derivType = derivHeading.replaceFirst(" (de|par)$", "").trim().toLowerCase();

    // we know this kind of derivation?
    Resource match = searchDerivationMatch(derivType);
    if (match == null) match = searchDerivationMatch(derivType.split(" ")[0]);
    if (match == null) return;

    this.addProperty(FRBROO.R2_is_derivative_of, targetWork)
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

  public F15_ComplexWork add(F22_SelfContainedExpression f22) {
    if (record == null || !record.isTUM()) {
      this.addProperty(MUS.U38_has_descriptive_expression, f22);
      return this;
    }

    boolean versionContained = versionContained();

    Property predicate = versionContained ?
      MUS.U38_has_descriptive_expression : FRBROO.R40_has_representative_expression;

    DoremusResource assignment = versionContained ?
      new M45_DescriptiveExpressionAssignment(record) :
      new F42_Representative_Expression_Assignment(record);

    assignment.addProperty(CIDOC.P141_assigned, f22)
      .addProperty(CIDOC.P140_assigned_attribute_to, this);

    this.model.add(assignment.getModel());

    this.addProperty(predicate, f22);
    return this;
  }

  private boolean versionContained() {
    List<String> notes = record.getDatafieldsByCode("600", 'a');
    return Arrays.stream(notes.toArray()).anyMatch(
      x -> ((String) x).toLowerCase().contains("versions"));
  }

  public F15_ComplexWork add(F14_IndividualWork f14) {
    this.addProperty(FRBROO.R10_has_member, f14);
    return this;
  }

  public F15_ComplexWork add(M42_PerformedExpressionCreation performance) {
    this.add(performance.getWork());
    this.add(performance.getExpression());
    return this;
  }

  public F15_ComplexWork add(M43_PerformedExpression performedExpression) {
    this.addProperty(FRBROO.R13_is_realised_in, performedExpression);
    return this;
  }

  public F15_ComplexWork add(M44_PerformedWork performedWork) {
    this.addProperty(FRBROO.R10_has_member, performedWork);
    return this;
  }
}
