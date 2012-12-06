package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
 * get some keyterm candidates using Stanford Syntax Parser.
 * 
 * @author Yibin Lin
 * 
 */
public class SyntaxParsing extends KeytermCandidateFinder {
  /**
   * instance of StanfordCoreNLP
   */
  private StanfordCoreNLP pipeline;

  /**
   * instance of Morphology, also part of StanfordCoreNLP, used by the filterCandidate method.
   * 
   * @see filterCandidate
   */
  private Morphology morph;

  /**
   * The unigram file path.
   */
  private static final String UNIGRAM_PATH = "src/main/resources/lexicon/cmudict.0.7a.gigaword.freq";

  /**
   * list of word with counts.
   */
  private ArrayList<WordCount> wordCounts;

  /**
   * Constructor, initialize StanfordCoreNLP and Morphology from StanfordCoreNLP, and load the sorted unigram counts. 
   * 
   * @throws ResourceInitializationException
   */
  public SyntaxParsing() throws ResourceInitializationException {
    super();
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, parse");
    pipeline = new StanfordCoreNLP(props);
    morph = new Morphology();
    File unigramf = new File(UNIGRAM_PATH);
    buildWordCount(unigramf);
  }

  /**
   * @override
   * 
   *           Get the keyterm candidate by using StanfordCoreNLP parser. The algorithm is roughly
   *           as follows: 1. Print the leaves of every node that is marked NP. 2. Print the leaves
   *           of every node that is marked VBP. 3. filter some of the candidates provided in 1 and
   *           2..
   * 
   * @see filterCandidate
   */
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

