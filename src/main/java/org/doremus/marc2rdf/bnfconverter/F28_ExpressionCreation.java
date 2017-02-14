package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCTerms;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class F28_ExpressionCreation extends DoremusResource {
  private static final RDFDatatype W3CDTF = TypeMapper.getInstance().getSafeTypeByName(DCTerms.getURI() + "W3CDTF");
  private List<Person> composers;
//  private static final String textDateRegex = "(1er|[\\d]{1,2}) (janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre) (\\d{4})";

  public F28_ExpressionCreation(String identifier) throws URISyntaxException {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);
  }

  public F28_ExpressionCreation(Record record) throws URISyntaxException {
    super(record);
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);

    /**************************** Work: Date of the work (expression représentative) ********/
    Resource dateMachine = getDateMachine();
    if (dateMachine != null) {
      this.resource.addProperty(CIDOC.P4_has_time_span, dateMachine);
    }

    /**************************** Work: Date of the work (expression représentative) ********/
    String dateText = getDateText();
    // TODO check! maybe this info could be better saved
    if (dateText != null) this.resource.addProperty(CIDOC.P3_has_note, dateText);

    /**************************** Work: is created by ***************************************/
    this.composers = ArtistConverter.getArtistsInfo(record);
    for (Person composer : composers) {
      this.resource.addProperty(CIDOC.P9_consists_of, model.createResource()
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
  private Resource getDateMachine() {
    for (ControlField field : record.getControlfieldsByCode("008")) {
      String fieldData = field.getData();

      //approximate date
      if (fieldData.charAt(36) == '?' || fieldData.charAt(46) == '?' ||
        fieldData.substring(30, 32).contains(".") ||
        fieldData.substring(40, 42).contains(".")) {

        String startString = fieldData.substring(28, 32).trim(); // could be 1723, 172. or 17..
        String endString = fieldData.substring(38, 42).trim(); // could be 1723, 172. or 17..

        if (startString.isEmpty() && endString.isEmpty()) continue;

        // there is at least one of the two
        if (startString.isEmpty()) startString = endString;
        else if (endString.isEmpty()) endString = startString;

        String date = startString.replaceAll("\\.", "0") + "/" + endString.replaceAll("\\.", "9");

        return model.createResource()
          .addProperty(RDF.type, CIDOC.E52_Time_Span)
          .addProperty(CIDOC.P81_ongoing_throughout, ResourceFactory.createTypedLiteral(date, W3CDTF));
      }
      // known date
      else {
        String startYear = fieldData.substring(28, 32);

        Pattern numberPat = Pattern.compile("\\d+");
        Matcher numberMatcher = numberPat.matcher(startYear);

        if (numberMatcher.find()) { // at least a digit is specified
          startYear = startYear.replaceAll("\\.|\\?", "0").trim();
        } else {
          startYear = "";
        }
        String startMonth = fieldData.substring(32, 34).replaceAll("\\.", "").trim();
        String startDay = fieldData.substring(34, 36).replaceAll("\\.", "").trim();

        String endYear = fieldData.substring(38, 42).replaceAll("\\.", "").trim();
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
          endDay = TimeSpan.getLastDay(endMonth, endYear);
          if (endDay == null) {
            System.out.println("File: " + record.getIdentifier());
            System.out.println("Date: " + fieldData);
            return null;
          }
        }

        String date = startYear + startMonth + startDay + "/" + endYear + endMonth + endDay;
        return model.createResource()
          .addProperty(RDF.type, CIDOC.E52_Time_Span)
          .addProperty(CIDOC.P82_at_some_time_within, ResourceFactory.createTypedLiteral(date, W3CDTF));
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
