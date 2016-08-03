package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.Converter;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/***
 * Correspond à la description développée de l'expression représentative
 ***/
public class F22_SelfContainedExpression {
  private static final String cidoc = "http://www.cidoc-crm.org/cidoc-crm/";

  private final Record record;
  private Model model;
  private URI uriF22;
  private Resource F22;

  public F22_SelfContainedExpression(Record record) throws URISyntaxException {
    this.record = record;
    this.model = ModelFactory.createDefaultModel();
    this.uriF22 = ConstructURI.build("Self_Contained_Expression", "F22");

    this.compute();
  }

  public String getUri() {
    return this.uriF22.toString();
  }

  public Resource asResource() {
    return this.F22;
  }

  private void compute() {
    F22 = model.createResource(getUri());
    F22.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression);

    /**************************** Expression: Context for the expression ********************/
    for (String dedication : getDedicace()) {
      F22.addProperty(model.createProperty(cidoc + "P67_refers_to"), model.createResource()
        .addProperty(RDF.type, MUS.M15_Dedication)
        .addProperty(model.createProperty(cidoc + "P3_has_note"), dedication));
    }

    /**************************** Expression: Title *****************************************/
    for (Literal title : getTitle())
      F22.addProperty(model.createProperty(cidoc + "P102_has_title"), title);


    /**************************** Expression: Catalogue *************************************/
    for (String catalog : getCatalog()) {
      Resource M1CatalogStatement = model.createResource()
        .addProperty(RDF.type, MUS.M1_Catalogue_Statement)
        .addProperty(model.createProperty(cidoc + "P3_has_note"), catalog);

      String[] catalogParts = catalog.split(" ");

      M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, catalogParts[0])
        .addProperty(MUS.U41_has_catalogue_number, catalogParts[1]);

      F22.addProperty(MUS.U16_has_catalogue_statement, M1CatalogStatement);
    }

    /**************************** Expression: Opus ******************************************/
    String opus = getOpus();
    if (opus != null) {
      Resource M2OpusStatement = model.createResource()
        .addProperty(RDF.type, MUS.M2_Opus_Statement)
        .addProperty(model.createProperty(cidoc + "P3_has_note"), opus);

      String[] opusParts = opus.split(",");
      M2OpusStatement.addProperty(MUS.U42_has_opus_number, opusParts[0].replaceAll("Op\\.", "").trim());

      if (opusParts.length > 1) {
        M2OpusStatement.addProperty(MUS.U43_has_opus_subnumber, opusParts[1].replaceAll("no", "").trim());
      }

      F22.addProperty(MUS.U17_has_opus_statement, M2OpusStatement);
    }

    /**************************** Expression: ***********************************************/
    for (String note : getNote())
      F22.addProperty(model.createProperty(cidoc + "P3_has_note"), note);

    /**************************** Expression: key *******************************************/
    for (String key : getKey()) {
      F22.addProperty(MUS.U11_has_key, model.createResource()
        .addProperty(RDF.type, MUS.M4_Key)
        .addProperty(model.createProperty(cidoc + "P1_is_identified_by"), model.createLiteral(key, "fr"))
      );
    }

    /*************************** Expression: Genre *****************************************/
    List<Resource> genreList = getGenre();
    for (Resource genre : genreList) F22.addProperty(MUS.U12_has_genre, genre);

    /**************************** Expression: Order Number **********************************/
    String orderNumber = getOrderNumber();
    if (orderNumber != null) F22.addProperty(MUS.U10_has_order_number, orderNumber);

    /**************************** Expression: Casting ***************************************/
    for (String castingString : getCasting()) {
      Resource M6Casting = model.createResource();
      M6Casting.addProperty(RDF.type, MUS.M6_Intended_Casting);

      String[] mopList = castingString.split(",");
      for (String mop : mopList) {
        Resource M23CastingDetail = model.createResource();
        M23CastingDetail.addProperty(RDF.type, MUS.M23_Casting_Detail);
        Literal mopLiteral = model.createLiteral(mop.toLowerCase().trim(), "fr");
        M23CastingDetail.addProperty(MUS.U1_has_intended_medium_of_performance,
          model.createResource()
            .addProperty(RDF.type, MUS.M14_Medium_Of_Performance)
            .addProperty(model.createProperty(cidoc + "P1_is_identified_by"), mopLiteral)
        );

        M6Casting.addProperty(MUS.U23_has_casting_detail, M23CastingDetail);
      }

      F22.addProperty(MUS.U13_has_intended_casting, M6Casting);
    }
  }

  public Model getModel() {
    return model;
  }

  /***********************************
   * La dedicace
   ***********************************/
  private List<String> getDedicace() {
    List<String> results = new ArrayList<>();

    for (DataField field : record.getDatafieldsByCode("600")) {
      if (field.isCode('a')) {
        String dedicace = field.getSubfield('a').getData();
        if (dedicace.startsWith("Dédicace")) {
          results.add(dedicace);
        }
      }
    }
    return results;
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
    List<String> results = new ArrayList<>();

    for (DataField field : record.getDatafieldsByCode("144")) {
      if (field.isCode('k')) results.add(field.getSubfield('k').getData());
    }
    return results;
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

    for (DataField field : record.getDatafieldsByCode("600")) {
      if (!field.isCode('a')) continue;

      String note = field.getSubfield('a').getData();
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
    List<String> keys = new ArrayList<>();

    List<DataField> titleFields1 = record.getDatafieldsByCode("444");
    titleFields1.addAll(record.getDatafieldsByCode("144"));

    for (DataField field : titleFields1) {
      if (field.isCode('t'))
        keys.add(field.getSubfield('t').getData());
    }
    return keys;
  }

  /***********************************
   * Le numero d'ordre
   ***********************************/
  private String getOrderNumber() {
    List<DataField> fields = record.getDatafieldsByCode("444");
    fields.addAll(record.getDatafieldsByCode("144"));

    for (DataField field : fields) {
      if (!field.isCode('n')) continue;

      return field.getSubfield('n').getData().replaceFirst("No", "").trim();
    }
    return null;
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
    List<String> castings = new ArrayList<>();

    for (DataField field : record.getDatafieldsByCode("144")) {
      if (!field.isCode('b')) continue;
      castings.add(field.getSubfield('b').getData());
    }

    return castings;
  }

}
