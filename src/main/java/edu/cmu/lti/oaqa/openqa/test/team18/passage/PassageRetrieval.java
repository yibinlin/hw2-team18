package edu.cmu.lti.oaqa.openqa.test.team18.passage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.jsoup.Jsoup;
import org.xml.sax.SAXException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerProduct;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;
import edu.cmu.lti.oaqa.openqa.test.team18.GoParser;

public class PassageRetrieval extends SimplePassageExtractor {

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    // System.out.print("question?"+question);
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    List<Keyterm> newK = new ArrayList<Keyterm>();
    for (Keyterm kt : keyterms) {
      // System.out.println(kt.getText());

      List<String> lg = new ArrayList<String>();
      try {
        GoParser gp = new GoParser("./src/main/resources/synonym.xml");
        lg = gp.findAllSynonyms(kt.toString());
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      for (String s : lg) {
        Keyterm newk = new Keyterm(s);
        // System.out.println(newk);
        newK.add(newk);
      }
    }
    for (Keyterm kt : keyterms) {
      List<String> ls = WordNetImpl.searchForSynonyms(kt.toString());
      for (String s : ls) {
        Keyterm newk = new Keyterm(s);
        // System.out.println(newk);
        newK.add(newk);
      }
    }
    boolean exist = false;
    for (Keyterm kt : newK) {
      exist = false;
      String s1 = kt.getText();
      String s2 = null;
      for (Keyterm kt2 : keyterms) {
        s2 = kt2.getText();
        if (s1.equals(s2)) {
          exist = true;
          break;
        }
      }
      // if (kt.toString()=="be"){
      // exist=true;
      // }
      if (!exist) {
        keyterms.add(kt);
      }
    }
//    for (Keyterm s : keyterms){
//      System.out.println(s);
//    }
    for (RetrievalResult document : documents) {
      // System.out.println("RetrievalResult: " + document.toString());
      String id = document.getDocID();
      try {
        String htmlText = wrapper.getDocText(id);

        // cleaning HTML text
        String text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
        // for now, making sure the text isn't too long
        text = text.substring(0, Math.min(50000, text.length()));
        
        IBMstrategy finder = new IBMstrategy(id, text, new KeytermWindowScorerSum());
        List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
          public String apply(Keyterm keyterm) {
            return keyterm.getText();
          }
        });
        List<PassageCandidate> passageSpans = finder.extractPassages(keytermStrings
                .toArray(new String[0]));
        for (PassageCandidate passageSpan : passageSpans) {
          // System.out.println(passageSpan.getDocID()+" "+passageSpan.getStart()+" "+passageSpan.getEnd());
          result.add(passageSpan);
        }
      } catch (SolrServerException e) {
        e.printStackTrace();
      }
    }
    // System.out.println(result);
    return result;
  }

}