package org.doremus.marc2rdf.main;

import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.CIDOC;
import org.geonames.Toponym;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class E53_Place extends DoremusResource {
  private static final String TODAY_REGEX = "[(\\[](?:=|auj(?:\\.|ourd'hui|actuellement)) (.+)";
  private static final Pattern TODAY_PATTERN = Pattern.compile(TODAY_REGEX);

  private Toponym tp;
  private String name;

  public E53_Place() {
    super();
  }

  public E53_Place(String rawString) {
    this(rawString, null, null);
  }

  public E53_Place(String rawString, String countryCode, String continent) {
    super();

    rawString = cleanString(rawString);
    if (rawString.equals("Cité de la musique (Paris)")) rawString = "Cité de la musique";
    this.name = null;

    tp = GeoNames.query(rawString, countryCode, continent);
    if (tp != null) {
      // simply download the file
      uri = GeoNames.toURI(tp.getGeoNameId());
      this.name = tp.getName();
    } else {
      System.out.println(rawString);
      try {
        uri = ConstructURI.build("E53_Place", rawString).toString();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    this.regenerateResource(uri);
    this.setClass(CIDOC.E53_Place);

    this.addProperty(RDFS.label, rawString.replaceAll("\\(.+\\)", ""), "fr")
      .addProperty(CIDOC.P1_is_identified_by, rawString, "fr");

    if (this.name != null) {
      this.addProperty(RDFS.label, this.name, "en")
        .addProperty(GeoNames.NAME, this.name);
    }
  }

  public static E53_Place withUri(String uri) {
    E53_Place p = new E53_Place();
    p.regenerateResource(uri);
    return p;
  }

  public void addSurroundingPlace(E53_Place external) {
    this.addProperty(CIDOC.P89_falls_within, external);
  }

  public boolean isGeonames() {
    return this.tp != null;
  }

  private String cleanString(String rawString) {
    rawString = rawString.trim()
      .replaceAll("\\(?\\?\\)?", "")
      .replaceAll("[,;:]?", "")
      .replaceAll(" +", " ")
      .replaceAll("\\( ?\\)", "")
      .replaceAll("(?i)^\\(the\\)", "")
      .replaceAll("(?i)(Né|Mort|Vit|inhumé)[eà]? à ", "")
      .replaceAll("^à ", "")
      .replaceAll("Grande[- ]Bretagne", "UK")
      .replaceAll("G[.-] ?B[.)]", "UK")
      .replaceAll("Isr[aä][eë]l", "Israel")
      .replaceAll("Tex[.)]", "Texas")
      .replaceAll("Conn[.)],?", "Connecticut")
      .replaceAll("Calif[.)],?", "California")
      .replaceAll("Del[.)],?", "Delaware")
      .replaceAll("Okla?[.)]", "Oklahoma")
      .replaceAll("Mich[.)]", "Michigan")
      .replaceAll("Fla[.)]", "Florida")
      .replaceAll("Flor[.)]", "Florida")
      .replaceAll("La[.)]", "Louisiana")
      .replaceAll("Tenn?[.)]", "Tennessee")
      .replaceAll("Ariz[.)]", "Arizona")
      .replaceAll("Ark?[.)]", "Arkansas")
      .replaceAll("W\\. ?Va?[.)]?", "West Virginia")
      .replaceAll("Va[.)]", "Virginia")
      .replaceAll("Ala[.)]", "Alabama")
      .replaceAll("Kent[.)]", "Kentucky")
      .replaceAll("Colo[.)]", "Colorado")
      .replaceAll("Nev[.)]", "Nevada")
      .replaceAll("Pa[.)]", "Pennsylvania")
      .replaceAll("Penn[.)]", "Pennsylvania")
      .replaceAll("Mass[.)]", "Massachusetts")
      .replaceAll("Mich[.)]", "Michigan")
      .replaceAll("Minn[.)]", "Minnesota")
      .replaceAll("Miss[.)]", "Mississipi")
      .replaceAll("Mis[.)]", "Missouri")
      .replaceAll("Wisc?[.)]", "Wisconsin")
      .replaceAll("Ind[.)]", "Indiana")
      .replaceAll("Mont\\.$", "Montana")
      .replaceAll("I[lI]l[.)]", "Illinois")
      .replaceAll("Ont[.)]", "Ontario")
      .replaceAll("Neb[.)]", "Nebraska")
      .replaceAll("Md[.)]", "Maryland")
      .replaceAll("N\\. ?J[.)]", "New Jersey")
      .replaceAll("N\\. ?Y[.)]", "New York")
      .replaceAll("N\\. ?D[.)]", "North Dakota")
      .replaceAll("S\\. ?D[.)]", "South Dakota")
      .replaceAll("N\\. ?C[.)]", "North Carolina")
      .replaceAll("S\\. ?C[.)]", "South Carolina")
      .replaceAll("B\\. ?C[.)]", "British Columbia")
      .replaceAll("C\\.[ -]?B[.)]", "British Columbia")
      .replaceAll("N\\. ?M[.)]", "New Mexico")
      .replaceAll("N\\. ?H[.)]", "New Hampshire")
      .replaceAll("N\\. ?B[.)]", "New Brunswick")
      .replaceAll("N\\. ?S[.)]", "Nova Scotia")
      .replaceAll("R\\. ?I[.)]", "Rhode Island")
      .replaceAll("Wash[.)]", "Washington")
      .replaceAll("Wash[.)]", "Washington")
      .trim();

    Matcher m = TODAY_PATTERN.matcher(rawString);
    if (m.find()) {
      String content = m.group(1);
      if (content.startsWith("République") || content.equals("Grèce"))
        rawString = rawString.replace(m.group(0), content);
      else
        rawString = content;
    }
    if (rawString.toLowerCase().contains("festival"))
      rawString = rawString.split("(?i)festival")[0];

    return rawString
      .replaceAll("[,.] ?$", "")
      .replaceAll("[?\\[\\]]", "").trim();
  }
}
