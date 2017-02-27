package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.util.ArrayList;
import java.util.List;

public class Utils {
  public static List<Integer> toRange(String rangeString) {
    if (!rangeString.contains(" à ")) return null;

    String[] singleNum = rangeString.split(" à ", 2);
    List<Integer> range = new ArrayList<>();
    try {
      int start = Integer.parseInt(singleNum[0]);
      int end = Integer.parseInt(singleNum[1]);

      for (int i = start; i <= end; i++) {
        range.add(i);
      }

    } catch (Exception e) {
      System.out.println("Not able to parse range in " + rangeString);
      System.out.println(e.getMessage());
      return null;
    }

    return range;
  }

  public static Literal toSafeNumLiteral(String str) {
    Model model = ModelFactory.createDefaultModel();
    if (str.matches("\\d+"))
      return model.createTypedLiteral(Integer.parseInt(str));
    else return model.createTypedLiteral(str);
  }
}
