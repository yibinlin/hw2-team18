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

/**
 * get some candidates using Stanford Parser.
 *  
 * @author Yibin Lin
 *
 */
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
    this.clearCandidates();
    Annotation document = new Annotation(text);
    pipeline.annotate(document);
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      Tree root = sentence.get(TreeAnnotation.class);
      goThroughTree(root);
      // System.out.println(root.label().value());
    }
    return candidates;
  }

  private synchronized void addCandidate(String phrase) {
    candidates.add(phrase);
  }
  
  private synchronized void clearCandidates()
  {
    candidates.clear();
  }

  /**
   * go through the whole tree node by node, by using a 
   * depth-first search-style approach. If a node is within
   * our category, then we print all the words enclosed in 
   * this consitituent.
   * 
   * @param node current node we are searching, used in the 
   * recursive call.
   */
  private void goThroughTree(Tree node) {
    if (node == null) {
      return;
    } else {
      System.out.println(node.label().value());
      if (checkCategory(node.label().value())) {
        StringBuffer sb = new StringBuffer();
        this.updateCandidate(node, sb);
        if(!this.filterCandidate(sb.toString()))
        {
          this.addCandidate(sb.toString());
        }
      }
      Tree[] children = node.children();
      for (Tree child : children) {
        goThroughTree(child);
      }
    }
  }

  /**
   * filter candidate string, currently this method does nothing.. just return false, 
   * and therefore has no effect to the system
   * 
   * @param string
   * @return true if the string is considered to be filtered.
   */
  private boolean filterCandidate(String string) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * Check if the category is what we wanted..
   * 
   * @param label
   *          the Penn Treebank label, such as "NP"
   * @return true if the category is what we wanted, false if no.
   */
  private boolean checkCategory(String label) {
    if (label.equalsIgnoreCase("NP")) {
      return true;
    }
    return false;
  }

  /**
   * update the candidates List for some part of the tree.
   * 
   * @param node
   *          the node of the tree. The method cancatenates all the words that was covered by the
   *          node and put it under the add to the candidates.
   */
  private void updateCandidate(Tree node, StringBuffer sb) {
    if (node.isLeaf()) {
      sb.append(" ");
      sb.append(node.label().value());
    } else {
      Tree[] children = node.children();
      for (Tree child : children) {
        updateCandidate(child, sb);
      }
      return;
    }
  }
}
