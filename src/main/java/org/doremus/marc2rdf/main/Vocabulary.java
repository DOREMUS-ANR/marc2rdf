package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for Vocabulary referencing.
 */

public class Vocabulary {
  private Model vocabulary;
  private HashMap<String, Resource> substitutionMap;

  public Vocabulary(String url) {
    vocabulary = ModelFactory.createDefaultModel();
    vocabulary.read(url, "TURTLE");

    // Build a map
    substitutionMap = new HashMap<>();

    // for each concept
    StmtIterator conceptIter =
      vocabulary.listStatements(new SimpleSelector(null, RDF.type, vocabulary.getResource(SKOS.Concept.toString())));

    if (!conceptIter.hasNext()) {
      System.out.println("Vocabulary constructor | Warning: No concepts in the reference rdf at " + url);
      return;
    }

    while (conceptIter.hasNext()) {
      Resource resource = conceptIter.nextStatement().getSubject();
      // get the labels
      StmtIterator labelIterator = resource.listProperties(SKOS.prefLabel);
      //for each label
      while (labelIterator.hasNext()) {
        String value = labelIterator.nextStatement().getObject().toString();
        //  add it to the map
        substitutionMap.put(value, resource);
      }
    }

  }

  public Model buildReferenceIn(Model model) {
    List<Statement> statementsToRemove = new ArrayList<>();

    for (Map.Entry<String, Resource> entry : substitutionMap.entrySet()) {
      String key = entry.getKey();
      Resource value = entry.getValue();

      StmtIterator iter = model.listStatements(new SimpleSelector(null, null, key));
      while (iter.hasNext()) {
        Statement s = iter.nextStatement();

        // System.out.println("FOUND " + key + " --> " + value);
        if (s.getPredicate().toString().equals("http://www.cidoc-crm.org/cidoc-crm/P1_is_identified_by")) {
          // replace the whole node
          StmtIterator parentIter = model.listStatements(new SimpleSelector(null, null, s.getSubject()));
          int howManyIteration = 0;
          while (parentIter.hasNext()) {
            Statement ps = parentIter.nextStatement();

            model.add(ps.getSubject(), ps.getPredicate(), value);
            statementsToRemove.add(ps);

            howManyIteration++;
          }
          if (howManyIteration != 1)
            System.out.println("Converter.link2Vocabulary | Exactly one iteration expected. Please check.");
        } else {
          // replace only the literal
          model.add(s.getSubject(), s.getPredicate(), value);
        }

        statementsToRemove.add(s);
      }
    }
    model.remove(statementsToRemove);

    return model;
  }
}
