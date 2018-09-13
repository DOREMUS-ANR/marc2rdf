package org.doremus.marc2rdf.bnfconverter;

import org.doremus.marc2rdf.main.*;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.marcparser.Subfield;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class F30_PublicationEvent extends DoremusResource {
  private static final String UNKNOW_REGEX = "(?i)\\[?s\\. ?[lnd]\\.]?";
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

  public F30_PublicationEvent(Record record) {
    super(record);
    this.setClass(FRBROO.F30_Publication_Event);

    if (record.isBIB()) parseBIB();
  }

  private void parseBIB() {
    // publishers
    List<Artist> publishers = record.getDatafieldsByCode(720).stream()
      .map(Person::fromIntermarcField).filter(Objects::nonNull)
      .collect(Collectors.toList());
    publishers.addAll(record.getDatafieldsByCode(730).stream()
      .map(CorporateBody::fromIntermarcField).filter(Objects::nonNull)
      .collect(Collectors.toList()));

    // publishers
    record.getDatafieldsByCode(260).forEach(df -> parsePublisherField(df, publishers, true));

    publishers.forEach(x -> this.addActivity(x, x.getFunction() == null ? "publisher" : x.getFunction()));

    // time
    this.addTimeSpan(parseDateMachine());
  }

  private TimeSpan parseDateMachine() {
    String data = record.getControlfieldByCode("008").getData();
    char code = data.charAt(6);
    String year = data.substring(8, 12);
    System.out.println(year);

    switch (code) {
      case 's':
        return new TimeSpan(year);
      case 'm':
      case 'q':
        String endYear = data.substring(13, 17);
        return new TimeSpan(year, endYear);
    }

    return null;
  }

  public static List<String> parsePublisherField(DataField df, boolean asPublisher) {
    return parsePublisherField(df, new ArrayList<>(), asPublisher);
  }

  private static List<String> parsePublisherField(DataField df, List<Artist> publishers, boolean asPublisher) {
    List<String> pubNotes = new ArrayList<>();
    String current = null;
    char previous = 'z';

    String place = null;
    boolean found = false;

    for (Subfield s : df.getSubfields()) {
      String value = s.getData();
      switch (s.getCode()) {
        case 'a':
          if (value.matches(UNKNOW_REGEX)) break;
          place = value;
          if (current != null) pubNotes.add(current);
          current = value;
          break;
        case 'b':
          if (previous == 'a') current += " (" + value + ")";
          break;
        case 'c':
          boolean ok = value.matches("\\[?distrib.+") ^ asPublisher; // THIS IS A XOR
          if (!ok || value.matches(UNKNOW_REGEX)) {
            place = null;
            break;
          }

          if (previous >= 'c') pubNotes.add(current);
          else if (current == null) current = value;
          else current += " : " + value;

          found = true;

          Artist cb = publishers.stream()
            .filter(x -> x.hasName(value))
            .findFirst().orElse(new CorporateBody(value));

          cb.addResidence(place);
          place = null;
          publishers.add(cb);
          break;
        case 'd':
          if (!found || value.matches(UNKNOW_REGEX)) break;
          Matcher m = TimeSpan.YEAR_PATTERN.matcher(value);
          if (!m.find()) continue;
          current += ", " + m.group(0);
      }
      previous = s.getCode();
    }
    if (current != null) pubNotes.add(current);
    return pubNotes;
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
          // I set it as city, it will be ignored if Geonames fails
          city = parts[0];
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

    if (publisher != null)
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

    this.addTimeSpan(timeSpan);
  }


  public F30_PublicationEvent add(F24_PublicationExpression expression) {
    this.addProperty(FRBROO.R24_created, expression);
    return this;
  }

  public F30_PublicationEvent add(F19_PublicationWork work) {
    this.addProperty(FRBROO.R19_created_a_realisation_of, work);
    return this;
  }

  public static String[] getEditionPrinceps(Record record) {
    // search edition princeps in the record
    return record.getDatafieldsByCode("600", 'a').stream()
      .filter(edition -> (edition.contains("éd.")) || (edition.contains("édition")))
      .findFirst().map(edition -> edition.split(";")).orElse(new String[0]);
  }
}
