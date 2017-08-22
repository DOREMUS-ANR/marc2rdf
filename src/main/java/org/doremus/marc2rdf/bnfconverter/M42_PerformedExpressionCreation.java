package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.*;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.vocabulary.VocabularyManager;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M42_PerformedExpressionCreation extends DoremusResource {
  private final StanfordLemmatizer slem;

  private static final String performanceRegex = "([eé]xécution|représentation) ([^:]+)?:";

  private static final String noteRegex1 = "(?i)(?:1[èe]?|Premi[èe])r?e? (?:[eé]xécution|représentation) +([^:]+ )*: (.+)";
  private static final String numericDateRegex = "(\\d{4})(?:-?([\\d\\.]{2})-?([\\d\\.]{2}))?";

  // "octobre ?, 1693 ?", "25 août, 1692 ?"
  private static final String frenchUncertainDate = "(?:le )?" + TimeSpan.frenchDayRegex + "? ?" + TimeSpan.frenchMonthRegex + "?(?: ?\\??,)? ?(\\d{4})(?: ?\\?)?";

  private boolean isPremiere;
  private F28_ExpressionCreation f28;
  private Resource M44_Performed_Work, M43_Performed_Expression, F31_Performance;
  private String place;
  private TimeSpan timeSpan;

  public M42_PerformedExpressionCreation(String note, Record record, F28_ExpressionCreation f28, int i) throws URISyntaxException {
    super(record);

    this.place = null;
    this.timeSpan = null;

    //check if it is a Premiere
    Pattern p = Pattern.compile(performanceRegex);
    Matcher m = p.matcher(note);
    m.find();
    isPremiere = m.group(2) == null;
    char flag = isPremiere ? 'p' : 'f';
    if (i > -1) flag += i;

    this.identifier = record.getIdentifier() + flag;
    regenerateResource();
    this.resource.addProperty(RDF.type, MUS.M42_Performed_Expression_Creation)
      .addProperty(CIDOC.P3_has_note, note);

    String performanceUri = ConstructURI.build("bnf", "F31_Performance", this.identifier).toString();
    String expressionUri = ConstructURI.build("bnf", "M43_PerformedExpression", this.identifier).toString();
    String workUri = ConstructURI.build("bnf", "M44_PerformedWork", this.identifier).toString();

    this.F31_Performance = model.createResource(performanceUri)
      .addProperty(RDF.type, FRBROO.F31_Performance)
      .addProperty(CIDOC.P3_has_note, note)
      .addProperty(CIDOC.P9_consists_of, this.resource);
    this.M43_Performed_Expression = model.createResource(expressionUri)
      .addProperty(RDF.type, MUS.M43_Performed_Expression);
    this.M44_Performed_Work = model.createResource(workUri)
      .addProperty(RDF.type, MUS.M44_Performed_Work)
      .addProperty(FRBROO.R9_is_realised_in, this.M43_Performed_Expression);
    this.resource.addProperty(FRBROO.R17_created, this.M43_Performed_Expression)
      .addProperty(FRBROO.R19_created_a_realisation_of, this.M44_Performed_Work);

    this.slem = Converter.stanfordLemmatizer;
    this.f28 = f28;

    parseNote(note);

    if (place != null) {
      this.resource.addProperty(CIDOC.P7_took_place_at, place);
      this.F31_Performance.addProperty(CIDOC.P7_took_place_at, place);
    }

    if (timeSpan != null) {
      timeSpan.setUri(this.uri + "/time");
      this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
      this.F31_Performance.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());

      this.model.add(timeSpan.getModel());
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
        place = modifier.substring(2).replaceFirst("\\(.+\\)", "");
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

        if (year == null && (month != null || day != null))
          timeSpan = new TimeSpan(endYear, month, day, endYear, endMonth, endDay);
        else timeSpan = new TimeSpan(endYear, endMonth, endDay);

        String[] parts = data.split(",? ?" + longDate + " ?,?");
        if (parts.length > 0) place = parts[0].trim();
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

    if (conductor != null) addRole(conductor, "conducteur");
  }

  private void addRole(String actor, String role) {
    actor = actor.trim();
    if (actor.isEmpty()) return;

    RDFNode actorRes;
    if (actor.equals("compositeur") || actor.equals("le compositeur")) {
      actorRes = f28.getComposers().get(0).asResource();
    } else actorRes = model.createLiteral(actor);

    Resource M28 = model.createResource()
      .addProperty(RDF.type, MUS.M28_Individual_Performance)
      .addProperty(CIDOC.P14_carried_out_by, actorRes);

    this.resource.addProperty(CIDOC.P9_consists_of, M28);

    if (role == null) {
      // TODO compare with the casting
      return;
    }

    role = role.trim();
    for (String r : role.split(" et ")) {
      if (r.equals("conducteur") || r.equals("direction"))
        M28.addProperty(MUS.U35_foresees_function_of_type, model.createLiteral("conducteur", "fr"));
      else {
        Resource mopMatch = VocabularyManager.searchInCategory(instrumentToSingular(r), "fr", "mop");
        if (mopMatch != null) M28.addProperty(MUS.U1_used_medium_of_performance, mopMatch);
      }
    }

  }

  private String instrumentToSingular(String r) {
    String[] parts = r.split(" ");
    if (parts.length == 1) return slem.lemmatize(parts[0]).get(0);

    // cornets à pistons --> cornet à pistons
    parts[0] = slem.lemmatize(parts[0]).get(0);
    return String.join(" ", parts);
  }

  public Resource getExpression() {
    return M43_Performed_Expression;
  }

  public Resource getWork() {
    return M44_Performed_Work;
  }

  public M42_PerformedExpressionCreation add(F25_PerformancePlan plan) {
    /**************************** exécution du plan ******************************************/
    this.F31_Performance.addProperty(FRBROO.R25_performed, plan.asResource());
    return this;
  }

  public static List<String> getPerformances(Record record) {
    List<String> notes = new ArrayList<>();
    Pattern p = Pattern.compile(performanceRegex);

    for (String note : record.getDatafieldsByCode("600", 'a')) {
      Matcher m = p.matcher(note);
      if (m.find()) notes.add(note);
    }
    return notes;
  }

  public boolean isPremiere() {
    return isPremiere;
  }
}
