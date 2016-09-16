package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.Converter;
import org.doremus.marc2rdf.main.DoremusResource;
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
public class PF22_SelfContainedExpression extends DoremusResource {
  public PF22_SelfContainedExpression(Record record, String identifier) throws URISyntaxException {
    super(record, identifier);
    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression);

    /**************************** Expression: Title *****************************************/
    for (String title : getTitle())
      this.resource.addProperty(CIDOC.P102_has_title, title);

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

      this.resource.addProperty(MUS.U17_has_opus_statement, M2OpusStatement);
    }

    /**************************** Expression: ***********************************************/
    for (String note : getNote())
      this.resource.addProperty(CIDOC.P3_has_note, note);

    /**************************** Expression: key *******************************************/
    for (String key : getKey()) {
      this.resource.addProperty(MUS.U11_has_key, model.createResource()
        .addProperty(RDF.type, MUS.M4_Key)
        .addProperty(CIDOC.P1_is_identified_by, model.createLiteral(key.trim(), "fr")) // Le nom du genre est toujours en français
      );
    }

    /**************************** Expression: Genre *****************************************/
    List<String> genres = getGenre();
    for (String genre : genres) {
      this.resource.addProperty(MUS.U12_has_genre, model.createResource()
        .addProperty(RDF.type, MUS.M5_Genre)
        .addProperty(CIDOC.P1_is_identified_by, model.createLiteral(genre, "fr")) // Le nom du genre est toujours en français
      );
    }
    /**************************** Expression: Order Number **********************************/
    for (String orderNumber : getOrderNumber()) {
      this.resource.addProperty(MUS.U10_has_order_number, orderNumber);
    }

    /**************************** Expression: Casting ***************************************/
    int castingNum = 0;
    for (String castingString : getCasting()) {
      Resource M6Casting = model.createResource(this.uri.toString() + "/casting/" + (++castingNum));
      M6Casting.addProperty(RDF.type, MUS.M6_Casting);
      M6Casting.addProperty(CIDOC.P3_has_note, castingString.trim());

      System.out.println(castingString.trim());
      this.resource.addProperty(MUS.U13_has_casting, M6Casting);
    }
  }

  public PF22_SelfContainedExpression add(PF50_ControlledAccessPoint accessPoint) {
    /**************************** Expression: Point d'Accès ********************************/
    this.resource.addProperty(CIDOC.P1_is_identified_by, accessPoint.asResource());
//    accessPoint.asResource().addProperty(model.createProperty(cidoc + "P1i_identifies"), this.resource);
    return this;
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

    List<String> catalogFields = record.getDatafieldsByCode("444", 'k');
    catalogFields.addAll(record.getDatafieldsByCode("144", 'k'));

    for (String catalog : catalogFields) {
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

    List<String> opusFields = record.getDatafieldsByCode("444", 'p');
    opusFields.addAll(record.getDatafieldsByCode("144", 'p'));

    for (String opus : opusFields) {
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

    List<String> opusFields = record.getDatafieldsByCode("909", 'a');
    // TODO code 919 has contents used also elsewhere
    opusFields.addAll(record.getDatafieldsByCode("919", 'a'));

    for (String opus : opusFields)
      results.add(opus.trim());

    return results;
  }

  /***********************************
   * Key (Tonalite)
   ***********************************/
  private List<String> getKey() {
    if (!record.isType("UNI:100")) return new ArrayList<>();
    return record.getDatafieldsByCode("909", 'd');
  }

  /***********************************
   * Le numero d'ordre
   ***********************************/
  private List<String> getOrderNumber() {
    if (!record.isType("AIC:14")) return new ArrayList<>();

    List<String> results = new ArrayList<>();

    List<String> fields = record.getDatafieldsByCode("444", 'n');
    fields.addAll(record.getDatafieldsByCode("144", 'n'));

    for (String orderNumber : fields) {
      orderNumber = orderNumber.replaceAll("(?i)No", "").trim();
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
    return record.getDatafieldsByCode("909", 'c');
  }

}
