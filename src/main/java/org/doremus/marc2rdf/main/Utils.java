package org.doremus.marc2rdf.main;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.util.ResourceUtils;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Utils {

  private static Map<String, Locale> localeMap;
  private static Map<String, String> intermarcScriptMap;
  private static Map<String, String> intermarcMopMap;

  public static final String opusHeaderRegex = "(?i)^(?:op(?:\\.|us| )|Oeuvre|WoO|Werk nr\\.?) ?" +
    "(?:post(?:hume|h?\\.|h))?";
  public static final String opusSubnumberRegex = "(?i)(?:,? n(?:[o°.]| °)[s.]?)";

  public final static String DURATION_REGEX = "(?:(\\d{1,2}) ?h)? ?(?:(\\d{1,2}) ?mi?[nm])? ??(\\d{1,2})?(?: s(?:ec)?)?$";
  private final static Pattern DURATION_PATTERN = Pattern.compile(DURATION_REGEX);
  private final static String[] DURATION_UNITS = new String[]{null, "H", "M", "S"};

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
    if (str.matches("\\d+"))
      return ResourceFactory.createTypedLiteral(Integer.parseInt(str));
    else return ResourceFactory.createTypedLiteral(str);
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

  public static Map<String, String> csvToMap(String csvPath, int fieldKey, int fieldValue) throws IOException {
    return csvToMap(csvPath, fieldKey, fieldValue, ";");
  }

  public static Map<String, String> csvToMap(String csvPath, int fieldKey, int fieldValue, String separator) throws
    IOException {
    String csvFile = Utils.class.getClass().getResource(csvPath).getFile();
    Map<String, String> map = new HashMap<>();


    BufferedReader br = new BufferedReader(new FileReader(csvFile));

    String line = br.readLine();
    boolean firstLine = true;

    while (line != null) {
      if (firstLine) firstLine = false; // header
      else if (!line.isEmpty()) {
        String str[] = line.split(separator);

        String value = str[fieldValue];
        String key = str[fieldKey];

        if (!value.isEmpty()) map.put(key, value);
      }
      line = br.readLine();
    }

    return map;
  }

  public static boolean areQuotesBalanced(String[] parts) {
    return Arrays.stream(parts)
      .allMatch(Utils::areQuotesBalanced);
  }

  public static boolean areQuotesBalanced(String p) {
    return (StringUtils.countMatches(p, "\"") % 2) == 0 &&
        (StringUtils.countMatches(p, "(") % 2) == (StringUtils.countMatches(p, ")") % 2);
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

  public static void catalogToUri(Model model, List<String> composers) {
    List<Statement> statementsToRemove = new ArrayList<>(),
      statementsToAdd = new ArrayList<>();
    Map<Resource, String> renamingMap = new HashMap<>();

    Property MODS_IDENTIFIER = model.createProperty("http://www.loc.gov/standards/mods/rdf/v1/#identifier");

    StmtIterator iter = model.listStatements(new SimpleSelector(null, MUS.U40_has_catalogue_name, (RDFNode) null));

    while (iter.hasNext()) {
      Statement s = iter.nextStatement();
      String catalogueName = s.getObject().toString();
      Resource match = VocabularyManager.getMODS("catalogue").findModsResource(catalogueName, composers);
      if (match == null) continue;

      Resource subj = s.getSubject();
      statementsToRemove.add(s);
      statementsToAdd.add(new StatementImpl(subj, s.getPredicate(), match));

      String newCatalogName = match.getProperty(MODS_IDENTIFIER).getObject().toString();

      if (!newCatalogName.equals(catalogueName))
        renamingMap.put(subj, subj.toString().replace(catalogueName, newCatalogName));
    }
    model.remove(statementsToRemove);
    model.add(statementsToAdd);

    for (Map.Entry<Resource, String> entry : renamingMap.entrySet())
      ResourceUtils.renameResource(entry.getKey(), entry.getValue());
  }


  public static String duration2iso(String f) {
    f = f.trim();
    Matcher m = DURATION_PATTERN.matcher(f);
    if (!m.find()) return null;

    StringBuilder duration = new StringBuilder("PT");

    for (int i = 1; i < DURATION_UNITS.length; i++)
      if (m.group(i) != null)
        duration.append(m.group(i)).append(DURATION_UNITS[i]);

    return duration.toString();
  }


  public static QuerySolution queryDoremus(ParameterizedSparqlString sparql) {
    QueryExecution qexec = QueryExecutionFactory.sparqlService("http://data.doremus.org/sparql", sparql.toString());
    ResultSet r = qexec.execSelect();
    if (!r.hasNext()) return null;
    return r.next();

  }

  public static RDFNode queryDoremus(ParameterizedSparqlString sparql, String var) {
    QuerySolution result = queryDoremus(sparql);
    if (result == null) return null;
    else return result.get(var);
  }


  public static List<String> splitKeepBrackets(String text, String delimiter) {
    List<String> p = new LinkedList<>(Arrays.asList(text.split(delimiter)));
    while (p.stream().anyMatch(Utils::areBracketsUnbalancedIn)) {
      String current, previous = p.get(0);
      for (int i = 1; i < p.size(); i++) {
        current = p.get(i);
        if (areBracketsUnbalancedIn(previous)) {
          String combo = previous + ", " + current;
          p.set(i - 1, combo);
          p.remove(i);
          break;
        } else previous = current;
      }
    }
    return p;
  }

  private static boolean areBracketsUnbalancedIn(String str) {
    return StringUtils.countMatches(str, "(") != StringUtils.countMatches(str, ")");
  }

  public static String[] extractBrackets(String txt) {
    if (!txt.contains("(") || !txt.contains(")"))
      return new String[]{txt, null};

    int start = txt.indexOf("("), end = txt.indexOf(")");
    String content = txt.substring(start + 1, end);
    txt = txt.substring(0, start) + txt.substring(end);
    return new String[]{txt.trim(), content.trim()};
  }
}

