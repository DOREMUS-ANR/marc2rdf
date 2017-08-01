package org.doremus.marc2rdf.bnfconverter;

import net.sf.junidecode.Junidecode;
import org.apache.jena.rdf.model.Literal;
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
import org.doremus.vocabulary.VocabularyManager;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Correspond à la description développée de l'expression représentative
 ***/
public class F22_SelfContainedExpression extends DoremusResource {
  private final String catalogFallbackRegex = "([a-z]+) ?(\\d+)";

  public F22_SelfContainedExpression(Record record, F28_ExpressionCreation f28) throws URISyntaxException {
    super(record);

    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression);
    this.resource.addProperty(DCTerms.identifier, record.getIdentifier());

    String ark = record.getAttrByName("IDPerenne").getData();
    if (ark != null)
      this.resource.addProperty(OWL.sameAs, model.createResource("http://data.bnf.fr/" + ark));


    /**************************** Expression: Context for the expression ********************/
    String dedication = getDedicace();
    if (dedication != null) {
      this.resource.addProperty(MUS.U44_has_dedication_statement, model.createResource(this.uri.toString() + "/dedication")
        .addProperty(RDF.type, MUS.M15_Dedication_Statement)
        .addProperty(CIDOC.P3_has_note, this.model.createLiteral(dedication, "fr")));
    }

    /**************************** Expression: Title *****************************************/
    for (Literal title : getTitle())
      this.resource.addProperty(MUS.U70_has_title, title).addProperty(RDFS.label, title);


    /**************************** Expression: Catalogue *************************************/
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

      String label = (catalogName != null) ? (catalogName + " " + catalogNum) : catalog;

      Resource M1CatalogStatement = model.createResource(this.uri.toString() + "/catalog/" + label.replaceAll("[ /]", "_"))
        .addProperty(RDF.type, MUS.M1_Catalogue_Statement)
        .addProperty(RDFS.label, label)
        .addProperty(CIDOC.P3_has_note, catalog.trim());


