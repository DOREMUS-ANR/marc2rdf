package org.doremus.marc2rdf.ppconverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.*;
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
public class PF22_SelfContainedExpression extends DoremusResource {
  private final StanfordLemmatizer slem;
  private static final String opusHeaderRegex = "(?i)^(?:op(?:\\.|us| )|Oeuvre|Werk nr\\.?) ?(?:post(?:hume|h?\\.|h))?";
  private static final String opusSubnumberRegex = "(?i)(?:,? n(?:[o°.]| °)s?)";
  private final PF28_ExpressionCreation f28;

  private List<String> opusMemory;

  public PF22_SelfContainedExpression(Record record, String identifier, PF28_ExpressionCreation f28) throws URISyntaxException {
    super(record, identifier);
    this.resource.addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression);
    this.resource.addProperty(DCTerms.identifier, identifier);
    this.resource.addProperty(OWL.sameAs, model.createResource("http://digital.philharmoniedeparis.fr/doc/CIMU/" + identifier));

    this.f28 = f28;
    this.slem = Converter.stanfordLemmatizer;
    this.opusMemory = new ArrayList<>();

    /**************************** Expression: Title *****************************************/
    for (String title : getTitle())
      this.resource.addProperty(MUS.U70_has_title, title).addProperty(RDFS.label, title);

    /**************************** Expression: Catalogue *************************************/
    for (String catalog : getCatalog()) {
      // sometimes there is the opus number inside here!
      if (catalog.matches(opusHeaderRegex + ".*")) {
        // System.out.println("opus in catalog found " + catalog);
        parseOpus(catalog);
        continue;
      }
      for (String c : catalog.split(" ; "))
        parseCatalog(c);
    }


    /**************************** Expression: Opus ******************************************/
    for (String opus : getOpus()) {
      // is it for real an opus information?

      // completely numeric: it is
      if (StringUtils.isNumeric(opus)) {
        addOpus(opus, opus);
        continue;
      }
      // if does not contain "Op." or "opus" etc., it is a catalog!
      if (!opus.matches(opusHeaderRegex + ".*")) {
        // System.out.println("catalog in opus found " + opus);
        parseCatalog(opus);
        continue;
      }

      parseOpus(opus);
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
      Literal label = model.createLiteral(genre, "fr");
      this.resource.addProperty(MUS.U12_has_genre, model.createResource()
        .addProperty(RDF.type, MUS.M5_Genre)
        .addProperty(RDFS.label, label)
        .addProperty(CIDOC.P1_is_identified_by, label)
      );
    }

    /**************************** Expression: Order Number **********************************/
    for (String orderNumber : getOrderNumber()) {
      List<Integer> range = Utils.toRange(orderNumber);
      if (range == null)
        this.resource.addProperty(MUS.U10_has_order_number, Utils.toSafeNumLiteral(orderNumber));
      else {
        for (int i : range) this.resource.addProperty(MUS.U10_has_order_number, model.createTypedLiteral(i));
      }
    }

    /**************************** Expression: Casting ***************************************/
    int castingNum = 0;

