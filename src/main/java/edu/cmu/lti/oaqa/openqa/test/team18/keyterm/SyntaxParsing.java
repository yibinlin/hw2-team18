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
  List<String> candidates;

  public SyntaxParsing() throws ResourceInitializationException {
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, parse");
    pipeline = new StanfordCoreNLP(props);
    candidates = new LinkedList<String>();
  }

  public List<String> getKeytermCandidates(String text) {
    
    Annotation document = new Annotation(text);
    pipeline.annotate(document);
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      Tree root = sentence.get(TreeAnnotation.class);
      Tree cNode = root;
      goThroughTree(root);
      System.out.println(root.label().value());
    }
    return candidates;
  }
  
  private synchronized void addCandidate(String phrase)
  {
      candidates.add(phrase);
  }

  private void goThroughTree(Tree node) {
    if(node == null)
    {
      return;
    }else
    {
      System.out.println(node.label().value());
      Tree[] children = node.children();
      for(Tree child : children)
      {
        goThroughTree(child);
      }
    }
  }
  
  /**
   * update the candidates List for some part of the tree.
   * 
   * @param node the node of the tree. The method cancatenates 
   * all the words that was covered by the node and put it under 
   * the add to the candidates. 
   */
  private void updateCandidate(Tree node)
  {
    
  }
}
