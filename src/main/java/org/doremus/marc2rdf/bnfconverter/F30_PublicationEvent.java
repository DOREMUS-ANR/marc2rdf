package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.*;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class F30_PublicationEvent extends DoremusResource {
  // "1re éd. : Paris : Revue et gazette musicale [1er janvier 1848]"
  // "1re éd. : Paris : Choudens, ca 1886"
  // "1re édition : Paris : Durand, 1891"
  // https://regex101.com/r/3n2YHU/2
  private static final String regex1 = "1res? éd(?:ition|\\.)?(?: (?:dans|in) (?:: )?.+\")?(?: \\(.+\\))?(?: :|,|.) (?:(.+)(?:, | \\[))?(?:\\[?([\\p{L}\\s\\d]+)?(\\d{4})(?:-(\\d{4}))?\\]?)";
  // "1re éd. : Paris : Enoch"
  // "1re éd. : Roma"
  private static final String regex2 = "1re éd(?:ition|\\.)? : ([\\p{L}\\s.'-]+)(?:: ([\\p{L}\\s.'\\-&]+))?";
  //"Date d'édition : Venise, 1608"
  //"Date d'édition : 1694"
  private static final String regex3 = "Date d'édition : (?:(.+), )?(\\d{4})";
  private static final String textDateRegex = "(?:(1er|[\\d]{1,2}) )?(janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)";

  private static int activityCount = 0;
  private F28_ExpressionCreation f28;

  public F30_PublicationEvent(String edition, Record record, F28_ExpressionCreation f28, int i) {
    super(record);
    this.f28 = f28;

    this.identifier = record.getIdentifier() + i;
    regenerateResource();

    this.setClass(FRBROO.F30_Publication_Event);
    addNote(edition);
    parseNote(edition);
  }

  public F30_PublicationEvent(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F30_Publication_Event);
  }

  private void parseNote(String note) {
    Pattern p = Pattern.compile(regex1);
    Pattern p2 = Pattern.compile(regex2);
    Pattern p3 = Pattern.compile(regex3);

    Matcher m = p.matcher(note);
    Matcher m2 = p2.matcher(note);
    Matcher m3 = p3.matcher(note);

    String city = null, publisher = null;
    if (m.find()) {
      String descr = m.group(1);
      String dateModifier = m.group(2);
      String year = m.group(3);
      String endYear = m.group(4);

      if (descr != null) {
        descr = descr.trim();
        if (descr.startsWith(":")) descr = descr.substring(1).trim();
        if (descr.endsWith(",")) descr = descr.substring(0, descr.length() - 1);

        String[] parts = descr.split("[,:]");
        if (parts.length == 1) {
          // I cannot know if it is a city or the publisher
          // i.e. "1re éd. : Barcelone, 1912", "1re éd. : Universal edition"
          // TODO try to disambiguate the city
          // if (!parts[0].contains("\"")) city = parts[0].trim();
        } else {
          publisher = parts[parts.length - 1].trim();
          if (publisher.contains("in")) publisher = null;
          city = parts[parts.length - 2].trim();
        }
      }

      parseTime(year, endYear, dateModifier);

    } else if (m2.find()) {
      city = m2.group(1);
      publisher = m2.group(2);

    } else if (m3.find()) {
      city = m3.group(1);
      String year = m3.group(2);
      parseTime(year);
    }

    if ("l'auteur".equals(publisher)) // hypothesis: singular means only one author
      addActivity(f28.getComposers().get(0), "publisher");
    else addActivity(new CorporateBody(publisher), "publisher");

    if (city != null && !city.startsWith("dans ") && !city.contains("&")) {
      city = city.replaceFirst("[.,:]^", "").trim();
      E53_Place place = new E53_Place(city);
      if (place.isGeonames())
        this.addProperty(CIDOC.P7_took_place_at, place);
    }
  }

  private void parseTime(String year) {
    parseTime(year, null, null);
  }

  private void parseTime(String year, String endYear, String dateModifier) {
    if (year == null) return;

    if (dateModifier == null) dateModifier = "";
    else dateModifier = dateModifier.trim();

    TimeSpan timeSpan = new TimeSpan(year, endYear);
    if (dateModifier.equals("ca")) {
      timeSpan.setQuality(TimeSpan.Precision.UNCERTAINTY);
    } else if (dateModifier.equalsIgnoreCase("Noël")) {
      timeSpan.setStartMonth("12"); // whole december
    } else if (!dateModifier.isEmpty()) {
      Pattern p = Pattern.compile(textDateRegex);
      Matcher m = p.matcher(dateModifier.toLowerCase());
      if (m.find()) {
        String day = m.group(1);
        String month = m.group(2);

        timeSpan.setStartMonth(month);
        timeSpan.setStartDay(day);
      }
    }

    timeSpan.setUri(this.uri + "/interval");
    this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
    this.model.add(timeSpan.getModel());
  }


  public F30_PublicationEvent add(F24_PublicationExpression expression) {
    this.resource.addProperty(FRBROO.R24_created, expression.asResource());
    return this;
  }

  public F30_PublicationEvent add(F19_PublicationWork work) {
    this.resource.addProperty(FRBROO.R19_created_a_realisation_of, work.asResource());
    return this;
  }

  public static String[] getEditionPrinceps(Record record) {
    // search edition princeps in the record
    for (String edition : record.getDatafieldsByCode("600", 'a')) {
      if ((edition.contains("éd.")) || (edition.contains("édition"))) return edition.split(";");
    }
    return new String[0];
  }

  public void addActivity(Artist agent, String function) {
    if (agent == null) return;

    Resource activity = model.createResource(this.uri + "/activity/" + ++activityCount)
      .addProperty(RDF.type, CIDOC.E7_Activity)
      .addProperty(MUS.U31_had_function, function)
      .addProperty(CIDOC.P14_carried_out_by, agent.asResource());

    this.addProperty(CIDOC.P9_consists_of, activity);
    this.model.add(agent.getModel());
  }
}
