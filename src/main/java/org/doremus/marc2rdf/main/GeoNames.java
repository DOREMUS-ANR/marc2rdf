package org.doremus.marc2rdf.main;

import org.geonames.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GeoNames {
  public static final String NAME = "http://www.geonames.org/ontology#name";

  static Map<String, Integer> cache; // cache String -> idGeoNames
  public static String destFolder;
  private static Map<String, String> countries;

  public static void setDestFolder(String folder) {
    destFolder = folder;
  }

  public static void setUser(String user) {
    WebService.setUserName(user); // add your username here
  }

  public static String toURI(int id) {
    return "http://sws.geonames.org/" + id + "/";
  }

  public static void downloadRdf(int id) {
    String uri = toURI(id) + "about.rdf";
    String outPath = Paths.get(destFolder, id + ".rdf").toString();
    if (new File(outPath).exists()) return; // it is already there!
    try {
      URL website = new URL(uri);
      ReadableByteChannel rbc = Channels.newChannel(website.openStream());
      FileOutputStream fos = new FileOutputStream(outPath);
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public static Toponym query(String rawString) {
    return query(rawString, null);
  }

  public static Toponym query(String rawString, String country) {
    Toponym tp = null;

    if (cache.containsKey(rawString)) {
      int k = cache.get(rawString);
      if (k != -1) {
        tp = new Toponym();
        tp.setGeoNameId(k);
      }
      return tp;
    }

    ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();

    if (country == null || country.isEmpty())
      searchCriteria.setQ(rawString);
    else searchCriteria.setQ(rawString + " (" + country + ")");

    searchCriteria.setMaxRows(1);
    searchCriteria.setLanguage("fr");
    searchCriteria.setFeatureClass(FeatureClass.P);

    try {
      ToponymSearchResult searchResult = WebService.search(searchCriteria);
      if (searchResult.getToponyms().size() > 0) {
        tp = searchResult.getToponyms().get(0);
        downloadRdf(tp.getGeoNameId());
        String countryCode = tp.getCountryCode();
        if (!cache.containsKey(countryCode)) {
          //  save the country also
          int countryId = Integer.parseInt(countries.get(countryCode));
          downloadRdf(countryId);
          addToCache(countryCode, countryId);
        }
      }

      addToCache(rawString, tp != null ? tp.getGeoNameId() : -1);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tp;
  }

  // Use cache for passing from ITEMA3 id to GeoNames id
  public static int get(String id) {
    return cache.getOrDefault(id, -1);
  }

  static void loadCache() {
    cache = new HashMap<>();
    try {
      FileInputStream fis = new FileInputStream("places.properties");
      Properties properties = new Properties();
      properties.load(fis);

      for (String key : properties.stringPropertyNames()) {
        cache.put(key, Integer.parseInt(properties.get(key).toString()));
      }
    } catch (IOException e) {
      System.out.println("No 'places.properties' file found. I will create it.");
    }

  }

  private static void addToCache(String key, int value) {
    cache.put(key, value);
    saveCache();
  }

  static void removeFromCache(String key) {
    cache.remove(key);
    saveCache();
  }

  private static void saveCache() {
    Properties properties = new Properties();

    for (Map.Entry<String, Integer> entry : cache.entrySet()) {
      properties.put(entry.getKey(), entry.getValue() + "");
    }

    try {
      properties.store(new FileOutputStream("places.properties"), null);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static void init(Properties properties) {
    loadCache();
    setUser(properties.getProperty("geonames_user"));

    try {
      countries = Utils.csvToMap("/countryInfoCSV.tsv", 0, 11, "\t");
    } catch (IOException e) {
      e.printStackTrace();
    }

    String outputFolderPath = properties.getProperty("defaultOutput");
    String geonamesFolder = Paths.get(outputFolderPath, "place", "geonames").toString();
    new File(geonamesFolder).mkdirs();
    setDestFolder(geonamesFolder);
  }
}
