package org.doremus.marc2rdf.bnfconverter;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class FunctionPerformerMap {
  private static List<FunctionPerformerMap> list = null;

  @CsvBindByName(column = "function")
  private String function;
  @CsvBindByName(column = "mop")
  private String mop;
  @CsvBindByName(column = "mopLabel")
  private String mopLabel;
  @CsvBindByName(column = "code")
  private String code;
  @CsvBindByName(column = "label")
  private String label;
  @CsvBindByName(column = "245abbr")
  private String abbr245;
  @CsvBindByName(column = "245label")
  private String label245;
  @CsvBindByName(column = "isLabelFun")
  private boolean isLabelFunction;


  public String getFunction() {
    if (this.function != null) return this.function;
    if (this.isLabelFunction) return this.label;
    return null;
  }

  public static FunctionPerformerMap get(String code) {
    if (code == null) return null;
    return getList().stream()
      .filter(f -> code.equals(f.code))
      .findAny().orElse(null);
  }

  public static FunctionPerformerMap getFrom245(String txt) {
    if (txt == null || txt.isEmpty()) return null;
    return getList().stream()
      .filter(f -> f.abbr245 != null && txt.contains(f.abbr245) || f.label != null && txt.contains(f.label245))
      .findFirst().orElse(null);
  }


  public static List<FunctionPerformerMap> getList() {
    if (list == null) init();
    return list;
  }

  private static void init() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    @SuppressWarnings("ConstantConditions")
    File csv = new File(cl.getResource("bib_function_performers.csv").getFile());

    try {
      //noinspection unchecked
      list = new CsvToBeanBuilder(new FileReader(csv)).withType(FunctionPerformerMap.class).build().parse();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }


  public Resource getMop() {
    if (this.mop != null)
      return VocabularyManager.getVocabulary("mop-iaml").getConcept(this.mop);
    if (this.isLabelFunction) return null;
    return ResourceFactory.createResource()
      .addProperty(RDF.type, MUS.M14_Medium_Of_Performance)
      .addProperty(RDFS.label, this.label);
  }
}
