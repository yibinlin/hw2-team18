/*
 *  Copyright 2012 Carnegie Mellon University
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
import org.xml.sax.SAXException;

/**
 * 
 * @author Zi Yang <ziy@cs.cmu.edu>
 * @author Yuchen Tian <yuchent@cmu.edu>
 * 
 */
public class Retrieval_Base extends AbstractRetrievalStrategist {

  protected Integer hitListSize;

  protected SolrWrapperExtend wrapper;
  
  protected GoParser goParser;
  protected NihParser nihParser;
  protected WikiRedirectParser wikiParser;
  private List<Keyterm> keyterms;

  @Override
	 /**
	   * Initialize parsers
	   *
	   * @return nothing
	   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
    } catch (ClassCastException e) { // all cross-opts are strings?
      this.hitListSize = Integer.parseInt((String) aContext
              .getConfigParameterValue("hit-list-size"));
    }
    try {
		this.goParser = new GoParser("src/main/resources/dict/synonym.xml");
		System.out.println("Go Parser loaded~");
	} catch (ParserConfigurationException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (SAXException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    try {
		this.nihParser = new NihParser("src/main/resources/dict/nih.txt");
		System.out.println("Nih Parser loaded~");
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    this.wikiParser = new WikiRedirectParser();
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");
    try {
      this.wrapper = new SolrWrapperExtend(serverUrl, serverPort, embedded, core);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override

	 /**
	   * Wrapper for retrieval documents,given keyterm and question
	   * @param List of keyterms,and question
	   * @return Return list of retrieved documents
	   */
  protected final List<RetrievalResult> retrieveDocuments(String questionText,
          List<Keyterm> keyterms) {
	//System.out.println("QuestionText:"+questionText);
    String query = formulateQuery(keyterms);
    this.keyterms = keyterms;
    return retrieveDocuments(query);
  };
  

     /**
	   * Expand user query using two dictionaries, NIH dictionary and GO database.
	   * @param List of Keyterms
	   * @return Return the synonyms in a List. If nothing find, return empty list
	   */
  protected List<String> expandQuery(List<Keyterm> keyterms){
	  List<String> relatedWords = new ArrayList<String> ();
	  for(Keyterm key:keyterms){
		  String word = key.getText();
		  List<String> goList = this.goParser.findAllSynonyms(word);
		  for(String rw:goList){
			  relatedWords.add(rw);
		  }
		  List<String> nihList = this.nihParser.findSynonyms(word);
		  for(String rw:nihList){
			  relatedWords.add(rw);
		  }
		  List<String> wikiList = this.wikiParser.findSynonyms(word);
		  int wikisize = 0;
		  if (wikiList.size() > 2){
			  wikisize = 2;
		  }
		  else{
			  wikisize = wikiList.size();
		  }
		  for(int i=0;i<wikisize;i++){
			  //relatedWords.add(wikiList.get(i));
		  }
	  }
	  return relatedWords;
  }
  
  /**
	   * Check whether a phrase has two or more words
	   * @param Phrase
	   * @return Return true if the phrase has more than one word, else false.
	   */
  private Boolean hasTwoMoreWords(String line){
    String text = line;
  	String[] words = text.split(" ");
  	if(words.length > 2) return true;
  	else return false;
  }
  
  private List<String> breakKeyterms(List<Keyterm> keyterms){
	  List<String> terms = new ArrayList<String> ();
	  for(Keyterm k:keyterms){
		  String text = k.getText();
		  String[] ts = text.split(" ");
		  for(int i=0; i < ts.length ; i++){
			  terms.add(ts[i].toLowerCase());
		  }
	  }
	  return terms;
  }
  
  /**
	   * Put the keyterms into the following format: +(keyterm1 keyterm2 keyterm3 ... relatedWord1 relatedWord2...) phrase1~1000 phrase2~1000
	   * @param List of Keyterms
	   * @return Return formulated query.
	   */
  protected String formulateQuery(List<Keyterm> keyterms) {
    StringBuffer result = new StringBuffer();
    
    List<String> relatedWords = expandQuery(keyterms);
    
    
    result.append("+(");
    for(int i=0; i < keyterms.size()-1; i++){
    	if(hasTwoMoreWords(keyterms.get(i).getText())){
    		result.append("\"" + keyterms.get(i).getText() + "\" ");
    	}
    	else{
    		result.append("" + keyterms.get(i).getText() + " ");
    	}
    }
    
    for(int i=0; i< relatedWords.size(); i++){
    	
    	String rw = relatedWords.get(i);
    	if(hasTwoMoreWords(rw)){
    		result.append("\"" + rw + "\" ");
    	}
    	else{
    		result.append("" + relatedWords.get(i) + " ");
    	}

    }
    if(hasTwoMoreWords(keyterms.get(keyterms.size()-1).getText())){
    	result.append("\"" + keyterms.get(keyterms.size()-1).getText() + "\") ");
    }
    else{
    	result.append(keyterms.get(keyterms.size()-1).getText() + ") ");
    }

    for(int i=0; i < keyterms.size(); i++){
    	if(hasTwoMoreWords(keyterms.get(i).getText())){
    		result.append("\""+keyterms.get(i).getText()+"\"~1500 ");
    	}
    }
    String query = result.toString();
    System.out.println("Lucene Query:" + query);
    return query.toLowerCase();
  }

  /**
	   * Run query from remote solr server
	   * @param Query
	   * @return List of Retrieval Results Class
	   */
  protected List<RetrievalResult> retrieveDocuments(String query) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      SolrDocumentList docs = wrapper.runQuery(query, hitListSize);
      List<String> queryTerms = breakKeyterms(this.keyterms);
      //OkapiBM25 ok = new OkapiBM25(docs);
      //return ok.BM25Rank(queryTerms, 1.5, 0.75, query);
      
      for (SolrDocument doc : docs) {
    	
    	
    	System.out.println("Fields");
    	
    	Collection<String> fields = doc.getFieldNames();
    	Iterator<String> iter = fields.iterator();
    	while(iter.hasNext()){
    		String f = iter.next();
    		System.out.println(f);
    	}
    	  
    	
    	RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
                (Float) doc.getFieldValue("score"), query);
        result.add(r);
        System.out.println(doc.getFieldValue("id"));
      }
    } catch (Exception e) {
      System.err.println("Error retrieving documents from Solr: " + e);
    }
    return result;
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    wrapper.close();
  }
}