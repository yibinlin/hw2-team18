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
 * 
 */
public class Retrieval_Base extends AbstractRetrievalStrategist {

  protected Integer hitListSize;

  protected SolrWrapperExtend wrapper;
  
  protected GoParser goParser;
  protected NihParser nihParser;

  @Override
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
  protected final List<RetrievalResult> retrieveDocuments(String questionText,
          List<Keyterm> keyterms) {
	//System.out.println("QuestionText:"+questionText);
    String query = formulateQuery(keyterms);
    return retrieveDocuments(query);
  };
  
  protected List<String> findSynonyms(String keyword){
	  
	  return new ArrayList<String>();
  }
  
 
  
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
	  }
	  return relatedWords;
  }
  

  protected String formulateQuery(List<Keyterm> keyterms) {
    StringBuffer result = new StringBuffer();
    
    List<String> relatedWords = expandQuery(keyterms);
    
    result.append("+(");
    for(int i=0; i < keyterms.size()-1; i++){
    	result.append(keyterms.get(i).getText() + " OR ");
    }
    
    for(int i=0; i< relatedWords.size(); i++){
    	result.append(relatedWords.get(i) + " OR ");
    }
    
    result.append(keyterms.get(keyterms.size()-1).getText() + ") ");

    for(int i=0; i < keyterms.size(); i++){
    	result.append(keyterms.get(i).getText()+"^10 ");
    }
    String query = result.toString();
    System.out.println("Lucene Query:" + query);
    return query;
  }

  
  protected List<RetrievalResult> retrieveDocuments(String query) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      SolrDocumentList docs = wrapper.runQuery(query, hitListSize);
      for (SolrDocument doc : docs) {
    	
    	/*
    	System.out.println("Fields");
    	
    	Collection<String> fields = doc.getFieldNames();
    	Iterator<String> iter = fields.iterator();
    	while(iter.hasNext()){
    		String f = iter.next();
    		System.out.println(f);
    	}*/
    	  
    	
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