package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.doremus.marc2rdf.bnfconverter.BNF2RDF;
import org.doremus.marc2rdf.marcparser.MarcXmlHandler;
import org.doremus.marc2rdf.marcparser.MarcXmlReader;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.ppconverter.PP2RDF;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class Converter {
  private static boolean marcOut;

  private static ArrayList<Vocabulary> vocabularies;
  public static Vocabulary genreVocabulary;

  public static Properties properties;
  private static List<String> notSignificativeTitleList = null;

  private enum INSTITUTION {
    PHILARMONIE,
    BNF
  }

  public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
    marcOut = Arrays.asList(args).indexOf("marc") > -1;

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
      String fich = file.getAbsolutePath();
      Model m;

      INSTITUTION institution;
      MarcXmlHandler.MarcXmlHandlerBuilder handlerBuilder;

      switch (file.getName().length()) {
        case 12:
          institution = INSTITUTION.BNF;
          handlerBuilder = BNF2RDF.bnfXmlHandlerBuilder;
          break;
        case 11:
          institution = INSTITUTION.PHILARMONIE;
          handlerBuilder = PP2RDF.ppXmlHandlerBuilder;
          break;
        default:
          System.out.println("Skipping not recognized file: " + file.getName());
          continue;
      }

      if (marcOut) {
        marcExport(fich, handlerBuilder);
        continue;
      }

      if (institution == INSTITUTION.BNF) { // Notice BNF
        m = BNF2RDF.convert(fich);
      } else {  // Notice PP
        m = PP2RDF.convert(fich);
      }

      for (Vocabulary v : vocabularies) v.buildReferenceIn(m);

      // Write the output file
      File fileName;
      if (outputFolderPath != null && !outputFolderPath.isEmpty()) {
        // default folder specified, write there
        fileName = Paths.get(outputFolderPath, file.getName().replaceFirst(".xml", ".ttl")).toFile();
      } else {
        // write in the same folder, in the "RDF" subfolder
        fileName = Paths.get(file.getParentFile().getAbsolutePath(), "RDF", file.getName().replaceFirst(".xml", ".ttl")).toFile();
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

  private static void marcExport(String file, MarcXmlHandler.MarcXmlHandlerBuilder handler) {
    String outputFolderPath = properties.getProperty("defaultOutput");

    try {
      MarcXmlReader reader = new MarcXmlReader(new FileInputStream(file), handler);
      StringBuilder marc = new StringBuilder();
      for (Record r : reader.getRecords()) {
        marc.append(r);
        marc.append("\n");
      }

      // Write the output file
      File inputFile = new File(file);
      File fileName;
      if (outputFolderPath != null && !outputFolderPath.isEmpty()) {
        // default folder specified, write there
        fileName = Paths.get(outputFolderPath, inputFile.getName().replaceFirst(".xml", ".marc")).toFile();
      } else {
        // write in the same folder, in the "RDF" subfolder
        fileName = Paths.get(inputFile.getParentFile().getAbsolutePath(), "RDF", inputFile.getName().replaceFirst(".xml", ".marc")).toFile();
      }
      //noinspection ResultOfMethodCallIgnored
      fileName.getParentFile().mkdirs();
      FileWriter out = new FileWriter(fileName);

      //System.out.println(marc);
      out.write(marc.toString());
      out.close();

    } catch (Exception e) {
      e.printStackTrace();
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

  public static boolean isNotSignificativeTitle(String title) {
    if (notSignificativeTitleList == null) {
      // Load it!
      String fileName = Converter.class.getClass().getResource(properties.getProperty("BNFGenres")).getFile();
      File fichier = new File(fileName);
      try {
        notSignificativeTitleList = new ArrayList<>();

        FileInputStream fis = new FileInputStream(fichier);

        XSSFWorkbook myWorkBook = new XSSFWorkbook(fis); // Trouver l'instance workbook du fichier XLSX
        XSSFSheet mySheet = myWorkBook.getSheetAt(0); // Retourne la 1ere feuille du workbook XLSX

        for (Row row : mySheet) { // Traverser chaque ligne du fichier XLSX
          Iterator<Cell> cellIterator = row.cellIterator();
          while (cellIterator.hasNext()) { // Pour chaque ligne, iterer chaque colonne
            Cell cell = cellIterator.next();
            notSignificativeTitleList.add(cell.getStringCellValue());
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
        System.err.println("I cannot load not significative title table: " + fileName);
        return false;
      }
    }

    return notSignificativeTitleList.contains(title);
  }


}

