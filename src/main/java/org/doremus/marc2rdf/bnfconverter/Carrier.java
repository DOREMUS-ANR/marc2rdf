package org.doremus.marc2rdf.bnfconverter;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Objects;

public class Carrier {
  private static List<Carrier> list = null;

  @CsvBindByName(column = "RDA-FR")
  private String label;
  @CsvBindByName(column = "code009")
  private String g009;
  @CsvBindByName(column = "code050")
  private String a050;
  @CsvBindByName(column = "avec 051$a")
  private String a051;
  @CsvBindByName(column = "modifier 051$a")
  private String a051_mod;


  public static String fromField050a(String input, List<String> field051a) {
    return getList().stream()
      .filter(x -> Objects.equals(input, x.a050))
      .filter(x -> field051a.stream()
        .anyMatch(y -> y.matches(x.get051regex())) == x.should051be())
      .map(x -> x.label)
      .findFirst().orElse(null);
  }

  private String get051regex() {
    if (a051_mod == null) return ".+";
    return "(" + a051_mod + ")";
  }

  private boolean should051be() {
    return !Objects.equals(a051_mod, "NOT");
  }

  public static List<Carrier> getList() {
    if (list == null) init();
    return list;
  }

  private static void init() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    @SuppressWarnings("ConstantConditions")
    File csv = new File(cl.getResource("support.csv").getFile());

    try {
      //noinspection unchecked
      list = new CsvToBeanBuilder(new FileReader(csv)).withType(Carrier.class).build().parse();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }


}
