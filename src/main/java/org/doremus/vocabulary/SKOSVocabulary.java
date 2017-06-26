package org.doremus.vocabulary;

import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.util.*;

public class SKOSVocabulary extends Vocabulary {
  private Map<String, List<Resource>> substitutionMap;

  public SKOSVocabulary(String name, Model model) {
    super(name, model);

    setSchemePathFromType(SKOS.ConceptScheme);

    // Build a map
    substitutionMap = new HashMap<>();

    // for each concept
    StmtIterator conceptIter =
      vocabulary.listStatements(new SimpleSelector(null, RDF.type, SKOS.Concept));

    if (!conceptIter.hasNext()) {
      System.out.println("SKOSVocabulary constructor | Warning: No concepts in the reference rdf at " + name);
      return;
    }

    while (conceptIter.hasNext()) {
      Resource resource = conceptIter.nextStatement().getSubject();
      // get the labels
      StmtIterator labelIterator = resource.listProperties(SKOS.prefLabel);
      //for each label
      while (labelIterator.hasNext()) {
        String value = labelIterator.nextStatement().getObject().toString();
        // get the list or create a new one
        List<Resource> ls = substitutionMap.computeIfAbsent(value, k -> new ArrayList<>());
        // add it to the list
        ls.add(resource);
      }

      labelIterator = resource.listProperties(SKOS.altLabel);
      //for each label
      while (labelIterator.hasNext()) {
        String value = labelIterator.nextStatement().getObject().toString();

        // get the list or create a new one
        List<Resource> ls = substitutionMap.computeIfAbsent(value, k -> new ArrayList<>());
        // add it to the list
        ls.add(resource);
      }
    }

  }

  @Override
  public Resource findConcept(String text, boolean strict) {
    for (Map.Entry<String, List<Resource>> entry : substitutionMap.entrySet()) {
      String key = entry.getKey();
      String keyPlain = key.replaceAll("@[a-z]{2,3}$", "");

      boolean textLangMatch = text.equalsIgnoreCase(key);
      boolean textOnlyMatch = !strict && text.replaceAll("@[a-z]{2,3}$", "").equalsIgnoreCase(keyPlain);

      if (textLangMatch || textOnlyMatch) {
        List<Resource> matches = entry.getValue();
        Resource bestMatch = null;
        for (Resource m : matches) {
          if (bestMatch == null) {
            bestMatch = m;
            continue;
          }

          // if I already had a "bestMatch"
          // choose the most specific one (skos:narrower)
          Statement narrower = bestMatch.getProperty(SKOS.narrower);
          if (narrower != null) bestMatch = m;
        }

        return bestMatch;
      }
    }
    return null;
  }

}
