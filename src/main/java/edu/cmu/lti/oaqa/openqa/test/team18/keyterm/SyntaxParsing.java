package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.uima.resource.ResourceInitializationException;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class SyntaxParsing {
  private StanfordCoreNLP pipeline;

  public SyntaxParsing() throws ResourceInitializationException {
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, parse");
    pipeline = new StanfordCoreNLP(props);
  }

  public List<String> getKeytermCandidates(String text) {
    List<String> candidates = new LinkedList<String>();
    Annotation document = new Annotation(text);
    pipeline.annotate(document);
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      Tree tree = sentence.get(TreeAnnotation.class);
      tree.pennPrint();
    }
    return candidates;
  }
}
