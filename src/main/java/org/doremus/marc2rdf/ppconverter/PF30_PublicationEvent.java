package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PF30_PublicationEvent extends DoremusResource {
  private static final String header2 = "(?:premi|1)[èeé]?re (?:[èeé]d(?:ition|\\.)|publication)";
  public static final String publicationHeaderRegex = "(?i)([èeé]dit(?:eur|[ée]|ion)|publié|imprimé|" + header2 + ")";
  public static final String noteRegex = "(?i)" + publicationHeaderRegex + ".+";

  private static final String noteRegex1 = "^[EÉ]dit(?:eur|ion)(?: \\(.+\\))? ?(?:: )?([^,\\n]+)(?:, ([^\\n\\d]+))?(?:, (\\d+))?$";
  private static final String noteRegex2 = "(?i)" + header2 + "s?[,.]?";
  private static final String noteRegex3 = "(?i)(?:[eé]dit|publi|imprim)[ée]{1,2}s? (.+)";

  public PF30_PublicationEvent(String note, Record record, String identifier) throws URISyntaxException {
    super(record, identifier);

    this.resource.addProperty(RDF.type, FRBROO.F30_Publication_Event);
    addNote(note);
    parseNote(note);
  }

  private void parseNote(String note) {
    // remove the dot in the end
    note = note.replaceFirst("\\.$", "");

    String publisher = null, city = null;

    TimeSpan timeSpan = null;

    Pattern p1 = Pattern.compile(noteRegex1);
    Matcher m1 = p1.matcher(note);

    Pattern p2 = Pattern.compile(noteRegex2);
    Matcher m2 = p2.matcher(note);

    Pattern p3 = Pattern.compile(noteRegex3);
    Matcher m3 = p3.matcher(note);

    // case 1: "Editeur : Ricordi"
    // "Editeur : Boosey & Hawkes, Londres, 1958",
    // "Editeur : Rouart-Lerolle (1952), puis Salabert"
    if (m1.find()) {
      publisher = m1.group(1);
      city = m1.group(2);
      String year = m1.group(3);


      String parenthesisYearRegex = "\\((\\d{4})\\)";
      Pattern pY = Pattern.compile(parenthesisYearRegex);
      Matcher mY = pY.matcher(publisher);
      if (year == null && mY.find()) {
        year = mY.group(1);
        publisher = publisher.replace(mY.group(0), "").trim();
      }

      if (year != null) timeSpan = new TimeSpan(year);

      if (city != null && Character.isLowerCase(city.charAt(0))) {
        // probably it is not a city => attach to publisher
        publisher += ", " + city;
        city = null;
      }

      // Case 2: "Première edition : Paris, Choudens, 1875."
      // "Première édition : Choudens, Paris, 1875."
      // "Première édition à Paris, Choudens en 1875."
    } else if (m2.find()) {
      boolean found = true;
      int iteration = 0;

      while (found) {
        ++iteration;
        if (iteration > 1) {
          break;
          // PRINT SOMETHING
          // TODO
        }

        String data = note.substring(m2.end());

        if (data.contains(";")) {
          // multiple first publications!
          String[] parts = data.split(" ?; ?");
          data = parts[0];
          // TODO the others
        }

        if (data.trim().matches("\\d{4}")) {
          timeSpan = new TimeSpan(data.trim());
          continue;
        }

        String dateRegex = "(?:[,:]| en )(?: vers)? ?[\\(\\[]? ?" + TimeSpan.frenchDateRegex + " ?[\\)\\]]?";
        Pattern pD = Pattern.compile(dateRegex);
        Matcher mD = pD.matcher(data);

        if (mD.find()) {
          timeSpan = new TimeSpan(mD.group(3), mD.group(2), mD.group(1));
          if (mD.group().contains(" vers ")) timeSpan.setQuality(TimeSpan.Precision.UNCERTAINTY);
        }
        Pattern pDR = Pattern.compile(TimeSpan.frenchDateRangeRegex);
        Matcher mDR = pDR.matcher(data);
        if (mDR.find()) {
          String day = mDR.group(1);
          String month = mDR.group(2);
          String year = mDR.group(3);

          String endDay = mDR.group(4);
          String endMonth = mDR.group(5);
          String endYear = mDR.group(6);

          if (year == null) year = endYear;
          timeSpan = new TimeSpan(year, month, day, endYear, endMonth, endDay);
        }

        // TODO I can not distinguish between city and publisher from here
        // need to disambiguate cities before

        found = m2.find();
      }

      // Case 3: "Publié par Le Cene à Amsterdam en 1725"
      // "édité en 1564"
      // "Imprimé pour la première fois à Paris ca 1757"
    } else if (m3.find()) {
      String data = m3.group(1);

      // extract date
      Pattern pD = Pattern.compile("(?:(en|ca|avant) )?(?:" + TimeSpan.frenchMonthRegex + " )?(\\d{4})(?:-(\\d{4}))?");
      Matcher mD = pD.matcher(data);
      if (mD.find()) {
        String modifier = mD.group(1), month = mD.group(2), year = mD.group(3), endYear = mD.group(4);
        if (modifier != null && modifier.equals("avant"))
          timeSpan = new TimeSpan(null, year);
        else timeSpan = new TimeSpan(year, endYear);
        timeSpan.setStartMonth(month);

        if (mD.group().startsWith("ca "))
          timeSpan.setQuality(TimeSpan.Precision.UNCERTAINTY);

        data = data.replace(mD.group(), "").trim().replaceFirst(" ?,$", "");
      }

      // extract city
      Pattern pC = Pattern.compile("à ([A-Z][^\\s]+)");
      Matcher mC = pC.matcher(data);
      if (mC.find()) {
        city = mC.group(1);
        data = data.replace(mC.group(), "").trim();
      }

      // extract publisher
      Pattern pP = Pattern.compile("(?:par|chez) ([^(),]+)");
      Matcher mP = pP.matcher(data);
      if (mP.find()) publisher = mP.group(1);
    }


    if (publisher != null) {
      Resource activity = model.createResource(this.uri + "/activity")
        .addProperty(RDF.type, CIDOC.E7_Activity)
        .addProperty(MUS.U35_foresees_function, model.createLiteral("editeur", "fr"))
        .addProperty(CIDOC.P14_carried_out_by, model.createResource()
          .addProperty(RDF.type, FRBROO.F11_Corporate_Body)
          .addProperty(CIDOC.P131_is_identified_by, publisher));

      this.resource.addProperty(CIDOC.P9_consists_of, activity);
    }

    if (city != null) this.resource.addProperty(CIDOC.P7_took_place_at, city);

    if (timeSpan != null) {
      timeSpan.setUri(this.uri + "/interval");

      this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
      this.model.add(timeSpan.getModel());
    }
  }

  public PF30_PublicationEvent add(PF24_PublicationExpression f24) {
    /**************************** création d'une expression de publication *************/
    this.resource.addProperty(FRBROO.R24_created, f24.asResource());
    return this;
  }

  public PF30_PublicationEvent add(PF19_PublicationWork f19) {
    /**************************** réalisation d'une work *******************************/
    this.resource.addProperty(FRBROO.R19_created_a_realisation_of, f19.asResource());
    return this;
  }

  public static String getEditionPrinceps(Record record) {
    if (!record.isType("UNI:100")) return null;

    // TODO Check if this info is also on 909
    for (String note : record.getDatafieldsByCode("919", 'a')) {
      for (String notePart : note.split(PP2RDF.dotSeparator)) {
        Pattern p = Pattern.compile(noteRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher m = p.matcher(notePart);

        if (m.find()) return notePart.trim();
      }
    }
    return null;
  }
}

