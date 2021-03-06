package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.*;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.marcparser.Subfield;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.Vocabulary;
import org.doremus.string2vocabulary.VocabularyManager;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PM42_PerformedExpressionCreation extends DoremusResource {
  private static final String SPECIFIC_COMMAND_REGEX = "(?i)commande des?(?: l['a])? ?" +
    "(Cité de la musique|Philharmonie de  Paris|Ensemble intercontemporain|Cris de Paris)";
  private static final Pattern SPECIFIC_COMMAND_PATTERN = Pattern.compile(SPECIFIC_COMMAND_REGEX);
  private static final String INTERPRETER_REGEX = "(?<!(?:organis|rapport)é )par (.+)";
  private static final Pattern INTERPRETER_PATTERN = Pattern.compile(INTERPRETER_REGEX);
  private static final String frenchCreationRegex = "(?i)(cr\u00e9ation fran\u00e7aise.*)";
  private static final String premiereRegex = "(?i)cr[eéè]ation mondiale";
  private static final String noPremiereRegex = frenchCreationRegex + "|(enregistrement (de )?jazz)|(1re reprise)";
  private static final String frenchUncertainDate = "(?:le |en )?(?:" + TimeSpan.frenchDayRegex + "? ?" + TimeSpan.frenchMonthRegex + "? )?(?:\\(\\?\\) )?(\\d{4})(?: ?\\?)?";
  private static final String conductorRegex = "(?:(?:placé )?(?:sous|par) la dir(?:\\.|ection) d[e'u]|dirigé par|" +
    "direction(?: :)?) ?([\\p{L} \\-']*(?:[A-Z]\\.)?[\\p{L} \\-']+)";
  private static final String DEDICATAIRE_REGEX = "\\(?(?:l[ea]s? |son |sa )?(?:dédica|commandi)taires?(?: de l'oeuvre)?\\)?";
  private static final String composerRegex = "(le )?compositeur|l'auteur";
  private static final String noUppercase = "[^A-Z]+";

  private static final String performanceRegex = "(?i)(?:(?:premi[èe]|1[èe]?)re? (?:représentation|[ée]x[ée]cution|audition|enregistrement|reprise) )|(?:cr[èée]{1,3}(?:ation|s)? )";

  private static final Vocabulary IAMLVoc = VocabularyManager.getVocabulary("mop-iaml");
  private static final Resource INSTRUMENT = IAMLVoc.getConcept("mui");
  private static final Resource ORCHESTRAS = IAMLVoc.getConcept("o");
  private static final Resource CHORUSES = IAMLVoc.getConcept("c");
  private static final Resource VOICE = IAMLVoc.getConcept("vun");

  private PF28_ExpressionCreation f28;

  private boolean isPremiere;
  private final PM44_PerformedWork M44_Performed_Work;
  private final PM43_PerformedExpression M43_Performed_Expression;
  private PF31_Performance F31_Performance;
  private E53_Place place;
  private TimeSpan timeSpan;

  public boolean hasWorkLinked = false;

  public PM42_PerformedExpressionCreation(String identifier) {
    super(identifier);
    this.setClass(MUS.M42_Performed_Expression_Creation);
    this.addProperty(DC.identifier, identifier);
    this.M43_Performed_Expression = new PM43_PerformedExpression(identifier);
    this.M44_Performed_Work = new PM44_PerformedWork(identifier);
    this.connectTriplet();
  }

  public PM42_PerformedExpressionCreation(Record record) {
    super(record);
    this.setClass(MUS.M42_Performed_Expression_Creation);
    this.addProperty(DC.identifier, this.identifier);

    List<String> concertIds = record.getDatafieldsByCode(935, 3);
    if (!concertIds.isEmpty())
      this.F31_Performance = new PF31_Performance(concertIds.get(0));

    this.M43_Performed_Expression = new PM43_PerformedExpression(record);
    this.M44_Performed_Work = new PM44_PerformedWork(record);
    this.connectTriplet();

    String premiereNote = searchInNote(premiereRegex);
    isPremiere = premiereNote != null;

    if (premiereNote != null) this.addNote(premiereNote);

    String frenchPremiereNote = searchInNote(frenchCreationRegex);
    if (frenchPremiereNote != null) this.addNote(frenchPremiereNote);

    this.addTimeSpan(getDate());

    this.addProperty(MUS.U205_has_cast_detail, getCastDetail(record));

    getMuseeMusique(record).forEach(note -> this.addProperty(MUS.U193_used_historical_instruments, note));

    this.addCommand(searchInNote(SPECIFIC_COMMAND_PATTERN, 1));

    for (String workId : record.getDatafieldsByCode(449, '3'))
      linkWorkById(workId);

    boolean shouldICreateAF22 = false;
    if (!this.hasWorkLinked && !record.isType("UNI:62")) {
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

      // 230 == composer
      shouldICreateAF22 = activities.stream()
        .filter(a -> "230".equals(a.getString(4)))
        .toArray().length > 0;

      if (isAnImprovisation()) {
        // F25 Performance Plan R25i was performed by M42 Performed Expression Creation
        PF25_PerformancePlan plan = new PF25_PerformancePlan(record);
        plan.setAsImprovisation();
        PF25_PerformancePlan mainF25 = this.F31_Performance.getRelatedF25();
        mainF25.add(plan);
        model.add(plan.getModel()).add(mainF25.getModel());
      }

    }

    if (shouldICreateAF22) linkNewWork(record);

    for (PM28_Individual_Performance ip : parseArtist(record, uri)) this.addProperty(CIDOC.P9_consists_of, ip);

    if (record.isType("UNI:42")) {
      for (String s : record.getDatafieldsByCode(462, 3))
        this.addProperty(CIDOC.P9_consists_of, new PM42_PerformedExpressionCreation(s));
    }
  }

  public PM42_PerformedExpressionCreation(String note, String identifier, int i, PF28_ExpressionCreation f28) {
    // Parse from a note of TUM
    super(identifier);

    this.place = null;
    this.timeSpan = null;

    this.f28 = f28;

    //check if it is a Premiere
    Pattern p = Pattern.compile(noPremiereRegex);
    Matcher m = p.matcher(note);
    isPremiere = !m.find();
    char flag = isPremiere ? 'p' : 'f';

    this.identifier += flag;
    if (i >= 0)
      this.identifier += i;

    try {
      this.uri = ConstructURI.build(this.sourceDb, this.className, this.identifier).toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    this.resource = model.createResource(this.uri)
      .addProperty(RDF.type, MUS.M42_Performed_Expression_Creation)
      .addProperty(RDFS.comment, note)
      .addProperty(CIDOC.P3_has_note, note);

    String performanceUri = null;
    try {
      performanceUri = ConstructURI.build("pp", "F31_Performance", this.identifier).toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    this.F31_Performance = new PF31_Performance(performanceUri, note, model);
    this.F31_Performance.add(this);

    this.M43_Performed_Expression = new PM43_PerformedExpression(this.identifier);
    this.M44_Performed_Work = new PM44_PerformedWork(this.identifier);
    this.connectTriplet();

    parseNote(note);

    if (place != null) {
      this.addProperty(CIDOC.P7_took_place_at, place);
      this.F31_Performance.setPlace(place);
    }

    this.addTimeSpan(timeSpan);
    this.F31_Performance.addTimeSpan(timeSpan);
  }

  private final static Pattern MUSEE_MUSIQUE_PATTERN = Pattern.compile("(?i)(musée de la Musique|collection)");

  static Stream<String> getMuseeMusique(Record record) {
    return record.getDatafieldsByCode(300, 'a').stream()
      .filter(n -> MUSEE_MUSIQUE_PATTERN.matcher(n).find());
  }

  static String getCastDetail(Record record) {
    List<String> fields = record.getDatafieldsByCode(200, 'g');
    if (record.getType().matches("UNI:[46]2C"))
      fields.addAll(record.getDatafieldsByCode(200, 'f'));
    return String.join("; ", fields);
  }

  public static List<PM28_Individual_Performance> parseArtist(Record record, String mainUri) {
    int counter = 0;
    List<PM28_Individual_Performance> activityList = new ArrayList<>();

    List<DataField> artistFields = record.getDatafieldsByCode(702);
    artistFields.addAll(record.getDatafieldsByCode(712));

    for (DataField df : artistFields) {
      boolean isAGroup = df.getEtiq().equals("712");
      Artist artist = isAGroup ? CorporateBody.fromUnimarcField(df) : Person.fromUnimarcField(df);

      Resource genericMop = null;
      List<String> functTypes = df.getSubfields('4').stream()
        .map(Subfield::getData)
        .collect(Collectors.toList());

      boolean parsed = false;
      boolean isPrincipal = df.hasSubfieldValue('4', "040");
      for (String functionType : functTypes) {
        boolean isInstrumentist = false;
        switch (functionType) { // function
          case "040": // principal artist, managed before
            break;
          // FUNCTIONS WITH MoPs
          case "545": // musician
            genericMop = isAGroup ? ORCHESTRAS : INSTRUMENT;
            isInstrumentist = true;
          case "721": // singer
            if (genericMop == null)
              genericMop = isAGroup ? CHORUSES : VOICE;

            String txt = searchArtistInNotes(artist, record);

            List<String> parts = Utils.splitKeepBrackets(txt, "(,| et | & )");

            String toBeAdded = "";
            for (int i = 1; i < parts.size(); i++) {
              String pt = toBeAdded + parts.get(i).trim();

              // Laurent Laberdesque, baryton (Figaro, valet du Comte Almaviva)
              String[] x = Utils.extractBrackets(pt);
              String operaRole = x[1];
              pt = x[0];

              pt = pt.replaceAll("\\)", "").trim();

              toBeAdded = "";
              if (pt.isEmpty()) continue;
              if (pt.matches("composit(ion|eurs?)") ||
                pt.matches("réalisat(ion|eurs?)") ||
                pt.equals("texte") || pt.equals("parole") || pt.equals("musique") ||
                pt.startsWith("de") || pt.startsWith("du") ||
                pt.contains("programmation") ||
                pt.contains("direction") || pt.contains("arrangement") ||
                pt.matches("\\d+"))
                continue;

              // Sometimes there is a list of comma-separated interpreters
              // they are not mops obviously
              if (Character.isUpperCase(pt.codePointAt(0))) continue;

              parsed = true;

              pt = pt.replaceFirst(" solo$", "")
                .replaceFirst("violoniste", "violin")
                .replaceFirst("violoncelliste", "cello");

              pt = pt.trim();
              if (!isInstrumentist)
                pt = PM23_Casting_Detail.toItalianSinger(pt, true);

              Resource mop = VocabularyManager.searchInCategory(pt, "fr", "mop", true);

              PM28_Individual_Performance ip = new PM28_Individual_Performance(mainUri, ++counter);
              if (operaRole != null && operaRole.contains("soliste")) {
                ip.setSolo();
                operaRole = operaRole.replace("soliste", "").trim();
                if (operaRole.isEmpty()) operaRole = null;
              }
              if (mop == null && operaRole != null) {
                mop = VocabularyManager.searchInCategory(operaRole, "fr", "mop", true);
                if (mop != null) operaRole = null;
              }

              if (mop == null && !pt.isEmpty())
                log("Mop not found: " + pt + " | Full line: " + txt, record);

              Resource _mop = (mop != null) ? mop : genericMop;
              ip.set(artist, _mop, null, isInstrumentist ? null : operaRole, txt, isPrincipal);
              activityList.add(ip);
            }

            if (!parsed) {
              // add at least the generic instrument
              PM28_Individual_Performance ip = new PM28_Individual_Performance(mainUri, ++counter);
              if (isAGroup)
                genericMop = guessMopFromArtist(artist, genericMop);
              ip.set(artist, genericMop, null, null, txt, isPrincipal);

              activityList.add(ip);
            }
            break;
          // FUNCTIONS without MoPs
          case "250": // chef d'orchestre
            String temp = searchArtistInNotes(artist, record);
            if (temp.contains("chef de choeur")) functionType = "195";
          case "195": // chef de chœur
          case "274": // danseur
          case "303": // disc jockey
          case "550": // narrateur
          case "590": // interprète
          case "780": // acteur / exécutant
          case "800": // intervenant / présentateur
            PM28_Individual_Performance ip = new PM28_Individual_Performance(mainUri, ++counter);
            ip.set(artist, (RDFNode) null, functionType, null, null, isPrincipal);

            activityList.add(ip);
        }
      }
    }

    return activityList;
  }

  private static Resource guessMopFromArtist(Artist artist, Resource fallback) {
    String _artist = artist.getFullName().toLowerCase();
    if (_artist.contains("orchestr")) {
      if (_artist.contains("symphon") || _artist.contains("philharmon"))
        return IAMLVoc.getConcept("ofu");
      return IAMLVoc.getConcept("oun");
    } else if (_artist.contains("ensemble")) {
      if (fallback == IAMLVoc.getConcept("c"))
        return IAMLVoc.getConcept("cve");
      return IAMLVoc.getConcept("oie");
    }

    return fallback;
  }

  private static String searchArtistInNotes(Artist artist, Record record) {
    String fn = artist.getFullName().toLowerCase();

    List<String> fields = record.getDatafieldsByCode(200, 'f');
    fields.addAll(record.getDatafieldsByCode(200, 'g'));

    for (String note : fields) {
      if (note.toLowerCase().contains(fn)) {
        if (!note.contains(";")) return note;

        return Utils.splitKeepBrackets(note, ";").stream()
          .filter(sub -> sub.toLowerCase().contains(fn))
          .findFirst().orElse("");
      }
    }
    return "";
  }

  private void addCommand(String commander) {
    if (commander == null) return;
    if (commander.toLowerCase().equals("Philharmonie de Paris")) {
      addCommand(PP2RDF.PHILHARMONIE);
      return;
    }
    if (commander.toLowerCase().equals("Cris de Paris"))
      commander = "les Cris de Paris";
    CorporateBody cb = new CorporateBody(commander);
    addCommand(cb.asResource());
    model.add(cb.getModel());
  }

  private void addCommand(Resource commander) {
    Resource activity = model.createResource(this.uri + "/activity/1")
      .addProperty(RDF.type, CIDOC.E7_Activity)
      .addProperty(CIDOC.P14_carried_out_by, commander)
      .addProperty(MUS.U31_had_function, "commanditaire", "fr");
    this.resource.addProperty(CIDOC.P9_consists_of, activity);
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
    note = note.replaceAll("\\.$", "");

    int counter = 0;
    note = note.split("(;|((,| |, )(puis|parce) ))")[0].trim();
    note = note.replaceAll(DEDICATAIRE_REGEX, "");
    note = note.replaceAll(", , (au|à la)", " $1")
      .replaceAll(", ,", ",").trim();

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
        Role r = makeRole(conductor, (mop == null) ? "conductor" : mop);
        this.resource.addProperty(CIDOC.P9_consists_of, r.toM28IndividualPerformance(this.uri + "/" + ++counter));
        first = false;
      }
      note = cleanString(note, m.group());
    }

    // extract interpreters
    List<Role> roles = new ArrayList<>();
    Pattern mopIntPattern = Pattern.compile("(.+) \\((.+)\\)");
    m = INTERPRETER_PATTERN.matcher(note);
    if (m.find()) {
      String interpreterString = m.group(1)
        .replaceAll("^ ?:", "").trim();

      // unbalanced parenthesis
      if (!Utils.areQuotesBalanced(interpreterString))
        interpreterString = "";        // too complex

      // "ne fut pas créé par lui mais par Ilona Kurz-Stepanova"
      if (interpreterString.contains(" par "))
        interpreterString = "";        // too complex

      if (interpreterString.contains(" dont "))
        interpreterString = interpreterString.split(" dont ")[0];

      interpreterString = interpreterString.replaceFirst(", à .+", "").replaceFirst(",? dans .+", "").trim();

      String interpreter = "";
      RDFNode mopMatch = null;

      for (String originalInterpreter : interpreterString.split("([,/]| et | avec )")) {
        String newInfo = originalInterpreter.replaceFirst("^(et|avec) ", "").trim();
        String mop;
        if (newInfo.isEmpty()) {
          interpreter = "";
          continue;
        }
        interpreter += newInfo;

        if (!Utils.areQuotesBalanced(interpreter)) {
          interpreter += ", ";
          continue; // wait for close parenthesis
        }

        if (!(newInfo.startsWith("(") && newInfo.endsWith(")")) && Utils.areQuotesBalanced(newInfo))
          mopMatch = VocabularyManager.searchInCategory(newInfo, "fr", "mop", true);

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
            mopMatch = VocabularyManager.searchInCategory(mop, "fr", "mop", false);
            if (mopMatch != null) interpreter = parts[0];
          }
        }

        if (mopMatch == null && interpreter.contains("(")) {
          Matcher mopIntMatcher = mopIntPattern.matcher(interpreter);
          if (mopIntMatcher.find()) {
            interpreter = mopIntMatcher.group(1);
            mop = mopIntMatcher.group(2);
            mopMatch = VocabularyManager.searchInCategory(mop, "fr", "mop", false);
          }
        }


        roles.add(makeRole(interpreter, mopMatch));
        interpreter = "";
      }

      for (Role r : roles)
        this.resource.addProperty(CIDOC.P9_consists_of, r.toM28IndividualPerformance(this.uri + "/" + ++counter));

      note = cleanString(note, m.group());
    }

    // extract places
    Pattern cityPattern = Pattern.compile("(?<!\\w)à ([\\p{L}- ]+(\\([\\p{L}- ]+\\))?)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    Matcher cityMatcher = cityPattern.matcher(note);
    if (cityMatcher.find()) {
      String city = cityMatcher.group(1);
      if (!city.matches(noUppercase)) {
        this.place = new E53_Place(city);
      }
    }

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

  public PM43_PerformedExpression getExpression() {
    return M43_Performed_Expression;
  }

  public Resource getWork() {
    return M44_Performed_Work.asResource();
  }

  public PM42_PerformedExpressionCreation add(PF25_PerformancePlan f25) {
    this.F31_Performance.add(f25);
    return this;
  }

  public PM42_PerformedExpressionCreation add(PF22_SelfContainedExpression f22) {
    this.M43_Performed_Expression.addProperty(MUS.U54_is_performed_expression_of, f22);
    if (this.F31_Performance != null)
      this.F31_Performance.addProperty(FRBROO.R66_included_performed_version_of, f22);

    this.model.add(M43_Performed_Expression.getModel())
      .add(F31_Performance.getModel());

    return this;
  }

  public boolean isPremiere() {
    return isPremiere;
  }

  public PF31_Performance getMainPerformance() {
    return this.F31_Performance;
  }

  void linkWorkById(String workIdentifier) {
    if (workIdentifier == null || workIdentifier.isEmpty())
      return;

    PF22_SelfContainedExpression f22 = new PF22_SelfContainedExpression(workIdentifier);
    PF14_IndividualWork f14 = new PF14_IndividualWork(workIdentifier);
    PF15_ComplexWork f15 = new PF15_ComplexWork(workIdentifier);

    if (isPremiere()) {
      f22.addPremiere(this);
      f14.addPremiere(this);
    }

    this.F31_Performance.add(f22);
    PF25_PerformancePlan f25 = this.F31_Performance.getRelatedF25().add(f22);

    this.add(f22);
    f15.add(this);

    this.hasWorkLinked = true;
    this.model.add(F31_Performance.getModel()).add(f25.getModel());
  }

  public void linkNewWork(Record record) {
    PF28_ExpressionCreation f28 = new PF28_ExpressionCreation(record);
    PF22_SelfContainedExpression f22 = new PF22_SelfContainedExpression(record);
    PF14_IndividualWork f14 = new PF14_IndividualWork(record.getIdentifier());

    PF25_PerformancePlan f25 = this.F31_Performance.getRelatedF25().add(f22);

    f28.add(f22).add(f14);
    f14.add(f22);

    if (isPremiere()) {
      f22.addPremiere(this);
      f14.addPremiere(this);
    }

    this.add(f22);
    this.model.add(f22.getModel()).add(f28.getModel()).add(f14.getModel());
    this.model.add(F31_Performance.getModel()).add(f25.getModel());
  }


  private TimeSpan getDate() {
    return TimeSpan.fromUnimarcField(record.getDatafieldByCode(981));
  }

  public void setConcert(PF31_Performance pf) {
    if (this.F31_Performance == null) this.F31_Performance = pf;
  }

  private boolean isAnImprovisation() {
    return record.getDatafieldsByCode(200, 'a').stream()
      .anyMatch(s -> s.contains("improvisation"));
  }
}
