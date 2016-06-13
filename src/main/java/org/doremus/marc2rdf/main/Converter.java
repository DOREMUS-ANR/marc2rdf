package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.doremus.marc2rdf.bnfconverter.BNF2RDF;
import org.doremus.marc2rdf.ppconverter.PP2RDF;
import org.doremus.marc2rdf.ppparser.MarcXmlReader;
import org.doremus.marc2rdf.ppparser.Record;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

public class Converter {

  private static ArrayList<Vocabulary> vocabularies;
  public static Vocabulary genreVocabulary;

  public static Properties properties;
  private static String fich = "";

  public static String getFile() {
    return fich;
  }

  public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
    String inputFolderPath;

    loadProperties();
    System.out.println("Running with the following properties: " + properties);

    try {
      loadVocabularies();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Cannot load vocabularies. Continuing without accessing to them.");
    }

    inputFolderPath = properties.getProperty("defaultInput");

    if (inputFolderPath == null || inputFolderPath.isEmpty())
      inputFolderPath = getDirFromFileChooser();

    /***********
     * Copier/Coller tous les fichiers dans un seul
     **************/
    listeRepertoire(new File(inputFolderPath));
  }

  private static String getDirFromFileChooser() throws InterruptedException {
    /* Selectionnez le dossier contenant les fichiers a convertir */
    FileChooser folder = new FileChooser();

    JFrame frame = new JFrame("MARC to RDF");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    // Add content to the window.
    frame.add(folder);
    // Display the window
    frame.pack();
    frame.setVisible(true);

    synchronized (folder) {
      while (!folder.getChoosed()) {
        folder.wait();
      }
    }

    String dirName = folder.getDir();

    if (dirName.isEmpty())
      folder.log.setText("No folder specified");
    else
      folder.log.setText("Conversion started");

    return dirName;
  }

  /*************************************************************************************************/
  private static void listeRepertoire(File repertoire) throws URISyntaxException, IOException {
    String outputFolderPath = properties.getProperty("defaultOutput");
    File folderTUMs = new File(properties.getProperty("TUMFolder"));


    if (!repertoire.isDirectory()) {
      System.out.println("Input directory not specified or not existing");
      return;
    }

    File[] list = repertoire.listFiles();
    if (list == null) {
      System.out.println("Input directory is empty");
      return;
    }

    for (File file : list) {
      if (!file.isFile() || !file.getName().endsWith(".xml")) continue;
      fich = file.getAbsolutePath();
      Model m = ModelFactory.createDefaultModel();

      if (file.getName().length() == 12) {
        // Notice BNF
        m = BNF2RDF.convert(file.getAbsolutePath());
      } else if (file.getName().length() == 11) {
        // Notice PP

        /******
         * Verifier si c'est une notice d'oeuvre ou un TUM
         **********/
        InputStream fileStream = new FileInputStream(file);
        // Charger le fichier MARCXML a parser
        MarcXmlReader reader = new MarcXmlReader(fileStream);
        String typeNotice = null;
        String idTUM = null;

        while (reader.hasNext()) {
          // Parcourir le fichier MARCXML
          Record s = reader.next();
          typeNotice = s.getType();
          if (typeNotice.equals("UNI:100")) {
            // Si c'est une notice d'oeuvre
            for (int i = 0; i < s.dataFields.size(); i++) {
              if (s.dataFields.get(i).getEtiq().equals("500")) {
                if (s.dataFields.get(i).isCode('3'))
                  idTUM = s.dataFields.get(i).getSubfield('3').getData();
              }
            }
          }
        }

        if ("AIC:14".equals(typeNotice)) {
          // Si c'est un TUM
          m = PP2RDF.convert(file.getAbsolutePath());
        } else if ("UNI:100".equals(typeNotice)) {
          // Si c'est une notice d'oeuvre

          m = PP2RDF.convert(file.getAbsolutePath());
          // Convertir la notice d'oeuvre
          File tum = getTUM(folderTUMs, idTUM);
          fich = tum.getAbsolutePath();
          m.add(PP2RDF.convert(tum.getAbsolutePath()));
          // Convertir le TUM correspondant
        } else {
          // TODO other notice types?
          System.out.println("Skipping not recognized PP notice type " + typeNotice + " for file " + file.getName());
          continue;
        }
      } else {
        System.out.println("Skipping not recognized file: " + file.getName());
        continue;
      }

      for (Vocabulary v : vocabularies) v.buildReferenceIn(m);

      // Write the output file
      File fileName;
      if (outputFolderPath != null && !outputFolderPath.isEmpty()) {
        // default folder specified, write there
        fileName = Paths.get(outputFolderPath, file.getName().replaceFirst(".xml", ".ttl")).toFile();
      } else {
        // write in the same folder, in the "RDF" subfolder
        fileName = Paths.get(file.getParentFile().getAbsolutePath(), "RDF", file.getName().replaceFirst(".ttl", ".xml")).toFile();
      }
      //noinspection ResultOfMethodCallIgnored
      fileName.getParentFile().mkdirs();
      FileWriter out = new FileWriter(fileName);

     // m.write(System.out, "TURTLE");
      m.write(out, "TURTLE");
      out.close();

    }
    for (File subList : list) {
      if (subList.isDirectory())
        listeRepertoire(subList);
    }
  }

  private static void loadVocabularies() throws IOException {
    final String vocabularyRoot = properties.getProperty("vocabularyRoot");
    vocabularies = new ArrayList<>();

    String token = properties.getProperty("githubToken");

    //get repo from GitHub
    RepositoryService service = new RepositoryService();
    if (token != null && !token.isEmpty())
      service.getClient().setOAuth2Token(token);

    Repository repo = service.getRepository("DOREMUS-ANR", "knowledge-base");

    ContentsService contentsService = new ContentsService();
    for (RepositoryContents contents : contentsService.getContents(repo, "/vocabularies/")) {
      String url = vocabularyRoot + contents.getName();
      Vocabulary vocabulary = new Vocabulary(url);
      vocabularies.add(vocabulary);
      System.out.println("Loaded vocabulary at " + url);
      if (contents.getName().equals("genre.ttl")) {
        genreVocabulary = vocabulary;
      }
    }
  }


  private static File getTUM(final File folder, String idTUM) {
    for (File fileEntry : folder.listFiles()) {
      // System.out.println(fileEntry.getName());
      if (fileEntry.isDirectory()) {
        // FIXME the following result get lost
        return getTUM(fileEntry, idTUM);
      } else if (fileEntry.getName().equals(idTUM + ".xml")) {
        return fileEntry;
      }

    }
    return folder;
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