    removeDuplicates();
    return candidates;
  }

  /**
   * Build a unigram word count from a unigram file
   * 
   * @param unigramf
   */
  private void buildWordCount(File unigramf) {
    this.wordCounts = new ArrayList<WordCount>();

    FileReader fr;
    try {
      fr = new FileReader(unigramf);

      BufferedReader br = new BufferedReader(fr);
      String strLine;
      while ((strLine = br.readLine()) != null) {
        strLine = strLine.replaceAll("\n", "");
        String[] sep = strLine.split("  ", 2);
        if (sep.length == 0) {
          continue;
        } else if (sep.length == 1) {
          this.wordCounts.add(new WordCount(sep[0]));
        } else if (sep.length == 2) {
          this.wordCounts.add(new WordCount(sep[0], Integer.parseInt(sep[1])));
        } else {
          continue;
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Remove duplicates, and persist the order
   * 
   */
  private void removeDuplicates() {
    LinkedHashSet<String> set = new LinkedHashSet<String>();
    for (String s : candidates) {
      set.add(s);
    }

    this.clearCandidates();
    for (String s : set) {
      this.addCandidate(s);
    }
    return;
  }

  /**
   * go through the whole tree node by node, by using a depth-first search-style approach. If a node
   * is within our category, then we print all the words enclosed in this consitituent.
   * 
   * @param node
   *          current node we are searching, used in the recursive call.
   *          
   * @see filterCandidate
   */
  private void goThroughTree(Tree node) {
    if (node == null) {
      return;
    } else {
      // System.out.println(node.label().value());
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
   * The post processing of the strings. The problem arises when the StanfordCoreNLP 
   * syntax parse separate the proper noun and the "'s" into two words, therefore creating an 
   * empty space between them. The postprocessing method delete the gap between the proper noun 
   * and the "'s".
   * 
   * @param s the string that is guessed to be a keyterm.
   * @return it now deletes the possession form, e.g: "the student 's", will be
   * "the student's". 
   */
  private String postprocessing(String s) {
    s = s.replaceAll(" 's", "'s");
    return s;
  }

  /**
   * filter candidate string, it will return true if and only if: <br> 
   * 
   * 1. the string is a VBP and is a form of do and be.<br> 
   * 
   * 2. the string contains parenthesis (because if it is a word inside the
   * parenthesis, it should be only the word without parenthesis, and it will be captured by the
   * LingPipe tagger. Or it should be a phrase that contains part or all of the parenthesis,
   * therefore not a legitimate keyterm candidate, because none of the gold standard contains
   * parenthesis). <br> 
   * 
   * 3. the string starts with a special character <br> 
   * 
   * 4. the string ends with a special character. <br>
   * 
   * 5. The string ends with possessive tense (***'s). <br>
   * 
   * 6. The string is a single token, and it is a noun, and it is a frequent word. <br>
   * 
   * 7. The string matches an previously generated candidate exactly. <br>
   * 
   * @param string
   * @return true if the string is considered (better) to be filtered.
   */
  private boolean filterCandidate(String string, Tree node) {
    if (string.matches("[0-9]+ .*")) {
      return true;
    } else if (node.label().value().equalsIgnoreCase("VBP")) {
      // System.out.println(String.format("string: %s, label: %s", string, node.label().value()));
      String l = this.morph.lemma(string, node.label().value());
      if (l.equalsIgnoreCase("do") || l.equalsIgnoreCase("be")) {
        return true;
      }
    } else if (string.contains("(") || string.contains(")")) // filter left/right parenthesis..
    {
      return true;
    } else if (!string.matches(".*[a-zA-Z0-9]")) // if ends with a special character, we filter it..
    {
      return true;
    } else if (!string.matches("[a-zA-Z0-9].*")) // if starts with a special character, we filter
                                                 // it..
    {
      return true;
    } else if (string.matches(".*'s")) {
      return true;
    } else if (!string.contains(" ") && node.label().value().startsWith("NN")
            && isFrequentWord(string))
    // if it is a single token, and it is a noun, then we check whether it is a frequent word.
    {
      return true;
    } else if (node.label().value().startsWith("NN") && candidateContains(string))
    {
      return true;
    }

      return false;

  }

  /**
   * check with the Gigaword unigram corpus to find out whether the word is a frequent word or not.
   * 
   * @param string
   *          a noun word
   * @return true if the word count is more than or equal to 1000 in Gigaword, false otherwise.
   */
  private boolean isFrequentWord(String string) {
    int size = wordCounts.size();

    /**
     * Binary search part of the code.
     */
    int low = 0;
    int high = size - 1;
    int idx = -1;

    while (high >= low) {
      int middle = (low + high) / 2;
      if (wordCounts.get(middle).word.equalsIgnoreCase(string)) {
        idx = middle;
        break;
      }
      if (wordCounts.get(middle).word.compareTo(string.toUpperCase()) < 0) {
        low = middle + 1;
      }
      if (wordCounts.get(middle).word.compareTo(string.toUpperCase()) > 0) {
        high = middle - 1;
      }
    }

    if (idx == -1) {
      return false; // We don't filter this rare word
    } else if (wordCounts.get(idx).cnt < 1500) {
      return false; // the word count is less than 1000, we don't filter it.
    } else {
      return true;
    }
  }

  /**
   * Check if the category is what we wanted.. tree tag reference:
   * http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
   * 
   * @param label
   *          the Penn Treebank label, such as "NP"
   * @return true if the category is what we wanted, false if no.
   */
  private boolean checkCategory(String label) {
    if (label.equalsIgnoreCase("NP") || label.equalsIgnoreCase("VBP") || label.equalsIgnoreCase("VB")) {
      return true;
    } else if (label.equalsIgnoreCase("NN") || label.equalsIgnoreCase("NNS")) // lexical
    {
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
      // ignore the parenthesis..
      {
        return false;
      } else {
        sb.append(" ");
        sb.append(node.label().value());
        return result;
      }
    } else {
      if (node.label().value().equalsIgnoreCase("DT")
              || node.label().value().equalsIgnoreCase("IN") || node.label().value().equalsIgnoreCase("PRP")) {
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