      if (catalogNum != null) {
        Resource match = VocabularyManager.getMODS("catalogue").findModsResource(catalogName, f28.getComposers());

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

    /**************************** Expression: Opus ******************************************/
    String opus = getOpus();
    if (opus != null) {
      if (opus.matches("^\\[(.+)\\]$")) // fix "[Op. 3, no 2]"
        opus = opus.substring(1, opus.length() - 1);

      String[] opusParts = new String[]{opus};
      if (opus.matches(".*" + Utils.opusSubnumberRegex + ".*"))
        opusParts = opus.split(Utils.opusSubnumberRegex, 2);


      String number = opusParts[0].replaceAll(Utils.opusHeaderRegex, "").trim();
      String subnumber = opusParts.length > 1 ? opusParts[1].replaceAll("no", "").trim() : null;

      String id = number;
      if (subnumber != null) id += "-" + subnumber;

      Resource M2OpusStatement = model.createResource(this.uri + "/opus/" + id.replaceAll(" ", "_"))
        .addProperty(RDF.type, MUS.M2_Opus_Statement)
        .addProperty(CIDOC.P3_has_note, opus)
        .addProperty(RDFS.label, opus)
        .addProperty(MUS.U42_has_opus_number, number);

      if (subnumber != null) M2OpusStatement.addProperty(MUS.U43_has_opus_subnumber, subnumber);

      this.resource.addProperty(MUS.U17_has_opus_statement, M2OpusStatement);
    }

    /**************************** Expression: ***********************************************/
    for (String note : getNote()) this.resource.addProperty(CIDOC.P3_has_note, note);

    /**************************** Expression: key *******************************************/
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

    /*************************** Expression: Genre *****************************************/
    List<Resource> genreList = getGenre();
    for (Resource genre : genreList) this.resource.addProperty(MUS.U12_has_genre, genre);

    /**************************** Expression: Order Number **********************************/
    String orderNumber = getOrderNumber();
    if (orderNumber != null) {
      List<Integer> range = Utils.toRange(orderNumber);
      if (range == null)
        this.resource.addProperty(MUS.U10_has_order_number, Utils.toSafeNumLiteral(orderNumber));
      else
        for (int i : range) this.resource.addProperty(MUS.U10_has_order_number, model.createTypedLiteral(i));
    }

    /**************************** Expression: Casting ***************************************/
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
        M6Casting.addProperty(CIDOC.P3_has_note, castingString);

      // casting details
      int detailNum = 0;
      for (M23_Casting_Detail detail : castDetails) {
        Resource M23CastingDetail = detail.asResource(castingUri + "/detail/" + ++detailNum);
        M6Casting.addProperty(MUS.U23_has_casting_detail, M23CastingDetail);
        model.add(M23CastingDetail.getModel());
      }

      this.resource.addProperty(MUS.U13_has_casting, M6Casting);
    }
  }


  /***********************************
   * La dedicace
   ***********************************/

  private String getDedicace() {
    for (String dedicace : record.getDatafieldsByCode("600", 'a')) {
      if (dedicace.startsWith("Dédicace")) return dedicace;
    }
    return null;
  }

  /***********************************
   * Le titre
   **************************************/
  private List<Literal> getTitle() {
    List<Literal> titleList = new ArrayList<>();

    List<DataField> titleFields = record.getDatafieldsByCode("444");
    titleFields.addAll(record.getDatafieldsByCode("144"));

    for (DataField field : titleFields) {
      if (!field.isCode('a')) continue;

      String title = field.getSubfield('a').getData().trim(),
        language = null;

      if (title.isEmpty()) continue;

      if (field.isCode('h')) title += " | " + field.getSubfield('h').getData().trim();
      if (field.isCode('i')) title += " | " + field.getSubfield('i').getData().trim();

      if (field.isCode('w'))
        language = Utils.intermarcExtractLang(field.getSubfield('w').getData());

      Literal titleLiteral = (language == null || language.isEmpty()) ?
        this.model.createLiteral(title.trim()) :
        this.model.createLiteral(title.trim(), language);

      titleList.add(titleLiteral);
    }

    return titleList;
  }

  /***********************************
   * Le catalogue
   ***********************************/
  private List<String> getCatalog() {
    return record.getDatafieldsByCode("144", 'k');
  }

  /***********************************
   * L'opus
   ***********************************/
  private String getOpus() {
    DataField field = record.getDatafieldByCode("144");

    if (field == null || !field.isCode('p')) return null;
    return field.getSubfield('p').getData();
  }

  /***********************************
   * Note Libre
   ***********************************/
  private List<String> getNote() {
    List<String> notes = new ArrayList<>();

    for (String note : record.getDatafieldsByCode("600", 'a')) {
      if (!(note.contains("Date de composition")) && !(note.contains("Dates de composition")) && !(note.contains("comp."))
        && !(note.contains("1re éd.")) && !(note.contains("éd.")) && !(note.contains("édition"))
        && !(note.contains("1re exécution")) && !(note.contains("1re représentation")) && !(note.startsWith("Dédicace"))) {

        notes.add(note.trim());
      }
    }
    return notes;
  }

  /***********************************
   * Key (Tonalite)
   ***********************************/
  private List<String> getKey() {
    List<String> keys = record.getDatafieldsByCode("444", 't');
    keys.addAll(record.getDatafieldsByCode("144", 't'));

    return keys;
  }

  /***********************************
   * Le numero d'ordre
   ***********************************/
  private String getOrderNumber() {
    List<String> fields = record.getDatafieldsByCode("444", 'n');
    fields.addAll(record.getDatafieldsByCode("144", 'n'));

    if (fields.isEmpty()) return null;

    return fields.get(0).replaceFirst("No", "").trim();
  }

  /*****************************************
   * Les genres
   *************************************/
  private List<Resource> getGenre() {
    List<Resource> genres = new ArrayList<>();

    // search the code of the genre in the file
    for (ControlField field : record.getControlfieldsByCode("008")) {
      String codeGenre = field.getData().substring(18, 21).replace(".", "").replace("#", "").trim().toLowerCase();

      // special case
      if (codeGenre.isEmpty()) continue;
      if (codeGenre.equals("uu")) codeGenre = "uuu";

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
    this.resource.addProperty(MUS.U5_had_premiere, m42.asResource());
    return this;
  }
}
