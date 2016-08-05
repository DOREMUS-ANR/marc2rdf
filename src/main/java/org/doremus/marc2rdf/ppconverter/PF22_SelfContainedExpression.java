package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.Converter;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/***
 * Correspond à la description développée de l'expression représentative
 ***/
public class PF22_SelfContainedExpression {
  private final Resource F22;
  private Record record;
  private Model model;
  private URI uriF22;

  public PF22_SelfContainedExpression(Record record) throws URISyntaxException {
    this.record = record;
    this.model = ModelFactory.createDefaultModel();
    this.uriF22 = ConstructURI.build("Self_Contained_Expression", "F22");

    F22 = model.createResource(uriF22.toString());
    F22.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression);

    compute();
  }

  private void compute() {
    /**************************** Expression: Title *****************************************/
    for (String title : getTitle())
      F22.addProperty(CIDOC.P102_has_title, title);

    /**************************** Expression: Catalogue *************************************/
    for (String catalog : getCatalog()) {
      Resource M1CatalogStatement = model.createResource()
        .addProperty(RDF.type, MUS.M1_Catalogue_Statement)
        .addProperty(CIDOC.P3_has_note, catalog);

      String[] catalogParts = catalog.split(" ");

      M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, catalogParts[0])
        .addProperty(MUS.U41_has_catalogue_number, catalogParts[1]);

      F22.addProperty(MUS.U16_has_catalogue_statement, M1CatalogStatement);

    }

    /**************************** Expression: Opus ******************************************/
    for (String opus : getOpus()) {
      Resource M2OpusStatement = model.createResource()
        .addProperty(RDF.type, MUS.M2_Opus_Statement)
        .addProperty(CIDOC.P3_has_note, opus);

      // Op. 22 no 1
      // Op. 22, no 1
      // Op. 22,no 1
      String[] opusParts = opus.split("[, ] ?no");

      M2OpusStatement.addProperty(MUS.U42_has_opus_number, opusParts[0].replaceAll("Op\\.", "").trim());
      if (opusParts.length > 1) {
        M2OpusStatement.addProperty(MUS.U43_has_opus_subnumber, opusParts[1].replaceAll("no", "").trim());
      }

      F22.addProperty(MUS.U17_has_opus_statement, M2OpusStatement);
    }

    /**************************** Expression: ***********************************************/
    for (String note : getNote())
      F22.addProperty(CIDOC.P3_has_note, note);

    /**************************** Expression: key *******************************************/
    for (String key : getKey()) {
      F22.addProperty(MUS.U11_has_key, model.createResource()
        .addProperty(RDF.type, MUS.M4_Key)
        .addProperty(CIDOC.P1_is_identified_by, model.createLiteral(key, "fr")) // Le nom du genre est toujours en français
      );
    }

    /**************************** Expression: Genre *****************************************/
    List<String> genres = getGenre();
    for (String genre : genres) {
      F22.addProperty(MUS.U12_has_genre, model.createResource()
        .addProperty(RDF.type, MUS.M5_Genre)
        .addProperty(CIDOC.P1_is_identified_by, model.createLiteral(genre, "fr")) // Le nom du genre est toujours en français
      );
    }
    /**************************** Expression: Order Number **********************************/
    for (String orderNumber : getOrderNumber()) {
      F22.addProperty(MUS.U10_has_order_number, orderNumber);
    }

    /**************************** Expression: Casting ***************************************/
    for (String castingString : getCasting()) {
      Resource M6Casting = model.createResource();
      M6Casting.addProperty(RDF.type, MUS.M6_Intended_Casting);
      M6Casting.addProperty(CIDOC.P3_has_note, castingString);

      F22.addProperty(MUS.U13_has_intended_casting, M6Casting);
    }
  }

  public PF22_SelfContainedExpression add(PF50_ControlledAccessPoint accessPoint) {
    /**************************** Expression: Point d'Accès ********************************/
    F22.addProperty(CIDOC.P1_is_identified_by, accessPoint.asResource());
//    accessPoint.asResource().addProperty(model.createProperty(cidoc + "P1i_identifies"), F22);
    return this;
  }

  public Model getModel() {
    return model;
  }

  /***********************************
   * Le titre
   **************************************/
  private List<String> getTitle() {
    List<String> titleList = new ArrayList<>();
    List<DataField> fields;

    if (record.isType("AIC:14")) {
      fields = record.getDatafieldsByCode("444");
      fields.addAll(record.getDatafieldsByCode("144"));
    } else { // type UNI:100
      fields = record.getDatafieldsByCode("200");
    }

    for (DataField field : fields) {
      if (!field.isCode('a')) continue;

      String title = field.getSubfield('a').getData().trim();

      if (title.isEmpty() || Converter.isNotSignificativeTitle(title))
        continue;

      titleList.add(title);
    }

    return titleList;
  }


  /***********************************
   * Le catalogue
   ***********************************/
  private List<String> getCatalog() {
    if (!record.isType("AIC:14")) return new ArrayList<>();

    List<String> results = new ArrayList<>();

    List<DataField> catalogFields = record.getDatafieldsByCode("444");
    catalogFields.addAll(record.getDatafieldsByCode("144"));

    for (DataField field : catalogFields) {
      if (!field.isCode('k')) continue;

      String catalog = field.getSubfield('k').getData();
      if (!results.contains(catalog)) results.add(catalog);
    }
    return results;
  }

  /***********************************
   * L'opus
   ***********************************/
  private List<String> getOpus() {
    if (!record.isType("AIC:14")) return new ArrayList<>();

    List<String> results = new ArrayList<>();

    List<DataField> opusFields = record.getDatafieldsByCode("444");
    opusFields.addAll(record.getDatafieldsByCode("144"));

    for (DataField field : opusFields) {
      if (!field.isCode('p')) continue;

      String opus = field.getSubfield('p').getData();
      if (!results.contains(opus)) results.add(opus);
    }
    return results;
  }

  /***********************************
   * Note Libre
   ***********************************/
  private List<String> getNote() {
    if (!record.isType("UNI:100")) return new ArrayList<>();

    List<String> results = new ArrayList<>();

    List<DataField> opusFields = record.getDatafieldsByCode("909");
    // TODO code 919 has contents used also elsewhere
    opusFields.addAll(record.getDatafieldsByCode("919"));

    for (DataField field : opusFields) {
      if (!field.isCode('a')) continue;
      results.add(field.getSubfield('a').getData().trim());
    }

    return results;
  }

  /***********************************
   * Key (Tonalite)
   ***********************************/
  private List<String> getKey() {
    if (!record.isType("UNI:100")) return new ArrayList<>();

    List<String> results = new ArrayList<>();

    for (DataField field : record.getDatafieldsByCode("909")) {
      if (!field.isCode('d')) continue;
      results.add(field.getSubfield('d').getData().trim());
    }

    return results;
  }

  /***********************************
   * Le numero d'ordre
   ***********************************/
  private List<String> getOrderNumber() {
    if (!record.isType("AIC:14")) return new ArrayList<>();

    List<String> results = new ArrayList<>();

    List<DataField> fields = record.getDatafieldsByCode("444");
    fields.addAll(record.getDatafieldsByCode("144"));

    for (DataField field : fields) {
      if (!field.isCode('n')) continue;

      String orderNumber = field.getSubfield('n').getData().replaceAll("No", "").trim();
      if (!results.contains(orderNumber)) results.add(orderNumber);
    }

    return results;
  }

  /*****************************************
   * Les genres
   *************************************/
  private List<String> getGenre() {
    List<String> genres = new ArrayList<>();

    for (DataField field : record.getDatafieldsByCode("610")) {
      if (field.isCode('a') && field.isCode('b') && field.getSubfield('b').getData().equals("04")) {
        //$b=04 veut dire "genre"
        genres.add(field.getSubfield('a').getData().toLowerCase());
      }
    }
    return genres;
  }

  /***********************************
   * Casting
   ***********************************/
  private List<String> getCasting() {
    if (!record.isType("UNI:100")) return new ArrayList<>();

    List<String> castings = new ArrayList<>();

    for (DataField field : record.getDatafieldsByCode("909")) {
      if (!field.isCode('c')) continue;
      castings.add(field.getSubfield('c').getData().trim());
    }

    return castings;
  }

  public Resource asResource() {
    return F22;
  }
}
