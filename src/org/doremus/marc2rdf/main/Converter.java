package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.doremus.marc2rdf.bnfconverter.BNF2RDF;
import org.doremus.marc2rdf.ppconverter.PP2RDF;
import org.doremus.marc2rdf.ppparser.MarcXmlReader;
import org.doremus.marc2rdf.ppparser.Record;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class Converter {

  private static final String[] vocabularyNames = new String[]{"key", "mode", "derivation", "iaml", "rameau"};
  private static ArrayList<Model> vocabularies;

  public static Properties properties;
  private static String fich = "";

  public static String getFile() {
    return fich;
  }

  public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
    String inputFolderPath;

    loadProperties();
    System.out.println("Running with the following properties: " + properties);

    loadVocabularies();

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
          final File folderTUMs = new File(properties.getProperty("TUMFolder"));
          File tum = getTUM(folderTUMs, idTUM);
          fich = tum.getAbsolutePath();
          m.add(PP2RDF.convert(tum.getAbsolutePath()));
          // Convertir le TUM correspondant
        }
      }

      for (Model v : vocabularies) link2Vocabulary(m, v);

      // Write the output file
      File fileName = Paths.get(file.getParentFile().getAbsolutePath(), "RDF", file.getName() + ".ttl").toFile();
      //noinspection ResultOfMethodCallIgnored
      fileName.getParentFile().mkdirs();
      FileWriter out = new FileWriter(fileName);

      m.write(System.out, "TURTLE");
      m.write(out, "TURTLE");
      out.close();

    }
    for (File subList : list) {
      if (subList.isDirectory())
        listeRepertoire(subList);
    }
  }

  private static void loadVocabularies() {
    final String vocabularyRoot = properties.getProperty("vocabularyRoot");

    vocabularies = new ArrayList<>();

    for (String name : vocabularyNames) {
      String url = vocabularyRoot + name + ".ttl";
      Model vocabulary = ModelFactory.createDefaultModel();
      vocabulary.read(url, "TURTLE");
      System.out.println("Loaded vocabulary at " + url);
      vocabularies.add(vocabulary);
    }
  }


  private static void link2Vocabulary(Model model, Model vocabulary) {
    // Build a map
    HashMap<String, Resource> substitutionMap = new HashMap<>();

    // for each concept
    StmtIterator conceptIter =
      vocabulary.listStatements(new SimpleSelector(null, RDF.type, vocabulary.getResource(SKOS.Concept.toString())));

    if (!conceptIter.hasNext()) {
      System.out.println("No concepts in the reference rdf");
      return;
    }

    while (conceptIter.hasNext()) {
      Resource resource = conceptIter.nextStatement().getSubject();

      // get the labels
      NodeIterator labelIterator = vocabulary.listObjectsOfProperty(resource, SKOS.prefLabel);
      //for each label
      while (labelIterator.hasNext()) {
        String value = labelIterator.next().toString();
        //  add it to the map
        substitutionMap.put(value, resource);
      }
    }

    for (Map.Entry<String, Resource> entry : substitutionMap.entrySet()) {
      String key = entry.getKey();
      Resource value = entry.getValue();

      StmtIterator iter = model.listStatements(new SimpleSelector(null, null, key));
      List<Statement> statementsToRemove = new ArrayList<>();
      while (iter.hasNext()) {
        Statement s = iter.nextStatement();

        // System.out.println("FOUND " + key + " --> " + value);
        if (s.getPredicate().toString().equals("http://www.cidoc-crm.org/cidoc-crm/P1_is_identified_by")) {
          // replace the whole node
          StmtIterator parentIter = model.listStatements(new SimpleSelector(null, null, s.getSubject()));
          int howManyIteration = 0;
          while (parentIter.hasNext()) {
            Statement ps = parentIter.nextStatement();

            model.add(ps.getSubject(), ps.getPredicate(), value);
            statementsToRemove.add(ps);

            howManyIteration++;
          }
          if (howManyIteration != 1)
            System.out.println("Converter.link2Vocabulary | Exactly one iteration expected. Please check.");
        } else {
          // replace only the literal
          model.add(s.getSubject(), s.getPredicate(), value);
        }

        statementsToRemove.add(s);
      }
      model.remove(statementsToRemove);
    }
  }


  private static File getTUM(final File folder, String idTUM) {
    for (final File fileEntry : folder.listFiles()) {
      if (fileEntry.isDirectory()) {
        getTUM(fileEntry, idTUM);
      } else {
        if (fileEntry.getName().equals(idTUM + ".xml")) {
          return fileEntry;
        }
      }
    }
    return folder;
  }

  private static void loadProperties() {
    properties = new Properties();
    InputStream input = null;

    try {
      String filename = "config.properties";
      input = new FileInputStream(filename);
      properties.load(input);
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

  }
}
