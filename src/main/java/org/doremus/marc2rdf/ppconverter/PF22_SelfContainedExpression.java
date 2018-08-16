package org.doremus.marc2rdf.ppconverter;

import net.sf.junidecode.Junidecode;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/***
 * Correspond à la description développée de l'expression représentative
 ***/
public class PF22_SelfContainedExpression extends DoremusResource {
  private static String CATALOG_REGEX = "(?i)^([a-z]+)(\\d+.*)$";
  private static Pattern CATALOG_PATTERN = Pattern.compile(CATALOG_REGEX);

  private List<String> opusMemory;

  public PF22_SelfContainedExpression(String identifier) {
    super(identifier);
    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression);
  }

  public PF22_SelfContainedExpression(Record record) {
    this(record, record.getIdentifier(), null);
  }

  public PF22_SelfContainedExpression(Record record, String identifier, List<String> composers) {
    super(record, identifier);
    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression);


    if (record.isTUM()) convertUNI100(composers);
    else // UNI44, UNI2
      convertUNI44();
  }

  private void convertUNI44() {
    for (String title : getTitle())
      this.resource.addProperty(CIDOC.P102_has_title, title).addProperty(RDFS.label, title);
  }

  private void convertUNI100(List<String> composers) {
    this.resource.addProperty(DC.identifier, identifier);
    this.resource.addProperty(OWL.sameAs, model.createResource("http://digital.philharmoniedeparis.fr/doc/CIMU/" + identifier));

    this.opusMemory = new ArrayList<>();

    boolean titleFound = false;
    for (String title : getTitle()) {
      this.resource.addProperty(CIDOC.P102_has_title, title)
        .addProperty(RDFS.label, title);
      titleFound = true;
    }

    for (String subTitle : getSubTitle())
      this.resource.addProperty(MUS.U67_has_subtitle, subTitle);

    for (String uniTitle : getUniformTitle("144")) {
      this.resource.addProperty(MUS.U71_has_uniform_title, uniTitle);
      if(!titleFound) this.resource.addProperty(RDFS.label, uniTitle);
      titleFound = true;
    }

    for (String uniTitle : getUniformTitle("444")){
      this.resource.addProperty(MUS.U68_has_variant_title, uniTitle);
      if(!titleFound) this.resource.addProperty(RDFS.label, uniTitle);
      titleFound = true;
    }


    for (String catalog : getCatalog()) {
      // sometimes there is the opus number inside here!
      if (catalog.matches(Utils.opusHeaderRegex + ".*")) {
        log("opus in catalog found " + catalog);
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
        log("catalog in opus found " + opus);
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
      castingString = castingString.trim()
        .replaceFirst("\\.$", "");
      String castingUri = this.uri + "/casting/" + (++castingNum);
      PM6_Casting M6Casting = new PM6_Casting(castingString, castingUri);

      this.resource.addProperty(MUS.U13_has_casting, M6Casting.asResource());
      this.model.add(M6Casting.getModel());
    }

  }

  private void parseCatalog(String catalog, List<String> composers) {
    String[] catalogParts = catalog.split("[ .]", 2);
    String catalogName = null, catalogNum = null;

    if (catalogParts.length > 1) {
      catalogName = catalogParts[0].trim();
      catalogNum = catalogParts[1].trim();
    } else if (catalog.matches(CATALOG_REGEX)) {
      Matcher m = CATALOG_PATTERN.matcher(catalog);
      m.find();
      catalogName = m.group(1).trim();
      catalogNum = m.group(2).trim();
    } else log("Not parsable catalog: " + catalog);

    Resource match = VocabularyManager.getMODS("catalogue").findModsResource(catalogName, composers);
    if (match != null)
      catalogName = match.getProperty(model.createProperty("http://www.loc.gov/standards/mods/rdf/v1/#identifier")).getObject().toString();

    String label = (catalogName != null) ? (catalogName + " " + catalogNum) : catalog;

    Resource M1CatalogStatement = model.createResource(this.uri + "/catalog/" + label.replaceAll("[ /]", "_"))
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
          log("not parsed as range " + catalogNum);
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
      else
        range.forEach(i -> M2OpusStatement.addProperty(MUS.U43_has_opus_subnumber, model.createTypedLiteral(i)));
    }

    this.resource.addProperty(MUS.U17_has_opus_statement, M2OpusStatement);
  }


  public PF22_SelfContainedExpression add(PF50_ControlledAccessPoint accessPoint) {
    this.resource.addProperty(CIDOC.P1_is_identified_by, accessPoint.asResource());
    return this;
  }


  private List<String> getTitle() {
    List<String> titleList = new ArrayList<>();

    switch (record.getType()) {
      case "UNI:100":
      case "UNI:44":
      case "UNI:2":
        titleList = record.getDatafieldsByCode("200", 'a');
        break;
      default:
        return titleList;
    }

    return titleList.stream()
      .map(String::trim)
      .filter(t -> !t.isEmpty())
      .collect(Collectors.toList());
  }

  private List<String> getUniformTitle(String code) {
    List<String> titleList = new ArrayList<>();
    if (!record.isType("AIC:14")) return titleList;

    List<DataField> fields = record.getDatafieldsByCode(code);
    for (DataField field : fields) {
      Stream<Character> mainCodes = "agc".chars().mapToObj(i -> (char) i);
      Stream<Character> descrCodes = "rnkptu".chars().mapToObj(i -> (char) i);
      Stream<Character> partCodes = "hi".chars().mapToObj(i -> (char) i);

      boolean isAdaption = field.isCode('g') || field.isCode('c');
      String qualif = null;

      String version = field.getString('q');
      if (version != null) version += ")";

      List<String> s = Stream.of(field.getString('e'), version, field.getString('j'))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
      if (s.size() > 0)
        qualif = "(" + String.join("; ", s) + ")";


      List<String> main = mainCodes
        .map(field::getString)
        .filter(Objects::nonNull).collect(Collectors.toList());

      List<String> descr = descrCodes
        .map(field::getString)
        .filter(Objects::nonNull).collect(Collectors.toList());

      List<String> part = partCodes
        .map(field::getString)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());


      if (isAdaption) main.addAll(part);
      main.addAll(descr);
      if (qualif != null) main.add(qualif);
      if (!isAdaption) main.addAll(part);

      String t = String.join(". ", main)
        .replaceAll("(?:(\\))\\.|\\.( \\())", "$1$2");

      titleList.add(t);
    }

    return titleList.stream()
      .map(String::trim)
      .filter(t -> !t.isEmpty())
      .collect(Collectors.toList());
  }

  private List<String> getSubTitle() {
    List<String> titleList = new ArrayList<>();

    switch (record.getType()) {
      case "UNI:100":
        titleList = record.getDatafieldsByCode("200", 'e');
        break;
      default:
        return titleList;
    }

    return titleList.stream()
      .map(String::trim)
      .filter(t -> !t.isEmpty())
      .map(t -> t.replaceAll("(?i)^ou ", ""))
      .collect(Collectors.toList());
  }


  private List<String> getCatalog() {
    if (!record.isType("AIC:14")) return new ArrayList<>();


    List<String> catalogFields = record.getDatafieldsByCode("444", 'k');
    catalogFields.addAll(record.getDatafieldsByCode("144", 'k'));

    return catalogFields.stream()
      .map(String::trim)
      .distinct()
      .collect(Collectors.toList());
  }

  private List<String> getOpus() {
    if (!record.isType("AIC:14")) return new ArrayList<>();

    List<String> opusFields = record.getDatafieldsByCode("444", 'p');
    opusFields.addAll(record.getDatafieldsByCode("144", 'p'));

    return opusFields.stream().map(String::trim).distinct().collect(Collectors.toList());
  }

  private List<String> getNote() {
    if (!record.isType("UNI:100")) return new ArrayList<>();

    List<String> opusFields = record.getDatafieldsByCode("909", 'a');
    // TODO code 919 has contents used also elsewhere
    opusFields.addAll(record.getDatafieldsByCode("919", 'a'));

    return opusFields.stream().map(String::trim).collect(Collectors.toList());
  }

  private List<String> getKey() {
    if (!record.isType("UNI:100")) return new ArrayList<>();
    return record.getDatafieldsByCode("909", 'd');
  }

  private List<String> getOrderNumber() {
    if (!record.isType("AIC:14")) return new ArrayList<>();

    List<String> fields = record.getDatafieldsByCode("444", 'n');
    fields.addAll(record.getDatafieldsByCode("144", 'n'));

    return fields.stream()
      .map(orderNumber ->
        orderNumber.replaceAll("(?i)n(?:o| ?°|r\\.)s?", "").trim()
      )
      .distinct()
      .collect(Collectors.toList());
  }

  private List<String> getGenre() {
    return record.getDatafieldsByCode("610").stream()
      .filter(field -> field.isCode('a') && field.isCode('b'))
      .filter(field -> field.getString('b').equals("04"))    //$b=04 means "genre"
      .map(field -> field.getString('a').toLowerCase())
      .collect(Collectors.toList());
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

  public PF22_SelfContainedExpression add(PF22_SelfContainedExpression child) {
    this.resource.addProperty(FRBROO.R5_has_component, child.asResource());
    return this;
  }

}
