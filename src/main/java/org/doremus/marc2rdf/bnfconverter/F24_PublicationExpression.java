package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Person;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.marcparser.Subfield;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class F24_PublicationExpression extends DoremusResource {
  private static final Pattern EXTRAIT_PATTERN = Pattern.compile("(extr(ait|\\.)|choix)", Pattern.CASE_INSENSITIVE);

  private static final String UNKNOW_REGEX = "(?i)\\[?s\\. ?[lnd]\\.\\]?";

  private static final String SPONSOR_REGEX = "(soutien|parrainage|avec l'appui) d[ue]";
  private static final Pattern SPONSOR_PATTERN = Pattern.compile(SPONSOR_REGEX, Pattern.CASE_INSENSITIVE);

  private static final Pattern COMMA_NAME_SURNAME_PATTERN = Pattern.compile("[^ ]+ [^ ]+, [^ ]+ [^ ]+");
  private static final String ADD_MATERIAL_REGEX = "(Notices?|Synopsis|Livret|Texte des) .+";
  private static final String AWARD_REGEX = "(Diapason d'or|Grand prix|" +
    "Victoires de la musique|^prix|^r[èeé]compense|^oscar)";
  private static final Pattern AWARD_PATTERN = Pattern.compile(AWARD_REGEX, Pattern.CASE_INSENSITIVE);

  private static final String BNF_CATEGORIZATION_NS = "http://data.doremus.org/vocabulary/categorization/bnf/";
  private int countResp = 0;

  public F24_PublicationExpression(String identifier) {
    super(identifier);
    this.setClass(FRBROO.F24_Publication_Expression);
  }

  public F24_PublicationExpression(Record record) {
    super(record);
    this.setClass(FRBROO.F24_Publication_Expression);

    // additional material
    parseAdditionalMaterial();

    // titles
    titleFromField(245, false).forEach(title -> {
      this.addProperty(MUS.U167_has_title_proper, title);
      this.addProperty(RDFS.label, title);
    });
    titleFromField(247, false).forEach(
      title -> this.addProperty(MUS.U168_has_parallel_title, title));
    record.getDatafieldsByCode(245, 'e').forEach(title -> this.addProperty(MUS.U67_has_subtitle, title));
    titleFromField(750, true).forEach(title -> this.addProperty(MUS.U68_has_variant_title, title));
    titleFromField(751, true).forEach(title -> this.addProperty(MUS.U68_has_variant_title, title));

    // title statements
    titleFromField(245, true).forEach(this::addTitleStatement);
    titleFromField(247, true).forEach(title -> addTitleStatement(title, true));

    // title auxiliary info
    for (DataField dataField : record.getDatafieldsByCode(245)) {
      List<Literal> literals = extractResponsibilities(dataField, true);
      for (Literal literal : literals) {
        this.addStatement(this.uri + "/responsibility/" + ++countResp, literal, MUS.M157_Statement_of_Responsibility,
          MUS.U172_has_statement_of_responsibility_relating_to_title);
      }
    }
    for (DataField dataField : record.getDatafieldsByCode(247)) {
      List<Literal> literals = extractResponsibilities(dataField);
      for (Literal literal : literals) {
        this.addStatement(this.uri + "/responsibility/" + ++countResp, literal, MUS.M157_Statement_of_Responsibility,
          MUS.U173_has_parallel_statement_of_responsibility_relating_to_title);
      }
    }


    // cast statement
    String note = record.getDatafieldsByCode(245, 'j').stream().findFirst().orElse(null);
    Stream<String> annotation = record.getDatafieldsByCode(313).stream()
      .filter(df -> df.isCode('k'))
      .map(df -> df.getString('k') + " : " + String.join(" ; " + df.getStrings('a')));
    this.addStatement(this.uri + "/cast/1", note, MUS.M155_Cast_Statement, MUS.U174_has_cast_statement, annotation);

    note = record.getDatafieldsByCode(247, 'j').stream().findFirst().orElse(null);
    this.addStatement(this.uri + "/cast/2", note, MUS.M155_Cast_Statement, MUS.U175_has_parallel_cast_statement);

    // edition
    note = record.getDatafieldsByCode(250, 'a').stream().findFirst().orElse(null);
    List<String> annotations = record.getDatafieldsByCode(351, 'a');
    annotations.add(parseEditionSupplement());
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


    // publication
    i = 0;
    for (DataField df : record.getDatafieldsByCode(260))
      this.addStatement(this.uri + "/publication/" + ++i, parsePublicationField(df),
        MUS.M160_Publication_Statement, MUS.U184_has_publication_statement);
    i = 0;
    for (DataField df : record.getDatafieldsByCode(270))
      this.addStatement(this.uri + "/publication/" + ++i, parsePublicationField(df),
        CIDOC.E33_Linguistic_Object, MUS.U186_has_printing_or_manufacture_statement);


    // other titles
    titleFromField(290, false)
      .forEach(x -> this.addProperty(MUS.U224_has_title_proper_of_multipart_monograph, x));
    Property p = (record.getDatafieldByCode(395) == null) ?
      MUS.U221_has_title_proper_of_series : MUS.U222_has_title_proper_of_sub_series;
    titleFromField(295, false)
      .forEach(x -> this.addProperty(p, x));


    // illustrative content
    annotations = record.getDatafieldsByCode(300, 'a');
    annotations.addAll(record.getDatafieldsByCode(353, 'a'));
    for (String a : annotations) {
      if (a.toLowerCase().contains("disque illustré"))
        this.addProperty(MUS.U201_has_illustrative_content, a);
      else if (a.toLowerCase().contains("disque coloré"))
        this.addProperty(MUS.U202_has_colour_details, a);
      else if (a.toLowerCase().contains("pochette"))
        this.addProperty(MUS.U200_has_supplementary_content, a);
    }

    // sponsor
    annotations = record.getDatafieldsByCode(312, 'a');
    annotations.addAll(record.getDatafieldsByCode(300, 'a').stream()
      .filter(x -> SPONSOR_PATTERN.matcher(x).find()).collect(Collectors.toList()));
    annotations.forEach(x -> this.addProperty(MUS.U199_has_sponsor, x, "fr"));

    annotations = record.getDatafieldsByCode(317, 'a');
    annotations.addAll(record.getDatafieldsByCode(300, 'a'));
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
      M167_PublicationFragment fragment = new M167_PublicationFragment(this.identifier + "/fragment/" + i, df);
      this.addProperty(CIDOC.P148_has_component, fragment);
    }

    // summary
    for (String s : record.getDatafieldsByCode(330, 'a'))
      this.addProperty(MUS.U203_has_summary_or_abstract, s);

    // categorization
    for (DataField df : record.getDatafieldsByCode(640)) {
      String code = df.getString('a');
      if (code == null || code.isEmpty()) df.getStrings('b');
      this.addProperty(MUS.U19_is_categorized_as, toBnFCategorizationUri(code));
    }

    // label
    Stream<M54_Label_Name> labelNames = record.getDatafieldsByCode(723).stream().map(M54_Label_Name::new);
    labelNames.forEach(ln -> this.addProperty(MUS.U169_was_issued_under_label_name, ln));
    for (DataField df : record.getDatafieldsByCode("028")) {
      String label = df.getString('e');
      if (label == null || label.isEmpty()) continue;
      M54_Label_Name ln = labelNames.filter(l -> label.equals(l.getLabel())).findFirst().orElse(null);
      if (ln == null) continue;
      String _id = df.getString('a');


      this.addProperty(CIDOC.P1_is_identified_by, model.createResource(this.uri + "/id/" + _id)
        .addProperty(RDF.type, CIDOC.E42_Identifier)
        .addProperty(CIDOC.P2_has_type, "Référence commerciale")
        .addProperty(FRBROO.R8_consists_of, ln.asResource())
        .addProperty(FRBROO.R8_consists_of, _id)
        .addProperty(RDFS.label, _id)
      );
    }


    // link to f22
    List<DataField> fields = record.getDatafieldsByCode(144);
    fields.addAll(record.getDatafieldsByCode(744));
    fields.stream().filter(x -> x.isCode(3)).forEach(df -> {
      String l = df.getString('l');
      Property prop = EXTRAIT_PATTERN.matcher(l).find() ?
        MUS.U59_has_partial_published_recording : MUS.U58i_is_full_published_recording_of;
      String _id = df.getString(3);
      F22_SelfContainedExpression f22 = new F22_SelfContainedExpression(_id);
      f22.addProperty(prop, this);
      this.model.add(f22.getModel());

      if (l.toLowerCase().contains("arr.")) {
        F14_IndividualWork derived = getDerivedWork();
        derived.addProperty(FRBROO.R2_is_derivative_of, new F14_IndividualWork(_id));
        F15_ComplexWork f15 = new F15_ComplexWork(_id);
        f15.add(derived);
        this.model.add(f15.getModel());
      }
    });
  }

  private F14_IndividualWork getDerivedWork() {
    F22_SelfContainedExpression f22 = new F22_SelfContainedExpression(this.identifier + "d");
    F28_ExpressionCreation f28 = new F28_ExpressionCreation(this.identifier + "d");
    F14_IndividualWork f14 = new F14_IndividualWork(this.identifier + "d");

    Resource assignment = model.createResource(f14.getUri() + "/derivation_assignment")
      .addProperty(RDF.type, MUS.M39_Derivation_Type_Assignment)
      .addProperty(CIDOC.P14_carried_out_by, BNF2RDF.BnF)
      .addProperty(CIDOC.P41_classified, f14.asResource())
      .addProperty(CIDOC.P41_classified, f22.asResource());


    // title
    List<Literal> titles = parseTitleField(record.getDatafieldByCode(245), true, false);
    titles.forEach(f22::setTitle);
    // title translation
    titles = parseTitleField(record.getDatafieldByCode(247), true, false);
    titles.forEach(f22::setVariantTitle);

    // casting
    M6_Casting casting = new M6_Casting(record, f22.getUri() + "/casting/1");
    if (casting.doesContainsInfo()) this.addProperty(MUS.U13_has_casting, casting);

    // composers and derivation type
    record.getDatafieldsByCode(700)
      .forEach(df -> {
        Person person = ArtistConverter.parseArtistField(df);
        String role = "arrangeur";
        String deriv = null;

        switch (df.getString(4)) {
          case "0010":
          case "0011":
          case "0013":
            role = "adaptateur";
            deriv = "adaptation";
            break;
          case "0050":
          case "0051":
          case "0053":
            deriv = "arrangement";
            break;
          case "0430":
            role = "harmonisateur";
            deriv = "harmonisation";
            break;
          case "0730":
            role = "transcripteur";
            deriv = "transcription";
            break;
          case "0780":
            role = "orchestrateur";
            deriv = "orchestration";
        }

        f28.addActivity(person, role);
        if (deriv != null) {
          f14.setDerivationType(deriv);
          assignment.addProperty(CIDOC.P42_assigned, deriv);
        }
      });

    f28.add(f22).add(f14);
    f14.add(f22);
    this.model.add(f28.getModel());
    return f14;
  }


  public F24_PublicationExpression(String identifier, DataField df) {
    this(identifier);

    String content = null, authors = null, illContent = null;
    switch (df.getEtiq()) {
      case "323":
        String[] parts = df.getString('a').split("/");
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
      if (!authors.contains(";") && COMMA_NAME_SURNAME_PATTERN.matcher(authors).find()) delimiter = ",";
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
      this.addProperty(MUS.U201_has_illustrative_content, illContent, "fr");
      if (illContent.contains("en coul."))
        this.addProperty(MUS.U202_has_colour_details, illContent, "fr");
      this.addProperty(MUS.U214_has_colour_content, "couleur", "fr");
    }

    F3_ManifestationProductType f3 = F3_ManifestationProductType.fromField(df, this.identifier);
    if (f3 != null) {
      f3.add(this);
      this.model.add(f3.getModel());
    }
  }

  private Resource toBnFCategorizationUri(String code) {
    if (code == null || code.isEmpty()) return null;
    code = code.trim();
    Resource r = model.createResource(BNF_CATEGORIZATION_NS + code);
    model.createResource(this.uri + "/assignment/" + code)
      .addProperty(RDF.type, CIDOC.E13_Attribute_Assignment)
      .addProperty(CIDOC.P141_assigned, r)
      .addProperty(CIDOC.P14_carried_out_by, BNF2RDF.BnF);
    return r;
  }

  private void parseAdditionalMaterial() {
    int i = 0;

    List<DataField> fields = record.getDatafieldsByCode(323).stream()
      .filter(x -> x.getString('a') != null)
      .collect(Collectors.toList());
    fields.addAll(record.getDatafieldsByCode(353).stream()
      .filter(x -> x.getString('a').matches(ADD_MATERIAL_REGEX))
      .collect(Collectors.toList()));
    if (fields.size() == 0)
      fields.addAll(record.getDatafieldsByCode(280).stream()
        .filter(x -> x.getString('e') != null)
        .collect(Collectors.toList()));

    for (DataField df : fields) {
      F24_PublicationExpression material = new F24_PublicationExpression(this.identifier + "m" + ++i, df);
      this.addProperty(CIDOC.P165_incorporates, material);
      this.model.add(material.getModel());
    }
  }

  public String parsePublicationField(DataField df) {
    // hypothesis (checked in some samples), the a b c and d to take are always the first ones
    String a = df.getString('a'),
      b = df.getString('b'),
      c = df.getString('c'),
      d = df.getString('d');

    if (c == null || c.matches("\\[?distrib.+")) return null;

    String content = "";
    if (a != null && !a.matches(UNKNOW_REGEX)) {
      content += a;
      if (b != null) content += " (" + b + ")";
      content += " : ";

    }
    if (!c.matches(UNKNOW_REGEX)) content += c;

    if (d != null && !c.matches(UNKNOW_REGEX)) {
      content += ", " + d;
    }
    if (content.isEmpty()) return null;
    return content;
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

  private void addStatement(String uri, Object text, OntClass _class, Property property) {
    addStatement(uri, text, _class, property, (Stream<String>) null);
  }

  private void addStatement(String uri, Object text, OntClass _class, Property property, List<String> annotation) {
    addStatement(uri, text, _class, property, annotation.stream());
  }

  private void addStatement(String uri, Object text, OntClass _class, Property property, Stream<String> annotation) {
    if (text == null) return;
    Literal l = null;
    if (text instanceof String) l = model.createLiteral((String) text);
    else if (text instanceof Literal) l = (Literal) text;


    Resource resource = model.createResource(uri)
      .addProperty(RDF.type, _class)
      .addProperty(RDFS.comment, l);

    if (annotation != null)
      annotation.filter(Objects::nonNull).forEach(a -> resource.addProperty(MUS.U50_has_annotation, a));
    this.addProperty(property, resource);
  }


  private List<Literal> extractResponsibilities(DataField df) {
    return extractResponsibilities(df, false);
  }

  @SuppressWarnings("StringConcatenationInLoop")
  private List<Literal> extractResponsibilities(DataField df, boolean secondaryParsing) {
    List<Literal> list = new ArrayList<>();

    String language = null;
    String current = null;
    boolean secondaryDone = false;
    boolean aOk = false;
    for (Subfield sf : df.getSubfields()) {
      String value = sf.getData();
      switch (sf.getCode()) {
        case 'a':
          aOk = true;
          break;
        case 'd':
          break;
        case 'f':
          if (current != null) list.add(model.createLiteral(current, language));
          current = value;
          break;
        case 'g':
          current += ", " + value;
          if (secondaryParsing && aOk)
            if (!secondaryDone) {
              secondaryDone = true;
              this.addStatement(this.uri + "/responsibility/" + ++countResp, current, MUS.M157_Statement_of_Responsibility,
                MUS.U178_has_statement_of_responsibility_relating_to_edition);
            } else
              this.addStatement(this.uri + "/responsibility/" + ++countResp, current, MUS.M157_Statement_of_Responsibility,
                MUS.U179_has_parallel_statement_of_responsibility_relating_to_edition);

          break;
        case 'w':
          language = Utils.intermarcExtractLang(value);
        default:
          aOk = false;
      }
    }
    if (current != null) {
      list.add(model.createLiteral(current, language));
      if (secondaryParsing && aOk)
        if (!secondaryDone)
          this.addStatement(this.uri + "/responsibility/" + ++countResp, current, MUS.M157_Statement_of_Responsibility,
            MUS.U178_has_statement_of_responsibility_relating_to_edition);
        else
          this.addStatement(this.uri + "/responsibility/" + ++countResp, current, MUS.M157_Statement_of_Responsibility,
            MUS.U179_has_parallel_statement_of_responsibility_relating_to_edition);
    }
    return list;
  }

  private void addTitleStatement(Literal title) {
    addTitleStatement(title, false);
  }

  private void addTitleStatement(Literal title, boolean asParallel) {
    Resource titleStatement = model.createResource(this.uri + "/title")
      .addProperty(RDF.type, MUS.M156_Title_Statement)
      .addProperty(RDFS.comment, title);

    if (!asParallel) {
      List<String> notes = record.getDatafieldsByCode(300, 'a');
      notes.addAll(record.getDatafieldsByCode(350, 'a'));
      notes.stream()
        .filter(s -> s.toLowerCase().startsWith("titre"))
        .filter(s -> s.toLowerCase().contains("titre restitué") ||
          (!s.contains("compositeur") && !s.contains("interprète")))
        .forEach(s -> titleStatement.addProperty(MUS.U50_has_annotation, s));

      record.getDatafieldsByCode(750).stream()
        .filter(x -> x.isCode('k'))
        .map(x -> parseTitleField(x, true, true))
        .flatMap(List::stream)
        .forEach(s -> titleStatement.addProperty(MUS.U50_has_annotation, s));
    }
    this.addProperty(
      asParallel ? MUS.U171_has_parallel_title_statement : MUS.U170_has_title_statement,
      titleStatement);
  }

  private List<Literal> parseTitleField(DataField df, boolean parseE, boolean parseK) {
    List<Literal> list = new ArrayList<>();

    char lastCode = 0;
    String currentTitle = null, language = null, currentK = null;

    for (Subfield sf : df.getSubfields()) {
      String value = sf.getData();
      switch (sf.getCode()) {
        case 'k':
          if (parseK) currentK = value;
          break;
        case 'a':
        case 'b':
        case 'c':
          if (currentTitle != null) {
            if (currentK != null) currentTitle = currentK + " \"" + currentTitle + "\"";
            currentK = null;
            list.add(model.createLiteral(currentTitle, language));
          }
          currentTitle = value
            .replaceAll("\\|", "")
            .replaceAll("\\.{3} ?\\[etc\\.] ?$", "")
            .replaceAll("^(?i)(suivi d[e']|pr[eèé]c[eèé]d[eèé][eèé]? d[e']|avec)", "")
            .replaceAll("\\s+", " ");
          break;
        case 'e':
          if (parseE)
            currentTitle += " : " + value;
          break;
        case 'h':
          currentTitle += ". " + value;
          break;
        case 'i':
          currentTitle += (lastCode == 'h' ? ", " : ". ") + value;
          break;
        case 'w':
          language = Utils.intermarcExtractLang(value);
      }
      lastCode = sf.getCode();
      if (currentTitle != null) {
        if (currentK != null) currentTitle = currentK + " \"" + currentTitle + "\"";
        list.add(model.createLiteral(currentTitle, language));
      }
    }
    return list;
  }

  private Stream<Literal> titleFromField(int field, boolean parseE) {
    return titleFromField(record.getDatafieldsByCode(field), parseE);
  }

  private Stream<Literal> titleFromField(List<DataField> dfs, boolean parseE) {
    return dfs.stream()
      .map(df -> parseTitleField(df, parseE, false))
      .flatMap(List::stream);
  }


  public F24_PublicationExpression add(F22_SelfContainedExpression expression) {
    this.resource.addProperty(CIDOC.P165_incorporates, expression.asResource());
    return this;
  }

}
