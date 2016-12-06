package org.doremus.marc2rdf.ppconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.XSD;
import org.doremus.marc2rdf.marcparser.DataField;
import org.doremus.marc2rdf.marcparser.MarcXmlHandler;
import org.doremus.marc2rdf.marcparser.MarcXmlReader;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

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

  public static Model convert(String file) throws FileNotFoundException, URISyntaxException {
    File folderTUMs = new File(properties.getProperty("TUMFolder"));

    /************* Creer un modele vide **************************/
    //Model model = VirtModel.openDatabaseModel("DOREMUS", "jdbc:virtuoso://localhost:1111", "dba", "dba");
    Model model = ModelFactory.createDefaultModel();
    MarcXmlReader reader = new MarcXmlReader(file, PP2RDF.ppXmlHandlerBuilder);

    boolean found = false;
    for (Record r : reader.getRecords()) {
      /******
       * Verifier si c'est une notice d'oeuvre ou un TUM
       **********/
      //noinspection StatementWithEmptyBody
      if (r.isType("AIC:14")) {
        // Si c'est un TUM
        // nothing more required
      } else if (r.isType("UNI:100")) {
        // Si c'est une notice d'oeuvre
        String idTUM = getIdTum(r);

        if (idTUM == null) {
          System.out.println("Notice without TUM specified: " + file);
          continue;
        }

        // Convertir le TUM correspondant
        File tum = getTUM(folderTUMs, idTUM);
        if(tum == null){
          System.out.println("TUM specified but not found: notice " + file + ", tum " + idTUM);
          continue;
        }
        MarcXmlReader tumReader = new MarcXmlReader(tum.getAbsolutePath(), PP2RDF.ppXmlHandlerBuilder);
        new RecordConverter(tumReader.getRecords().get(0), model, r.getIdentifier());
      } else {
        // TODO other notice types?
        // System.out.println("Skipping not recognized PP notice type " + r.getType() + " for file " + file);
        continue;
      }
      found = true;
      new RecordConverter(r, model);
    }
    if (!found) return null;

    model.setNsPrefix("mus", MUS.getURI());
    model.setNsPrefix("ecrm", CIDOC.getURI());
    model.setNsPrefix("efrbroo", FRBROO.getURI());
    model.setNsPrefix("xsd", XSD.getURI());
    model.setNsPrefix("dcterms", DCTerms.getURI());

    // Remove empty nodes
    String query = "delete where {?x ?p \"\" }";
    UpdateAction.parseExecute(query, model);

    return model;

    /****************************************************************************************/
     /* String query = "WITH GRAPH  <DOREMUS>"+
                 "delete where {?x ?p \"\" }";
	    VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(query, model);
	    vur.exec();
		/****************************************************************************************/
  }

  private static String getIdTum(Record r) {
    for (DataField field : r.getDatafieldsByCode("500")) {
      if (field.isCode('3'))
        return field.getSubfield('3').getData().trim();
    }
    return null;
  }

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

}
