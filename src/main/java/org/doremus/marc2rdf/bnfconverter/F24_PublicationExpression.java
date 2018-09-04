package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.Artist;
import org.doremus.marc2rdf.main.Person;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.Vocabulary;
import org.doremus.string2vocabulary.VocabularyManager;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class F24_PublicationExpression extends BIBDoremusResource {
  private static final String SPONSOR_REGEX = "(soutien|parrainage|avec l'appui) d[ue]";
  private static final Pattern SPONSOR_PATTERN = Pattern.compile(SPONSOR_REGEX, Pattern.CASE_INSENSITIVE);

  private static final Pattern COMMA_NAME_SURNAME_PATTERN = Pattern.compile("[^ ]+ [^ ]+, [^ ]+ [^ ]+");
  private static final String ADD_MATERIAL_REGEX = "(Notices?|Synopsis|Livret|Texte des) .+";
  private static final String AWARD_REGEX = "(Diapason d'or|Grand prix|" +
    "Victoires de la musique|^prix|^r[èeé]compense|^oscar)";
  private static final Pattern AWARD_PATTERN = Pattern.compile(AWARD_REGEX, Pattern.CASE_INSENSITIVE);

  private static final String BNF_CATEGORIZATION_NS = "http://data.doremus.org/vocabulary/categorization/bnf/";
  private static final String MUS_SUP_CONTENT_REGEX = "(?i)^(Not(?:e sur|ice)|Pr[éeè](?:sentation|face)" +
    "|Introduction|Apparat critique)";


  private static final Map<String, String> ILL_LABEL_MAP;

  static {
    Map<String, String> aMap = new HashMap<>();
    aMap.put("ill. ", "illustration(s)");
    aMap.put("portr. ", "portrait(s)");
    aMap.put("fac - sim. ", "fac - similé(s)");
    aMap.put("couv.ill. ", "couverture illustrée");
    aMap.put("en coul.", "en couleur");
    ILL_LABEL_MAP = Collections.unmodifiableMap(aMap);
  }


  public F24_PublicationExpression(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F24_Publication_Expression);
  }

  public F24_PublicationExpression(Record record) {
    super(record);
    this.setClass(FRBROO.F24_Publication_Expression);
    System.out.println(identifier);

    String control9 = record.getControlfieldByCode("009").getData();
    if (record.isMUS()) {
      String a051 = record.getDatafieldByCode("051", 'a');
      char control92 = control9.charAt(2);

      if ("ntm".equals(a051) || control92 == ' ' || control92 == 'd')
        this.addProperty(MUS.U227_has_content_type, "musique notée");
      else if ("tcm".equals(a051) || control92 == 'f')
        this.addProperty(MUS.U227_has_content_type, "musique notée tactile");
    }

    // additional material
    parseAdditionalMaterial();

    titleFromField(245, false).findFirst().ifPresent(System.out::println);

    // titles
    parseTitleAndStatements(true);
    parseResponsibilities(true);
    parseCastStatement();

    // title + responsibility
    List<String> annotations = record.getDatafieldsByCode(300, 'a');
    if (record.isDAV()) annotations.addAll(record.getDatafieldsByCode(350, 'a'));
    List<String> finalAnnotations = annotations.stream()
      .filter(t -> {
        if (record.isMUS()) {
          if (!t.startsWith("Par")) return false;
          return Stream.of("déclaration", "dépôt légal", "DL").anyMatch(t::contains);
        }
        return Stream.of("compositeur", "interprète").anyMatch(t::contains);
      }).collect(Collectors.toList());
    record.getDatafieldsByCode(245).stream()
      .map(M167_PublicationFragment::concatFields)
      .map(tsr -> model.createResource(this.uri + "/title_responsibility")
        .addProperty(RDF.type, MUS.M158_Title_and_Statement_of_Responsibility)
        .addProperty(RDFS.comment, tsr))
      .peek(res -> finalAnnotations.forEach(a -> res.addProperty(MUS.U50_has_annotation, a)))
      .forEach(x -> this.addProperty(MUS.U220_has_title_and_statement_of_responsibility, x));


    // edition
    String note = record.getDatafieldsByCode(250, 'a')
      .stream().findFirst().orElse(null);
    //noinspection ArraysAsListWithZeroOrOneArgument
    annotations = Stream.of(parseEditionSupplement()).filter(Objects::nonNull).collect(Collectors.toList());
    if (record.isMUS()) {
      annotations.addAll(record.getDatafieldsByCode(420).stream()
        .filter(df -> df.isCode(3))
        .map(df -> {
          String txt = df.getString('t');
          if (txt == null) txt = "";
          if (df.isCode('y')) txt += ", ISBN " + df.getString('y');
          if (df.isCode('z')) txt += ", ISMN " + df.getString('y');
          return "Supplément de : " + txt;
        })
        .collect(Collectors.toList()));
    }
    if (record.isDAV()) annotations.addAll(record.getDatafieldsByCode(351, 'a'));

    this.addStatement(this.uri + "/edition/1", note, MUS.M159_Edition_Statement, MUS.U176_has_edition_statement,
      annotations);

    int i = 1;
    for (String n : record.getDatafieldsByCode(250, 'd')) {
      this.addStatement(this.uri + "/edition/" + ++i, n, MUS.M159_Edition_Statement,
        MUS.U177_has_parallel_edition_statement);
    }

    for (String n : record.getDatafieldsByCode(250, 'b')) {
      Property p = n.startsWith("=") ? MUS.U181_has_parallel_additional_edition_statement :
        MUS.U180_has_additional_edition_statement;
      this.addStatement(this.uri + "/edition/" + ++i, n, MUS.M159_Edition_Statement, p);
    }

    // music format statement
    if (record.isMUS()) {
      record.getDatafieldsByCode(258, 'f').forEach(txt ->
        this.addProperty(MUS.U182_has_music_format_statement, txt));
      record.getDatafieldsByCode(258, 'g').forEach(txt ->
        this.addProperty(MUS.U183_has_parallel_music_format_statement, txt));

      this.addProperty(MUS.U229_has_music_format, parseMusicFormat(control9.charAt(3)));
    }

    // publication
    i = 0;
    int j = 0;
    for (DataField df : record.getDatafieldsByCode(260)) {
      if (df.getIndicator1() == ' ' || record.isDAV())
        for (String x : F30_PublicationEvent.parsePublisherField(df, true))
          this.addStatement(this.uri + "/publication/" + ++i, x,
            MUS.M160_Publication_Statement, MUS.U184_has_publication_statement);
      for (String x : F30_PublicationEvent.parsePublisherField(df, false))
        this.addStatement(this.uri + "/distribution/" + ++j, x,
          MUS.M161_Distribution_Statement, MUS.U185_has_distribution_statement);

      if (df.getIndicator1() != 1 || record.isDAV()) continue;
      String x = df.getString('r');
      this.addStatement(this.uri + "/publication/" + ++i, x,
        MUS.M160_Publication_Statement, MUS.U184_has_publication_statement);
    }

    i = 0;
    if (record.isDAV())
      for (DataField df : record.getDatafieldsByCode(270)) {
        if (df.getIndicator1() == ' ' || record.isDAV())
          for (String x : F30_PublicationEvent.parsePublisherField(df, true))
            this.addStatement(this.uri + "/manufacture/" + ++i, x,
              CIDOC.E33_Linguistic_Object, MUS.U186_has_printing_or_manufacture_statement);

        if (df.getIndicator1() != 1 || record.isDAV()) continue;
        String x = df.getString('r');
        this.addStatement(this.uri + "/manufacture/" + ++i, x,
          CIDOC.E33_Linguistic_Object, MUS.U186_has_printing_or_manufacture_statement);
      }

    // other titles
    record.getDatafieldsByCode(245, 'e').stream()
      .filter(x -> record.isDAV() || !x.matches("^(pour|f[üo]r|per|para).+"))
      .forEach(title -> this.addProperty(MUS.U67_has_subtitle, title));
    titleFromField(290, false)
      .forEach(x -> this.addProperty(MUS.U224_has_title_proper_of_multipart_monograph, x));
    Property p = (record.getDatafieldByCode(395) == null) ?
      MUS.U221_has_title_proper_of_series : MUS.U222_has_title_proper_of_sub_series;
    titleFromField(295, false).forEach(x -> this.addProperty(p, x));


    // illustrative content
    if (record.isMUS()) {
      annotations = record.getDatafieldsByCode(280, 'c');
      for (String a : annotations) {
        String _a = a.toLowerCase();
        for (Map.Entry<String, String> entry : ILL_LABEL_MAP.entrySet()) {
          String key = entry.getKey();
          String value = entry.getValue();
          _a = _a.replaceAll(key, value);
        }

        if (a.toLowerCase().contains("en coul."))
          this.addProperty(MUS.U202_has_colour_details, _a);
        if (ILL_LABEL_MAP.keySet().stream().anyMatch(a::contains))
          this.addProperty(MUS.U201_has_illustrative_content, a);
      }
    }

    annotations = record.getDatafieldsByCode(300, 'a');
    annotations.addAll(record.getDatafieldsByCode(353, 'a'));
    for (String a : annotations) {
      if (record.isDAV()) {
        if (a.toLowerCase().contains("disque illustré"))
          this.addProperty(MUS.U201_has_illustrative_content, a);
        if (a.toLowerCase().contains("disque coloré"))
          this.addProperty(MUS.U202_has_colour_details, a);
        if (a.toLowerCase().contains("pochette"))
          this.addProperty(MUS.U200_has_supplementary_content, a);
      } else if (record.isMUS()) {
        if (a.matches(MUS_SUP_CONTENT_REGEX))
          this.addProperty(MUS.U200_has_supplementary_content, a);
      }
    }

    // sponsor
    annotations = record.getDatafieldsByCode(300, 'a').stream()
      .filter(x -> SPONSOR_PATTERN.matcher(x).find()).collect(Collectors.toList());
    if (record.isDAV()) annotations.addAll(record.getDatafieldsByCode(312, 'a'));
    annotations.forEach(x -> this.addProperty(MUS.U199_has_sponsor, x, "fr"));

    annotations = record.getDatafieldsByCode(300, 'a');
    if (record.isDAV()) annotations.addAll(record.getDatafieldsByCode(317, 'a'));
    annotations.stream()
      .filter(x -> AWARD_PATTERN.matcher(x).find())
      .forEach(x -> this.addProperty(MUS.U24_has_award, this.model.createResource()
        .addProperty(RDF.type, MUS.M36_Award)
        .addProperty(RDFS.comment, x, "fr")
        .addProperty(CIDOC.P3_has_note, "fr")));

    // annotations
    record.getDatafieldsByCode(300, 'a').stream()
      .filter(x -> !AWARD_PATTERN.matcher(x).find())
      .filter(x -> !SPONSOR_PATTERN.matcher(x).find())
      .filter(x -> !x.matches(ADD_MATERIAL_REGEX))
      .filter(x -> !x.toLowerCase().contains("disque illustré"))
      .filter(x -> !x.toLowerCase().contains("disque coloré"))
      .filter(x -> !x.toLowerCase().contains("pochette"))
      .forEach(x -> this.addProperty(MUS.U50_has_annotation, x));


    record.getDatafieldsByCode(324, 'a')
      .forEach(x -> this.addProperty(MUS.U204_has_reproduction_note, x));

    // fragments
    i = 0;
    for (DataField df : record.getDatafieldsByCode(331)) {
      M167_PublicationFragment fragment = new M167_PublicationFragment(this.uri + "/fragment/" + ++i, df);
      this.addProperty(CIDOC.P148_has_component, fragment);
    }

    // summary
    record.getDatafieldsByCode(330, 'a')
      .forEach(s -> this.addProperty(MUS.U203_has_summary_or_abstract, s));
    if (record.isMUS())
      for (DataField df : record.getDatafieldsByCode(830)) {
        String txt = df.getString('a');
        if (txt == null) txt = "";
        if (df.getSubfield(2) != null)
          txt += " [source: " + df.getSubfield(2) + "]";

        this.addProperty(MUS.U203_has_summary_or_abstract, txt);
      }

    // categorization
    Stream<Resource> cats;
    if (record.isDAV()) cats = parseCategorization(record);
    else cats = Stream.of(control2categorization(control9));

    cats.filter(Objects::nonNull)
      .peek(r -> this.addProperty(MUS.U19_is_categorized_as, r))
      .forEach(r -> model.createResource(this.uri + "/assignment/" + r.getLocalName())
        .addProperty(RDF.type, CIDOC.E13_Attribute_Assignment)
        .addProperty(CIDOC.P141_assigned, r)
        .addProperty(CIDOC.P14_carried_out_by, BNF2RDF.BnF)
      );


    // identifiers
    if (record.isDAV()) {
      Supplier<Stream<M54_Label_Name>> labelNames =
        () -> record.getDatafieldsByCode(723).stream().map(M54_Label_Name::new);
      labelNames.get().forEach(ln -> this.addProperty(MUS.U169_was_issued_under_label_name, ln));
      for (DataField df : record.getDatafieldsByCode("028")) {
        String label = df.getString('e');
        if (label == null || label.isEmpty()) continue;
        M54_Label_Name ln = labelNames.get()
          .filter(l -> label.equals(l.getLabel()))
          .findFirst().orElse(null);
        if (ln == null) continue;
        String _id = df.getString('a');

        this.addComplexIdentifier(_id, "Référence commerciale", ln);
      }
    }
    if (record.isMUS()) {
      List<DataField> fields = record.getDatafieldsByCode("020");
      fields.addAll(record.getDatafieldsByCode("024"));
      fields.forEach(df -> {
        String _id = df.getString('a');
        String sym = df.getString('b');
        String type = df.getEtiq().equals("020") ? "ISBN" : "ISMN";
        this.addComplexIdentifier(_id, type, sym);
      });

      record.getDatafieldsByCode("023").forEach(df -> {
        String _id = df.getString('a');
        String sym = df.getString('b');
        Artist issuer = Artist.fromString(df.getString('e'));
        String type = df.getIndicator1() == '1' ? "numéro d'éditeur" : "Cotage";
        this.addComplexIdentifier(_id, type, issuer, sym);
      });
    }

  }

  private Resource control2categorization(String control9) {
    String code = String.valueOf(control9.charAt(5));
    if (!"01562".contains(code)) return null;
    if ("2".equals(code)) code += control9.charAt(6);
    return toBnFCategorizationUri(code);
  }

  public F24_PublicationExpression(String identifier, String txt) {
    this(identifier);
    // field 327$a of MUS

    if (txt.matches("(fasc|n)\\. \\d+ *[,:] .+"))
      txt = txt.split("[,:]", 2)[1].trim();

    String[] parts = txt.split(" / ", 2);
    String trs = parts[0];

    // TODO not clear in the mapping
    // if (parts.length > 1) {
    // String castStatement = parts[1];
    // }

    Resource titleRespStatement = model.createResource(this.uri + "/title_responsibility")
      .addProperty(RDF.type, MUS.M158_Title_and_Statement_of_Responsibility)
      .addProperty(RDFS.comment, trs);
    this.addProperty(MUS.U220_has_title_and_statement_of_responsibility, titleRespStatement);
  }


  public F24_PublicationExpression(String identifier, DataField df) {
    this(identifier);

    String content = null, authors = null, illContent = null;
    switch (df.getEtiq()) {
      case "323":
        String[] parts = df.getString('a').replaceAll("[,;]$", "").split("/");
        content = parts[0];
        if (parts.length > 1) authors = parts[1];
        break;
      case "353":
      case "300":
        content = df.getString('a');
        break;
      case "280":
        String[] temp = df.getString('e').split("[(,:;]");
        content = temp[0];

        illContent = Arrays.stream(temp)
          .filter(s -> s.startsWith("ill."))
          .findFirst().orElse(null);
    }

    if (content == null) return;

    this.addNote(content, "fr");

    if (authors != null && !authors.trim().isEmpty()) {
      F30_PublicationEvent f30_publicationEvent = new F30_PublicationEvent(this.identifier);

      String delimiter = ";";
      if (!authors.contains(delimiter) && COMMA_NAME_SURNAME_PATTERN.matcher(authors).find()) delimiter = ",";
      for (String fullName : authors.split(delimiter)) {
        Person p;

        if (fullName.contains(",")) {
          String[] parts = fullName.split(",");
          String firstName = parts[1];
          String lastName = parts[0];
          p = new Person(firstName, lastName, null);
        } else p = new Person(fullName);
        p.interlink();
        f30_publicationEvent.addActivity(p, "auteur du texte");
      }

      f30_publicationEvent.add(this);
      this.model.add(f30_publicationEvent.getModel());
    }

    if (illContent != null) {
      illContent = illContent.replaceAll("ill\\.", "illustration(s)")
        .replaceAll("en coul\\.", "en couleur");

      this.addProperty(MUS.U201_has_illustrative_content, illContent, "fr");
      if (illContent.contains("en couleur"))
        this.addProperty(MUS.U202_has_colour_details, illContent, "fr");
      this.addProperty(MUS.U214_has_colour_content, "couleur", "fr");
    }

    F3_ManifestationProductType f3 = F3_ManifestationProductType.fromField(df, this.identifier);
    if (f3 != null) {
      f3.add(this);
      this.model.add(f3.getModel());
    }
  }

  private static Resource MF_MATERIAL = ResourceFactory.createResource("http://data.doremus.org/format/material");
  private static Resource MF_PARTS = ResourceFactory.createResource("http://data.doremus.org/format/parts");
  private static Resource MF_SOLOIST = ResourceFactory.createResource("http://data.doremus.org/format/soloist");
  private static Resource MF_VOICEINST = ResourceFactory.createResource(
    "http://data.doremus.org/format/voice_instrument");


  private Resource parseMusicFormat(char code) {
    Vocabulary tos = VocabularyManager.getVocabulary("iaml-tos");

    switch (code) {
      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'm':
      case 'z':
        return tos.getConcept(String.valueOf(code));
      case 'f':
        return MF_SOLOIST;
      case 'p':
        return MF_PARTS;
      case 'q':
        return MF_MATERIAL;
      case 'x':
        return MF_VOICEINST;
    }
    return null;
  }

  static Stream<Resource> parseCategorization(Record record) {
    return record.getDatafieldsByCode(640).stream()
      .map(df -> {
        String code = df.getString('a');
        if (code == null || code.isEmpty()) code = df.getString('b');
        return code;
      }).filter(Objects::nonNull)
      .map(F24_PublicationExpression::toBnFCategorizationUri);
  }

  private static Resource toBnFCategorizationUri(String code) {
    if (code == null || code.isEmpty()) return null;
    code = code.trim();
    return ResourceFactory.createResource(BNF_CATEGORIZATION_NS + code);
  }

  private void parseAdditionalMaterial() {
    int i = 0;

    List<DataField> fields = record.getDatafieldsByCode(323).stream()
      .filter(x -> x.getString('a') != null)
      .collect(Collectors.toList());
    if (record.isDAV())
      fields.addAll(record.getDatafieldsByCode(353).stream()
        .filter(x -> x.getString('a').matches(ADD_MATERIAL_REGEX))
        .collect(Collectors.toList()));
    if (record.isMUS() || fields.size() == 0)
      fields.addAll(record.getDatafieldsByCode(280).stream()
        .filter(x -> x.getString('e') != null)
        .collect(Collectors.toList()));

    for (DataField df : fields) {
      F24_PublicationExpression material = new F24_PublicationExpression(this.identifier + "m" + ++i, df);
      this.addProperty(CIDOC.P165_incorporates, material);
      this.model.add(material.getModel());
    }

    if (record.isDAV()) {
      List<String> subPubs = record.getDatafieldsByCode(327, 'a');
      if (subPubs.size() == 1) subPubs = Arrays.asList(subPubs.get(0).split(";"));
      for (String txt : subPubs) {
        F24_PublicationExpression material = new F24_PublicationExpression(this.identifier + "m" + ++i, txt);
        this.addProperty(CIDOC.P165_incorporates, material);
        this.model.add(material.getModel());
      }
    }

  }

  private String parseEditionSupplement() {
    DataField df = record.getDatafieldByCode(470);
    if (df == null) return null;
    String intro;
    switch (df.getIndicator2()) {
      case '1':
        intro = "Numéro de:";
        break;
      case '2':
        intro = "Tiré-à-part de:";
        break;
      case '3':
        intro = "Extrait de:";
        break;
      default:
        return null;
    }
    StringBuilder sb = new StringBuilder(intro);
    sb.append(" \"");
    sb.append(df.getString('t'));
    sb.append("\", ISSN");
    sb.append(df.getString('x'));
    String v = df.getString('v');
    String d = df.getString('d');
    if (d != null || v != null) sb.append(", ");
    if (v != null) sb.append(", ").append(v);
    if (d != null) sb.append(" (").append(d).append(")");
    return sb.toString();
  }


  public F24_PublicationExpression add(F22_SelfContainedExpression expression) {
    this.addProperty(CIDOC.P165_incorporates, expression);
    return this;
  }

  public F24_PublicationExpression add(M43_PerformedExpression performedExpression) {
    this.addProperty(MUS.U55_incorporates_performed_expression, performedExpression);
    return this;
  }
}
