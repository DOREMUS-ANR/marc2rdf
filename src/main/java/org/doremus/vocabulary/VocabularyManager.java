package org.doremus.vocabulary;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VocabularyManager {
  private static List<Vocabulary> vocabularies;
  private static Map<String, List<Vocabulary>> vocabularyMap;
  private static final ParameterizedSparqlString propertyMatchingSPARQL =
    new ParameterizedSparqlString(
      "select ?s ?o ?label where {\n" +
        " { ?s ?p ?o . ?o ecrm:P1_is_identified_by ?label }\n" +
        " UNION\n" +
        " { ?s ?p ?label }\n" +
        " FILTER(isLiteral(?label))}");
  private static Map<Property, String> property2FamilyMap;

  public static void init() throws IOException {
    vocabularies = new ArrayList<>();
    vocabularyMap = new HashMap<>();

    propertyMatchingSPARQL.setNsPrefix("ecrm", CIDOC.NS);
    property2FamilyMap = new HashMap<>();
    property2FamilyMap.put(MUS.U1_used_medium_of_performance, "mop");
    property2FamilyMap.put(MUS.U2_foresees_use_of_medium_of_performance_of_type, "mop");
    property2FamilyMap.put(MUS.U11_has_key, "key");
    property2FamilyMap.put(MUS.U12_has_genre, "genre");

    // load vocabularies from resource folder
    String vocabularyDirPath = VocabularyManager.class.getClassLoader().getResource("vocabulary").getPath();
    File vocabularyDir = new File(vocabularyDirPath);

    File[] files = vocabularyDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ttl"));
    for (File file : files) {
      Vocabulary vocabulary = Vocabulary.fromFile(file);
      if (vocabulary == null) continue;
      vocabularies.add(vocabulary);

      List<Vocabulary> thisCategory = vocabularyMap.computeIfAbsent(vocabulary.getCategory(), k -> new ArrayList<>());
      thisCategory.add(vocabulary);
      Collections.sort(thisCategory);
    }

    Collections.sort(vocabularies);
  }

  public static Vocabulary getVocabulary(String name) {
    for (Vocabulary v : vocabularies)
      if (name.equals(v.getName())) return v;

    return null;
  }

  public static MODS getMODS(String name) {
    Vocabulary v = getVocabulary(name);
    if (v != null && v instanceof MODS) return (MODS) v;
    return null;
  }

  private static List<Vocabulary> getVocabularyCategory(String category) {
    return vocabularyMap.get(category);
  }

  public static void string2uri(Model m) {
    for (Map.Entry<Property, String> entry : property2FamilyMap.entrySet())
      propertyMatching(m, entry.getKey(), entry.getValue());
  }


  private static Model propertyMatching(Model model, Property property, String category) {
    List<Statement> statementsToRemove = new ArrayList<>(),
      statementsToAdd = new ArrayList<>();

    propertyMatchingSPARQL.setParam("?p", property);
    QueryExecution qexec = QueryExecutionFactory.create(propertyMatchingSPARQL.asQuery(), model);
    ResultSet result = qexec.execSelect();

    while (result.hasNext()) {
      QuerySolution res = result.next();
      Literal label = res.get("label").asLiteral();

      Resource concept = searchInCategory(label.toString(), null, category);
      if (concept == null) continue; //match not found

      Resource subject = res.get("s").asResource();
      if (res.get("o") != null) {
        Resource object = res.get("o").asResource();
        // remove all properties of the object
        for (StmtIterator it = object.listProperties(); it.hasNext(); )
          statementsToRemove.add(it.nextStatement());
        // remove the link between the object and the subject
        statementsToRemove.add(new StatementImpl(subject, property, object));
      } else
        statementsToRemove.add(new StatementImpl(subject, property, label));


      statementsToAdd.add(new StatementImpl(subject, property, concept));
    }

    model.remove(statementsToRemove);
    model.add(statementsToAdd);

    return model;
  }


  public static Resource searchInCategory(String label, String lang, String category) {
    String langLabel = lang != null ? label + "@" + lang : label;
    List<Vocabulary> vList = getVocabularyCategory(category);
    Resource concept;
    // first check: text + language
    for (Vocabulary v : vList) {
      concept = v.findConcept(langLabel, true);
      if (concept != null) return concept;
    }
    // second check: text without caring about the language
    for (Vocabulary v : vList) {
      concept = v.findConcept(label, false);
      if (concept != null) return concept;
    }
    return null;
  }

}
