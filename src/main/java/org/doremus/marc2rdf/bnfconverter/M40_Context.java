package org.doremus.marc2rdf.bnfconverter;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.main.E53_Place;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class M40_Context extends DoremusResource {
  private static List<ContextMap> list = null;
  private List<E53_Place> places;
  private String label;

  public M40_Context(DataField df) {
    super();
    places = new ArrayList<>();

    String continent = null;
    List<String> placeChain = df.getStrings('m');
    if (placeChain.size() > 1)
      continent = guessContinent(placeChain.remove(0));

    E53_Place previous = null;
    for (String p : placeChain) {
      E53_Place place = new E53_Place(p, null, continent);
      if (place.isGeonames()) {
        places.add(place);
        if (previous != null) place.addSurroundingPlace(previous);
        previous = place;
      } else break;
    }

    this.label = df.getString('e');
    if (label == null || label.isEmpty()) return;
    this.uri = getRameauUri(df.toString());

    if (this.uri != null) {
      this.resource = model.createResource(this.uri);
    } else {
      try {
        this.regenerateResource(ConstructURI.build("M40_Context", label).toString());
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      this.setClass(MUS.M40_Context);
      this.addProperty(RDFS.label, label)
        .addProperty(CIDOC.P1_is_identified_by, label);
    }
    this.addProperty(SKOS.related, getGeoContext());
  }

  @Override
  public Resource asResource() {
    if (resource != null) return null;
    else return super.asResource();
  }

  private static String getRameauUri(String txt) {
    return getList().stream()
      .filter(x -> txt.equalsIgnoreCase(x.bnfField))
      .map(ContextMap::getRameauUri)
      .findFirst().orElse(null);
  }

  public E53_Place getGeoContext() {
    if (places.size() == 0) return null;
    return places.get(places.size() - 1);
  }

  public static M40_Context fromField(DataField df) {
    String value = df.getString('a');
    if (!"Traditions".equals(value)) return null;
    return new M40_Context(df);
  }

  private static String guessContinent(String txt) {
    switch (txt.toLowerCase()) {
      case "océanie":
        return "OC";
      case "afrique":
      case "afrique du nord":
      case "afrique du sud":
        return "AF";
      case "asie centrale":
      case "asie":
        return "AS";
      case "amérique de nord":
      case "amérique du nord":
        return "NA";
      case "amérique du sud":
      case "amérique latine":
        return "SA";
      case "europe":
      case "europe centrale":
        return "EU";
      case "amérique centrale":
      case "amérique":
        return "";
    }
    return null;
  }

  private static List<ContextMap> getList() {
    if (list == null) init();
    return list;
  }

  @SuppressWarnings({"ConstantConditions", "unchecked"})
  private static void init() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    File csv = new File(cl.getResource("ethnic_map.csv").getFile());
    try {
      list = new CsvToBeanBuilder(new FileReader(csv)).withType(ContextMap.class).build().parse();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

  @SuppressWarnings("WeakerAccess") // requires to be public for the CSV conversion
  public static class ContextMap {
    private static final String RAMEAU_BASE = "http://data.bnf.fr/ark:/12148/cb%%%z";
    @CsvBindByName(column = "bnf_id")
    private String bnfId;
    @CsvBindByName(column = "bnf_field")
    private String bnfField;
    @CsvBindByName(column = "score")
    private float score;
    @CsvBindByName(column = "rameau_id")
    private String rameauId;
    @CsvBindByName(column = "rameau_field")
    private String rameauField;

    private String getRameauUri() {
      return RAMEAU_BASE.replace("%%%", this.rameauId);
    }
  }
}
