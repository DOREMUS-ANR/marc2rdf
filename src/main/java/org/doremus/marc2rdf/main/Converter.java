package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.doremus.marc2rdf.bnfconverter.BNF2RDF;
import org.doremus.marc2rdf.marcparser.MarcXmlHandler;
import org.doremus.marc2rdf.marcparser.MarcXmlReader;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.marc2rdf.ppconverter.PP2RDF;
import org.doremus.ontology.*;
import org.doremus.string2vocabulary.VocabularyManager;

import javax.swing.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Properties;

public class Converter {
  public final static String SCHEMA = "http://schema.org/";
  private static boolean marcOut;

  public static Properties properties;
  private static int maxFilesInFolder, filesInCurrentFolder, currentFolder;
  private static Model general;
  private static boolean oneFile;
  private static boolean splitFiles;
  private static String outputFolderPath;


  public static void main(String[] args) throws IOException, InterruptedException {
    marcOut = Arrays.asList(args).indexOf("marc") > -1;

    loadProperties();
    System.out.println("Running with the following properties: " + properties);

    GeoNames.init(properties);


    ClassLoader classLoader = Converter.class.getClassLoader();
    URL vocabularyFolder = classLoader.getResource("vocabulary");
    URL p2fTable = classLoader.getResource("property2family.csv");
    assert p2fTable != null && vocabularyFolder != null;
    VocabularyManager.setVocabularyFolder(vocabularyFolder.getPath());
    VocabularyManager.init(p2fTable);

    splitFiles = Boolean.parseBoolean(properties.getProperty("splitFiles", "false"));

    String inputFolderPath = properties.getProperty("defaultInput");
    outputFolderPath = properties.getProperty("defaultOutput");

    maxFilesInFolder = Integer.parseInt(properties.getProperty("maxFilesInAFolder"));
    filesInCurrentFolder = 0;
    currentFolder = 0;

    if (inputFolderPath == null || inputFolderPath.isEmpty())
      inputFolderPath = getDirFromFileChooser();

    oneFile = Boolean.parseBoolean(properties.getProperty("oneFile", "false"));
    general = ModelFactory.createDefaultModel();
    convertDirectory(new File(inputFolderPath));
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

  private static void convertDirectory(File directory) throws IOException {

    if (!directory.isDirectory()) {
      System.out.println("Input directory not specified or not existing");
      return;
    }

    File[] list = directory.listFiles();
    if (list == null) {
      System.out.println("Input directory is empty");
      return;
    }

//    int i = 100;
    for (File file : list) {
//      if(i < 0) break;
//      --i;

      if (file.isDirectory()) {
        convertDirectory(file);
        continue;
      }

      if (!file.isFile() || !file.getName().endsWith(".xml"))
        continue;

      String fich = file.getAbsolutePath();
      Model m;

      AbstractConverter conv;
      MarcXmlHandler.MarcXmlHandlerBuilder handlerBuilder;

      switch (file.getName().length()) {
        case 12:
          handlerBuilder = BNF2RDF.bnfXmlHandlerBuilder;
          conv = new BNF2RDF();
          break;
        case 11:
          handlerBuilder = PP2RDF.ppXmlHandlerBuilder;
          conv = new PP2RDF();
          break;
        default:
          if (splitFiles && file.getName().startsWith("Export")) {
            // ALOES
            try {
              fileToFolder(file);
            } catch (XMLStreamException | TransformerException e) {
              e.printStackTrace();
            }
          } else System.out.println("Skipping not recognized file: " + file.getName());
          continue;
      }

      if (marcOut) {
        marcExport(fich, handlerBuilder);
        continue;
      }

      m = conv.convert(file);
      if (m == null) continue;

      m.setNsPrefix("mus", MUS.getURI());
      m.setNsPrefix("ecrm", CIDOC.getURI());
      m.setNsPrefix("efrbroo", FRBROO.getURI());
      m.setNsPrefix("xsd", XSD.getURI());
      m.setNsPrefix("dcterms", DCTerms.getURI());
      m.setNsPrefix("foaf", FOAF.getURI());
      m.setNsPrefix("rdfs", RDFS.getURI());
      m.setNsPrefix("prov", PROV.getURI());
      m.setNsPrefix("owl", OWL.getURI());
      m.setNsPrefix("time", Time.getURI());
      m.setNsPrefix("schema", SCHEMA);

      VocabularyManager.string2uri(m);

      // Write the output file
      // write a unique file
      if (oneFile) {
        general.add(m);
        continue;
      }
      // write file by file
      String outFolder;
      if (outputFolderPath != null && !outputFolderPath.isEmpty()) {
        outFolder = nextFolder();
      } else {// write in the same folder, in the "RDF" subfolder
        String parentFolder = file.getParentFile().getAbsolutePath();
        outFolder = Paths.get(parentFolder, "RDF").toString();
      }
      writeModelToFile(m, outFolder, file.getName());
    }

    if (oneFile)
      writeModelToFile(general, outputFolderPath, directory.getName() + ".ttl");
  }

  private static String nextFolder() {
    // default folder specified, write there
    if (filesInCurrentFolder > maxFilesInFolder) {
      ++currentFolder;
      filesInCurrentFolder = 0;
    }
    ++filesInCurrentFolder;
    return Paths.get(outputFolderPath, String.valueOf(currentFolder)).toString();
  }

  private static void writeModelToFile(Model m, String folder, String name) throws IOException {
    name = name.replaceFirst(".xml", ".ttl");

    File outputFile = Paths.get(folder, name).toFile();
    //noinspection ResultOfMethodCallIgnored
    outputFile.getParentFile().mkdirs();
    FileWriter out = new FileWriter(outputFile);

    // m.write(System.out, "TURTLE");
    m.write(out, "TURTLE");
    out.close();
  }

  private static void fileToFolder(File f) throws XMLStreamException, IOException, TransformerException {
    // Separate the long files in a folder with a file for each record
    if (!f.getName().endsWith(".xml")) return;
    removeUTF8BOM(f);
    System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

    String fileName = f.getName()
      .replaceFirst("\\.xml", "")
      .replaceFirst("^Export", "");
    System.out.println("Splitting file: " + fileName);

    String dataFolderPath = f.getParent();
    new File(Paths.get(dataFolderPath, fileName).toString()).mkdirs();

    XMLInputFactory xif = XMLInputFactory.newInstance();
    XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(f));
    xsr.next(); // Skip Doctype
    xsr.nextTag(); // Advance to statements element

    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer();
    QName recordName = QName.valueOf("NOTICE");

    boolean transformed = false;
    while (xsr.hasNext()) {
      if (!transformed) xsr.next();
      else transformed = false;
      if (!xsr.isStartElement() || !xsr.getName().equals(recordName)) continue;

      StringWriter stringWriter = new StringWriter();
      String id = xsr.getAttributeValue(null, "id");
      t.transform(new StAXSource(xsr), new StreamResult(stringWriter));
      transformed = true;
      String recordString = stringWriter.toString();
      Files.write(Paths.get(dataFolderPath, fileName, id + ".xml"), recordString.getBytes(),
        StandardOpenOption.CREATE);
    }
    xsr.close();
  }

  private static final String UTF8_BOM = "\uFEFF";

  private static void removeUTF8BOM(File f) throws IOException {
    // remove UTF8 BOM
    // https://stackoverflow.com/questions/4569123/content-is-not-allowed-in-prolog-saxparserexception
    boolean firstLine = true;
    FileInputStream fis = new FileInputStream(f);
    BufferedReader r = new BufferedReader(new InputStreamReader(fis, "UTF8"));

    StringBuilder sb = new StringBuilder();
    for (String s; (s = r.readLine()) != null; ) {
      if (firstLine) {
        if (s.startsWith(UTF8_BOM)) s = s.substring(1);
        firstLine = false;
      } else sb.append("\n");
      sb.append(s);
    }
    r.close();
    Files.delete(f.toPath());

    String output = sb.toString().replaceAll("&#(31|28|29);", " ")
      .replaceAll("&#30;", "f");

    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF8"));
    w.write(output);
    w.close();
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

