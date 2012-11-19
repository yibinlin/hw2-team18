package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class AggregatedExtractor extends AbstractKeytermExtractor {

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    // TODO Auto-generated method stub
    return null;
  }

  public static void main(String[] args) {
    AggregatedExtractor ae = new AggregatedExtractor();
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
          System.out.println(kt.getText());
        }
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
