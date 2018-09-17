package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.marc2rdf.main.AbstractConverter;
import org.doremus.marc2rdf.main.ConstructURI;
import org.doremus.marc2rdf.marcparser.ControlField;
import org.doremus.marc2rdf.marcparser.MarcXmlHandler;
import org.doremus.marc2rdf.marcparser.MarcXmlReader;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.PROV;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.time.Instant;

public class BNF2RDF extends AbstractConverter {
  public static final MarcXmlHandler.MarcXmlHandlerBuilder bnfXmlHandlerBuilder = new MarcXmlHandler.MarcXmlHandlerBuilder();
  public static final Resource BnF = ResourceFactory.createResource(
    "http://data.doremus.org/organization/BnF");
  public static final Resource DOREMUS = ResourceFactory.createResource(
    "http://data.doremus.org/organization/DOREMUS");

  /******
   * Constants that represents the kind of record
   ******/
  private static final char TUM = 'u';
  private static final char PERSON = 'p';
  private static final char ORGANIZATION = 'c'; //collectivité

  private static boolean modifiedOut = false;
  private boolean somethingHasBeenConverted;
  private BIBRecordConverter mainRecordConv;

  public BNF2RDF() {
    super();
    this.graphName = "bnf";
    this.somethingHasBeenConverted = false;
  }

  public Model convert(File file) throws FileNotFoundException {
//    if (!file.getName().endsWith("37763359.xml")) return null;
    MarcXmlReader reader = new MarcXmlReader(file, BNF2RDF.bnfXmlHandlerBuilder);

    if (reader.getRecords() == null || reader.getRecords().size() == 0) {
      System.out.println("Exception occurred parsing file " + file);
    }

    mainRecordConv = null;

    for (Record r : reader.getRecords()) {
//      try {
//        ControlField leader = r.getControlfieldByCode("leader");
//        String code = leader.getData().charAt(r.isBIB() ? 22 : 9) + "";
//        File dstDir = Paths.get("/Users/pasquale/Desktop/DOREMUSio/src/bnf2", r.getType(), code).toFile();
//        FileUtils.copyFileToDirectory(file, dstDir);
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//      if (true) return null;


      if (r.getType() == null) return null;

      if (r.isBIB()) {
        if (!r.isDAV() && !r.isMUS()) break;
        if (r.getLevel() > 1) continue;
        BIBRecordConverter conv = convertBIB(r, mainRecordConv);
        if (mainRecordConv == null) mainRecordConv = conv;
      } else convertAuthority(r);
    }

    if (!somethingHasBeenConverted) return null;

    if (!modifiedOut) modifiedOut = this.addModified();

    // Remove empty nodes
    String query = "delete where {?x ?p \"\" }";
    UpdateAction.parseExecute(query, model);

    return model;
  }

  private BIBRecordConverter convertBIB(Record r, BIBRecordConverter upperRecord) {
    somethingHasBeenConverted = true;
    return new BIBRecordConverter(r, model, upperRecord);
  }

  private void convertAuthority(Record r) {
    ControlField leader = r.getControlfieldByCode("leader");
    if (leader == null) return;

    char code = leader.getData().charAt(9);
    try {
      switch (code) {
        case TUM:
          somethingHasBeenConverted = true;
          new RecordConverter(r, model);
          break;
        case PERSON:
          somethingHasBeenConverted = true;
          new ArtistConverter(r, model);
          break;
        case ORGANIZATION:
          //TODO
          break;
        default:
          System.out.println("Not recognized kind of Authority record: " + code);
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  static Resource computeProvActivity(String identifier, Resource intermarc, Model model) {
    String uri = null;
    try {
      uri = ConstructURI.build("bnf", "prov", identifier).toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return model.createResource(uri)
      .addProperty(RDF.type, PROV.Activity).addProperty(RDF.type, PROV.Derivation)
      .addProperty(PROV.used, intermarc)
      .addProperty(RDFS.comment, "Reprise et conversion de la notice MARC de la BnF", "fr")
      .addProperty(RDFS.comment, "Resumption and conversion of the MARC record of the BnF", "en")
      .addProperty(PROV.atTime, Instant.now().toString(), XSDDatatype.XSDdateTime);
  }

  static Resource computeProvIntermarc(String ark, Model model) {
    return model.createResource("http://catalogue.bnf.fr/" + ark + ".intermarc")
      .addProperty(RDF.type, PROV.Entity).addProperty(PROV.wasAttributedTo, BnF);
  }


}

