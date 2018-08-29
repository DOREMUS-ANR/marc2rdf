package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.*;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.util.ArrayList;
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
  private F31_Performance mainPerformance;
  private E53_Place place;
  private TimeSpan timeSpan;
  private int countConsistOf;
  private List<M28_Individual_Performance> individualPerformances;

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

    this.mainPerformance = new F31_Performance(this.identifier);
    this.mainPerformance.addNote(note);
    this.mainPerformance.add(this);

    this.expression = new M43_PerformedExpression(this.identifier);
    f28.expression.titles.forEach(title -> this.expression.addProperty(RDFS.label, title));

    this.work = new M44_PerformedWork(this.identifier);
    this.work.add(this.expression);

    this.add(this.expression).add(this.work);

    this.f28 = f28;

    parseNote(note);

    this.addProperty(CIDOC.P7_took_place_at, place);
    this.mainPerformance.setPlace(place);

    this.mainPerformance.addTimeSpan(timeSpan);
    this.addTimeSpan(timeSpan);
  }

  public M42_PerformedExpressionCreation(Record record, String identifier) {
    super(identifier);
    this.record = record;
    this.countConsistOf = 0;

    this.setClass(MUS.M42_Performed_Expression_Creation);

    this.individualPerformances = new ArrayList<>();
    record.getDatafieldsByCodePropagate(111).forEach(df -> parsePerformer(df, false));
    record.getDatafieldsByCodePropagate(711).forEach(df -> parsePerformer(df, false));
    record.getDatafieldsByCodePropagate(101).forEach(df -> parsePerformer(df, true));
    record.getDatafieldsByCodePropagate(701).forEach(df -> parsePerformer(df, true));

    // specific instrument
    for (String txt : record.getDatafieldsByCode(314, 'i')) {
      if (txt.contains(";")) {
        this.addProperty(MUS.U193_used_historical_instruments, txt);
        continue;
      }
      FunctionPerformerMap fpm = FunctionPerformerMap.getFrom245(txt);
      if (fpm == null) continue;
      Resource mop = fpm.getMop();
      if (mop == null) continue;
      M28_Individual_Performance target = individualPerformances.stream()
        .filter(ip -> ip.hasMop(mop))
        .findFirst().orElse(null);
      if (target == null) {
        target = new M28_Individual_Performance(this.uri + "/" + ++countConsistOf);
        target.setMoP(mop);
        M28_Individual_Performance ensemble = individualPerformances.stream()
          .filter(M28_Individual_Performance::isEnsemble)
          .findFirst().orElse(null);
        if (ensemble != null) ensemble.add(target);
      }
      target.setSpecificMop(txt);
    }

    individualPerformances.forEach(ip -> this.addProperty(CIDOC.P9_consists_of, ip));

    // cast detail
    StringBuilder castDetail = new StringBuilder(
      String.join(" ; ", record.getDatafieldsByCode(245, 'j')));
    if (!castDetail.toString().isEmpty()) castDetail.append(". ");
    record.getDatafieldsByCode(313).stream()
      .map(df -> {
        String d = df.getString('k') + " : ";
        List<String> a = df.getStrings('a').stream()
          .filter(txt -> !txt.matches("(?i).*détail d(es interprètes|u personnel).*"))
          .collect(Collectors.toList());
        if (a.size() < 1) return "";
        return d + String.join(" ; ", a) + ". ";
      }).forEachOrdered(castDetail::append);
    this.addProperty(MUS.U205_has_cast_detail, castDetail.toString());
  }

  private void parsePerformer(DataField df, boolean isPerson) {
    Artist agent = isPerson ? ArtistConverter.parseArtistField(df) : CorporateBody.fromUnimarcField(df);
    FunctionPerformerMap function = FunctionPerformerMap.get(df.getString(4));
    String character = df.getString(9);

    M28_Individual_Performance ip = new M28_Individual_Performance(this.uri + "/" + ++countConsistOf);
    ip.setAgent(agent);
    ip.setCharacter(character);
    if (function != null) {
      ip.setFunction(function.getFunction());
      ip.setMoP(function.getMop());
    }

    this.individualPerformances.add(ip);
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
              if (intpt.matches("^[a-z].+")) addIndividualPerformance(intpt, null);
              else addIndividualPerformance(intpt, role);
            }
          }
        }

      } else System.out.println("Not parsed note on record " + record.getIdentifier() + " : " + data);
    }

    if (conductor != null) addIndividualPerformance(conductor, "conductor");
  }

  private void addIndividualPerformance(String agentName, String role) {
    agentName = agentName.trim();
    if (agentName.isEmpty()) return;

    Artist agent = agentName.equals("compositeur") || agentName.equals("le compositeur") ?
      f28.getComposers().get(0) : Artist.fromString(agentName);

    M28_Individual_Performance ip = new M28_Individual_Performance(this.uri + "/" + ++countConsistOf);
    ip.setAgent(agent);

    if (role == null) return;

    role = role.trim();
    for (String r : role.split(" et ")) {
      if (r.equals("conductor") || r.equals("direction")) ip.setFunction("conductor");
      else ip.setMoP(VocabularyManager.searchInCategory(r, "fr", "mop", true));
    }

    this.addProperty(CIDOC.P9_consists_of, ip);
  }

  public M43_PerformedExpression getExpression() {
    return expression;
  }

  public M44_PerformedWork getWork() {
    return work;
  }

  public M42_PerformedExpressionCreation add(F25_PerformancePlan plan) {
    this.addProperty(FRBROO.R25_performed, plan);
    if (this.mainPerformance != null) this.mainPerformance.add(plan);
    return this;
  }

  public M42_PerformedExpressionCreation add(F22_SelfContainedExpression f22) {
    this.expression.addProperty(MUS.U54_is_performed_expression_of, f22);
    if (this.mainPerformance != null) this.mainPerformance.add(f22);
    return this;
  }

  public M42_PerformedExpressionCreation add(M43_PerformedExpression performedExpression) {
    this.addProperty(FRBROO.R17_created, performedExpression);
    this.expression = performedExpression;
    return this;
  }

  public M42_PerformedExpressionCreation add(M44_PerformedWork performedWork) {
    this.addProperty(FRBROO.R19_created_a_realisation_of, performedWork);
    this.work = performedWork;
    return this;
  }

  public M42_PerformedExpressionCreation add(F4_ManifestationSingleton manuscript) {
    this.addProperty(CIDOC.P16_used_specific_object, manuscript);
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

  public F31_Performance getMainPerformance() {
    return this.mainPerformance;
  }

  public void setPlace(E53_Place place) {
    this.addProperty(CIDOC.P7_took_place_at, place);
  }

  public void setGeoContext(E53_Place geoContext) {
    this.addProperty(MUS.U65_has_geographical_context, geoContext);
  }

}
