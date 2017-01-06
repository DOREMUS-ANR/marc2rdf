package org.doremus.marc2rdf.ppconverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Literal;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

      String[] catalogParts = catalog.replaceAll("\\.", " ").split(" ");

      if (catalogParts.length > 1) {
        M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, catalogParts[0])
          .addProperty(MUS.U41_has_catalogue_number, catalogParts[1]);
      } else {
        System.out.println("Not parsable catalog: " + catalog);
        // TODO what to do with not parsable catalogs?
      }

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
      castingString = castingString.trim();

      Resource M6Casting = model.createResource(this.uri.toString() + "/casting/" + (++castingNum));
      M6Casting.addProperty(RDF.type, MUS.M6_Casting);
      M6Casting.addProperty(CIDOC.P3_has_note, castingString);

      if (castingString.startsWith("Rôles :")) {
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

        // replace french numeral with number
        castingString = castingString.replaceAll("(?i)^une? ", "1 ")
          .replaceAll("(?i)^deux ", "2 ")
          .replaceAll("(?i)^trois ", "3 ")
          .replaceAll("(?i)^quatre ", "4 ")
          .replaceAll("(?i)^cinq ", "5 ")
          .replaceAll("(?i)^six ", "6 ");


        for (String match : castingString.split(partSplitterRegex)) {
          match = match.trim();

          // i.e. "clarinette soliste", "piano, (solo)"
          String soloRegex1 = ", \\(solo\\)", soloRegex2 = " sol(o|iste)s?$";
          String part = match.replaceAll(soloRegex1, "")
            .replaceAll(soloRegex2, "").trim();
          boolean isSolo = !part.equals(match);

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
            // TODO singularize part

            String subParts = matcher.group(2).trim();
            for (String subMatch : subParts.split("\\+")) {
              String subPart = subMatch.trim().replaceAll(soloRegex1, "")
                .replaceAll(soloRegex2, "").trim();
              boolean isSubSolo = !subPart.equals(match);

              int subQuantity;
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

              M6Casting.addProperty(MUS.U23_has_casting_detail, makeCastingDetail(subPart.trim(), subQuantity, isSubSolo));
              quantity -= subQuantity;
            }
          }


          M6Casting.addProperty(MUS.U23_has_casting_detail, makeCastingDetail(part, quantity, isSolo));

        }

      }

      this.resource.addProperty(MUS.U13_has_casting, M6Casting);
    }
  }

  public Resource makeCastingDetail(String name, int quantity, boolean solo) {
    Resource M23CastingDetail = model.createResource();

    if (solo)
      M23CastingDetail.addProperty(MUS.U36_foresees_responsibility_of_type, model.createLiteral("soloist", "fr"));
    if (quantity > -1) M23CastingDetail.addProperty(MUS.U9_has_quantity, model.createTypedLiteral(quantity));

    M23CastingDetail.addProperty(RDF.type, MUS.M23_Casting_Detail);
    Literal mopLiteral = model.createLiteral(name, "fr");


    M23CastingDetail.addProperty(MUS.U2_foresees_use_of_medium_of_performance_of_type,
      model.createResource()
        .addProperty(RDF.type, MUS.M14_Medium_Of_Performance)
        .addProperty(CIDOC.P1_is_identified_by, mopLiteral)
    );

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
      if (!results.contains(catalog)) results.add(catalog.trim());
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

}
