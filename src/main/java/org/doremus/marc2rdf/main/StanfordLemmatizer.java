package org.doremus.marc2rdf.main;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class StanfordLemmatizer {
  private static StanfordLemmatizer defaultLemmatizer = new StanfordLemmatizer();
  private StanfordCoreNLP pipeline;

  public StanfordLemmatizer() {
    // Create StanfordCoreNLP object properties, with POS tagging
    // (required for lemmatization), and lemmatization
    Properties props;
    props = PropertiesUtils.asProperties(
      "annotators", "tokenize,ssplit,pos,lemma,parse,natlog",
      "ssplit.isOneSentence", "true",
      "tokenize.language", "fr");

    this.pipeline = new StanfordCoreNLP(props);
  }

  public List<String> lemmatize(String documentText) {
    List<String> lemmas = new LinkedList<>();
    if(documentText.length() < 1 ) return lemmas;

    // Create an empty Annotation just with the given text
    Annotation document = new Annotation(documentText);
    // run all Annotators on this text
    this.pipeline.annotate(document);
    // Iterate over all of the sentences found
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      // Iterate over all tokens in a sentence
      for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
        // Retrieve and add the lemma for each word into the
        // list of lemmas
        lemmas.add(token.get(LemmaAnnotation.class));
      }
    }
    return lemmas;
  }


  public static StanfordLemmatizer getDefaultLemmatizer(){
    return defaultLemmatizer;
  }


}
