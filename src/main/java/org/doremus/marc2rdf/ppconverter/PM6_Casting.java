package org.doremus.marc2rdf.ppconverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PM6_Casting {
  private static final List<String> voices = Arrays.asList("soprano", "contralto", "tenor", "ténor",
    "basse", "voix", "choeur", "coeurs");

  private final Resource resource;
  private final Model model;

  private List<PM23_Casting_Detail> cDets;

  public PM6_Casting(String note, String uri, Model model) {
    Resource M6Casting = model.createResource(uri)
      .addProperty(RDF.type, MUS.M6_Casting)
      .addProperty(RDFS.comment, note)
      .addProperty(CIDOC.P3_has_note, note);

    this.resource = M6Casting;
    this.model = model;
    this.cDets = new ArrayList<>();

    int detailNum = 0;

    if (note.equals("Instrumental")) {
      // do nothing
    } else if (note.startsWith("Rôles :")) {
      // TODO Opera roles
      // Rôles : Nymphe de la Seine, La Gloire, Nymphe des Tuileries, Nymphe de la Marne, Alceste, Céphise, Thétis, Proserpine, Esprit repoussé, Diane (sopranos) ; Admète, Lychas, Apollon, Alecton (haute-contre) ; Cléante, Phérès (ténors) ; Alcide, Charon, Eole (barytons) ; Straton, Pluton (basse). Choeurs, orchestre symphonique.
    } else if (note.contains(":") || note.contains(".")) {
      // TODO complex
      // Ouverture : les bois par 2, 2 cors, 2 trompettes et 1 tuba, timales et quintette à cordes. Musique de scène : pour deux sopranos, choeur féminin et orchestre ( les bois par 2, 2 cors, 3 trompettes, 3 trombones, 1 ophicléide ou tuba, timbales, petite batterie et quintette à cordes
    } else {
      // FIX: there are cases with unbalanced parenthesis in the MARC file
      int countLeft = StringUtils.countMatches(note, "(");
      int countRight = StringUtils.countMatches(note, ")");
      if (countLeft > countRight) note += ")";

      // replace unuseful strings
      note = note
        .replaceAll("(?i)^pour", "")
        .replaceAll("\\(\\d+ exécutants\\)", "").trim();

      // replace commas and conjunction inside parenthesis with '+'
      String tempString = note;
      String complexParenthesisRegex = "\\(([^),]+?)(?:(?:,| et)([^,]+?))+\\)";
      Pattern complexParethesisPattern = Pattern.compile(complexParenthesisRegex);
      Matcher complexParentesisMatcher = complexParethesisPattern.matcher(note);
      while (complexParentesisMatcher.find()) {
        String curText = complexParentesisMatcher.group();
        String newText = curText.replaceAll(getPartSplitterRegex(",;"), " + ");
        tempString = tempString.replace(curText, newText);
      }
      note = tempString;

      String[] splits;
      if (note.contains(";")) splits = note.split(getPartSplitterRegex(";"));
      else splits = note.split(getPartSplitterRegex(","));

      for (String match : splits) {
        match = match.trim();

        // i.e. "clarinette soliste", "piano, (solo)"
        String soloRegex1 = ", \\(solo\\)",
          soloRegex2 = " sol[oi]s?$",
        soloRegex3 = " solistes?";
        String part = match
          .replaceAll(soloRegex1, "")
          .replaceAll(soloRegex2, "")
          .replaceAll(soloRegex3, "").trim();

        if (part.isEmpty()) continue;

        boolean isSolo = !part.equals(match);

        // replace french numeral with number
        part = part.replaceAll("(?i)^une? ", "1 ")
          .replaceAll("(?i)^deux ", "2 ")
          .replaceAll("(?i)^trois ", "3 ")
          .replaceAll("(?i)^quatre ", "4 ")
          .replaceAll("(?i)^cinq ", "5 ")
          .replaceAll("(?i)^six ", "6 ")
          .replaceAll("(?i)^huit ", "8 ")
          .replaceAll("(?i)^onze ", "11 ");

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
            subPart = subPart
              .replaceAll("jouant ", "")
              .replaceAll("l[ae] seconde? doublant avec l[ae] ", "").trim();
            if (subPart.startsWith("en ") | subPart.startsWith("à ") || subPart.startsWith("de ")) {
              subPart = part + " " + subPart;
            }
            cDets.add(new PM23_Casting_Detail(subPart.trim(), subQuantity, isSubSolo, uri + "/detail/" + ++detailNum));
            quantity -= subQuantity;
          }
        }
        cDets.add(new PM23_Casting_Detail(part, quantity, isSolo, uri + "/detail/" + ++detailNum));
      }

      for (int i = 0; i < cDets.size(); i++) {
        PM23_Casting_Detail current = cDets.get(i);
        String currentName = current.getLName();

        if (currentName == null && current.quantity > -1 && i < cDets.size() - 1) {
          currentName = current.name = cDets.get(i + 1).name;
        }

        if (currentName == null) continue;

        if (currentName.equalsIgnoreCase("II"))
          current.name = cDets.get(i - 1).name;

        if (currentName.equals("alto") || currentName.equals("baryton")) {
          // workaround for https://github.com/DOREMUS-ANR/marc2rdf/issues/53
          String prev = "***", foll = "***";
          if (i > 0) prev = cDets.get(i - 1).getLName();
          if (i < cDets.size() - 1) foll = cDets.get(i + 1).getLName();

          if (voices.contains(prev) || voices.contains(foll))
            current.setAsVoice();
        }

        M6Casting.addProperty(MUS.U23_has_casting_detail, current.asResource(model));
      }
    }
  }

  private String getPartSplitterRegex(String separator) {
    return "([" + separator + "](?!\\s+(\\(solo\\)|dont|I+))\\s?(et )?| et )";
  }

  public Resource asResource() {
    return resource;
  }

}
