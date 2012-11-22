package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class AggregatedExtractor extends AbstractKeytermExtractor {

  SyntaxParsing sp;
  
  @Override
  /**
   * TBD: Need to uncomment the code: super.initialize....
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    //super.initialize(aContext);
    sp = new SyntaxParsing();
  }
  
  @Override
  protected List<Keyterm> getKeyterms(String question) {
    List<Keyterm> res = new LinkedList<Keyterm>();
    List<String> candidates = sp.getKeytermCandidates(question);
    for(String s : candidates)
    {
      res.add(new Keyterm(s));
    }
    return res;
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
        for(Keyterm kt : keyterms)
        {
          System.out.println("keyterm: " + kt.getText());
        }
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
