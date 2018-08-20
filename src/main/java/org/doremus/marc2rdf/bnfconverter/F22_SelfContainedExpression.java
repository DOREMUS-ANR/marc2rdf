package org.doremus.marc2rdf.bnfconverter;

import net.sf.junidecode.Junidecode;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.CorporateBody;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Person;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/***
 * Correspond à la description développée de l'expression représentative
 ***/
public class F22_SelfContainedExpression extends DoremusResource {
  private final static String CATALOG_FALLBACK_REGEX = "([a-z]+) ?(\\d+)";
  private final static Pattern CATALOG_FALLBACK_PATTERN = Pattern.compile(CATALOG_FALLBACK_REGEX);

  private final static String DEDICACE_STRING = "(?i)(?:Sur l'édition, d|D)[ée]dicaces?(?: ?: ?)?(.+)";
  private final static Pattern DEDICACE_PATTERN = Pattern.compile(DEDICACE_STRING);
  private final static String DEDICACE_PARSE_REGEX = "(?:dédicaces? (?:: )?|en souvenir |^)" +
    "(?:[Ààa](?: la mémoire d[e'] ?| l'| )|aux? |pour |de )([^()\\n;]+)";
  private final static Pattern DEDICACE_PARSE_PATTERN = Pattern.compile(DEDICACE_PARSE_REGEX, Pattern.CASE_INSENSITIVE);
  private final static String DEDICACE_PARSE_SEPARATOR = "(,| et (?:à |aux? |l')?)";
  private static final String TITLE_REGEX = "^(mad(?:ame|emoiselle)|Monsieur|m(?:lle|me|r.)|dr |(vi)?comte(?:sse)?|prince(?:sse)?|(archi)?duc(?:hesse)?|soprano)";

  public List<Literal> titles;

  public F22_SelfContainedExpression(Record record, F28_ExpressionCreation f28) {
    super(record);

    this.setClass(FRBROO.F22_Self_Contained_Expression);
    this.addProperty(DC.identifier, record.getIdentifier());

    String ark = record.getAttrByName("IDPerenne").getData();
    if (ark != null)
      this.resource.addProperty(OWL.sameAs, model.createResource("http://data.bnf.fr/" + ark));

    this.titles = new ArrayList<>();

    int dedCount = 0;
    for (String dedication : getDedicace()) {
      dedication = dedication.replaceAll("\\s+", " ").trim();
      String dedUri = this.uri + "/dedication/" + ++dedCount;
      Resource ded = model.createResource(dedUri)
        .addProperty(RDF.type, MUS.M15_Dedication_Statement)
        .addProperty(RDFS.comment, dedication, "fr")
        .addProperty(CIDOC.P3_has_note, dedication, "fr");
      this.resource.addProperty(MUS.U44_has_dedication_statement, ded);

      Matcher m = DEDICACE_PARSE_PATTERN.matcher(dedication
        .replaceAll("\"", "")
        .replaceAll("\\([^(]+\\)", ""));
      while (m.find()) {
        String target = m.group(1)
          .replaceAll("\\?\\[\\]", "")
          .replaceAll(", ([^ A-Z]+.+)", "");
        for (String t : target.split(DEDICACE_PARSE_SEPARATOR)) {
          t = t.trim().replaceAll("^l[ae]", "");
          if (t.contains("élève") || t.contains("mort") || t.contains("victimes") ||
            t.matches("^(sa|son|ma|mon|de) .+"))
            continue;
          t = t.replaceAll(TITLE_REGEX, "").trim();
          if (t.isEmpty()) continue;
          Resource r = Person.getFromDoremus(t, null);
          if (r == null) r = CorporateBody.getFromDoremus(t);

          if (r != null) {
            ded.addProperty(CIDOC.P67_refers_to, r);
            model.add(r.listProperties());
          }
        }
      }
    }

    List<Literal> s444 = getTitle(444, true);
    List<Literal> ns444 = getTitle(444, false);
    List<Literal> a144 = getTitle(144, true);
    a144.addAll(getTitle(144, false));

    for (Literal title : s444)
      this.resource.addProperty(MUS.U70_has_original_title, title).addProperty(RDFS.label, title);
    this.titles = s444;

    ns444.forEach(this::setVariantTitle);
    a144.forEach(title -> this.addProperty(MUS.U71_has_uniform_title, title));

    if (s444.size() == 0) {
      a144.addAll(ns444);
      if (a144.size() > 0) {
        this.resource.addProperty(RDFS.label, a144.get(0));
        this.titles.add(a144.get(0));
      }
    }


    for (String catalog : getCatalog()) {
      String[] catalogParts = catalog.split(" ", 2);
      String catalogName = null, catalogNum = null;

      //fix: sometimes the catalog is written as "C172" instead of "C 172"
      // I try to separate letters and numbers
      if (catalogParts.length < 2) {
        Matcher matcher = CATALOG_FALLBACK_PATTERN.matcher(catalog);
        if (matcher.find()) {
          catalogParts = new String[]{matcher.group(1), matcher.group(2)};
        }
      }
      if (catalogParts.length > 1) {
        catalogName = catalogParts[0].trim();
        catalogNum = catalogParts[1].trim();
      }
      if ("WoO".equalsIgnoreCase(catalogName)) {
        addOpus(catalog);
        continue;
      }
      String label = (catalogName != null) ? (catalogName + " " + catalogNum) : catalog;

      Resource M1CatalogStatement = model.createResource(this.uri + "/catalog/" + label.replaceAll("[ /]", "_"))
        .addProperty(RDF.type, MUS.M1_Catalogue_Statement)
        .addProperty(RDFS.label, label)
        .addProperty(CIDOC.P3_has_note, catalog.trim());


      if (catalogNum != null) {
        Resource match = VocabularyManager.getMODS("catalogue").findModsResource(catalogName, f28.getComposerUris());

        if (match == null)
          M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, catalogName);
        else M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, match);

        M1CatalogStatement.addProperty(MUS.U41_has_catalogue_number, catalogNum);
      } else log("Not parsable catalog: " + catalog);


