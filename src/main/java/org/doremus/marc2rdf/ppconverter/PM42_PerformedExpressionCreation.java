package org.doremus.marc2rdf.ppconverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.*;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.marcparser.Subfield;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class PM42_PerformedExpressionCreation extends DoremusResource {
  private static final String frenchCreationRegex = "(?i)(cr\u00e9ation fran\u00e7aise.*)";
  private static final String premiereRegex = "(?i)cr[eéè]ation mondiale";
  private static final String noPremiereRegex = frenchCreationRegex + "|(enregistrement (de )?jazz)|(1re reprise)";
  private static final String frenchUncertainDate = "(?:le |en )?(?:" + TimeSpan.frenchDayRegex + "? ?" + TimeSpan.frenchMonthRegex + "? )?(?:\\(\\?\\) )?(\\d{4})(?: ?\\?)?";
  private static final String conductorRegex = "(?:(?:placé )?(?:sous|par) la dir(?:\\.|ection) d[e'u]|dirigé par|direction(?: :)?) ?((?:[A-Z]\\.)?[\\p{L} \\-']+)";
  private static final String interpreterRegex = "(?<!(?:organis|rapport)é )par (.+)";
  private static final String dedicataireRegex = "\\(?(?:l[ea]s? |son |sa )?(?:dédica|commandi)taires?(?: de l'oeuvre)?\\)?";
  private static final String composerRegex = "(le )?compositeur|l'auteur";
  private static final String noUppercase = "[^A-Z]+";

  private static final String performanceRegex = "(?i)(?:(?:premi[èe]|1[èe]?)re? (?:représentation|[ée]x[ée]cution|audition|enregistrement|reprise) )|(?:cr[èée]{1,3}(?:ation|s)? )";

  private static final Resource INSTRUMENT = VocabularyManager.searchInCategory("instrument", "fr", "mop");
  private static final Resource VOICE = VocabularyManager.searchInCategory("voix", "fr", "mop");

  private PF28_ExpressionCreation f28;

  private boolean isPremiere;
  private PM44_PerformedWork M44_Performed_Work;
  private PM43_PerformedExpression M43_Performed_Expression;
  private Resource F31_Performance;
  private String place;
  private TimeSpan timeSpan;

  private int countConsistOf;
  private boolean hasWorkLinked = false;

  public PM42_PerformedExpressionCreation(Record record) throws URISyntaxException {
    super(record);
    this.resource.addProperty(RDF.type, MUS.M42_Performed_Expression_Creation);

    this.countConsistOf = 0;

    this.M43_Performed_Expression = new PM43_PerformedExpression(record);
    this.M44_Performed_Work = new PM44_PerformedWork(this.identifier);
    this.connectTriplet();

    String premiereNote = searchInNote(premiereRegex);
    isPremiere = premiereNote != null;

    if (premiereNote != null) this.addNote(premiereNote);

    String frenchPremiereNote = searchInNote(premiereRegex);
    if (frenchPremiereNote != null) this.addNote(frenchPremiereNote);

    timeSpan = getDate();
    if (timeSpan != null) {
      timeSpan.setUri(this.uri + "/interval");
      this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
      this.model.add(timeSpan.getModel());
    }

    StringJoiner sj = new StringJoiner("; ");
    record.getDatafieldsByCode(200, 'g').forEach(sj::add);
    this.resource.addProperty(MUS.U205_has_cast_detail, sj.toString());

    Pattern p = Pattern.compile("(?i)musée de la Musique");
    for (String n : record.getDatafieldsByCode(300, 'a')) {
      if (p.matcher(n).find())
        this.resource.addProperty(MUS.U193_used_historical_instruments, n);
    }

    String commandNote = searchInNote("commande de la Philharmonie de Paris");
    if (commandNote != null) this.addCommand(PP2RDF.organizationURI);
    commandNote = searchInNote("commande de l'Ensemble intercontemporain");
    if (commandNote != null) this.addCommand(getEnsambleIntercontemporainUri());

    for (String workId : record.getDatafieldsByCode(419, '3'))
      linkWorkById(workId);

    boolean shouldICreateAF22 = false;
    if (!this.hasWorkLinked) {
      // TODO Si 701$4 a une valeur autre que 230 et que cette valeur a pour contexte F28
      // dans le référentiel des fonctions, alors toujours créer un nouveau F22.
      // Puis, indiquer en M31 la valeur du référentiel qui correspond.

      // TODO Si 701$4=750 (qui signifie "autres"), chercher à identifier le rôle de la personne en analysant le
      // contenu de 200$g (selon la même méthode que pour identifier les MOP).
      // Si le rôle qui se rattache à la personne contient les mots :
      //- lumières, lui donner la fonction "éclairagiste"
      //- costumes, lui donner la fonction "costumier"
      List<DataField> activities = record.getDatafieldsByCode(701);
      activities.addAll(record.getDatafieldsByCode(700));

      shouldICreateAF22 = activities.stream()
        .filter(a -> "230".equals(a.getString(4)))
        .toArray().length > 0;
    }

    if (shouldICreateAF22) {
      PF28_ExpressionCreation f28 = new PF28_ExpressionCreation(record);
      PF22_SelfContainedExpression f22 = new PF22_SelfContainedExpression(record);
      PF14_IndividualWork f14 = new PF14_IndividualWork(record.getIdentifier());

      f28.add(f22).add(f14);
      f14.add(f22);
      this.add(f22);
      model.add(f22.getModel()).add(f28.getModel()).add(f14.getModel());
    }

    List<DataField> artistFields = record.getDatafieldsByCode(702);
    artistFields.addAll(record.getDatafieldsByCode(712));

    for (DataField df : artistFields) {
      Artist artist = df.getEtiq().equals("712") ?
        CorporateBody.fromUnimarcField(df) :
        Person.fromUnimarcField(df);

      Resource genericMop = null;
      List<String> functTypes = df.getSubfields('4').stream()
        .map(Subfield::getData)
        .collect(Collectors.toList());

      boolean isPrincipal = df.hasSubfieldValue('4', "040");
      for (String functionType : functTypes)
        switch (functionType) { // function
          case "040": // principal artist, managed before
            break;
          // FUNCTIONS WITH MoPs
          case "545": // musician
            genericMop = INSTRUMENT;
          case "721": // singer
            if (genericMop == null) genericMop = VOICE;

            String txt = searchArtistInNote(artist);
            String originalTxt = txt;
            if (txt == null) txt = "";

            String operaRole = null;
            if (txt.contains("(")) { // Laurent Laberdesque, baryton (Figaro, valet du Comte Almaviva)
              int start = txt.indexOf("("),
                end = txt.indexOf(")");

              operaRole = txt.substring(start + 1, end);
              txt = txt.substring(0, start);
            }
            List<String> parts = new LinkedList<>(Arrays.asList(txt.split(",")));
            // workaround if there is no ","
            if (parts.size() < 2) parts.add(".");

            for (int i = 1; i < parts.size(); i++) {
              String pt = parts.get(i).trim();
              if (pt.equals("composition") || pt.equals("direction")) continue;
              if (Character.isUpperCase(pt.codePointAt(0))) {
                System.out.println("Considered not a mop/function: " + pt);
                continue;
              }
              Resource mop = VocabularyManager.searchInCategory(pt, "fr", "mop");

              PM28_Individual_Performance ip = new PM28_Individual_Performance(this.uri, ++countConsistOf);
              ip.setMop(mop != null ? mop : genericMop);
              if (mop == null && !pt.equals(".")) System.out.println("Mop not found: " + pt);

              ip.addNote(originalTxt);
              ip.setActor(artist);
              ip.setCharacter(operaRole);
              if (isPrincipal) ip.setAsPrincipal();

              this.resource.addProperty(CIDOC.P9_consists_of, ip.asResource());
              this.model.add(ip.getModel()).add(artist.getModel());
            }
            break;
          // FUNCTIONS without MoPs
          case "195": // chef de chœur
          case "250": // chef d'orchestre
          case "274": // danseur
          case "303": // disc jockey
          case "550": // narrateur
          case "590": // interprète
          case "780": // acteur / exécutant
          case "800": // intervenant / présentateur
            PM28_Individual_Performance ip = new PM28_Individual_Performance(this.uri, ++countConsistOf);
            ip.setActor(artist);
            ip.setFunctionByCode(functionType);
            if (isPrincipal) ip.setAsPrincipal();
            this.resource.addProperty(CIDOC.P9_consists_of, ip.asResource());
            this.model.add(ip.getModel());
        }
    }
  }

  private String searchArtistInNote(Artist artist) {
    List<String> fields = record.getDatafieldsByCode(200, 'f');
    fields.addAll(record.getDatafieldsByCode(200, 'g'));
    for (String note : fields)
      if (note.contains(artist.getFullName())) return note;
    return null;
  }

  private String searchInNote(String regex) {
    Pattern p = Pattern.compile(regex);
    List<String> fields = record.getDatafieldsByCode(200, 'a');
    fields.addAll(record.getDatafieldsByCode(200, 'e'));

    for (String note : fields)
      if (p.matcher(note).find()) return note;
    return null;
  }

  private void addCommand(String commander_uri) {
    Resource activity = model.createResource(this.uri + "/activity/1")
      .addProperty(RDF.type, CIDOC.E7_Activity)
      .addProperty(CIDOC.P14_carried_out_by, model.createResource(commander_uri))
      .addProperty(MUS.U31_had_function, "commanditaire", "fr");
    this.resource.addProperty(CIDOC.P9_consists_of, activity);
  }

  public PM42_PerformedExpressionCreation(String note, String identifier, int i, PF28_ExpressionCreation f28) throws URISyntaxException {
    // Parse from a note of TUM
    super(identifier);

    this.place = null;
    this.timeSpan = null;

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

    this.F31_Performance = model.createResource(performanceUri)
      .addProperty(RDF.type, FRBROO.F31_Performance)
      .addProperty(RDFS.comment, note)
      .addProperty(CIDOC.P3_has_note, note)
      .addProperty(CIDOC.P9_consists_of, this.resource);
    this.M43_Performed_Expression = new PM43_PerformedExpression(this.identifier);
    this.M44_Performed_Work = new PM44_PerformedWork(this.identifier);
    this.connectTriplet();

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

  private void connectTriplet() {
    this.M44_Performed_Work.asResource()
      .addProperty(FRBROO.R9_is_realised_in, this.M43_Performed_Expression.asResource());
    this.resource.addProperty(FRBROO.R17_created, this.M43_Performed_Expression.asResource())
      .addProperty(FRBROO.R19_created_a_realisation_of, this.M44_Performed_Work.asResource());

    this.model.add(M43_Performed_Expression.getModel())
      .add(M44_Performed_Work.getModel());
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
    StanfordLemmatizer slem = Converter.stanfordLemmatizer;
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
    return M43_Performed_Expression.asResource();
  }

  public Resource getWork() {
    return M44_Performed_Work.asResource();
  }

  public PM42_PerformedExpressionCreation add(PF25_PerformancePlan f25) {
    this.F31_Performance.addProperty(FRBROO.R25_performed, f25.asResource());
    return this;
  }

  public PM42_PerformedExpressionCreation add(PF22_SelfContainedExpression f22) {
    this.M43_Performed_Expression.asResource()
      .addProperty(MUS.U54_is_performed_expression_of, f22.asResource());
    if (this.F31_Performance != null)
      this.F31_Performance.asResource()
        .addProperty(FRBROO.R66_included_performed_version_of, f22.asResource());
    return this;
  }


  public boolean isPremiere() {
    return isPremiere;
  }

  public Resource getMainPerformance() {
    return this.F31_Performance;
  }

  void linkWorkById(String workIdentifier) throws URISyntaxException {
    if (workIdentifier == null || workIdentifier.isEmpty())
      return;

    PF22_SelfContainedExpression f22 = new PF22_SelfContainedExpression(workIdentifier);
    PF14_IndividualWork f14 = new PF14_IndividualWork(workIdentifier);

    if (isPremiere()) {
      // FIXME
      f22.addPremiere(this);
      f14.addPremiere(this);
    } else {
      this.add(f22);
    }

    this.hasWorkLinked = true;
  }


  private TimeSpan getDate() {
    List<String> dateList = record.getDatafieldsByCode(981, 'a');
    if (dateList.size() < 1) return null;

    String date = dateList.get(0);
    if (!date.matches("\\d{8}")) return null;

    String y = date.substring(0, 4),
      m = date.substring(4, 6),
      d = date.substring(6);
    return new TimeSpan(y, m, d);
  }

  private static CorporateBody ensambleIntercontemporain = null;

  private String getEnsambleIntercontemporainUri() throws URISyntaxException {
    if (ensambleIntercontemporain == null) {
      ensambleIntercontemporain = new CorporateBody("Ensamble Intercontemporain");
      model.add(ensambleIntercontemporain.getModel());
    }
    return ensambleIntercontemporain.getUri().toString();
  }
}
