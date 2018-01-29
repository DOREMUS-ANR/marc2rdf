package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateAction;
import org.doremus.marc2rdf.marcparser.MarcXmlHandler;
import org.doremus.marc2rdf.marcparser.MarcXmlReader;
import org.doremus.marc2rdf.marcparser.Record;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.doremus.marc2rdf.main.Converter.properties;

public class PP2RDF {
  public static final MarcXmlHandler.MarcXmlHandlerBuilder ppXmlHandlerBuilder =
    new MarcXmlHandler.MarcXmlHandlerBuilder()
      .recordLabel("NOTICE")
      .datafieldLabel("champs")
      .subfieldLabel("SOUSCHAMP")
      .controlfieldLabel(false)
      .tagLabel("UnimarcTag")
      .codeLabel("UnimarcSubfield")
      .typeLabel("type")
      .idlabel("id");
  public static final String organizationURI = "http://data.doremus.org/organization/Philharmonie_de_Paris";
  public static final String doremusURI = "http://data.doremus.org/organization/DOREMUS";

  public static final String dotSeparator = "(?<![^A-Z][A-Z]|[oO]p|Hob|réf|[èeé]d|dir|Mus|[Ss]t|sept)\\.";

  public static Model convert(String file) throws FileNotFoundException, URISyntaxException {
    List<File> folderTUMs = Arrays.stream(properties.getProperty("TUMFolder").split(","))
      .map(File::new)
      .collect(Collectors.toList());

    Model model = ModelFactory.createDefaultModel();
    MarcXmlReader reader = new MarcXmlReader(file, PP2RDF.ppXmlHandlerBuilder);

    boolean found = false;
    for (Record r : reader.getRecords()) {
      // We convert TUMs contextually to notice d'oeuvre
      if (r.isType("AIC:14")) continue;

      // notice d'oeuvre: retrieve and convert TUM also
      if (r.isType("UNI:100")) {
        String idTUM = getIdTum(r);
        if (idTUM == null) {
          System.out.println("Notice without TUM specified: " + file);
          continue;
        }
        File tum = searchTUM(folderTUMs, idTUM);
        if (tum == null) {
          System.out.println("TUM specified but not found: notice " + r.getIdentifier() + ", tum " + idTUM);
          continue;
        }
        MarcXmlReader tumReader = new MarcXmlReader(tum.getAbsolutePath(), PP2RDF.ppXmlHandlerBuilder);
        new RecordConverter(tumReader.getRecords().get(0), model, r.getIdentifier());
      }


      RecordConverter mainRecord = new RecordConverter(r, model);
      if (mainRecord.isConverted()) found = true;
    }
    if (!found) return null;


    // Remove empty nodes
    String query = "delete where {?x ?p \"\" }";
    UpdateAction.parseExecute(query, model);

    return model;
  }

  private static String getIdTum(Record r) {
    return r.getDatafieldsByCode("500", '3').stream().findFirst().map(String::trim).orElse(null);
  }

  /**
   * Search TUM in multiple folders
   **/
  private static File searchTUM(List<File> folders, String idTUM) {
    for (File f : folders) {
      File tum = getTUM(f, idTUM);
      if (tum != null) return tum;
    }
    return null;
  }

  /**
   * Search TUM in a single folder
   **/
  private static File getTUM(final File folder, String idTUM) {
    for (File fileEntry : folder.listFiles()) {
      if (fileEntry.isDirectory()) {
        File f = getTUM(fileEntry, idTUM);
        if (f != null) return f;
      } else if (fileEntry.getName().equals(idTUM + ".xml")) {
        return fileEntry;
      }

    }
    return null;
  }


  public static String guessType(Record record) {
    List<String> funCodes = record.getDatafieldByCode(700, 4);
    funCodes.addAll(record.getDatafieldByCode(701, 4));
    funCodes.addAll(record.getDatafieldByCode(702, 4));
    funCodes.addAll(record.getDatafieldByCode(712, 4));

    if (funCodes.size() == 1 && funCodes.get(0).equals("800"))
      return "spoken word";
    else if (funCodes.contains("195") || funCodes.contains("230") ||
      funCodes.contains("545") || funCodes.contains("721"))
      return "performed music";

    return null;
  }


}
