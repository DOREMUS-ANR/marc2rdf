package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Utils {
  private static Map<String, Locale> localeMap;
  private static Map<String, String> intermarcScriptMap;
  private static Map<String, String> intermarcMopMap;

  public static final String opusHeaderRegex = "(?i)^(?:op(?:\\.|us| )|Oeuvre|WoO|Werk nr\\.?) ?(?:post(?:hume|h?\\" +
    ".|h))?";
  public static final String opusSubnumberRegex = "(?i)(?:,? n(?:[o°.]| °)[s.]?)";

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
      System.out.println("--> " + e.getMessage());
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

  public static String toISO2Lang(String lang) {
    if (lang == null || lang.isEmpty()) return null;
    if (lang.length() == 2) return lang;

    // workarounds
    if (lang.equals("fre")) return "fr";
    if (lang.equals("dut")) return "nl";
    if (lang.equals("ger")) return "de";
    if (lang.equals("amr")) return "hy";
    if (lang.equals("arm")) return "hy";
    if (lang.equals("gre")) return "el";
    if (lang.equals("chi")) return "zh";
    if (lang.equals("rum")) return "ro";
    if (lang.equals("baq")) return "eus";
    if (lang.equals("tib")) return "bo";
    if (lang.equals("cze")) return "cs";
    if (lang.equals("mac")) return "mk";
    if (lang.equals("geo")) return "ka";
    if (lang.equals("slo")) return "sk";
    if (lang.equals("ice")) return "is";

    if (localeMap == null) { //init
      String[] languages = Locale.getISOLanguages();
      localeMap = new HashMap<>(languages.length);
      for (String language : languages) {
        Locale locale = new Locale(language);
        localeMap.put(locale.getISO3Language(), locale);
      }
    }
    Locale l = localeMap.get(lang);
    if (l == null) return lang;
    return l.toLanguageTag();
  }

  public static String toLangDefaultScript(String lang) {
    if (lang == null || lang.isEmpty()) return null;

    return LocaleUtility.getScript(new Locale(lang));
  }

  public static String intermarcToLangScript(String code) {
    if (code == null || code.isEmpty() || code.equals(".")) return null;
    if (intermarcScriptMap == null) { // init
      try {
        intermarcScriptMap = csvToMap("/intermarc2LangScript.csv", 0, 1);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    String script = intermarcScriptMap.get(code);
    return (script == null || script.isEmpty() || script.equals("Zzzz")) ? null : script;
  }

  public static String itermarc2mop(String intermarcInput) {
    if (intermarcMopMap == null) {
      try {
        intermarcMopMap = csvToMap("/intermarc2iaml.csv", 3, 0);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return intermarcMopMap.get(intermarcInput);
  }


  private static Map<String, String> csvToMap(String csvPath, int fieldKey, int fieldValue) throws IOException {
    String csvFile = Utils.class.getClass().getResource(csvPath).getFile();
    Map<String, String> map = new HashMap<>();


    BufferedReader br = new BufferedReader(new FileReader(csvFile));

    String line = br.readLine();
    boolean firstLine = true;

    while (line != null) {
      if (firstLine) firstLine = false; // header
      else {
        String str[] = line.split(";");

        String value = str[fieldValue];
        String key = str[fieldKey];

        if (!value.isEmpty()) map.put(key, value);
      }
      line = br.readLine();
    }

    return map;
  }

  public static String intermarcExtractLang(String data) {
    if (data == null || data.length() < 6) return null;

    String lang = data.substring(6)
      .replaceAll("[.\\d]", "").trim();
    lang = Utils.toISO2Lang(lang);

    String script = Utils.intermarcToLangScript(data.substring(4, 5));
    if (script == null) return lang;

    if (lang != null && !script.equals(Utils.toLangDefaultScript(lang))) {
      // it is a transliteration: add the script
      lang += "-" + script;
    } else if (lang == null && !script.equals("Latn"))
      lang = "und-" + script;

    return lang;
  }
}

