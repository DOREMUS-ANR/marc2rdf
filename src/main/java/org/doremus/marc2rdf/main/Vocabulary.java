package org.doremus.marc2rdf.main;

import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.doremus.ontology.CIDOC;

import java.util.*;

import static edu.stanford.nlp.util.StringUtils.containsIgnoreCase;

/**
 * Utility for Vocabulary referencing.
 */

public class Vocabulary implements Comparable<Vocabulary> {
  private Model vocabulary;
  private HashMap<String, Resource> substitutionMap;
  private String schemePath;
  private final String MODS = "http://www.loc.gov/standards/mods/rdf/v1/#";

  public Vocabulary(String url) {
    vocabulary = ModelFactory.createDefaultModel();
    vocabulary.read(url, "TURTLE");

    // Save default path
    StmtIterator conceptSchemeIter =
      vocabulary.listStatements(new SimpleSelector(null, RDF.type, SKOS.ConceptScheme));
    if (!conceptSchemeIter.hasNext()) {
      System.out.println("Vocabulary constructor | Warning: No ConceptScheme in the reference rdf at " + url + ". Method \"findConcept\" will not work for it.");
    } else {
      schemePath = conceptSchemeIter.nextStatement().getSubject().toString();
    }

    if (schemePath != null && !schemePath.endsWith("/") && !schemePath.endsWith("#"))
      schemePath += "/";

    // Build a map
    substitutionMap = new HashMap<>();

    // for each concept
    StmtIterator conceptIter =
      vocabulary.listStatements(new SimpleSelector(null, RDF.type, SKOS.Concept));

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

      labelIterator = resource.listProperties(SKOS.altLabel);
      //for each label
      while (labelIterator.hasNext()) {
        String value = labelIterator.nextStatement().getObject().toString();
        //  add it to the map
        substitutionMap.put(value, resource);
      }

    }

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
        if (pred.equals(CIDOC.P102_has_title) || pred.equals(CIDOC.P3_has_note)) {
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

  public Resource findConcept(String code) {
    if (schemePath == null) return null;

    Resource concept = vocabulary.getResource(schemePath + code);

    if (vocabulary.contains(concept, null, (RDFNode) null)) {
      return concept;
    } else return null;
  }

  public Resource findModsResource(String identifier) {
    return findModsResource(identifier, null);
  }

  public Resource findModsResource(String identifier, List<String> composers) {
    if (identifier == null || identifier.isEmpty()) return null;

    List<Resource> candidateCatalogs = new ArrayList<>();

    // search all catalogs with that identifier
    StmtIterator iter = vocabulary.listStatements(new SimpleSelector(null, vocabulary.getProperty(MODS, "identifier"), (RDFNode) null) {
      public boolean selects(Statement s) {
        String obj = s.getObject().toString();
        return obj.equalsIgnoreCase(identifier);
      }
    });

    while (iter.hasNext()) {
      Resource resource = iter.nextStatement().getSubject();
      candidateCatalogs.add(resource);
    }

    if (candidateCatalogs.size() == 0) return null;
    else if (candidateCatalogs.size() > 1) {
      if (composers != null) {
        // load related artists
        for (Resource res : candidateCatalogs) {
          Object subjectName = res.getProperty(vocabulary.getProperty(MODS, "subjectName")).getObject();
          System.out.println(subjectName.toString());
          if (containsIgnoreCase(composers, subjectName.toString())) {
            // found!
            return res;
          }
        }
      }
      System.out.println("Too many results for catalog " + identifier + " and composers " + composers + ". It will be not linked.");
      return null;
    } else return candidateCatalogs.get(0);
  }


  @Override
  public int compareTo(Vocabulary v) {
    if (this.schemePath == null) return 1;
    if (v.schemePath == null) return -1;

    // give priority to iaml
    boolean thisIaml = this.schemePath.contains("/iaml");
    boolean thatIaml = v.schemePath.contains("/iaml");

    if (thisIaml == thatIaml) return this.schemePath.compareTo(v.schemePath);

    if (thisIaml) return -1;
    return 1;
  }
}
