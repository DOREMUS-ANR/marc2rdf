package org.doremus.marc2rdf.main;

import org.doremus.isnimatcher.ISNI;
import org.doremus.isnimatcher.ISNIRecord;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ISNIWrapper {
  private static HashMap<String, String> cache; // fullName -> isni
  private static String folder = null;

  public static ISNIRecord get(String isni) {
    if (folder == null) {
      folder = Converter.properties.getProperty("ISNIFolder");
      loadCache();
    }

    Path p = Paths.get(folder, isni + ".xml");
    if (p.toFile().exists()) {
      try {
        return ISNIRecord.fromFile(p);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (JAXBException e) {
        e.printStackTrace();
      }
    } else {
      try {
        ISNIRecord r = ISNI.get(isni);
        r.save(p);
        return r;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public static ISNIRecord search(String fullName, String date) {

    String k = fullName;
    if (date != null) {
      if (date.length() > 4)
        date = date.substring(0, 4);
      k += "|" + date;
    }
    if (cache.containsKey(k))
      return get(cache.get(k));

    try {
      ISNIRecord r = ISNI.search(fullName, date);
      r.save(Paths.get(folder, r.id + ".xml"));
      return r;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static void saveCache() {
    Properties properties = new Properties();

    for (Map.Entry entry : cache.entrySet()) {
      properties.put(entry.getKey(), entry.getValue() + "");
    }

    try {
      properties.store(new FileOutputStream("isni.properties"), null);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  static void loadCache() {
    cache = new HashMap<>();
    try {
      FileInputStream fis = new FileInputStream("isni.properties");
      Properties properties = new Properties();
      properties.load(fis);

      for (String key : properties.stringPropertyNames())
        cache.put(key, properties.get(key).toString());
    } catch (IOException e) {
      System.out.println("No 'isni.properties' file found. I will create it.");
    }

  }

}
