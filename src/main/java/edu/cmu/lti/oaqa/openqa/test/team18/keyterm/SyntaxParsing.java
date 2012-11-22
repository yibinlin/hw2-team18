package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.uima.resource.ResourceInitializationException;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * get some candidates using Stanford Parser.
 * 
 * @author Yibin Lin
 * 
 */
public class SyntaxParsing extends KeytermCandidateFinder{
  private StanfordCoreNLP pipeline;
  Morphology morph;

  public SyntaxParsing() throws ResourceInitializationException {
    super();
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, parse");
    pipeline = new StanfordCoreNLP(props);
    morph = new Morphology();
  }

  public List<String> getKeytermCandidates(String text) {
    this.clearCandidates();
    text = text.replaceAll("[0-9]+\\|", "");
    // System.out.println(text);
    Annotation document = new Annotation(text);
    pipeline.annotate(document);
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      Tree root = sentence.get(TreeAnnotation.class);
      goThroughTree(root);
    }
    return candidates;
  }


  /**
   * go through the whole tree node by node, by using a depth-first search-style approach. If a node
   * is within our category, then we print all the words enclosed in this consitituent.
   * 
   * @param node
   *          current node we are searching, used in the recursive call.
   */
  private void goThroughTree(Tree node) {
    if (node == null) {
      return;
    } else {
      //System.out.println(node.label().value());
      if (checkCategory(node.label().value())) {
        StringBuffer sb = new StringBuffer();
        boolean updateSuccess = this.updateCandidate(node, sb);
        if (updateSuccess && !this.filterCandidate(sb.toString().trim(), node)) {
          this.addCandidate(postprocessing(sb.toString().trim()));
        }
      }
      Tree[] children = node.children();
      for (Tree child : children) {
        goThroughTree(child);
      }
    }
  }

  /**
   * 
   * @param s
   * @return
   */
  private String postprocessing(String s) {
    s = s.replaceAll(" 's", "'s");
    return s;
  }

  /**
   * filter candidate string, currently this method does nothing.. just return false, and therefore
   * has no effect to the system
   * 
   * @param string
   * @return true if the string is considered (better) to be filtered.
   */
  private boolean filterCandidate(String string, Tree node) {
    if(string.matches("[0-9]+ .*"))
    {
      return true;
    }
    else if(node.label().value().equalsIgnoreCase("VBP"))
    {
      //System.out.println(String.format("string: %s, label: %s", string, node.label().value()));
      String l = this.morph.lemma(string, node.label().value());
      if(l.equalsIgnoreCase("do") || l.equalsIgnoreCase("be"))
      {
        return true;
      }
    }
    else if(string.contains("(") || string.contains(")")) //filter left/right parenthesis..
    {
      return true;
    }
    else if(!string.matches(".*[a-zA-Z0-9]")) //if ends with a special character, we filter it..
    {
      return true;
    }
    else if(!string.matches("[a-zA-Z0-9].*")) //if starts with a special character, we filter it..
    {
      return true;
    }
    
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
    if (label.equalsIgnoreCase("NP") || label.equalsIgnoreCase("VBP")) {
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
  private boolean updateCandidate(Tree node, StringBuffer sb) {
    boolean result = true;
    
    if (node.isLeaf()) {
      if ((node.label().value().equals("-LRB-") || node.label().value().equals("-RRB-")))
      //ignore the parenthesis..
      {
        return false;
      }
      else
      {
        sb.append(" ");
        sb.append(node.label().value());
        return result;
      }
    } else {
      if(node.label().value().equalsIgnoreCase("DT") || node.label().value().equalsIgnoreCase("IN"))
      {
        return false;
      }
      Tree[] children = node.children();
      for (Tree child : children) {
        result = result && updateCandidate(child, sb);
      }
      return result;
    }
  }
}
