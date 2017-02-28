package org.doremus.vocabulary;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.List;

import static edu.stanford.nlp.util.StringUtils.containsIgnoreCase;

public class MODS extends Vocabulary {
  // MODS class/properties shortcut
  private static final Model m = ModelFactory.createDefaultModel();
  public static final String uri = "http://www.loc.gov/standards/mods/rdf/v1/#";
  public static final Resource ModsResource = m.createResource(uri + "ModsResource");
  public static final Property subjectName = m.createProperty(uri + "subjectName");

  private static final ParameterizedSparqlString modsSearch = new ParameterizedSparqlString(
    "prefix modsrdf: <http://www.loc.gov/standards/mods/rdf/v1/#>\n" +
      "select distinct ?cat where {\n" +
      "  { ?cat modsrdf:identifier ?id}\n" +
      "  UNION {\n" +
      "    ?cat modsrdf:identifierGroup ?ig .\n" +
      "    ?ig modsrdf:identifierGroupValue ?id\n" +
      "  }\n" +
      "}");


  public MODS(String name, Model model) {
    super(name, model);

    setSchemePathFromType("http://www.w3.org/ns/dcat#Catalog");

  }

  @Override
  public Resource findConcept(String text, boolean strict) {
    return findModsResource(text, null);
  }

  public Resource findModsResource(String identifier, List<String> composers) {
    if (identifier == null || identifier.isEmpty()) return null;

    // search all catalogs with that identifier
    modsSearch.setLiteral("id", identifier);
    QueryExecution qexec = QueryExecutionFactory.create(modsSearch.asQuery(), vocabulary);
    ResultSet result = qexec.execSelect();
    List<Resource> candidateCatalogs = new ArrayList<>();

    // add them to the canditates list
    while (result.hasNext()) {
      Resource resource = result.next().get("cat").asResource();
      candidateCatalogs.add(resource);
    }

    if (candidateCatalogs.size() == 0) return null;
    else if (candidateCatalogs.size() > 1) {
      if (composers != null) {
        // load related artists
        for (Resource res : candidateCatalogs) {
          String subjectName = res.getProperty(MODS.subjectName).getObject().toString();
          if (containsIgnoreCase(composers, subjectName) || containsIgnoreCase(composers, subjectName.replaceAll("-", " "))) {
            // found!
            return res;
          }
        }
      }
      // System.out.println("Too many results for catalog " + identifier + " and composers " + composers + ". It will be not linked.");
      return null;
    } else return candidateCatalogs.get(0);
  }

}
