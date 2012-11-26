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
import edu.cmu.lti.oaqa.openqa.test.team18.retrieval.GoParser;

public class PassageRetrieval extends SimplePassageExtractor {

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    List<Keyterm> newK = new ArrayList<Keyterm>();
    //Search for synonyms of gene of Keyterms
    //find all sysnonyms and add to a list newK
    for (Keyterm kt : keyterms) {
      List<String> lg = new ArrayList<String>();
      try {
        GoParser gp = new GoParser("./src/main/resources/dict/synonym.xml");
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
        newK.add(newk);
      }
    }
    //search for synonyms of verbs of Keyterms
    //Add all synonyms into list newK
    for (Keyterm kt : keyterms) {
      List<String> ls = WordNetImpl.searchForSynonyms(kt.toString());
      for (String s : ls) {
        Keyterm newk = new Keyterm(s);
        newK.add(newk);
      }
    }
//    for (Keyterm kt: keyterms){
//      System.out.println(kt);
//    }
    //add the Keyterms in newK to list Keyterm
    //before add, search Keyterms to make sure all the keyterms in list Keyeterms are unique. 
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
      if (!exist) {
        keyterms.add(kt);
      }
    }
    //get the document and start to retrieve passage
    //main strategy of retrieval is from class IBMstategy
    for (RetrievalResult document : documents) {
      String id = document.getDocID();
      try {
        String htmlText = wrapper.getDocText(id);
        //clean htmeText
        String text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
        text = text.substring(0, Math.min(50000, text.length()));
        //create new finder and get the result
        IBMstrategy finder = new IBMstrategy(id, text, new KeytermWindowScorerSum());
        List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
          public String apply(Keyterm keyterm) {
            return keyterm.getText();
          }
        });
        //get the result of lists and return
        List<PassageCandidate> passageSpans = finder.extractPassages(keytermStrings
                .toArray(new String[0]));
        for (PassageCandidate passageSpan : passageSpans) {
          result.add(passageSpan);
        }
      } catch (SolrServerException e) {
        e.printStackTrace();
      }
    }
    return result;
  }

}