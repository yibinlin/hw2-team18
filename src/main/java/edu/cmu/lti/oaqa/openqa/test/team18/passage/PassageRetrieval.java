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

public class PassageRetrieval extends SimplePassageExtractor {

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    List<Keyterm> rkeyterms = new ArrayList<Keyterm>();
    int total = keyterms.size()-1;
    for (int i=total;i>=0;i--){
      rkeyterms.add(keyterms.get(i));
    }
    List<List<String>> keytermM = getSynonyms(rkeyterms);
    //find the sysnonyms of keyterms
    
    
    //keyterms=findSynonyms(keyterms);
    
    
    //get the document and start to retrieve passage
    //main strategy of retrieval is from class IBMstategy
    for (RetrievalResult document : documents) {
      String id = document.getDocID();
      try {
        String htmlText = wrapper.getDocText(id);
        //clean htmeText
        //String text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
        htmlText = htmlText.substring(0, Math.min(80000, htmlText.length()));
        //create new finder and get the result
        //IBMstrategy finder = new IBMstrategy(id, text, new KeytermWindowScorerSum());
        SiteQwithMatrix finder = new SiteQwithMatrix (id, htmlText, new KeytermWindowScorerSum());
        List<String> keytermStrings = Lists.transform(rkeyterms, new Function<Keyterm, String>() {
          public String apply(Keyterm keyterm) {
            return keyterm.getText();
          }
        });
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

//  public List<Keyterm> findSynonyms(List<Keyterm> keyterms){
//    List<Keyterm> newK = new ArrayList<Keyterm>();
//    //Search for synonyms of gene of Keyterms
//    //find all sysnonyms and add to a list newK
//    for (Keyterm kt : keyterms) {
//      List<String> lg = new ArrayList<String>();
//      try {
//        GoParser gp = new GoParser("src/main/resources/dict/synonym.xml");
//        lg = gp.findAllSynonyms(kt.toString());
//      } catch (ParserConfigurationException e) {
//        e.printStackTrace();
//      } catch (SAXException e) {
//        e.printStackTrace();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//      for (String s : lg) {
//        Keyterm newk = new Keyterm(s);
//        newK.add(newk);
//      }
//    }
//    for (Keyterm kt : keyterms) {
//      List<String> ln = new ArrayList<String>();
//      try {
//        NihParser np= new NihParser("src/main/resources/dict/nih.txt");
//        ln=np.findSynonyms(kt.toString());
//      } catch (IOException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//      }
//      for (String s : ln) {
//        Keyterm newk = new Keyterm(s);
//        newK.add(newk);
//      }
//    }
//    //search for synonyms of verbs of Keyterms
//    //Add all synonyms into list newK
////    for (Keyterm kt : keyterms) {
////      List<String> ls = WordNetImpl.searchForSynonyms(kt.toString());
////      for (String s : ls) {
////        Keyterm newk = new Keyterm(s);
////        newK.add(newk);
////      }
////    }
//
//    //add the Keyterms in newK to list Keyterm
//    //before add, search Keyterms to make sure all the keyterms in list Keyeterms are unique. 
//    boolean exist = false;
//    for (Keyterm kt : newK) {
//      exist = false;
//      String s1 = kt.getText();
//      String s2 = null;
//      for (Keyterm kt2 : keyterms) {
//        s2 = kt2.getText();
//        if (s1.equals(s2)||s1.contains("\\")) {
//          exist = true;
//          break;
//        }
//      }
//      if (!exist) {
//        keyterms.add(kt);
//      }
//    }
//    return keyterms;
//  }
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
      boolean exist = false;
      for (String s1 : nk) {
        exist = false;
        for (String s2 : kl) {
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