package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Person;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.util.List;

public class F28_ExpressionCreation extends DoremusResource {
  private List<Person> composers;

  public F28_ExpressionCreation(String identifier) throws URISyntaxException {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);
  }

  public F28_ExpressionCreation(Record record) throws URISyntaxException {
    super(record);
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);

    /**************************** Work: Date of the work (expression représentative) ********/
    TimeSpan dateMachine = getDateMachine();
    if (dateMachine != null) {
      dateMachine.setUri(this.uri + "/time");
      Resource dateRes = dateMachine.asResource();
      if (dateRes != null) {
        this.resource.addProperty(CIDOC.P4_has_time_span, dateMachine.asResource());
        this.model.add(dateMachine.getModel());
      }
    }

    /**************************** Work: Date of the work (expression représentative) ********/
    String dateText = getDateText();
    if (dateText != null) this.resource.addProperty(CIDOC.P3_has_note, dateText);

    /**************************** Work: is created by ***************************************/
    this.composers = ArtistConverter.getArtistsInfo(record);
    int composerCount = 0;
    for (Person composer : composers) {
      this.resource.addProperty(CIDOC.P9_consists_of, model.createResource(this.uri + "/activity/" + ++composerCount)
        .addProperty(RDF.type, CIDOC.E7_Activity)
        .addProperty(MUS.U31_had_function_of_type, model.createLiteral("compositeur", "fr"))
        .addProperty(CIDOC.P14_carried_out_by, composer.asResource())
      );
      model.add(composer.getModel());
    }
  }


  public F28_ExpressionCreation add(F22_SelfContainedExpression expression) {
    /**************************** Expression: created ***************************************/
    this.resource.addProperty(FRBROO.R17_created, expression.asResource());
//    expression.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R17i_was_created_by"), F28);
    return this;
  }

  public F28_ExpressionCreation add(F14_IndividualWork f14) {
    /**************************** Work: created a realisation *******************************/
    this.resource.addProperty(FRBROO.R19_created_a_realisation_of, f14.asResource());
//    f14.asResource().addProperty(model.createProperty(FRBROO.getURI() + "R19i_was_realised_through"), F28);
    return this;
  }


  /*************
   * Date de creation de l'expression (Format machine)
   ***********************/
  private TimeSpan getDateMachine() {
    for (ControlField field : record.getControlfieldsByCode("008")) {
      String fieldData = field.getData();

      //approximate date
      if (fieldData.charAt(36) == '?' || fieldData.charAt(46) == '?' ||
        fieldData.substring(30, 32).contains(".") ||
        fieldData.substring(40, 42).contains(".")) {

        String startString = fieldData.substring(28, 32).trim(); // could be 1723, 172. or 17..
        String endString = fieldData.substring(38, 42).trim(); // could be 1723, 172. or 17..

        if (!startString.matches("\\d.+")) startString = "";
        if (!endString.matches("\\d.+")) endString = "";

        if (startString.isEmpty() && endString.isEmpty()) continue;

        // there is at least one of the two
        if (startString.isEmpty()) startString = endString;
        else if (endString.isEmpty()) endString = startString;

        startString = startString.replaceAll("[.-]", "0");
        endString = endString.replaceAll("[.-]", "9");

        return new TimeSpan(startString, endString);
      }
      // known date
      else {
        String startYear = fieldData.substring(28, 32);

        if (startYear.matches("\\d+.+")) { // at least a digit is specified
          startYear = startYear.replaceAll("[.?-]", "0").trim();
        } else startYear = "";

        String startMonth = fieldData.substring(32, 34).replaceAll("[.-]", "").trim();
        String startDay = fieldData.substring(34, 36).replaceAll("[.-]", "").trim();

        String endYear = fieldData.substring(38, 42).replaceAll("[.-]", "").trim();
        String endMonth = fieldData.substring(42, 44).replaceAll("[.-]", "").trim();
        String endDay = fieldData.substring(44, 46).replaceAll("[.-]", "").trim();

        if (startYear.isEmpty() && endYear.isEmpty()) continue;

        return new TimeSpan(startYear, startMonth, startDay, endYear, endMonth, endDay);
      }
    }

    return null;
  }

  /*************
   * Date de création de l'expression (Format texte)
   ***********************/
  private String getDateText() {
    for (String date : record.getDatafieldsByCode("100", 'a')) {
      if (date.contains("comp.") || date.contains("Date de composition") || date.contains("Dates de composition"))
        return date;
    }
    return null;
  }

  public List<Person> getComposers() {
    return this.composers;
  }
}
