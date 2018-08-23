package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.E53_Place;
import org.doremus.marc2rdf.main.TimeSpan;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class M42_PerformedExpressionCreation extends DoremusResource {

  private static final String performanceRegex = "([eé]xécution|représentation) ([^:]+)?:";
  public static final Pattern performancePattern = Pattern.compile(performanceRegex);

  private static final String noteRegex1 = "(?i)(?:1[èe]?|Premi[èe])r?e? (?:[eé]xécution|représentation) +([^:]+ )*: (.+)";
  private static final String numericDateRegex = "(\\d{4})(?:-?([\\d.]{2})-?([\\d.]{2}))?";

  // "octobre ?, 1693 ?", "25 août, 1692 ?"
  private static final String frenchUncertainDate = "(?:le )?" + TimeSpan.frenchDayRegex + "? ?" + TimeSpan.frenchMonthRegex + "?(?: ?\\??,)? ?(\\d{4})(?: ?\\?)?";

  private boolean isPremiere;
  private F28_ExpressionCreation f28;
  private M43_PerformedExpression expression;
  private M44_PerformedWork work;
  private Resource F31_Performance;
  private E53_Place place;
  private TimeSpan timeSpan;
  private int countConsistOf;

  public M42_PerformedExpressionCreation(String note, Record record, F28_ExpressionCreation f28, int i) {
    super(record);

    this.place = null;
    this.timeSpan = null;
    this.countConsistOf = 0;

    //check if it is a Premiere
    Matcher m = performancePattern.matcher(note);
    m.find();
    isPremiere = m.group(2) == null;
    char flag = isPremiere ? 'p' : 'f';
    if (i > -1) flag += i;

    this.identifier = record.getIdentifier() + flag;
    regenerateResource();


    this.setClass(MUS.M42_Performed_Expression_Creation);
    addNote(note);

    String performanceUri = null;
    try {
      performanceUri = ConstructURI.build("bnf", "F31_Performance", this.identifier).toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    this.F31_Performance = model.createResource(performanceUri)
      .addProperty(RDF.type, FRBROO.F31_Performance)
      .addProperty(CIDOC.P3_has_note, note)
      .addProperty(RDFS.comment, note)
      .addProperty(CIDOC.P9_consists_of, this.resource);

    this.expression = new M43_PerformedExpression(this.identifier);
    f28.expression.titles.forEach(title -> this.expression.addProperty(RDFS.label, title));

    this.work = new M44_PerformedWork(this.identifier);
    this.work.add(this.expression);

    this.addProperty(FRBROO.R17_created, this.expression)
      .addProperty(FRBROO.R19_created_a_realisation_of, this.work);

    this.f28 = f28;

    parseNote(note);

    if (place != null) {
      this.addProperty(CIDOC.P7_took_place_at, place);
      this.F31_Performance.addProperty(CIDOC.P7_took_place_at, place.asResource());
    }

    if (timeSpan != null) {
      timeSpan.setUri(this.F31_Performance.getURI() + "/interval");
      this.addTimeSpan(timeSpan);
      this.F31_Performance.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
    }
  }

  private void parseNote(String note) {
    // to complex case: 1re exécution : Festival de Bordeaux, 8 juin 1969, par Jean Guillou, puis Zwolle (Pays-Bas), 18 juin 1969, par Charles de Wolff
    note = note.split(", puis")[0];

    Pattern p1 = Pattern.compile(noteRegex1);
    Matcher m1 = p1.matcher(note);

    String conductor = null;

    if (m1.find()) {
      String modifier = m1.group(1);
      String data = m1.group(2);

      String longDate = "(?:" + frenchUncertainDate + "?-)?" + frenchUncertainDate;
      Pattern pDR = Pattern.compile(longDate);
      Matcher mDR = pDR.matcher(data);

      if (modifier != null) modifier = modifier.trim();
      if (modifier != null && modifier.startsWith("à ") && !modifier.startsWith("à l'occasion")) {
        String pl = modifier.substring(2).replaceFirst("\\(.+\\)", "");
        place = new E53_Place(pl);
        if (!place.isGeonames()) place = null;
      }

      if (data.matches(numericDateRegex)) {
        Pattern pD = Pattern.compile(numericDateRegex);
        Matcher mD = pD.matcher(data);
        mD.find();

        timeSpan = new TimeSpan(mD.group(1), mD.group(2), mD.group(3));
      } else if (mDR.find()) {
        String day = mDR.group(1),
          month = mDR.group(2),
          year = mDR.group(3),

          endDay = mDR.group(4),
          endMonth = mDR.group(5),
          endYear = mDR.group(6);

        if (year == null) {
          if (month != null || day != null)
            timeSpan = new TimeSpan(endYear, month, day, endYear, endMonth, endDay);
          else timeSpan = new TimeSpan(endYear, endMonth, endDay);
        } else timeSpan = new TimeSpan(year, month, day, endYear, endMonth, endDay);


        if (mDR.group().contains("?")) {
          if (timeSpan.hasEndYear()) {
            String[] parts = mDR.group().split("-");
            if (parts[0].contains("?"))
              timeSpan.setStartQuality(TimeSpan.Precision.UNCERTAINTY);
            if (parts[1].contains("?"))
              timeSpan.setEndQuality(TimeSpan.Precision.UNCERTAINTY);
          } else {
            // only start date
            timeSpan.setStartQuality(TimeSpan.Precision.UNCERTAINTY);
          }
        }

        String[] parts = data.split(",? ?" + longDate + " ?,?");
        if (parts.length > 0) {
          place = new E53_Place(parts[0].trim());
          if (!place.isGeonames()) place = null;
        }
        String post = parts.length > 1 ? parts[1].trim() : "";

        Pattern pC = Pattern.compile("(?:sous la dir(?:\\.|ection) d['eu] ?|dir\\. : )([^)]+)");
        Matcher mC = pC.matcher(post);
        if (mC.find()) {
          conductor = mC.group(1);

          post = post.replace(mC.group(0), "");
        }
        if (post.startsWith("par ")) {
          Pattern pI = Pattern.compile("([\\p{L} \\-.']+)(?:\\(([^)]+)\\))?");
          Matcher mI = pI.matcher(post.substring(4));

          while (mI.find()) {
            String interpreter = mI.group(1),
              role = mI.group(2);
            for (String intpt : interpreter.split("(,| et) ")) {
              // groups starts normally with lowercase, i.e. les Wiener Sängerknaben
              if (intpt.matches("^[a-z].+")) addRole(intpt, null);
              else addRole(intpt, role);
            }
          }
        }

      } else System.out.println("Not parsed note on record " + record.getIdentifier() + " : " + data);
    }

    if (conductor != null) addRole(conductor, "conductor");
  }

  private void addRole(String actor, String role) {
    actor = actor.trim();
    if (actor.isEmpty()) return;

    RDFNode actorRes;
    if (actor.equals("compositeur") || actor.equals("le compositeur")) {
      actorRes = f28.getComposers().get(0).asResource();
    } else actorRes = model.createLiteral(actor);

    Resource M28 = model.createResource(this.uri + "/" + ++countConsistOf)
      .addProperty(RDF.type, MUS.M28_Individual_Performance)
      .addProperty(CIDOC.P14_carried_out_by, actorRes);

    this.resource.addProperty(CIDOC.P9_consists_of, M28);

    if (role == null) {
      // TODO compare with the casting
      return;
    }

    role = role.trim();
    for (String r : role.split(" et ")) {
      if (r.equals("conductor") || r.equals("direction"))
        M28.addProperty(MUS.U31_had_function, model.createLiteral("conductor", "en"));
      else {
        Resource mopMatch = VocabularyManager.searchInCategory(r, "fr", "mop", true);
        if (mopMatch != null) M28.addProperty(MUS.U1_used_medium_of_performance, mopMatch);
      }
    }

  }

  public M43_PerformedExpression getExpression() {
    return expression;
  }

  public M44_PerformedWork getWork() {
    return work;
  }

  public M42_PerformedExpressionCreation add(F25_PerformancePlan plan) {
    this.F31_Performance.addProperty(FRBROO.R25_performed, plan.asResource());
    return this;
  }

  public M42_PerformedExpressionCreation add(F22_SelfContainedExpression f22) {
    this.expression.addProperty(MUS.U54_is_performed_expression_of, f22);
    this.F31_Performance.addProperty(FRBROO.R66_included_performed_version_of, f22.asResource());
    return this;
  }

  public static List<String> getPerformances(Record record) {
    return record.getDatafieldsByCode("600", 'a').stream()
      .filter(note -> performancePattern.matcher(note).find())
      .collect(Collectors.toList());
  }

  public boolean isPremiere() {
    return isPremiere;
  }

  public Resource getMainPerformance() {
    return this.F31_Performance;
  }
}
