package org.doremus.marc2rdf.bnfconverter;

import net.sf.junidecode.Junidecode;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Correspond à la description développée de l'expression représentative
 ***/
public class F22_SelfContainedExpression extends DoremusResource {
  private final static String catalogFallbackRegex = "([a-z]+) ?(\\d+)";
  private final static String DEDICACE_STRING = "(?i)(?:Sur l'édition, d|D)[ée]dicaces?(?: ?: ?)?(.+)";
  private final static Pattern DEDICACE_PATTERN = Pattern.compile(DEDICACE_STRING);

  public List<Literal> titles;

  public F22_SelfContainedExpression(Record record, F28_ExpressionCreation f28) throws URISyntaxException {
    super(record);

    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression);
    this.resource.addProperty(DCTerms.identifier, record.getIdentifier());

    String ark = record.getAttrByName("IDPerenne").getData();
    if (ark != null)
      this.resource.addProperty(OWL.sameAs, model.createResource("http://data.bnf.fr/" + ark));

    this.titles = new ArrayList<>();

    int dedCount = 0;
    for (String dedication : getDedicace()) {
      dedication = dedication.trim();
      String dedUri = this.uri.toString() + "/dedication/" + ++dedCount;
      this.resource.addProperty(MUS.U44_has_dedication_statement, model.createResource(dedUri)
        .addProperty(RDF.type, MUS.M15_Dedication_Statement)
        .addProperty(RDFS.comment, dedication, "fr")
        .addProperty(CIDOC.P3_has_note, dedication, "fr"));
    }

    List<Literal> s444 = getTitle(444, true);
    List<Literal> ns444 = getTitle(444, false);
    List<Literal> a144 = getTitle(144, true);
    a144.addAll(getTitle(144, false));

    for (Literal title : s444)
      this.resource.addProperty(MUS.U70_has_original_title, title).addProperty(RDFS.label, title);
    this.titles = s444;

    for (Literal title : ns444)
      this.resource.addProperty(MUS.U68_has_variant_title, title);
    for (Literal title : a144)
      this.resource.addProperty(MUS.U71_has_uniform_title, title);

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
        Pattern pattern = Pattern.compile(catalogFallbackRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(catalog);
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

      Resource M1CatalogStatement = model.createResource(this.uri.toString() + "/catalog/" + label.replaceAll("[ /]", "_"))
        .addProperty(RDF.type, MUS.M1_Catalogue_Statement)
        .addProperty(RDFS.label, label)
        .addProperty(CIDOC.P3_has_note, catalog.trim());


      if (catalogNum != null) {
        Resource match = VocabularyManager.getMODS("catalogue").findModsResource(catalogName, f28.getComposerUris());

        if (match == null)
          M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, catalogName);
        else M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, match);

