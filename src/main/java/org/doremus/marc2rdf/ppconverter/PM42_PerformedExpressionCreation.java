package org.doremus.marc2rdf.ppconverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
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


public class PM42_PerformedExpressionCreation extends DoremusResource {
  private static final String frenchCreationRegex = "(Cr\u00e9ation fran\u00e7aise.+)";
  private static final String noPremiereRegex = frenchCreationRegex + "|(enregistrement (de )?jazz)|(1re reprise)";
  private static final String frenchUncertainDate = "(?:le |en )?(?:" + TimeSpan.frenchDayRegex + "? ?" + TimeSpan.frenchMonthRegex + "? )?(?:\\(\\?\\) )?(\\d{4})(?: ?\\?)?";
  private static final String conductorRegex = "(?:(?:placé )?(?:sous|par) la dir(?:\\.|ection) d[e'u]|dirigé par|direction(?: :)?) ?((?:[A-Z]\\.)?[\\p{L} \\-']+)";
  private static final String interpreterRegex = "(?<!(?:organis|rapport)é )par (.+)";
  private static final String dedicataireRegex = "\\(?(?:l[ea]s? |son |sa )?(?:dédica|commandi)taires?(?: de l'oeuvre)?\\)?";
  private static final String composerRegex = "(le )?compositeur|l'auteur";
  private static final String noUppercase = "[^A-Z]+";

  private static final String performanceRegex = "(?i)(?:(?:premi[èe]|1[èe]?)re? (?:représentation|[ée]x[ée]cution|audition|enregistrement|reprise) )|(?:cr[èée]{1,3}(?:ation|s)? )";

  private final StanfordLemmatizer slem;
  private final PF28_ExpressionCreation f28;

  private boolean isPremiere;
  private Resource M44_Performed_Work, M43_Performed_Expression, F31_Performance;
  private String place;
  private TimeSpan timeSpan;

  private int countConsistOf;

