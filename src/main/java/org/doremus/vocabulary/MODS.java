package org.doremus.vocabulary;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.doremus.marc2rdf.main.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static edu.stanford.nlp.util.StringUtils.containsIgnoreCase;

public class MODS extends Vocabulary {
  // MODS class/properties shortcut
  private static final Model m = ModelFactory.createDefaultModel();
  public static final String uri = "http://www.loc.gov/standards/mods/rdf/v1/#";
  public static final Resource ModsResource = m.createResource(uri + "ModsResource");


  public MODS(String name, Model model) {
    super(name, model);

    setSchemePathFromType("http://www.w3.org/ns/dcat#Catalog");

  }

  @Override
  public Resource findConcept(String text, boolean strict) {
    return findModsResource(text, null);
  }

  public Resource findModsResource(String identifier, List<Person> subjects) {
    if (identifier == null || identifier.isEmpty()) return null;

    String modsSearch =
      "prefix modsrdf: <http://www.loc.gov/standards/mods/rdf/v1/#>\n" +
        "select distinct ?cat where {\n" +
        "  { ?cat modsrdf:identifier ?id}\n" +
        "  UNION {\n" +
        "    ?cat modsrdf:identifierGroup / modsrdf:identifierGroupValue ?id\n" +
        "  }\n" +
        "  FILTER (lcase(str(?id)) = \"" + identifier.toLowerCase() + "\")\n" +
        "}";

    // search all catalogs with that identifier
    QueryExecution qexec = QueryExecutionFactory.create(modsSearch, vocabulary);
    ResultSet result = qexec.execSelect();
    List<Resource> candidateCatalogs = new ArrayList<>();

    // add them to the canditates list
    while (result.hasNext()) {
      Resource resource = result.next().get("cat").asResource();
      candidateCatalogs.add(resource);
    }

    if (candidateCatalogs.size() == 0)
      return null;

    if (candidateCatalogs.size() > 1) {
      if (subjects != null) {
        // load related artists
        for (Resource res : candidateCatalogs) {
          String curSubject = res.getProperty(DCTerms.subject).getObject().toString();
          for (Person s : subjects) {
            if (Objects.equals(s.getUri().toString(), curSubject))
              return res;
          }
        }
        // System.out.println("Too many results for catalog " + identifier + " and composers " + composers + ". It will be not linked.");
      }
      return null;
    }

    return candidateCatalogs.get(0);
  }
}
