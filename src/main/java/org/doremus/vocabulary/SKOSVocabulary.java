package org.doremus.vocabulary;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SKOSVocabulary extends Vocabulary {
  private Map<String, Resource> substitutionMap;

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
        //  add it to the map
        substitutionMap.put(value, resource);
      }

      labelIterator = resource.listProperties(SKOS.altLabel);
      //for each label
      while (labelIterator.hasNext()) {
        String value = labelIterator.nextStatement().getObject().toString();
        //  add it to the map
        substitutionMap.put(value, resource);
      }
    }

  }

  @Override
  public Resource findConcept(String text, boolean strict) {
    for (Map.Entry<String, Resource> entry : substitutionMap.entrySet()) {
      String key = entry.getKey();
      String keyPlain = key.replaceAll("@[a-z]{2,3}$", "");

      boolean textLangMatch = text.equalsIgnoreCase(key);
      boolean textOnlyMatch = !strict && text.replaceAll("@[a-z]{2,3}$", "").equalsIgnoreCase(keyPlain);

      if (textLangMatch || textOnlyMatch) return entry.getValue();
    }
    return null;
  }

  public Model buildReferenceIn(Model model) {
    List<Statement> statementsToRemove = new ArrayList<>(),
      statementsToAdd = new ArrayList<>();

    for (Map.Entry<String, Resource> entry : substitutionMap.entrySet()) {
      String key = entry.getKey();
      String keyPlain = key.replaceAll("@[a-z]{2,3}$", "");
      Resource value = entry.getValue();
      StmtIterator iter = model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
        public boolean selects(Statement s) {
          String obj = s.getObject().toString();
          return obj.equalsIgnoreCase(key) || obj.replaceAll("@[a-z]{2,3}$", "").equalsIgnoreCase(keyPlain);
        }
      });

      while (iter.hasNext()) {
        Statement s = iter.nextStatement();

        // System.out.println("FOUND " + key + " --> " + value);
        Property pred = s.getPredicate();
        if (pred.equals(CIDOC.P102_has_title) || pred.equals(CIDOC.P3_has_note) || pred.equals(MUS.U40_has_catalogue_name) || pred.equals(RDFS.label)) {
          // genres in title and notes should be maintained
        } else if (s.getPredicate().equals(CIDOC.P1_is_identified_by)) {
          // replace the whole node
          StmtIterator parentIter = model.listStatements(new SimpleSelector(null, null, s.getSubject()));
          int howManyIteration = 0;
          while (parentIter.hasNext()) {
            Statement ps = parentIter.nextStatement();

            statementsToRemove.add(ps);
            StmtIterator subjIter = ps.getObject().asResource().listProperties();
            while (subjIter.hasNext()) {
              statementsToRemove.add(subjIter.nextStatement());
            }
            statementsToAdd.add(new StatementImpl(ps.getSubject(), ps.getPredicate(), value));

            howManyIteration++;
          }
          if (howManyIteration != 1)
            System.out.println("Converter.link2Vocabulary | Exactly one iteration expected. Please check.");
        } else {
          // replace only the literal
          statementsToRemove.add(s);
          statementsToAdd.add(new StatementImpl(s.getSubject(), s.getPredicate(), value));
        }

      }
    }
    model.remove(statementsToRemove);
    model.add(statementsToAdd);

    return model;
  }

}
