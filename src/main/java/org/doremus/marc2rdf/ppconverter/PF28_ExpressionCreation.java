package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.*;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PF28_ExpressionCreation extends DoremusResource {
  private List<Person> composers;

  public PF28_ExpressionCreation(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F28_Expression_Creation);
  }

  public PF28_ExpressionCreation(Record record) {
    this(record, record.getIdentifier());
  }

  public PF28_ExpressionCreation(Record record, String identifier) {
    super(record, identifier);
    this.setClass(FRBROO.F28_Expression_Creation);

    if (record.isTUM()) convertUNI100();
    else if (record.getType().matches("UNI:4[42]")) convertUNI44();
  }

  private void convertUNI44() {
    this.composers = findArtistWithFunction(Function.COMPOSER);
    this.composers.forEach(composer -> this.addActivity(composer, "compositeur"));
  }

  private void convertUNI100() {
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
      this.addTimeSpan(timeSpan);
    }

    String dateText = getDateText();
    if (dateText != null && !dateText.isEmpty())
      this.resource.addProperty(CIDOC.P3_has_note, dateText);

    String period = getPeriod();
    if (period != null) {
      String periodUri = getPeriodUri(period);
      Resource periodRes;
      try {

        if (periodUri != null) periodRes = model.createResource(periodUri);
        else {
          periodUri = ConstructURI.build("pp", "E4_Period", period).toString();
          periodRes = model.createResource(periodUri)
            .addProperty(RDF.type, CIDOC.E4_Period)
            .addProperty(RDFS.label, period, "fr")
            .addProperty(CIDOC.P1_is_identified_by, period, "fr");
        }

        this.resource.addProperty(CIDOC.P10_falls_within, periodRes);

      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    this.composers = findArtistWithFunction(Function.COMPOSER);
    this.composers.forEach(composer -> this.addActivity(composer, "compositeur"));

    List<Person> librettists = findArtistWithFunction(Function.LIBRETTIST);
    librettists.forEach(librettist -> this.addActivity(librettist, "librettist"));
  }

  public List<Person> getComposers() {
    return this.composers;
  }

  public List<String> getComposerUris() {
    return this.composers.stream()
      .map(Artist::getUri)
      .collect(Collectors.toList());
  }

  public PF28_ExpressionCreation add(PF25_PerformancePlan plan) {
    this.addProperty(FRBROO.R17_created, plan);
    return this;
  }

  public PF28_ExpressionCreation add(PF22_SelfContainedExpression expression) {
    this.addProperty(FRBROO.R17_created, expression);
    return this;
  }

  public PF28_ExpressionCreation add(PF14_IndividualWork work) {
    this.addProperty(FRBROO.R19_created_a_realisation_of, work);
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

  private String getPeriod() {
    if (!record.isType("UNI:100")) return null;
    for (DataField field : record.getDatafieldsByCode("610")) {
      String period = field.getString('a');
      String value = field.getString('b');
      if ("02".equals(value) && period != null) return period;
    }
    return null;
  }

  public enum Function {
    COMPOSER("230"),
    LIBRETTIST("480");

    private final String code;

    Function(final String code) {
      this.code = code;
    }
  }

  private List<Person> findArtistWithFunction(Function function) {
    if (record.isType("AIC:14")) return new ArrayList<>();

    List<DataField> fields = record.getDatafieldsByCode("700");
    if (!record.isTUM() && function == Function.COMPOSER)
      // 700 is composer by default only for work records
      fields = fields.stream()
        .filter(x -> x.hasSubfieldValue('4', function.code))
        .collect(Collectors.toList());

    fields.addAll(record.getDatafieldsByCode("701").stream()
      .filter(x -> x.hasSubfieldValue('4', function.code))
      .collect(Collectors.toList())
    );

    return fields.stream().map(Person::fromUnimarcField)
      .peek(Person::interlink)
      .collect(Collectors.toList());
  }


  private static String getPeriodUri(String input) {
    String local;
    if (input.contains("siècle")) {
      String century = input.substring(0, 2);
      local = century + "_century";
      if (input.contains("moitié")) {
        int begin = input.indexOf("moitié") - 6;
        String half = input.substring(begin, begin + 1);
        local += "_" + half;
      }
    } else if (input.equals("Renaissance"))
      local = "renaissance";
    else if (input.equals("Révolution française"))
      local = "french_revolution";
    else return null;

    return "http://data.doremus.org/period/" + local;
  }

}
