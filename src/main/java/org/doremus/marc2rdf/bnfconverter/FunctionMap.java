package org.doremus.marc2rdf.bnfconverter;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionMap {
  private static List<FunctionMap> list = null;

  @CsvBindByName(column = "context")
  private String context;

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

  public static FunctionMap get(String code) {
    if (code == null) return null;
    return getList().stream()
      .filter(f -> code.equals(f.code))
      .findAny().orElse(null);
  }

  public static FunctionMap getFrom245(String txt, String context) {
    if (txt == null || txt.isEmpty()) return null;
    return getList(context).stream()
      .filter(f -> f.abbr245 != null && txt.contains(f.abbr245) || f.label245 != null && txt.contains(f.label245))
      .findFirst().orElse(null);
  }


  private static List<FunctionMap> getList() {
    if (list == null) init();
    return list;
  }

  public static List<FunctionMap> getList(String context) {
    return getList().stream().filter(x -> x.context.equals(context)).collect(Collectors.toList());
  }

  private static void init() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    @SuppressWarnings("ConstantConditions")
    File csv = new File(cl.getResource("bib_function_mapping.csv").getFile());

    try {
      //noinspection unchecked
      list = new CsvToBeanBuilder(new FileReader(csv)).withType(FunctionMap.class).build().parse();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }


  public Resource getMop() {
    if (this.mop != null)
      return VocabularyManager.getVocabulary("mop-iaml").getConcept(this.mop);
    if (this.isLabelFunction) return null;
    return ModelFactory.createDefaultModel().createResource()
      .addProperty(RDF.type, MUS.M14_Medium_Of_Performance)
      .addProperty(RDFS.label, this.label);
  }
}
