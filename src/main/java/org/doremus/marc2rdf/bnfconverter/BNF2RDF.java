package org.doremus.marc2rdf.bnfconverter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.XSD;
import org.doremus.marc2rdf.marcparser.MarcXmlHandler;
import org.doremus.marc2rdf.marcparser.MarcXmlReader;
import org.doremus.marc2rdf.marcparser.Record;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.io.IOException;
import java.net.URISyntaxException;

public class BNF2RDF {
  public static final MarcXmlHandler.MarcXmlHandlerBuilder bnfXmlHandlerBuilder = new MarcXmlHandler.MarcXmlHandlerBuilder();


  public static Model convert(String file) throws URISyntaxException, IOException {

    /************* Creer un modele vide **************************/
    //Model model = VirtModel.openDatabaseModel("DOREMUS", "jdbc:virtuoso://localhost:1111", "dba", "dba");
    Model model = ModelFactory.createDefaultModel();
    MarcXmlReader reader = new MarcXmlReader(file, BNF2RDF.bnfXmlHandlerBuilder);

    for (Record r : reader.getRecords()) {
      new RecordConverter(r, model);
    }

    model.setNsPrefix("mus", MUS.getURI());
    model.setNsPrefix("ecrm", CIDOC.getURI());
    model.setNsPrefix("frbroo", FRBROO.getURI());
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
}
