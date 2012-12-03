package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * The Aggregated Extractor uses both Stanford CoreNLP and 
 * LingPipe Gene Tagger. It also places the output of LingPipe Gene 
 * Tagger at the beginning of the result list.
 * 
 * It has a main method for testing, the main method is not the entry point
 * of the project.
 * 
 * @author Yibin Lin
 *
 */
public class AggregatedExtractor extends AbstractKeytermExtractor {

  SyntaxParsing sp;

  LingPipeNER lpn;
  
  DashKiller dk;

  @Override
  /**
   * initialize StanfordCoreNLP and Lingpipe by initializing the 
   * constructors of SyntaxParsing and LingPipeNER object.
   * 
   * Every time we are testing, we need to comment out the line
   * "super.initialize(...)" 
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    sp = new SyntaxParsing();
    lpn = new LingPipeNER();
    dk = new DashKiller();
  }

  @Override
  /**
   * Get Keyterms using both Stanford CoreNLP Syntax Parser and 
   * LingPipe package. 
   * 
   * Every time we are testing, we need to comment out 
   * "this.log(...)" lines because it is related to context.
   * @return a list of extracted keyterms.
   */
  protected List<Keyterm> getKeyterms(String question) {
    LinkedList<Keyterm> res = new LinkedList<Keyterm>();
    List<String> synCandidates = sp.getKeytermCandidates(question);
    // TBD
    this.log("Starting to get keyterms from StanfordCoreNLP...");
    List<String> lingpipeCandidates = lpn.getKeytermCandidates(question);
    this.log("Starting to get keyterms from Lingpipe...");
    List<String> dkCandidates = dk.getKeytermCandidates(question);
    this.log("Starting to get keyterms from Dash Killer...");
    
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
          res.addFirst(new Keyterm(s));
        }
      }
    }
    
    for (String s : dkCandidates) {
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

  /**
   * Only for internal testing purposes. It is not in the pipeline. 
   * @param args
   */
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
        //System.out.println(strLine);
        
        List<Keyterm> keyterms = ae.getKeyterms(strLine);
        
        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(strLine);
        String qid = "";
        
        if (m.find()) {
            qid = m.group();
            qid += "|1 1|";
        }
        
        for (Keyterm kt : keyterms) {
          String printout = qid;
          printout += kt;
          System.out.println(printout);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
