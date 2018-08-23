package org.doremus.marc2rdf.bnfconverter;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.doremus.marc2rdf.main.DoremusResource;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class M5_Genre extends DoremusResource {
  private static List<ChantContextMap> list = null;
  private final Resource subGenre;
  private List<Resource> religiousContext;

  public M5_Genre(DataField df) {
    // field 143
    super(df.getString('a'));
    religiousContext = new ArrayList<>();


    String label = df.getString('a');
    String subLabel = df.getString('b');

    ChantContextMap match = getList().stream()
      .filter(x -> label.equalsIgnoreCase(x.field))
      .findFirst().orElse(null);

    if (match != null) {
      setUri(match.genreUri);
      this.religiousContext = match.getContext().stream()
        .map(uri -> model.createResource(uri))
        .peek(ctx -> this.addProperty(SKOS.related, ctx))
        .collect(Collectors.toList());
    } else {
      this.setClass(MUS.M5_Genre);
      this.addProperty(RDFS.label, label)
        .addProperty(CIDOC.P1_is_identified_by, label);
    }

    this.subGenre =
      VocabularyManager.searchInCategory(subLabel, null, "genre", false);
  }


  public static M5_Genre fromField(DataField df) {
    // field 143
    String value = df.getString('a');
    if (!value.startsWith("Chant")) return null;
    return new M5_Genre(df);
  }

  public Resource getSubGenre() {
    return subGenre;
  }

  public List<Resource> getContext() {
    return religiousContext;
  }


  private static class ChantContextMap {
    @CsvBindByName(column = "143 $a")
    private String field;
    @CsvBindByName(column = "genre label")
    private String genreLabel;
    @CsvBindByName(column = "genre URI")
    private String genreUri;
    @CsvBindByName(column = "context label")
    private String contextLabel;
    @CsvBindByName(column = "context uri")
    private String contextUri;

    public List<String> getContext() {
      if (contextUri == null) return new ArrayList<>();
      return Arrays.asList(contextUri.split("\n"));
    }
  }

  private static List<ChantContextMap> getList() {
    if (list == null) init();
    return list;
  }


  @SuppressWarnings({"ConstantConditions", "unchecked"})
  private static void init() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    File csv = new File(cl.getResource("mapping143chant.csv").getFile());
    try {
      list = new CsvToBeanBuilder(new FileReader(csv)).withType(ChantContextMap.class).build().parse();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

}
