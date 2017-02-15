package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Person;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class PF28_ExpressionCreation extends DoremusResource {
  private List<Person> composers;

  public PF28_ExpressionCreation(Record record, String identifier) throws URISyntaxException {
    super(record, identifier);
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);

    /**************************** Work: Date of the work (expression représentative) ********/
    String[] dateMachine = getDateMachine();
    if (dateMachine != null) {
      TimeSpan timeSpan = new TimeSpan(dateMachine[0], dateMachine[1]);
      timeSpan.setUri(this.uri + "/time");
      this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
      this.model.add(timeSpan.getModel());
    }

    /**************************** Work: Date of the work (expression représentative) ********/
    String dateText = getDateText();
    if (dateText != null && !dateText.isEmpty())
      // TODO check: too generic?
      this.resource.addProperty(CIDOC.P3_has_note, dateText);

    /**************************** Work: Period of the work **********************************/
    String period = getPeriod();
    if (period != null) {
      this.resource.addProperty(CIDOC.P10_falls_within, model.createResource(this.uri + "/period")
        .addProperty(RDF.type, CIDOC.E4_Period)
        .addProperty(CIDOC.P1_is_identified_by, getPeriod()));
    }

    /**************************** Work: is created by ***************************************/
    this.composers = getComposer();
    int composersCount = 0;
    for (Person composer : this.composers) {
      this.resource.addProperty(CIDOC.P9_consists_of, model.createResource(this.uri + "/activity/" + ++composersCount)
        .addProperty(RDF.type, CIDOC.E7_Activity)
        .addProperty(MUS.U31_had_function_of_type, model.createLiteral("compositeur", "fr"))
        .addProperty(CIDOC.P14_carried_out_by, composer.asResource())
      );

      model.add(composer.getModel());
    }
  }

  public List<Person> getComposers() {
    return this.composers;
  }

  public PF28_ExpressionCreation add(PF25_PerformancePlan plan) {
    /**************************** création d'une expression de plan d'exécution *************/
    this.resource.addProperty(FRBROO.R17_created, plan.asResource());
    return this;
  }

  public PF28_ExpressionCreation add(PF22_SelfContainedExpression expression) {
    /**************************** Expression: created ***************************************/
    this.resource.addProperty(FRBROO.R17_created, expression.asResource());
    // expression.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R17i_was_created_by"), F28);
    return this;
  }

  public PF28_ExpressionCreation add(PF14_IndividualWork work) {
    /**************************** Work: created a realisation *******************************/
    this.resource.addProperty(FRBROO.R19_created_a_realisation_of, work.asResource());
//    work.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R19i_was_realised_through"), F28);

    return this;
  }


  public Model getModel() {
    return model;
  }

  /*************
   * Date de creation de l'expression (Format machine)
   ***********************/
  private String[] getDateMachine() {
    if (!record.isType("UNI:100")) return null;

    String start = "", end = "";

    for (DataField field : record.getDatafieldsByCode("909")) {
      if (!field.isCode('g') && !field.isCode('h')) continue;

      if (field.isCode('g')) start = field.getSubfield('g').getData().trim();
      if (field.isCode('h')) end = field.getSubfield('h').getData().trim();

      if (start.isEmpty() && end.isEmpty()) return null;
      return new String[]{start.replaceFirst("\\.$", ""), end.replaceFirst("\\.$", "")};
    }

    return null;
  }

  /*************
   * Date de création de l'expression (Format texte)
   ***********************/
  private String getDateText() {
    if (!record.isType("UNI:100")) return null;

    for (DataField field : record.getDatafieldsByCode("909")) {
      if (!field.isCode('b')) continue;
      return field.getSubfield('b').getData().trim();
    }
    return null;
  }

  /*************
   * Période de création de l'expression
   ********************************/
  private String getPeriod() {
    if (!record.isType("UNI:100")) return null;

    for (DataField field : record.getDatafieldsByCode("610")) {
      String period = "", value = "";
      if (field.isCode('a')) period = field.getSubfield('a').getData().trim();
      if (field.isCode('b')) value = field.getSubfield('b').getData().trim();

      if (value.equals("02") && !period.isEmpty()) return period;
    }

    return null;
  }

  private List<Person> getComposer() throws URISyntaxException {
    if (!record.isType("UNI:100")) return new ArrayList<>();

    List<Person> composers = new ArrayList<>();

    List<DataField> fields = record.getDatafieldsByCode("700");
    for (DataField field : record.getDatafieldsByCode("701")) {
      //prendre en compte aussi le 701 que si son $4 a la valeur "230"
      if (field.isCode('4') && field.getSubfield('4').getData().equals("230")) {
        //230 is the composer
        fields.add(field);
      }
    }

    for (DataField field : fields) {
      String firstName = null, lastName = null, birthDate = null, deathDate = null;
      if (field.isCode('a')) { // surname
        lastName = field.getSubfield('a').getData().trim();
      }
      if (field.isCode('b')) { // name
        firstName = field.getSubfield('b').getData().trim();
      }
      if (field.isCode('f')) { // birth - death dates
        String[] dates = field.getSubfield('f').getData().split("-");
        birthDate = dates[0].trim();
        if (dates.length > 1) deathDate = dates[1].trim();
      }

      composers.add(new Person(firstName, lastName, birthDate, deathDate, null));
    }
    return composers;
  }
}