      this.resource.addProperty(MUS.U16_has_catalogue_statement, M1CatalogStatement);
    }

    String opus = getOpus();
    if (opus != null) {
      if (opus.matches("^\\[(.+)\\]$")) // fix "[Op. 3, no 2]"
        opus = opus.substring(1, opus.length() - 1);

      addOpus(opus);
    }

    for (String note : getNote()) addNote(note);

    for (String key : getKey()) {
      key = key.replaceFirst("\\.$", "").trim(); //remove final dot
      Literal label = model.createLiteral(key, "fr");
      String keyUri = this.uri + "/key/" + Junidecode.unidecode(key).toLowerCase().replaceAll(" ", "_");

      this.resource.addProperty(MUS.U11_has_key,
        model.createResource(keyUri)
          .addProperty(RDF.type, MUS.M4_Key)
          .addProperty(CIDOC.P1_is_identified_by, label)
          .addProperty(RDFS.label, label)
      );
    }

    List<Resource> genreList = getGenre();
    for (Resource genre : genreList) this.resource.addProperty(MUS.U12_has_genre, genre);

    String orderNumber = getOrderNumber();
    if (orderNumber != null) {
      List<Integer> range = Utils.toRange(orderNumber);
      if (range == null)
        this.resource.addProperty(MUS.U10_has_order_number, Utils.toSafeNumLiteral(orderNumber));
      else
        for (int i : range) this.resource.addProperty(MUS.U10_has_order_number, model.createTypedLiteral(i));
    }

    M6_Casting casting = new M6_Casting(record, this.uri + "/casting/1");
    if(casting.doesContainsInfo())
      this.addProperty(MUS.U13_has_casting, casting);

    List<String> characters = record.getDatafieldsByCode("608", 'b');
    if (characters.size() > 0)
      this.addProperty(MUS.U33_has_set_of_characters,
        model.createResource(this.uri + "/character")
          .addProperty(RDF.type, MUS.M33_Set_of_Characters)
          .addProperty(CIDOC.P3_has_note, String.join("; ", characters)));
  }

  public F22_SelfContainedExpression(String identifier) {
    super(identifier);
  }

  private void addOpus(String note) {
    Property numProp = MUS.U42_has_opus_number,
      subProp = MUS.U17_has_opus_statement;

    if (note.startsWith("WoO")) {
      numProp = MUS.U69_has_WoO_number;
      subProp = MUS.U76_has_WoO_subnumber;
    }

    String[] opusParts = new String[]{note};
    if (note.matches(".*" + Utils.opusSubnumberRegex + ".*"))
      opusParts = note.split(Utils.opusSubnumberRegex, 2);


    String number = opusParts[0].replaceAll(Utils.opusHeaderRegex, "").trim();
    String subnumber = opusParts.length > 1 ? opusParts[1].replaceAll("no", "").trim() : null;

    String id = number;
    if (subnumber != null) id += "-" + subnumber;

    Resource M2OpusStatement = model.createResource(this.uri + "/opus/" + id.replaceAll(" ", "_"))
      .addProperty(RDF.type, MUS.M2_Opus_Statement)
      .addProperty(CIDOC.P3_has_note, note)
      .addProperty(RDFS.label, note)
      .addProperty(numProp, number);

    if (subnumber != null)
      M2OpusStatement.addProperty(subProp, subnumber);

    this.resource.addProperty(MUS.U17_has_opus_statement, M2OpusStatement);
  }

  private List<String> getDedicace() {
    List<String> dedicaces = new ArrayList<>();
    for (String ded : record.getDatafieldsByCode("600", 'a')) {
      Matcher m = DEDICACE_PATTERN.matcher(ded);

      while (m.find()) {
        String content = m.group(1);
        if (content.startsWith("\"") || content.startsWith("Dédicaces :")) {
          dedicaces.addAll(Arrays.asList(content.split(";")));
        } else dedicaces.add(ded);
      }
    }
    return dedicaces;
  }

  private List<Literal> getTitle(int code, boolean significativeWanted) {
    List<Literal> titleList = new ArrayList<>();
    List<DataField> titleFields = record.getDatafieldsByCode(code);

    String DESCR_CHARS = "ejbtunpkqfcg";

    for (DataField field : titleFields) {
      if (!field.isCode('a')) continue;

      StringBuilder title = new StringBuilder(field.getString('a'));
      String language = null;

      if (title.length() == 0) continue;

      if (!isMeaningfulTitle(title.toString())) {
        if (significativeWanted) continue;
        for (char c : DESCR_CHARS.toCharArray())
          if (field.isCode(c)) title.append(". ").append(field.getString(c));
      }

      if (field.isCode('h')) title.append(". ").append(field.getString('h'));
      if (field.isCode('i')) title.append(". ").append(field.getString('i'));

      if (field.isCode('w'))
        language = Utils.intermarcExtractLang(field.getString('w'));

      Literal titleLiteral = (language == null || language.isEmpty()) ?
        this.model.createLiteral(title.toString().trim()) :
        this.model.createLiteral(title.toString().trim(), language);

      titleList.add(titleLiteral);
    }

    return titleList;
  }

  private List<String> notMeaningfulTitles = null;

  private boolean isMeaningfulTitle(String title) {
    if (notMeaningfulTitles == null) loadNotMeaningfulTitles();
    return notMeaningfulTitles.stream().noneMatch(str -> str.trim().equals(title));
  }

  private void loadNotMeaningfulTitles() {
    notMeaningfulTitles = new ArrayList<>();
    File file = new File(this.getClass().getClassLoader().getResource("notMeaningfulTitles.txt").getFile());

    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        notMeaningfulTitles.add(line.trim());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<String> getCatalog() {
    return record.getDatafieldsByCode("144", 'k');
  }

  private String getOpus() {
    DataField field = record.getDatafieldByCode("144");

    if (field == null || !field.isCode('p')) return null;
    return field.getSubfield('p').getData();
  }

  private List<String> getNote() {
    return record.getDatafieldsByCode("600", 'a').stream()
      .filter(note -> !note.contains("éd.") && !note.contains("édition") &&
        !M42_PerformedExpressionCreation.performancePattern.matcher(note).find() &&
        !note.matches(DEDICACE_STRING) &&
        !note.matches(F28_ExpressionCreation.MOTIVATIONS_REGEX) &&
        !note.matches(F28_ExpressionCreation.INFLUENCE_REGEX) &&
        !note.matches(F28_ExpressionCreation.DATE_REGEX_1) && !note.matches(F28_ExpressionCreation.DATE_REGEX_2))
      .map(String::trim)
      .collect(Collectors.toList());
  }

  private List<String> getKey() {
    List<String> keys = record.getDatafieldsByCode("444", 't');
    keys.addAll(record.getDatafieldsByCode("144", 't'));
    return keys;
  }

  private String getOrderNumber() {
    List<String> fields = record.getDatafieldsByCode("444", 'n');
    fields.addAll(record.getDatafieldsByCode("144", 'n'));
    if (fields.isEmpty()) return null;

    return fields.get(0).replaceFirst("No", "").trim();
  }

  private List<Resource> getGenre() {
    List<Resource> genres = new ArrayList<>();

    // search the code of the genre in the file
    for (ControlField field : record.getControlfieldsByCode("008")) {
      String codeGenre = field.getData().substring(18, 21)
        .replace(".", "")
        .replace("#", "").trim().toLowerCase();

      // special case
      if (codeGenre.isEmpty()) continue;
      if (codeGenre.equals("uu")) continue; // unknown form

      Resource res = VocabularyManager.getVocabulary("genre-iaml").getConcept(codeGenre);

      if (res == null) log("Code genre not found: " + codeGenre);
      else genres.add(res);
    }

    return genres;
  }

  public F22_SelfContainedExpression add(F30_PublicationEvent f30) {
    this.addProperty(MUS.U4_had_princeps_publication, f30.asResource());
    return this;
  }

  public F22_SelfContainedExpression addPremiere(M42_PerformedExpressionCreation m42) {
    this.addProperty(MUS.U5_had_premiere, m42.getMainPerformance());
    return this;
  }

  public F22_SelfContainedExpression addMovement(F22_SelfContainedExpression movement) {
    this.addProperty(FRBROO.R5_has_component, movement.asResource());
    return this;
  }

  public void setTitle(Literal title) {
    this.addProperty(CIDOC.P102_has_title, title);
  }

  public void setVariantTitle(Literal title) {
    this.addProperty(MUS.U68_has_variant_title, title);
  }
}
