package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PF30_PublicationEvent extends DoremusResource {
  public static final String publicationHeaderRegex = "(?i)([EÉ]diteur|(?:Premi|1)[èe]re (?:[eé]dition|publication))";
  private static final String noteRegex = "(?:\\. )?(" + publicationHeaderRegex + ".+)";

  private static final String noteRegex1 = "[EÉ]diteur(?: \\(.+\\))? ?: ?([^,\\n]+)(?:, ([^\\n\\d]+))?(?:, (\\d+))?$";
  private static final String noteRegex2 = "(?i)(?:Premi|1)[èe]re (?:[eé]dition|[p]ublication)s?[,.]?";

  public PF30_PublicationEvent(String note, Record record, String identifier) throws URISyntaxException {
    super(record, identifier);

    this.resource.addProperty(RDF.type, FRBROO.F30_Publication_Event);
    this.resource.addProperty(CIDOC.P3_has_note, note);

    parseNote(note);
  }

  private void parseNote(String note) {
    // remove eventual other info
    note = note.replaceFirst("(?i)cr[ée]{1,3}(ation)? .+", "").trim();
    // remove the dot in the end
    note = note.replaceFirst("\\.$", "");

    String publisher = null, city = null,
      year = null, month = null, day = null,
      endYear = null, endMonth = null, endDay = null;

    Pattern p1 = Pattern.compile(noteRegex1);
    Matcher m1 = p1.matcher(note);

    Pattern p2 = Pattern.compile(noteRegex2);
    Matcher m2 = p2.matcher(note);

    // case 1: "Editeur : Ricordi"
    // "Editeur : Boosey & Hawkes, Londres, 1958",
    // "Editeur : Rouart-Lerolle (1952), puis Salabert"
    if (m1.find()) {
      publisher = m1.group(1);
      city = m1.group(2);
      year = m1.group(3);


      String parenthesisYearRegex = "\\((\\d{4})\\)";
      Pattern pY = Pattern.compile(parenthesisYearRegex);
      Matcher mY = pY.matcher(publisher);
      if (year == null && mY.find()) {
        year = mY.group(1);
        publisher = publisher.replace(mY.group(0), "").trim();
      }

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
//        System.out.println(data);

        if (data.contains(";")) {
          // multiple first publications!
          String[] parts = data.split(" ?; ?");
          data = parts[0];
          // TODO the others
        }

        if (data.trim().matches("\\d{4}")) {
          year = data.trim();
          continue;
        }

        String dateRegex = "(?:[,:]| en )(?: vers)? ?[\\(\\[]? ?" + TimeSpan.frenchDateRegex + " ?[\\)\\]]?";
        Pattern pD = Pattern.compile(dateRegex);
        Matcher mD = pD.matcher(data);

        if (mD.find()) {
          day = mD.group(1);
          month = mD.group(2);
          year = mD.group(3);
        }

        Pattern pDR = Pattern.compile(TimeSpan.frenchDateRangeRegex);
        Matcher mDR = pDR.matcher(data);
        if (mDR.find()) {
          day = mDR.group(1);
          month = mDR.group(2);
          year = mDR.group(3);

          endDay = mDR.group(4);
          endMonth = mDR.group(5);
          endYear = mDR.group(6);

          if(year == null) year = endYear;
        }

        // TODO I can not distinguish between city and publisher from here
        // need to disambiguate cities before

        found = m2.find();
      }

    } else {
      System.out.println(note);
    }
//    System.out.println(publisher + " | " + city + " | " + year);


    if (publisher != null) {
      Resource activity = model.createResource(this.uri + "/activity")
        .addProperty(RDF.type, CIDOC.E7_Activity)
        .addProperty(MUS.U35_foresees_function_of_type, model.createLiteral("editeur", "fr"))
        .addProperty(CIDOC.P14_carried_out_by, model.createResource()
          .addProperty(RDF.type, FRBROO.F11_Corporate_Body)
          .addProperty(CIDOC.P131_is_identified_by, publisher));

      this.resource.addProperty(CIDOC.P9_consists_of, activity);
    }

    if (city != null) this.resource.addProperty(CIDOC.P7_took_place_at, city);

    if (year != null) {
      TimeSpan timeSpan = (endYear == null) ?
        new TimeSpan(year, month, day) :
        new TimeSpan(year, month, day, endYear, endMonth, endDay);
      timeSpan.setUri(this.uri + "/time");

      this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
      this.model.add(timeSpan.getModel());
    }

//    note = note.replaceFirst(publicationHeaderRegex, "");
//    System.out.println(note);
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
    DataField field = record.getDatafieldByCode("919");
    if (field == null || !field.isCode('a')) return null;

    String note = field.getSubfield('a').getData();
    Pattern p = Pattern.compile(noteRegex);
    Matcher m = p.matcher(note);

    if (m.find()) return m.group(1);
    else return null;
  }
}