        M1CatalogStatement.addProperty(MUS.U41_has_catalogue_number, catalogNum);
      } else {
        System.out.println("Not parsable catalog: " + catalog);
        // TODO what to do with not parsable catalogs?
      }

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

    List<M23_Casting_Detail> castDetails = new ArrayList<>();
    for (String castingDetail : getCastMembers()) {
      String mopCode = castingDetail.substring(0, 2);
      String mopNum = castingDetail.substring(2, 4);
      String iamlCode = Utils.itermarc2mop(mopCode);

      if (iamlCode == null)
        System.out.println("Iaml code not found for mop: " + mopCode);
      else
        castDetails.add(new M23_Casting_Detail(iamlCode, mopNum));
    }
    for (String castingDetail : getSoloists()) {
      String mopCode = castingDetail.substring(0, 2);
      String mopNum = castingDetail.substring(2, 4);
      String iamlCode = Utils.itermarc2mop(mopCode);

      if (iamlCode == null)
        System.out.println("Iaml code not found for mop: " + mopCode);
      else
        castDetails.add(new M23_Casting_Detail(iamlCode, mopNum, true));
    }

    List<String> castingNotes = getCasting();

    // if I have the info about casting
    if (castingNotes.size() > 0 || castDetails.size() > 0) {
      String castingUri = this.uri.toString() + "/casting/1";
      Resource M6Casting = model.createResource(castingUri);
      M6Casting.addProperty(RDF.type, MUS.M6_Casting);

      // casting notes
      for (String castingString : castingNotes)
        M6Casting.addProperty(CIDOC.P3_has_note, castingString)
          .addProperty(RDFS.comment, castingString);

      // casting details
      int detailNum = 0;
      for (M23_Casting_Detail detail : castDetails) {
        Resource M23CastingDetail = detail.asResource(castingUri + "/detail/" + ++detailNum);
        M6Casting.addProperty(MUS.U23_has_casting_detail, M23CastingDetail);
        model.add(M23CastingDetail.getModel());
      }

      this.resource.addProperty(MUS.U13_has_casting, M6Casting);
    }

    List<String> characters = record.getDatafieldsByCode("608", 'b');
    if (characters.size() > 0)
      this.resource.addProperty(MUS.U33_has_set_of_characters,
        model.createResource(this.uri.toString() + "/character")
          .addProperty(RDF.type, MUS.M33_Set_of_Characters)
          .addProperty(CIDOC.P3_has_note, String.join("; ", characters)));
  }

  public F22_SelfContainedExpression(String identifier) throws URISyntaxException {
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
        if (content.startsWith("\""))
          dedicaces.addAll(Arrays.asList(content.split(";")));
        else dedicaces.add(ded);
      }
    }
    return dedicaces;
  }

  private List<Literal> getTitle(int code, boolean significativeWanted) {
    List<Literal> titleList = new ArrayList<>();


    List<DataField> titleFields = record.getDatafieldsByCode(code);


    for (DataField field : titleFields) {
      if (!field.isCode('a')) continue;

      StringBuilder title = new StringBuilder(field.getSubfield('a').getData().trim());
      String language = null;

      if (title.length() == 0) continue;

      if (!isMeaningfulTitle(title.toString())) {
        if (significativeWanted) continue;
        else
          for (char c : new char[]{'e', 'j', 'b', 't', 'u', 'n', 'p', 'k', 'q', 'f', 'c', 'g'})
            if (field.isCode(c)) title.append(". ").append(field.getSubfield(c).getData().trim());
      }

      if (field.isCode('h')) title.append(". ").append(field.getSubfield('h').getData().trim());
      if (field.isCode('i')) title.append(". ").append(field.getSubfield('i').getData().trim());

      if (field.isCode('w'))
        language = Utils.intermarcExtractLang(field.getSubfield('w').getData());

      Literal titleLiteral = (language == null || language.isEmpty()) ?
        this.model.createLiteral(title.toString().trim()) :
        this.model.createLiteral(title.toString().trim(), language);

      titleList.add(titleLiteral);
    }

    return titleList;
  }

  private List<String> notMeaningfulTitles = null;

  private boolean isMeaningfulTitle(String title) {
    if (notMeaningfulTitles == null) {
      try {
        loadNotMeaningfulTitles();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return !notMeaningfulTitles.stream().anyMatch(str -> str.trim().equals(title));
  }

  private void loadNotMeaningfulTitles() throws IOException {
    notMeaningfulTitles = new ArrayList<>();
    File file = new File(this.getClass().getClassLoader().getResource("notMeaningfulTitles.txt").getFile());

    try (Scanner scanner = new Scanner(file)) {

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        notMeaningfulTitles.add(line.trim());
      }

      scanner.close();

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
    List<String> notes = new ArrayList<>();

    for (String note : record.getDatafieldsByCode("600", 'a')) {
      if (!note.contains("éd.") && !note.contains("édition") &&
        !M42_PerformedExpressionCreation.performancePattern.matcher(note).find() &&
        !note.matches(DEDICACE_STRING) &&
        !note.matches(F28_ExpressionCreation.MOTIVATIONS_REGEX) &&
        !note.matches(F28_ExpressionCreation.INFLUENCE_REGEX) &&
        !note.matches(F28_ExpressionCreation.DATE_REGEX_1) && !note.matches(F28_ExpressionCreation.DATE_REGEX_2)) {
        notes.add(note.trim());
      }
    }
    return notes;
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
      String codeGenre = field.getData().substring(18, 21).replace(".", "").replace("#", "").trim().toLowerCase();

      // special case
      if (codeGenre.isEmpty()) continue;
      if (codeGenre.equals("uu")) continue; // unknown form

      Resource res = VocabularyManager.getVocabulary("genre-iaml").getConcept(codeGenre);

      if (res == null)
        System.out.println("Code genre not found: " + codeGenre + " in record " + record.getIdentifier());
      else genres.add(res);
    }

    return genres;
  }

  /***********************************
   * Casting
   ***********************************/
  private List<String> getCasting() {
    return record.getDatafieldsByCode("144", 'b');
  }

  private List<String> getCastMembers() {
    return record.getDatafieldsByCode("048", 'a');
  }

  private List<String> getSoloists() {
    return record.getDatafieldsByCode("048", 'b');
  }


  public F22_SelfContainedExpression add(F30_PublicationEvent f30) {
    this.resource.addProperty(MUS.U4_had_princeps_publication, f30.asResource());
    return this;
  }

  public F22_SelfContainedExpression addPremiere(M42_PerformedExpressionCreation m42) {
    this.resource.addProperty(MUS.U5_had_premiere, m42.getMainPerformance());
    return this;
  }

  public F22_SelfContainedExpression addMovement(F22_SelfContainedExpression movement) {
    this.resource.addProperty(FRBROO.R5_has_component, movement.asResource());
    return this;
  }
}