  public PM42_PerformedExpressionCreation(String note, Record record, String identifier, int i, PF28_ExpressionCreation f28) throws URISyntaxException {
    super(record, identifier);

    this.place = null;
    this.timeSpan = null;

    this.slem = Converter.stanfordLemmatizer;
    this.f28 = f28;

    this.countConsistOf = 0;

    //check if it is a Premiere
    Pattern p = Pattern.compile(noPremiereRegex);
    Matcher m = p.matcher(note);
    isPremiere = !m.find();
    char flag = isPremiere ? 'p' : 'f';

    this.identifier += flag;
    if (i >= 0)
      this.identifier += i;

    this.uri = ConstructURI.build(this.sourceDb, this.className, this.identifier);

    this.resource = model.createResource(this.uri.toString())
      .addProperty(RDF.type, MUS.M42_Performed_Expression_Creation)
      .addProperty(RDFS.comment, note)
      .addProperty(CIDOC.P3_has_note, note);

    String performanceUri = ConstructURI.build("pp", "F31_Performance", this.identifier).toString();
    String expressionUri = ConstructURI.build("pp", "M43_PerformedExpression", this.identifier).toString();
    String workUri = ConstructURI.build("pp", "M44_PerformedWork", this.identifier).toString();

    this.F31_Performance = model.createResource(performanceUri)
      .addProperty(RDF.type, FRBROO.F31_Performance)
      .addProperty(RDFS.comment, note)
      .addProperty(CIDOC.P3_has_note, note)
      .addProperty(CIDOC.P9_consists_of, this.resource);
    this.M43_Performed_Expression = model.createResource(expressionUri)
      .addProperty(RDF.type, MUS.M43_Performed_Expression);
    this.M44_Performed_Work = model.createResource(workUri)
      .addProperty(RDF.type, MUS.M44_Performed_Work)
      .addProperty(FRBROO.R9_is_realised_in, this.M43_Performed_Expression);
    this.resource.addProperty(FRBROO.R17_created, this.M43_Performed_Expression)
      .addProperty(FRBROO.R19_created_a_realisation_of, this.M44_Performed_Work);

    parseNote(note);

    if (place != null) {
      this.resource.addProperty(CIDOC.P7_took_place_at, place);
      this.F31_Performance.addProperty(CIDOC.P7_took_place_at, place);
    }

    if (timeSpan != null) {
      timeSpan.setUri(this.F31_Performance.getURI() + "/interval");
      this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
      this.F31_Performance.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());

      this.model.add(timeSpan.getModel());
    }

  }

  private void parseNote(String note) {
    note = note.split("(;|((,| |, )(puis|parce) ))")[0].trim();
    note = note.replaceAll(dedicataireRegex, "");

    // extract dates
    Pattern p = Pattern.compile(frenchUncertainDate);
    Matcher m = p.matcher(note);
    if (m.find()) {
      timeSpan = new TimeSpan(m.group(3), m.group(2), m.group(1));
      if (m.group().contains("?"))
        timeSpan.setQuality(TimeSpan.Precision.UNCERTAINTY);
      note = cleanString(note, m.group());
    }

    // extract conductor
    p = Pattern.compile(conductorRegex);
    m = p.matcher(note);
    if (m.find()) {
      String conductorString = m.group(1);
      boolean first = true;
      for (String conductor : conductorString.split(" (et|avec) ")) {
        String mop = null;
        if (conductor.contains(" au ") || conductor.contains(" à ")) {
          // i.e. "Leonard Bernstein avec Philippe Entremont au piano"
          String[] parts = conductor.split(" (au|à) (la)?");
          conductor = parts[0];
          if (!first) mop = parts[1];
        }
        Role r = makeRole(conductor, (mop == null) ? "conducteur" : mop);
        this.resource.addProperty(CIDOC.P9_consists_of, r.toM28IndividualPerformance(this.uri + "/" + ++countConsistOf));
        first = false;
      }
      note = cleanString(note, m.group());
    }

    // extract interpreters
    List<Role> roles = new ArrayList<>();
    Pattern mopIntPattern = Pattern.compile("(.+) \\((.+)\\)");
    p = Pattern.compile(interpreterRegex);
    m = p.matcher(note);
    if (m.find()) {
      String interpreterString = m.group(1);

      // unbalanced parenthesis
      if (StringUtils.countMatches(interpreterString, "(") != StringUtils.countMatches(interpreterString, ")"))
        interpreterString = "";        // too complex

      // "ne fut pas créé par lui mais par Ilona Kurz-Stepanova"
      if (interpreterString.contains(" par "))
        interpreterString = "";        // too complex

      if (interpreterString.contains(" dont "))
        interpreterString = interpreterString.split(" dont ")[0];

      interpreterString = interpreterString.replaceFirst(", à .+", "").replaceFirst(",? dans .+", "").trim();

      String interpreter = "";
      RDFNode mopMatch;

      for (String originalInterpreter : interpreterString.split("([,/]| et | avec )")) {
        String newInfo = originalInterpreter.replaceFirst("^(et|avec) ", "").trim();
        String mop;
        if (newInfo.isEmpty()) {
          interpreter = "";
          continue;
        }
        interpreter += newInfo;

        if (StringUtils.countMatches(interpreter, "(") > StringUtils.countMatches(interpreter, ")")) {
          interpreter += ", ";
          continue; // wait for close parenthesis
        }

        mopMatch = VocabularyManager.searchInCategory(newInfo, "fr", "mop");
        if (mopMatch != null && !roles.isEmpty()) {
          // i should add this info to the previous one
          roles.get(roles.size() - 1).setFunction(mopMatch);
          interpreter = "";
          continue;
        }

        if (!interpreter.contains("compositeur") && !interpreter.contains("auteur")
          && interpreter.matches(noUppercase)) {
          interpreter = "";
          continue;
        }

        if (interpreter.startsWith("lors") || interpreter.equals("Londres"))
          break; // workaround

        if (interpreter.contains(" au ") || interpreter.contains(" à ")) {
          // i.e. "Philippe Entremont au piano"
          String[] parts = interpreter.split(" (au|à) (la)?");
          mop = parts[1];

          if (mop != null) {
            mop = mop.replaceAll("\\(.+\\)", "").trim();
            // i take the mop only if it is in the vocabulary
            mopMatch = VocabularyManager.searchInCategory(mop, "fr", "mop");
            if (mopMatch != null) interpreter = parts[0];
          }
        }

        if (mopMatch == null && interpreter.contains("(")) {
          Matcher mopIntMatcher = mopIntPattern.matcher(interpreter);
          if (mopIntMatcher.find()) {
            interpreter = mopIntMatcher.group(1);
            mop = mopIntMatcher.group(2);
            mopMatch = VocabularyManager.searchInCategory(mop, "fr", "mop");
          }
        }


        roles.add(makeRole(interpreter, mopMatch));
        interpreter = "";
      }

      for (Role r : roles)
        this.resource.addProperty(CIDOC.P9_consists_of, r.toM28IndividualPerformance(this.uri + "/" + ++countConsistOf));

      note = cleanString(note, m.group());
    }

    // extract places
    Pattern cityPattern = Pattern.compile("(?<!\\w)à ([\\p{L}- ]+(\\([\\p{L}- ]+\\))?)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    Matcher cityMatcher = cityPattern.matcher(note);
    if (cityMatcher.find()) {
      String city = cityMatcher.group(1);
      if (!city.matches(noUppercase)) this.place = city;
    }

  }

  private String instrumentToSingular(String r) {
    String[] parts = r.split(" ");
    if (parts.length == 1) return slem.lemmatize(parts[0]).get(0);

    // cornets à pistons --> cornet à pistons
    parts[0] = slem.lemmatize(parts[0]).get(0);
    return String.join(" ", parts);
  }


  private static String cleanString(String note, String group) {
    if (note == null) return null;
    if (group == null || group.isEmpty()) return note;
    note = note.replace(group, "").replaceAll("[(\\[][)\\]]", "").trim();
    note = note.replaceFirst(" ?,$", "").replaceFirst(", (,\\)\\])", "$1");
    return note;
  }

  private Role makeRole(String actor, RDFNode role) {
    actor = actor.trim();
    if (actor.matches(composerRegex))
      return new Role(f28.getComposers().get(0).asResource(), role, this.model);
    return new Role(actor, role, this.model);
  }

  private Role makeRole(String actor, String role) {
    if (role == null) return makeRole(actor, (String) null);
    role = role.trim();
    actor = actor.trim();

    if (!role.equals("conducteur")) role = instrumentToSingular(role);
    if (actor.matches(composerRegex))
      return new Role(f28.getComposers().get(0).asResource(), role, this.model);

    return new Role(actor, role, this.model);
  }


  public static List<String> getPerformances(Record record) {
    if (!record.isType("UNI:100")) return new ArrayList<>();

    List<String> results = new ArrayList<>();
    Pattern pubPattern = Pattern.compile(PF30_PublicationEvent.noteRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    Pattern perfPattern = Pattern.compile(performanceRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    Pattern datePattern = Pattern.compile(TimeSpan.frenchDateRegex);

    for (String note : record.getDatafieldsByCode("919", 'a')) {
      String[] noteParts = note.split(PP2RDF.dotSeparator);
      for (String notePart : noteParts) {
        notePart = notePart.trim();
        if (notePart.isEmpty()) continue;

        Matcher m = perfPattern.matcher(notePart);
        if (m.find()) {
          results.add(notePart.trim());
          continue;
        }

        // sometimes there is an unique note without header
        // i.e. "Londres, 10 juin 1921, sous la direction de Serge Koussevitski"
        if (noteParts.length == 1 && !notePart.contains("composition") && !pubPattern.matcher(notePart).find() && datePattern.matcher(notePart).find()) {
          results.add(notePart.trim());
        }
      }
    }
    return results;
  }

  public Resource getExpression() {
    return M43_Performed_Expression;
  }

  public Resource getWork() {
    return M44_Performed_Work;
  }

  public PM42_PerformedExpressionCreation add(PF25_PerformancePlan f25) {
    this.F31_Performance.addProperty(FRBROO.R25_performed, f25.asResource());
    return this;
  }

  public PM42_PerformedExpressionCreation add(PF22_SelfContainedExpression f22) {
    this.M43_Performed_Expression.addProperty(MUS.U54_is_performed_expression_of, f22.asResource());
    this.F31_Performance.addProperty(FRBROO.R66_included_performed_version_of, f22.asResource());
    return this;
  }


  public boolean isPremiere() {
    return isPremiere;
  }

  public Resource getMainPerformance() {
    return this.F31_Performance;
  }

}
