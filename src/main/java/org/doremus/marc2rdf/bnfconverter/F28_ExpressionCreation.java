package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class F28_ExpressionCreation extends DoremusResource {
  private static final RDFDatatype W3CDTF = TypeMapper.getInstance().getSafeTypeByName(DCTerms.getURI() + "W3CDTF");

  public F28_ExpressionCreation(String identifier) throws URISyntaxException {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);
  }

  public F28_ExpressionCreation(Record record) throws URISyntaxException {
    super(record);
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);

    /**************************** Work: Date of the work (expression représentative) ********/
    String dateMachine = getDateMachine();
    if (dateMachine != null) {
      this.resource.addProperty(CIDOC.P4_has_time_span, model.createResource()
        .addProperty(RDF.type, CIDOC.E52_Time_Span)
        .addProperty(CIDOC.P82_at_some_time_within, ResourceFactory.createTypedLiteral(dateMachine, W3CDTF)));
    }

    /**************************** Work: Date of the work (expression représentative) ********/
    String dateText = getDateText();
    // TODO check! maybe this info could be better saved
    if (dateText != null) this.resource.addProperty(CIDOC.P3_has_note, dateText);

    /**************************** Work: is created by ***************************************/
    for (String composer : getComposer()) {
      this.resource.addProperty(CIDOC.P9_consists_of, model.createResource()
        .addProperty(RDF.type, CIDOC.E7_Activity)
        .addProperty(MUS.U31_had_function_of_type, model.createLiteral("compositeur", "fr"))
        .addProperty(CIDOC.P14_carried_out_by, model.createResource()
          .addProperty(RDF.type, CIDOC.E21_Person)
          .addProperty(CIDOC.P1_is_identified_by, composer)
        )
      );
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
  private String getDateMachine() {
    for (ControlField field : record.getControlfieldsByCode("008")) {
      String fieldData = field.getData();

      /***************************** Cas 1 *********************************/
      if (fieldData.charAt(36) == ' ' &&
        (fieldData.length() == 46 || fieldData.charAt(46) == ' ')) {

        String startYear = fieldData.substring(28, 32).replaceAll("\\.", "0").trim();
        String startMonth = fieldData.substring(32, 34).replaceAll("\\.", "").trim();
        String startDay = fieldData.substring(34, 36).replaceAll("\\.", "").trim();

        String endYear = fieldData.substring(38, 42).replaceAll("\\.", "9").trim();
        String endMonth = fieldData.substring(42, 44).replaceAll("\\.", "").trim();
        String endDay = fieldData.substring(44, 46).replaceAll("\\.", "").trim();

        if (startYear.isEmpty() && endYear.isEmpty()) continue;

        // there is at least one of the two year
        if (startYear.isEmpty()) startYear = endYear.substring(0, 2) + "00"; //beginning of the century
        if (endYear.isEmpty()) {
          endYear = startYear;
          endMonth = startMonth;
          endDay = startDay;
        }

        if (startMonth.isEmpty()) startMonth = "01";
        if (startDay.isEmpty()) startDay = "01";
        if (endMonth.isEmpty()) endMonth = "12";
        if (endDay.isEmpty()) {
          try {
            endDay = getLastDay(endMonth, endYear);
          } catch (ParseException e) {
            e.printStackTrace();
            return null;
          }
        }

        return startYear + startMonth + startDay + "/" + endYear + endMonth + endDay;
      }
      /***************************** Cas 2 *********************************/
      else if (fieldData.charAt(36) == '?' || fieldData.charAt(46) == '?' ||
        fieldData.substring(30, 32).contains(".") ||
        fieldData.substring(40, 42).contains("..")) {

        String startString = fieldData.substring(28, 32).trim(); // could be 1723, 172. or 17..
        String endString = fieldData.substring(38, 42).trim(); // could be 1723, 172. or 17..

        if (startString.isEmpty() && endString.isEmpty()) continue;

        // there is at least one of the two
        if (startString.isEmpty()) startString = endString;
        else if (endString.isEmpty()) endString = startString;

        return startString.replaceAll("\\.", "0") + "/" + endString.replaceAll("\\.", "9");
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

  /*****************
   * Le compositeur qui a crée l'oeuvre
   ******************************/
  private List<String> getComposer() {
    List<String> composers = new ArrayList<>();

    //TODO Conserver le contenu du $3 qui fait le lien vers la notice d'autorité Personne
    for (DataField field : record.getDatafieldsByCode("100")) {
      StringBuilder buffer = new StringBuilder();

      if (field.isCode('a')) { // surname
        buffer.append(field.getSubfield('a').getData());
      }
      if (field.isCode('m')) { // name
        if (buffer.length() > 0)
          buffer.append(", ");
        buffer.append(field.getSubfield('m').getData());
      }
      if (field.isCode('d')) { // birth - death dates
        buffer.append("(").append(field.getSubfield('d').getData()).append(")");
      }

      if (buffer.length() > 0) composers.add(buffer.toString());
    }

    return composers;
  }

  private static String getLastDay(String month, String year) throws ParseException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Date convertedDate = dateFormat.parse(year + month + "01");
    Calendar c = Calendar.getInstance();
    c.setTime(convertedDate);
    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
    return c.get(Calendar.DAY_OF_MONTH) + "";
  }

}
