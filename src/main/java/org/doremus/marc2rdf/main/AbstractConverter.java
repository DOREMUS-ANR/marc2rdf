package org.doremus.marc2rdf.main;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.DCTerms;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;

public abstract class AbstractConverter {
  protected Model model;
  protected String graphName;

  public AbstractConverter() {
    this.model = ModelFactory.createDefaultModel();
  }

  public abstract Model convert(File file) throws FileNotFoundException;

  protected boolean addModified() {
    model.createResource("http://data.doremus.org/" + this.graphName)
      .addProperty(DCTerms.modified, Instant.now().toString(), XSDDatatype.XSDdateTime);
    return true;
  }

}
