package org.doremus.marc2rdf.ppconverter;

import net.sf.junidecode.Junidecode;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.Utils;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/***
 * Correspond à la description développée de l'expression représentative
 ***/
public class PF22_SelfContainedExpression extends DoremusResource {

  private List<String> opusMemory;

  public PF22_SelfContainedExpression(String identifier) throws URISyntaxException {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression);
  }

  public PF22_SelfContainedExpression(Record record) throws URISyntaxException {
    this(record, record.getIdentifier(), null);
  }

  public PF22_SelfContainedExpression(Record record, String identifier, List<String> composers) throws
    URISyntaxException {
    super(record, identifier);
    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression);


    if (record.isTUM()) convertUNI100(composers);
    else if ("UNI:44".equals(record.getType())) convertUNI44();
  }

  private void convertUNI44() {
    for (String title : getTitle())
      this.resource.addProperty(CIDOC.P102_has_title, title).addProperty(RDFS.label, title);
  }

  private void convertUNI100(List<String> composers) {
    this.resource.addProperty(DCTerms.identifier, identifier);
    this.resource.addProperty(OWL.sameAs, model.createResource("http://digital.philharmoniedeparis.fr/doc/CIMU/" + identifier));

    this.opusMemory = new ArrayList<>();

    for (String title : getTitle())
      this.resource.addProperty(MUS.U70_has_original_title, title).addProperty(RDFS.label, title);

    for (String catalog : getCatalog()) {
      // sometimes there is the opus number inside here!
      if (catalog.matches(Utils.opusHeaderRegex + ".*")) {
        // System.out.println("opus in catalog found " + catalog);
        parseOpus(catalog);
        continue;
      }
      for (String c : catalog.split(" ; "))
        parseCatalog(c, composers);
    }


    for (String opus : getOpus()) {
      // is it for real an opus information?

      // completely numeric: it is
      if (StringUtils.isNumeric(opus)) {
        addOpus(opus, opus);
        continue;
      }
      // if does not contain "Op." or "opus" etc., it is a catalog!
      if (!opus.matches(Utils.opusHeaderRegex + ".*")) {
        // System.out.println("catalog in opus found " + opus);
        parseCatalog(opus, composers);
        continue;
      }

      parseOpus(opus);
    }

    for (String note : getNote()) addNote(note);

    for (String key : getKey()) {
      key = key.replaceFirst("\\.$", "").trim(); //remove final dot
      String keyUri = this.uri + "/key/" + Junidecode.unidecode(key).toLowerCase().replaceAll(" ", "_");
      this.resource.addProperty(MUS.U11_has_key,
        model.createResource(keyUri)
          .addProperty(RDF.type, MUS.M4_Key)
          .addProperty(RDFS.label, key, "fr")
          .addProperty(CIDOC.P1_is_identified_by, key, "fr")
      );
    }

    List<String> genres = getGenre();
    for (String genre : genres) {
      Literal label = model.createLiteral(genre, "fr");
      this.resource.addProperty(MUS.U12_has_genre, model.createResource()
        .addProperty(RDF.type, MUS.M5_Genre)
        .addProperty(RDFS.label, label)
        .addProperty(CIDOC.P1_is_identified_by, label)
      );
    }

    for (String orderNumber : getOrderNumber()) {
      List<Integer> range = Utils.toRange(orderNumber);
      if (range == null)
        this.resource.addProperty(MUS.U10_has_order_number, Utils.toSafeNumLiteral(orderNumber));
      else {
        for (int i : range) this.resource.addProperty(MUS.U10_has_order_number, model.createTypedLiteral(i));
      }
    }

    int castingNum = 0;

    for (String castingString : getCasting()) {
      castingString = castingString.trim().replaceFirst("\\.$", "");
      String castingUri = this.uri.toString() + "/casting/" + (++castingNum);
      PM6_Casting M6Casting = new PM6_Casting(castingString, castingUri, model);

      this.resource.addProperty(MUS.U13_has_casting, M6Casting.asResource());
    }

  }

  private void parseCatalog(String catalog, List<String> composers) {
    String[] catalogParts = catalog.split("[ .]", 2);
    String catalogName = null, catalogNum = null;

    if (catalogParts.length > 1) {
      catalogName = catalogParts[0].trim();
      catalogNum = catalogParts[1].trim();
    } else if (catalog.matches("^([a-zA-Z]+)(\\d+.*)$")) {
      Matcher m = Pattern.compile("^([a-zA-Z]+)(\\d+.*)$").matcher(catalog);
      m.find();
      catalogName = m.group(1).trim();
      catalogNum = m.group(2).trim();
    } else {
      System.out.println("Not parsable catalog: " + catalog);
      // TODO what to do with not parsable catalogs?
    }

    Resource match = VocabularyManager.getMODS("catalogue").findModsResource(catalogName, composers);
    if (match != null)
      catalogName = match.getProperty(model.createProperty("http://www.loc.gov/standards/mods/rdf/v1/#identifier")).getObject().toString();

    String label = (catalogName != null) ? (catalogName + " " + catalogNum) : catalog;

    Resource M1CatalogStatement = model.createResource(this.uri.toString() + "/catalog/" + label.replaceAll("[ /]", "_"))
      .addProperty(RDF.type, MUS.M1_Catalogue_Statement)
      .addProperty(RDFS.label, label)
      .addProperty(CIDOC.P3_has_note, catalog);

    if (match != null)
      M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, match);
    else if (catalogName != null)
      M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, catalogName);

    this.resource.addProperty(MUS.U16_has_catalogue_statement, M1CatalogStatement);

    if (catalogNum == null) return;

    if (catalogNum.contains("-")) {
      // i.e. "SWV 369-397"

      catalogParts = catalogNum.split("-");

      // strip repeated catalog name (i.e. "H 457-H 458-H 459")
      for (int i = 0; i < catalogParts.length; i++) {
        String part = catalogParts[i];
        part = part.replaceFirst("^" + catalogName + " ", "");
        catalogParts[i] = part;
      }

      if (catalogParts.length == 2) {
        try {
          int start = Integer.parseInt(catalogParts[0]),
            end = Integer.parseInt(catalogParts[1]);

          // put the internal number also
          for (int j = start + 1; j < end; j++) {
            M1CatalogStatement.addProperty(MUS.U41_has_catalogue_number, model.createTypedLiteral(j));
          }
        } catch (NumberFormatException e) {
          System.out.println("not parsed as range " + catalogNum);
          // DO NOTHING
        }
      }

      for (String part : catalogParts)
        M1CatalogStatement.addProperty(MUS.U41_has_catalogue_number, Utils.toSafeNumLiteral(part));

    } else M1CatalogStatement.addProperty(MUS.U41_has_catalogue_number, Utils.toSafeNumLiteral(catalogNum));
  }

  private void parseOpus(String opus) {
    String opusData = opus.replaceFirst(Utils.opusHeaderRegex, "").replaceFirst("\\.$", "").trim();
    if (opusData.isEmpty()) {
      // it means that it is only written "op. posthume" or similar
      // add it as a note to F22
      this.resource.addProperty(CIDOC.P3_has_note, opus);
      return;
    }

    if (opusData.matches(".*" + Utils.opusSubnumberRegex + ".*")) {
      String[] opusParts = opusData.split(Utils.opusSubnumberRegex);

      addOpus(opus, opusParts[0].trim(), opusParts[1].trim());
    } else addOpus(opus, opusData);
  }

  private void addOpus(String note) {
    addOpus(note, null, null);
  }

  private void addOpus(String note, String number) {
    addOpus(note, number, null);
  }

  private void addOpus(String note, String number, String subnumber) {
    String memoryObj = number;
    if (subnumber != null) memoryObj += "-" + subnumber;
//    System.out.println(note + " --> " + memoryObj);

    if (opusMemory.contains(memoryObj)) return;
    opusMemory.add(memoryObj);

    Resource M2OpusStatement = model.createResource(this.uri + "/opus/" + memoryObj.replaceAll(" ", "_"))
      .addProperty(RDF.type, MUS.M2_Opus_Statement)
      .addProperty(CIDOC.P3_has_note, note.trim())
      .addProperty(RDFS.label, note.trim());

    if (number != null) {
      if (number.contains(" et ")) {
        for (String singleNum : number.split(" et ")) {
          M2OpusStatement.addProperty(MUS.U42_has_opus_number, singleNum.trim());
        }
      } else M2OpusStatement.addProperty(MUS.U42_has_opus_number, number);
    }
    if (subnumber != null) {
      List<Integer> range = Utils.toRange(subnumber);

      if (range == null) M2OpusStatement.addProperty(MUS.U43_has_opus_subnumber, subnumber);
      else {
        for (int i : range)
          M2OpusStatement.addProperty(MUS.U43_has_opus_subnumber, model.createTypedLiteral(i));

      }
    }

    this.resource.addProperty(MUS.U17_has_opus_statement, M2OpusStatement);
  }


  public PF22_SelfContainedExpression add(PF50_ControlledAccessPoint accessPoint) {
    this.resource.addProperty(CIDOC.P1_is_identified_by, accessPoint.asResource());
//    accessPoint.asResource().addProperty(model.createProperty(cidoc + "P1i_identifies"), this.resource);
    return this;
  }


  private List<String> getTitle() {
    List<String> titleList = new ArrayList<>();
    List<DataField> fields;

    switch (record.getType()) {
      case "AIC:14":
        fields = record.getDatafieldsByCode("444");
        fields.addAll(record.getDatafieldsByCode("144"));
        break;
      case "UNI:100":
      case "UNI:44":
        fields = record.getDatafieldsByCode("200");
        break;
      default:
        return titleList;
    }

    for (DataField field : fields) {
      if (!field.isCode('a')) continue;
      String title = field.getString('a').trim();
      titleList.add(title);
    }

    return titleList;
  }


  private List<String> getCatalog() {
    if (!record.isType("AIC:14")) return new ArrayList<>();

    List<String> results = new ArrayList<>();

    List<String> catalogFields = record.getDatafieldsByCode("444", 'k');
    catalogFields.addAll(record.getDatafieldsByCode("144", 'k'));

    for (String catalog : catalogFields) {
      if (!results.contains(catalog.trim())) results.add(catalog.trim());
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
      String o = opus.trim();
      if (!results.contains(o)) results.add(o);
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
      orderNumber = orderNumber.replaceAll("(?i)n(?:o| ?°)s?", "").trim();
      if (!results.contains(orderNumber)) results.add(orderNumber);
    }

    return results;
  }

  private List<String> getGenre() {
    List<String> genres = new ArrayList<>();

    for (DataField field : record.getDatafieldsByCode("610")) {
      if (field.isCode('a') && field.isCode('b') && field.getSubfield('b').getData().trim().equals("04")) {
        //$b=04 veut dire "genre"
        genres.add(field.getSubfield('a').getData().toLowerCase().trim());
      }
    }
    return genres;
  }

  private List<String> getCasting() {
    if (!record.isType("UNI:100")) return new ArrayList<>();
    return record.getDatafieldsByCode("909", 'c');
  }

  public PF22_SelfContainedExpression add(PF30_PublicationEvent f30) {
    this.resource.addProperty(MUS.U4_had_princeps_publication, f30.asResource());
    return this;
  }

  public PF22_SelfContainedExpression addPremiere(PM42_PerformedExpressionCreation m42) {
    this.resource.addProperty(MUS.U5_had_premiere, m42.getMainPerformance());
    return this;
  }

  public void link2concert(List<String> df) {
    if (df.isEmpty()) return;
    String concert = df.get(0);
    try {
      PF31_Performance f31 = new PF31_Performance(concert);
      PF25_PerformancePlan f25 = new PF25_PerformancePlan(concert, true);
      f31.add(this);
      f25.add(this);

      this.model.add(f31.getModel()).add(f25.getModel());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
}
