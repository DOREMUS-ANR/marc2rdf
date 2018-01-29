package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.ConstructURI;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PF28_ExpressionCreation extends DoremusResource {
  private List<Person> composers;
  private int composersCount = 0;

  public PF28_ExpressionCreation(String identifier) throws URISyntaxException {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);
  }

  public PF28_ExpressionCreation(Record record) throws URISyntaxException {
    this(record, record.getIdentifier());
  }

  public PF28_ExpressionCreation(Record record, String identifier) throws URISyntaxException {
    super(record, identifier);
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);

    if (record.isTUM()) convertUNI100();
    else if ("UNI:44".equals(record.getType())) convertUNI44();
  }

  private void convertUNI44() throws URISyntaxException {
    List<DataField> activities = record.getDatafieldsByCode(701);
    activities.addAll(record.getDatafieldsByCode(700));
    for (DataField a : activities) {
      if (!a.isCode(4)) return;
      if (!"230".equals(a.getString(4))) return;

      String surname = a.getString('a');
      String name = a.getString('b');

      // FIXME convert person record
      Person person = new Person(name, surname, null);

      Resource activity = model.createResource(this.uri + "/activity/" + ++composersCount)
        .addProperty(RDF.type, CIDOC.E7_Activity)
        .addProperty(CIDOC.P14_carried_out_by, person.asResource())
        .addProperty(MUS.U31_had_function, "compositeur", "fr");
      this.resource.addProperty(CIDOC.P9_consists_of, activity);

      model.add(person.getModel());
    }

    // unstructured roles
    char[] unstrRolesCode = new char[]{'f', 'g'};
    for (DataField df : record.getDatafieldsByCode(200)) {
      for (char c : unstrRolesCode)
        if (df.isCode(c))
          this.resource.addProperty(MUS.U226_has_responsibility_detail, df.getString(c));
    }
  }

  private void convertUNI100() throws URISyntaxException {

    String[] dateMachine = getDateMachine();
    if (dateMachine != null) {
      Pattern p = Pattern.compile(TimeSpan.frenchDateRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
      String start = dateMachine[0], end = dateMachine[1];
      TimeSpan timeSpan = null;
      if (start.length() <= 4 && end.length() <= 4) {
        timeSpan = new TimeSpan(start, end);
      } else {
        Matcher ms = p.matcher(start), me = p.matcher(end);
        if (ms.find() && me.find()) {
          timeSpan = new TimeSpan(
            ms.group(3), ms.group(2), ms.group(1),
            me.group(3), me.group(2), me.group(1));
        } else {
          System.out.println(record.getIdentifier() + " | not a date: " + start + "/" + end);
        }
      }
      if (timeSpan != null) {
        timeSpan.setUri(this.uri + "/interval");
        this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
        this.model.add(timeSpan.getModel());
      }
    }

    String dateText = getDateText();
    if (dateText != null && !dateText.isEmpty())
      this.resource.addProperty(CIDOC.P3_has_note, dateText);

    String period = getPeriod();
    if (period != null) {
      Literal periodLiteral = model.createLiteral(period, "fr");
      this.resource.addProperty(CIDOC.P10_falls_within, model.createResource(ConstructURI.build("pp", "E4_Period", period).toString())
        .addProperty(RDF.type, CIDOC.E4_Period)
        .addProperty(RDFS.label, periodLiteral)
        .addProperty(CIDOC.P1_is_identified_by, periodLiteral));
    }

    this.composers = getComposer();
    for (Person composer : this.composers) {
      this.resource.addProperty(CIDOC.P9_consists_of, model.createResource(this.uri + "/activity/" + ++composersCount)
        .addProperty(RDF.type, CIDOC.E7_Activity)
        .addProperty(MUS.U31_had_function, model.createLiteral("compositeur", "fr"))
        .addProperty(CIDOC.P14_carried_out_by, composer.asResource())
      );

      model.add(composer.getModel());
    }
  }

  public List<Person> getComposers() {
    return this.composers;
  }

  public List<String> getComposerUris() {
    return this.composers.stream()
      .map(e -> e.getUri().toString())
      .collect(Collectors.toList());
  }

  public PF28_ExpressionCreation add(PF25_PerformancePlan plan) {
    this.resource.addProperty(FRBROO.R17_created, plan.asResource());
    return this;
  }

  public PF28_ExpressionCreation add(PF22_SelfContainedExpression expression) {
    this.resource.addProperty(FRBROO.R17_created, expression.asResource());
    // expression.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R17i_was_created_by"), F28);
    return this;
  }

  public PF28_ExpressionCreation add(PF14_IndividualWork work) {
    this.resource.addProperty(FRBROO.R19_created_a_realisation_of, work.asResource());
//    work.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R19i_was_realised_through"), F28);

    return this;
  }


  public Model getModel() {
    return model;
  }

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
