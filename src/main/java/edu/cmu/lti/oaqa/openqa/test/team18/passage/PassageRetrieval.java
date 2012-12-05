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
import edu.cmu.lti.oaqa.openqa.test.team18.retrieval.NihParser;
import edu.cmu.lti.oaqa.openqa.test.team18.retrieval.WikiRedirectParser;

/**
 * @author Haohan Wang
 * Main class for passgage retrieval
 * Get the text and keyterms ready
 * and call other classes to generate the passage
 */

public class PassageRetrieval extends SimplePassageExtractor {

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    List<Keyterm> rkeyterms = new ArrayList<Keyterm>();
    //reverse the sequence of keyterms
    //because the sequence of keyterms passage phase can get is different from the one keyterm phase generates
    int total = keyterms.size()-1;
    for (int i=total;i>=0;i--){
      rkeyterms.add(keyterms.get(i));
    }
    //find the sysnonyms of keyterms
    List<List<String>> keytermM = getSynonyms(rkeyterms);
    //get the document and start to retrieve passage
    //main strategy of retrieval is from class IBMstategy
    for (RetrievalResult document : documents) {
      String id = document.getDocID();
      try {
        String htmlText = wrapper.getDocText(id);
        //truncate the fist at most 80000 chars of the htmltext
        htmlText = htmlText.substring(0, Math.min(80000, htmlText.length()));
        //create new finder and get the result
        SiteQwithMatrix finder = new SiteQwithMatrix (id, htmlText, new KeytermWindowScorerSum());
        //get the result of lists and return
        List<PassageCandidate> passageSpans = finder.extractPassages(keytermM);
        for (PassageCandidate passageSpan : passageSpans) {
          result.add(passageSpan);
        }
      } catch (SolrServerException e) {
        e.printStackTrace();
      }
    }
    return result;
  }
  /**
   * generate the synonyms for keyterms
   * 
   * @param keyterms
   * get a list of Keyterm, the order reflects the weight of each keyterm
   * 
   * @return a list of list of strings
   * return a matrix of strings
   * each list of string is a set of synonyms of this keyterm
   * a matrix is a list of all the sysnonyms
   * the order of the matrix is the same as the input keyterm
   */
  public List<List<String>> getSynonyms(List<Keyterm> keyterms) {
    List<List<String>> result = new ArrayList<List<String>>();
    for (Keyterm m : keyterms) {
      String k = m.toString();
      List<String> kl = new ArrayList<String>();
      List<String> nk = new ArrayList<String>();
      List<String> r = new ArrayList<String>();
      kl.add(k);
      k = k.toLowerCase();
      try {
        GoParser gp = new GoParser("src/main/resources/dict/synonym.xml");
        r = gp.findAllSynonyms(k);
        for (String s : r) {
          nk.add(s);
        }
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        NihParser np = new NihParser("src/main/resources/dict/nih.txt");
        r = np.findSynonyms(k);
        for (String s : r) {
          nk.add(s);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      WikiRedirectParser wrp = new WikiRedirectParser();
      r = wrp.findSynonyms(k); 
      for (String s : r) {
        nk.add(s);
      }
      String [] split = k.split(" ");
      String sk = null;
      if (split.length>=2){
        for (String a:split){
          sk = a;
        }
        r=wrp.findSynonyms(sk);
        for (String lsri :r){
          String f=k;
          f=f.replace(sk, lsri);
          nk.add(f);
        }
      }
      
      boolean exist = false;
      for (String s1 : nk) {
        exist = false;
        for (String s2 : kl) {
          // '\' will cause futur exception so we do not allow '\' exist in keyterms
          if (s1.equals(s2) || s1.contains("\\")) {
            exist = true;
            break;
          }
        }
        if (!exist) {
          kl.add(s1);
        }
      }
      result.add(kl);
    }
    return result;
  }
}