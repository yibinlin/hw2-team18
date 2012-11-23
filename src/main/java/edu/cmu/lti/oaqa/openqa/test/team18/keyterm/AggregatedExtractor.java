package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class AggregatedExtractor extends AbstractKeytermExtractor {

  SyntaxParsing sp;

  LingPipeNER lpn;

  @Override
  /**
   * TBD: Need to uncomment the code: super.initialize....
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    // super.initialize(aContext);
    sp = new SyntaxParsing();
    lpn = new LingPipeNER();
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    List<Keyterm> res = new LinkedList<Keyterm>();
    List<String> synCandidates = sp.getKeytermCandidates(question);
    // TBD
    // this.log("Starting to get keyterms from StanfordCoreNLP...");
    List<String> lingpipeCandidates = lpn.getKeytermCandidates(question);
    // this.log("Starting to get keyterms from Lingpipe...");

    // System.out.println(synCandidates);
    // System.out.println(lingpipeCandidates);

    for (String s : synCandidates) {
      if (question.contains(s)) {
        res.add(new Keyterm(s));
      }
    }

    for (String s : lingpipeCandidates) {
      if (question.contains(s)) {
        if (!findKeyterm(res, s)) {
          res.add(new Keyterm(s));
        }
      }
    }
    return res;
  }

  /**
   * Find whether a string is already included in the keyterm list.
   * 
   * @param l
   *          keyterm list
   * @param s
   *          a query string
   * @return true if the string is already included, false otherwise.
   */
  private boolean findKeyterm(List<Keyterm> l, String s) {
    for (Keyterm k : l) {
      if (k.getText().equalsIgnoreCase(s)) {
        // System.out.println(k.getText());
        // System.out.println(s);
        return true;
      }
    }
    return false;
  }

  public static void main(String[] args) {
    AggregatedExtractor ae = new AggregatedExtractor();
    try {
      ae.initialize(null);
    } catch (ResourceInitializationException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    FileReader fr;
    try {
      String path = "src/main/resources/input/back.txt";
      fr = new FileReader(path);
      BufferedReader br = new BufferedReader(fr);
      String strLine;
      // Read File Line By Line
      while ((strLine = br.readLine()) != null) {
        // Print the content on the console
        System.out.println(strLine);
        List<Keyterm> keyterms = ae.getKeyterms(strLine);
        for (Keyterm kt : keyterms) {
          System.out.println("keyterm: " + kt.getText());
        }
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
