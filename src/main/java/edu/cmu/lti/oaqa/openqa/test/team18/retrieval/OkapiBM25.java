package edu.cmu.lti.oaqa.openqa.test.team18.retrieval;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

//import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.index.Term;
import org.jsoup.Jsoup;
import org.xml.sax.SAXException;

public class OkapiBM25 {

  public static List sortByValue(final Map m) {
        List keys = new ArrayList();
        keys.addAll(m.keySet());
        Collections.sort(keys, new Comparator() {
            public int compare(Object o1, Object o2) {
                Object v1 = m.get(o1);
                Object v2 = m.get(o2);
                if (v1 == null) {
                    return (v2 == null) ? 0 : 1;
                }
                else if (v1 instanceof Comparable) {
                    return ((Comparable) v1).compareTo(v2);
                }
                else {
                    return 0;
                }
            }
        });
        return keys;
    }

  private class MetaDocument{
    public Map<String,Integer> termFrequency;
    public int length;
    public SolrDocument doc;

    public MetaDocument(String rawText,SolrDocument d){

      this.termFrequency = new HashMap<String,Integer>();
      this.doc = d;
      this.length = 0;
      String text = Jsoup.parse(rawText).text().replaceAll("([\177-\377\0-\32]*)", "");
      //System.out.println(text);

      StringTokenizer st = new StringTokenizer(text);
      while(st.hasMoreTokens()){
        String word = st.nextToken();
        if (this.termFrequency.containsKey(word)){
          this.termFrequency.put(word.toLowerCase(), this.termFrequency.get(word) + 1);
        }
        else{
          this.termFrequency.put(word.toLowerCase(), 0);
        }
        this.length += 1;
      }
    }

    public boolean hasWord(String word){
      return this.termFrequency.containsKey(word.toLowerCase());
    }
  }

  private List<MetaDocument> docs;

  private Map<String,Double> getTermIDF(List<String> words){
    int N = this.docs.size();
    Map<String,Double> idf = new HashMap<String,Double>();
    for(String w:words){
      int occur = 0;
      for(MetaDocument d:this.docs){
        if(d.hasWord(w)){
          occur += 1;
        }
      }
      idf.put(w, Math.log((N+0.5)/(occur+0.5)));
    }
    return idf;
  }

  public OkapiBM25(SolrDocumentList docs){

    this.docs = new ArrayList<MetaDocument>();
    for(SolrDocument doc:docs){
      ArrayList<String> rawText = (ArrayList<String>) doc.getFieldValue("text");
      this.docs.add(new MetaDocument(rawText.get(0), doc));

    }
  }


  public List<RetrievalResult> BM25Rank(List<String> queryTerms,double k,double b,String query){
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    Map<MetaDocument,Double> rank = new HashMap<MetaDocument,Double>();
    Map<String,Double> idf = getTermIDF(queryTerms);
    Double avgdl = 0.0;
    for(MetaDocument d:this.docs){
      avgdl += d.length;
    }
    avgdl = avgdl/this.docs.size();

    for(MetaDocument d:this.docs){
      double score = 0.0;
      for(String q:queryTerms){
        if(d.hasWord(q)){
           int tf = d.termFrequency.get(q);
           System.out.println(q);
           System.out.println("tf " + tf);
           System.out.println("idf " + idf.get(q)*(tf*(k+1)));
           System.out.println("denomiator:"+(tf + k*(1-b+b*((double) d.length)/(avgdl))));
           double incre = (double) idf.get(q) * (tf*(k+1))/(tf + k*(1-b+b*((double) d.length)/(avgdl)));
           System.out.println("incre:"+incre);
           System.out.println("final:"+(score+incre));
           score = score + incre;
        }
      double rawscore =  (Float) d.doc.getFieldValue("score");
      score = Math.sqrt(score*rawscore);
      rank.put(d, score);
      System.out.println("score " + score);
      }     
    }
    List rankedDocs = sortByValue(rank);
    for(int i = 0; i < 10; i++){
      Object rd = rankedDocs.get(i);
      MetaDocument d = (MetaDocument) rd;
      RetrievalResult r = new RetrievalResult((String) d.doc.getFieldValue("id"),
                  (Float) d.doc.getFieldValue("score"), query);
      result.add(r);
    }
    return result;
  }


}