package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.Converter;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/***
 * Correspond à la description développée de l'expression représentative
 ***/
public class F22_SelfContainedExpression extends DoremusResource {
  public F22_SelfContainedExpression(Record record) throws URISyntaxException {
    super(record);

    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression);

    /**************************** Expression: Context for the expression ********************/
    String dedication = getDedicace();
    if (dedication != null) {
      this.resource.addProperty(CIDOC.P67_refers_to, model.createResource(this.uri.toString()+"/dedication")
        .addProperty(RDF.type, MUS.M15_Dedication)
        .addProperty(CIDOC.P3_has_note, this.model.createLiteral(dedication, "fr")));
    }

    /**************************** Expression: Title *****************************************/
    for (Literal title : getTitle()) this.resource.addProperty(CIDOC.P102_has_title, title);


    /**************************** Expression: Catalogue *************************************/
    for (String catalog : getCatalog()) {
      Resource M1CatalogStatement = model.createResource()
        .addProperty(RDF.type, MUS.M1_Catalogue_Statement)
        .addProperty(CIDOC.P3_has_note, catalog);

      String[] catalogParts = catalog.split(" ");

      M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, catalogParts[0])
        .addProperty(MUS.U41_has_catalogue_number, catalogParts[1]);

      this.resource.addProperty(MUS.U16_has_catalogue_statement, M1CatalogStatement);
    }

    /**************************** Expression: Opus ******************************************/
    String opus = getOpus();
    if (opus != null) {
      Resource M2OpusStatement = model.createResource()
        .addProperty(RDF.type, MUS.M2_Opus_Statement)
        .addProperty(CIDOC.P3_has_note, opus);

      String[] opusParts = opus.split(",");
      M2OpusStatement.addProperty(MUS.U42_has_opus_number, opusParts[0].replaceAll("Op\\.", "").trim());

      if (opusParts.length > 1) {
        M2OpusStatement.addProperty(MUS.U43_has_opus_subnumber, opusParts[1].replaceAll("no", "").trim());
      }

      this.resource.addProperty(MUS.U17_has_opus_statement, M2OpusStatement);
    }

    /**************************** Expression: ***********************************************/
    for (String note : getNote()) this.resource.addProperty(CIDOC.P3_has_note, note);

    /**************************** Expression: key *******************************************/
    for (String key : getKey()) {
      this.resource.addProperty(MUS.U11_has_key, model.createResource()
        .addProperty(RDF.type, MUS.M4_Key)
        .addProperty(CIDOC.P1_is_identified_by, model.createLiteral(key, "fr"))
      );
    }

    /*************************** Expression: Genre *****************************************/
    List<Resource> genreList = getGenre();
    for (Resource genre : genreList) this.resource.addProperty(MUS.U12_has_genre, genre);

    /**************************** Expression: Order Number **********************************/
    String orderNumber = getOrderNumber();
    if (orderNumber != null) this.resource.addProperty(MUS.U10_has_order_number, orderNumber);

    /**************************** Expression: Casting ***************************************/
    for (String castingString : getCasting()) {
      Resource M6Casting = model.createResource();
      M6Casting.addProperty(RDF.type, MUS.M6_Intended_Casting)
        .addProperty(CIDOC.P3_has_note, castingString);

      String[] mopList = castingString.split(",");
      for (String mop : mopList) {
        Resource M23CastingDetail = model.createResource();
        M23CastingDetail.addProperty(RDF.type, MUS.M23_Casting_Detail);
        Literal mopLiteral = model.createLiteral(mop.toLowerCase().trim(), "fr");
        M23CastingDetail.addProperty(MUS.U1_has_intended_medium_of_performance,
          model.createResource()
            .addProperty(RDF.type, MUS.M14_Medium_Of_Performance)
            .addProperty(CIDOC.P1_is_identified_by, mopLiteral)
        );

        M6Casting.addProperty(MUS.U23_has_casting_detail, M23CastingDetail);
      }

      this.resource.addProperty(MUS.U13_has_intended_casting, M6Casting);
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

      String title = field.getSubfield('a').getData();
      String language = null;

      if (title.isEmpty() || Converter.isNotSignificativeTitle(title))
        continue;

      if (field.isCode('h'))
        title += field.getSubfield('h').getData();
      if (field.isCode('i'))
        title += field.getSubfield('i').getData();


      if (field.isCode('w')) {
        language = field.getSubfield('w').getData().substring(6, 8);
      }

      Literal titleLiteral;
      if (language == null)
        titleLiteral = this.model.createLiteral(title.trim());
      else
        titleLiteral = this.model.createLiteral(title.trim(), language);

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
      String codeGenre = field.getData().substring(18, 21).replace(".", "").trim();

      // special case
      if (codeGenre.isEmpty()) continue;
      if (codeGenre.equals("uu")) codeGenre = "uuu";

      Resource res = null;
      if (Converter.genreVocabulary != null)
        res = Converter.genreVocabulary.findConcept(codeGenre);

      if (res == null) System.out.println("Code genre not found: " + codeGenre);
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

}
