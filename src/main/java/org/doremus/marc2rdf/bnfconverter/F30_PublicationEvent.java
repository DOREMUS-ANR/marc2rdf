package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class F30_PublicationEvent extends DoremusResource {
  private static final RDFDatatype W3CDTF = TypeMapper.getInstance().getSafeTypeByName(DCTerms.getURI() + "W3CDTF");

  // "1re éd. : Paris : Revue et gazette musicale [1er janvier 1848]"
  // "1re éd. : Paris : Choudens, ca 1886"
  // "1re édition : Paris : Durand, 1891"
  // https://regex101.com/r/3n2YHU/1
  private static final String regex1 = "1re éd(?:ition|\\.)?(?: (?:dans|in) (?:: )?.+\")?(?: \\(.+\\))?(?: :|,|.) (?:(.+)(?:, | \\[))?(?:\\[?([\\p{L}\\s\\d]+)?(\\d{4})(?:-(\\d{4}))?\\]?)";
  // "1re éd. : Paris : Enoch"
  // "1re éd. : Roma"
  private static final String regex2 = "1re éd(?:ition|\\.)? : ([\\p{L}\\s.'-]+)(?:: ([\\p{L}\\s.'\\-&]+))?";
  //"Date d'édition : Venise, 1608"
  //"Date d'édition : 1694"
  private static final String regex3 = "Date d'édition : (?:(.+), )?(\\d{4})";
  private static final String textDateRegex = "(?:(1er|[\\d]{1,2}) )?(janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)";

  private final F28_ExpressionCreation f28;


  public F30_PublicationEvent(String edition, Record record, F28_ExpressionCreation f28) throws URISyntaxException {
    super(record);
    this.f28 = f28;

    this.resource.addProperty(RDF.type, FRBROO.F30_Publication_Event);
    this.resource.addProperty(CIDOC.P3_has_note, edition);

    parseNote(edition);
  }

  private void parseNote(String note) {
    Pattern p = Pattern.compile(regex1);
    Pattern p2 = Pattern.compile(regex2);
    Pattern p3 = Pattern.compile(regex3);

    Matcher m = p.matcher(note);
    Matcher m2 = p2.matcher(note);
    Matcher m3 = p3.matcher(note);


    if (m.find()) {
      String descr = m.group(1);
      String dateModifier = m.group(2);
      String year = m.group(3);
      String endYear = m.group(4);

      String city = null, publisher = null;
      if (descr != null) {
        descr = descr.trim();
        if (descr.startsWith(":")) descr = descr.substring(1).trim();
        if (descr.endsWith(",")) descr = descr.substring(0, descr.length() - 1);

        String[] parts = descr.split("[,:]");
        if (parts.length == 1) {
          if (!parts[0].contains("\"")) city = parts[0].trim();
        } else {
          publisher = parts[parts.length - 1].trim();
          if (publisher.contains("in")) publisher = null;
          city = parts[parts.length - 2].trim();
        }
      }
      if (city != null) this.resource.addProperty(CIDOC.P7_took_place_at, city);
      addPublisher(publisher);

      parseTime(year, endYear, dateModifier);

    } else if (m2.find()) {
      String city = m2.group(1), publisher = m2.group(2);
      if (city != null) this.resource.addProperty(CIDOC.P7_took_place_at, city);
      addPublisher(publisher);

    } else if (m3.find()) {
      String city = m2.group(1), year = m2.group(2);
      if (city != null) this.resource.addProperty(CIDOC.P7_took_place_at, city);
      parseTime(year);
    }

  }

  private void addPublisher(String publisher) {
    if (publisher == null) return;

    Resource activity = model.createResource()
      .addProperty(RDF.type, CIDOC.E7_Activity)
      .addProperty(MUS.U35_foresees_function_of_type, model.createLiteral("editeur", "fr"));

    if (publisher.equals("l'auteur")) // hypothesis: singular means only one author
      activity.addProperty(CIDOC.P14_carried_out_by, f28.getComposers().get(0).asResource());
    else // TODO uri of the publisher
      activity.addProperty(CIDOC.P14_carried_out_by, model.createResource()
        .addProperty(RDF.type, FRBROO.F11_Corporate_Body)
        .addProperty(CIDOC.P131_is_identified_by, publisher));

    this.resource.addProperty(CIDOC.P9_consists_of, activity);
  }

  private void parseTime(String year) {
    parseTime(year, null, null);
  }

  private void parseTime(String year, String endYear, String dateModifier) {
    if (year == null) return;

    if (dateModifier == null) dateModifier = "";
    else dateModifier = dateModifier.trim();

    Property timeSpanProp = CIDOC.P82_at_some_time_within;

    String startDate = year + "0101", endDate = year + "1231", fullDate = null;
    // whole year as default
    if (endYear != null) {
      endDate = endYear + "1231";
    }
    if (dateModifier.equals("ca")) {
      timeSpanProp = CIDOC.P81_ongoing_throughout;
    } else if (dateModifier.equalsIgnoreCase("Noël")) {
      startDate = year + "1201"; // whole december
    } else {
      fullDate = parseTextualDate(dateModifier, year);
    }

    if (fullDate == null) fullDate = startDate + "/" + endDate;

    Resource timeSpan = model.createResource(this.uri + "/time")
      .addProperty(RDF.type, CIDOC.E52_Time_Span)
      .addProperty(timeSpanProp,
        ResourceFactory.createTypedLiteral(fullDate, W3CDTF));

    if (timeSpan != null)
      this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan);

  }


  private String parseTextualDate(String text, String YYYY) {
    if (text.isEmpty()) return null;

    Pattern p = Pattern.compile(textDateRegex);
    Matcher m = p.matcher(text.toLowerCase());
    if (!m.find()) {
      System.out.println("date complement not parsed: " + text);
      return null;
    }

    String day = m.group(1);
    String month = m.group(2);
    String MM = null;
    switch (month) {
      case "janvier":
        MM = "01";
        break;
      case "février":
        MM = "02";
        break;
      case "mars":
        MM = "03";
        break;
      case "avril":
        MM = "04";
        break;
      case "mai":
        MM = "05";
        break;
      case "juin":
        MM = "06";
        break;
      case "juillet":
        MM = "07";
        break;
      case "août":
        MM = "08";
        break;
      case "septembre":
        MM = "09";
        break;
      case "octobre":
        MM = "10";
        break;
      case "novembre":
        MM = "11";
        break;
      case "décembre":
        MM = "12";
    }

    if (day == null) {
      // whole month
      return YYYY + MM + "01/" + YYYY + MM + F28_ExpressionCreation.getLastDay(MM, YYYY);
    }
    day = day.trim();
    if (day.equals("1er")) day = "01";
    else if (day.length() < 2) day = "0" + day;

    String singleDay = YYYY + MM + day;
    return singleDay + "/" + singleDay;
  }


  public F30_PublicationEvent add(F24_PublicationExpression expression) {
    /**************************** création d'une expression de publication *************/
    this.resource.addProperty(FRBROO.R24_created, expression.asResource());
    return this;
  }

  public F30_PublicationEvent add(F19_PublicationWork work) {
    /**************************** création d'une expression de publication *************/
    this.resource.addProperty(FRBROO.R19_created_a_realisation_of, work.asResource());
    return this;
  }

  public static String getEditionPrinceps(Record record) {
    // search edition priceps in the record
    for (String edition : record.getDatafieldsByCode("600", 'a')) {
      if ((edition.contains("éd.")) || (edition.contains("édition"))) return edition;
    }
    return null;
  }
}
