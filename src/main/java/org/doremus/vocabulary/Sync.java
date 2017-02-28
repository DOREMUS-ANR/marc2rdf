package org.doremus.vocabulary;

import org.apache.commons.io.FileUtils;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class Sync {
  private static Properties properties;

  public static void main(String args[]) throws IOException {
    System.out.println("Vocabulary sync started");

    loadProperties();

    String vocabularyRoot = properties.getProperty("vocabularyRoot");
    String token = properties.getProperty("githubToken");

    //get repo from GitHub
    RepositoryService service = new RepositoryService();
    if (token != null && !token.isEmpty()) service.getClient().setOAuth2Token(token);

    Repository repo = service.getRepository("DOREMUS-ANR", "knowledge-base");

    // empty directory
    String vocabularyDir = Sync.class.getClassLoader().getResource("vocabulary").getPath().replace("/build/resources/main/", "/src/main/resources/");
    System.out.println("Destination folder: " + vocabularyDir);
    FileUtils.deleteDirectory(new File(vocabularyDir));

    // load new contents
    ContentsService contentsService = new ContentsService();
    for (RepositoryContents content : contentsService.getContents(repo, "/vocabularies/")) {
      if (!content.getName().endsWith(".ttl")) continue;

      String vName = content.getName();
      String url = vocabularyRoot + vName;

      File target = new File(vocabularyDir + "/" + vName);
      System.out.println(target.getPath());
      FileUtils.copyURLToFile(new URL(url), target);
    }
  }

  private static void loadProperties() {
    properties = new Properties();
    String filename = "config.properties";

    try {
      InputStream input = new FileInputStream(filename);
      properties.load(input);
      input.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

  }

}