    for (String castingString : getCasting()) {
      castingString = castingString.trim().replaceFirst("\\.$", "");

      String castingUri = this.uri.toString() + "/casting/" + (++castingNum);
      Resource M6Casting = model.createResource(castingUri);
      M6Casting.addProperty(RDF.type, MUS.M6_Casting);
      M6Casting.addProperty(CIDOC.P3_has_note, castingString);
      int detailNum = 0;

      if (castingString.equals("Instrumental")) {
        // do nothing
      } else if (castingString.startsWith("Rôles :")) {
        // TODO Opera roles
        // Rôles : Nymphe de la Seine, La Gloire, Nymphe des Tuileries, Nymphe de la Marne, Alceste, Céphise, Thétis, Proserpine, Esprit repoussé, Diane (sopranos) ; Admète, Lychas, Apollon, Alecton (haute-contre) ; Cléante, Phérès (ténors) ; Alcide, Charon, Eole (barytons) ; Straton, Pluton (basse). Choeurs, orchestre symphonique.
      } else if (castingString.contains(":") || castingString.contains(".")) {
        // TODO complex
        // Ouverture : les bois par 2, 2 cors, 2 trompettes et 1 tuba, timales et quintette à cordes. Musique de scène : pour deux sopranos, choeur féminin et orchestre ( les bois par 2, 2 cors, 3 trompettes, 3 trombones, 1 ophicléide ou tuba, timbales, petite batterie et quintette à cordes
      } else {
        // FIX: there are cases with unbalanced parenthesis in the MARC file
        int countLeft = StringUtils.countMatches(castingString, "(");
        int countRight = StringUtils.countMatches(castingString, ")");
        if (countLeft > countRight) castingString += ")";

        // replace unuseful strings
        castingString = castingString.replaceAll("\\(\\d+ exécutants\\)", "").trim();

        String partSplitterRegex = "([,;](?!\\s+(\\(solo\\)|dont|I+))\\s?(et )?| et )";

        // replace commas and conjunction inside parenthesis with '+'
        String tempString = castingString;
        String complexParenthesisRegex = "\\(([^),]+?)(?:(?:,| et)([^,]+?))+\\)";
        Pattern complexParethesisPattern = Pattern.compile(complexParenthesisRegex);
        Matcher complexParentesisMatcher = complexParethesisPattern.matcher(castingString);
        while (complexParentesisMatcher.find()) {
          String curText = complexParentesisMatcher.group();
          String newText = curText.replaceAll(partSplitterRegex, " + ");
          tempString = tempString.replace(curText, newText);
        }
        castingString = tempString;

        for (String match : castingString.split(partSplitterRegex)) {
          match = match.trim();

          // i.e. "clarinette soliste", "piano, (solo)"
          String soloRegex1 = ", \\(solo\\)", soloRegex2 = " sol(o|iste)s?$";
          String part = match.replaceAll(soloRegex1, "")
            .replaceAll(soloRegex2, "").trim();
          boolean isSolo = !part.equals(match);

          // replace french numeral with number
          part = part.replaceAll("(?i)^une? ", "1 ")
            .replaceAll("(?i)^deux ", "2 ")
            .replaceAll("(?i)^trois ", "3 ")
            .replaceAll("(?i)^quatre ", "4 ")
            .replaceAll("(?i)^cinq ", "5 ")
            .replaceAll("(?i)^six ", "6 ");

          int quantity = -1;

          Pattern numRegex1 = Pattern.compile("(.+)\\((\\d+)\\)$"); // i.e. Violons (5)''
          Pattern numRegex2 = Pattern.compile("^(\\d+) (.+)"); // i.e. "3 hautbois"
          Matcher matcher = numRegex1.matcher(part);
          if (matcher.find()) {
            part = matcher.group(1).trim();
            quantity = Integer.parseInt(matcher.group(2).trim());
          } else {
            matcher = numRegex2.matcher(part);
            if (matcher.find()) {
              part = matcher.group(2).trim();
              quantity = Integer.parseInt(matcher.group(1).trim());
            }
          }

          // sub instruments i.e. "3 bassoons dont 1 contrabassoon"
          String subInstrumentRegex = "^[^(]+(\\(?dont ([^)\\n]+)\\)?)$";
          Pattern subInstrumentPattern = Pattern.compile(subInstrumentRegex);
          matcher = subInstrumentPattern.matcher(part);
          if (matcher.find()) {

            part = part.replace(matcher.group(1), "").trim();
            if (part.endsWith(",")) part = part.substring(0, part.length() - 1).trim();

            String subParts = matcher.group(2).trim();
            for (String subMatch : subParts.split("\\+")) {
              String subPart = subMatch.trim().replaceAll(soloRegex1, "")
                .replaceAll(soloRegex2, "").trim();
              boolean isSubSolo = !subPart.equals(match);

              int subQuantity;
              // replace french numeral with number
              subPart = subPart.replaceAll("(?i)^une? ", "1 ")
                .replaceAll("(?i)^deux ", "2 ")
                .replaceAll("(?i)^trois ", "3 ")
                .replaceAll("(?i)^quatre ", "4 ")
                .replaceAll("(?i)^cinq ", "5 ")
                .replaceAll("(?i)^six ", "6 ");

              Matcher subMatcher = numRegex2.matcher(subPart);
              if (subMatcher.find()) {
                subPart = subMatcher.group(2).trim();
                subQuantity = Integer.parseInt(subMatcher.group(1).trim());
              } else subQuantity = 1;

              // special cases
              if (subPart.equals("la petite")) subPart = "piccolo";
              subPart = subPart.replaceAll("jouant ", "").replaceAll("l[ae] seconde? doublant avec l[ae] ", "").trim();
              if (subPart.startsWith("en ") | subPart.startsWith("à ") || subPart.startsWith("de ")) {
                subPart = part + " " + subPart;
              }

              M6Casting.addProperty(MUS.U23_has_casting_detail, makeCastingDetail(subPart.trim(), subQuantity, isSubSolo, castingUri + "/detail/" + ++detailNum));
              quantity -= subQuantity;
            }
          }
          M6Casting.addProperty(MUS.U23_has_casting_detail, makeCastingDetail(part, quantity, isSolo, castingUri + "/detail/" + ++detailNum));

        }

      }

      this.resource.addProperty(MUS.U13_has_casting, M6Casting);
    }
  }

  private void parseCatalog(String catalog) {
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

    Resource match = VocabularyManager.getMODS("catalogue").findModsResource(catalogName, Person.toIdentifications(f28.getComposers()));
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
    String opusData = opus.replaceFirst(opusHeaderRegex, "").replaceFirst("\\.$", "").trim();
    if (opusData.isEmpty()) {
      // it means that it is only written "op. posthume" or similar
      // add it as a note to F22
      this.resource.addProperty(CIDOC.P3_has_note, opus);
      return;
    }

    if (opusData.matches(".*" + opusSubnumberRegex + ".*")) {
      String[] opusParts = opusData.split(opusSubnumberRegex);

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

  public Resource makeCastingDetail(String name, int quantity, boolean solo, String uri) {
    name = name.replaceAll("non spécifiée", "");

    // punctual fix
    if (name.equals("flûtes")) name = "flûte";
    if (name.equals("contrebasses")) name = "contrebasse";
//    System.out.println(name + " --> " + slem.lemmatize(name));
    // singularize
    name = instrumentToSingular(name);


    Resource M23CastingDetail = model.createResource(uri)
      .addProperty(RDF.type, MUS.M23_Casting_Detail);

    if (solo)
      M23CastingDetail.addProperty(MUS.U36_foresees_responsibility_of_type, model.createLiteral("soloist", "fr"));

    if (quantity > -1)
      M23CastingDetail.addProperty(MUS.U30_foresees_quantity_of_mop, model.createTypedLiteral(quantity));

    Literal mopLiteral = model.createLiteral(name, "fr");

    Resource match = VocabularyManager.searchInCategory(name, "fr", "mop");
    if (match != null)
      M23CastingDetail.addProperty(MUS.U2_foresees_use_of_medium_of_performance_of_type, match);
    else M23CastingDetail.addProperty(MUS.U2_foresees_use_of_medium_of_performance_of_type, model.createResource()
      .addProperty(RDF.type, MUS.M14_Medium_Of_Performance)
      .addProperty(RDFS.label, mopLiteral)
      .addProperty(CIDOC.P1_is_identified_by, mopLiteral));

    return M23CastingDetail;
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
      if (!title.isEmpty()) titleList.add(title);
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

  /*****************************************
   * Les genres
   *************************************/
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

  /***********************************
   * Casting
   ***********************************/
  private List<String> getCasting() {
    if (!record.isType("UNI:100")) return new ArrayList<>();
    return record.getDatafieldsByCode("909", 'c');
  }

  public PF22_SelfContainedExpression add(PF30_PublicationEvent f30) {
    this.resource.addProperty(MUS.U4_had_princeps_publication, f30.asResource());
    return this;
  }

  public PF22_SelfContainedExpression addPremiere(PM42_PerformedExpressionCreation m42) {
    this.resource.addProperty(MUS.U5_had_premiere, m42.asResource());
    return this;
  }

  private String instrumentToSingular(String r) {
    String[] parts = r.split(" ");
    if (parts.length == 1) return slem.lemmatize(parts[0]).get(0);

    // cornets à pistons --> cornet à pistons
    parts[0] = slem.lemmatize(parts[0]).get(0);
    return String.join(" ", parts);
  }


}
