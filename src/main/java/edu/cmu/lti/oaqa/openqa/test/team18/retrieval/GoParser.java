package edu.cmu.lti.oaqa.openqa.test.team18.retrieval;

import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.xml.*;
import javax.xml.parsers.*;

import org.w3c.dom.Document;  
import org.w3c.dom.Element;  
import org.w3c.dom.NamedNodeMap;  
import org.w3c.dom.Node;  
import org.w3c.dom.NodeList;  
import org.xml.sax.SAXException;




public class GoParser {
	
	
	

	private Map<String,Map<String,List<String>>> synonymDictionary;
	private Set<String> termSet;
	
	

	 /**
	   * Building Ditionary from synonym.xml. This dictionary describes the following concept: Mapping a gene-related term
	   * to four sets, one for exact matches, one for a broad term that covers the given term, one for narrow terms that 
	   * are contained under the given term, and one for terms that are related to the given one.
	   * 
	   * @param Document
	   *          Constructed from synonyms.xml
	   * @return Return the constructed dictionary.
	   */
	private Map<String,Map<String,List<String>>> buildDictionary(Document doc){
		
		Element root = doc.getDocumentElement();
		
		
		Map<String,Map<String,List<String>>> dictionary = new HashMap<String,Map<String,List<String>>>();
		this.termSet = new TreeSet<String>();
		
		NodeList termList = doc.getElementsByTagName("term");
		for(int i = 0; i < termList.getLength(); i++){
			Element termElement = (Element) termList.item(i);
			Element nameElement = (Element) termElement.getElementsByTagName("name").item(0);
			String name = nameElement.getTextContent();
			this.termSet.add(name);

			Map<String,List<String>> synonymGroup = new HashMap<String,List<String>>();
			synonymGroup.put("exact", new ArrayList<String>());
			synonymGroup.put("related", new ArrayList<String>());
			synonymGroup.put("broad", new ArrayList<String>());
			synonymGroup.put("narrow", new ArrayList<String>());
			
			NodeList synonyms = termElement.getElementsByTagName("synonym");
			for(int j=0; j<synonyms.getLength(); j++){
				Element synonymElement = (Element) synonyms.item(j);
				String synonym = synonymElement.getTextContent();
				String scope = synonymElement.getAttribute("scope");
				//System.out.println(scope);
				List<String> synonymList = (List<String>) synonymGroup.get(scope);
				
				//System.out.println(synonym);
				
				synonymList.add(synonym);
			}
			
			dictionary.put(name,synonymGroup);		
			
		}
		
		return dictionary;
	}
	
	 /**
	   * Constructor function 
	   * @param Filename
	   *          filepath that points to the synonyms.xml
	   * @return Return the constructed dictionary using buildDictionary.
	   */
	
	public GoParser (String filename) throws ParserConfigurationException, SAXException, IOException{
		
        DocumentBuilderFactory dfb = DocumentBuilderFactory.newInstance();  
        DocumentBuilder db = dfb.newDocumentBuilder();
        Document dom = db.parse(new File(filename));
        this.synonymDictionary = buildDictionary(dom);
	}
	
	
	 /**
	   * Find synonyms given a scope 
	   * @param Gene,Scope
	   *          Gene specifies the term you are up to, and scope specifies the degree of coverge.
	   * @return Return the synonyms in a List. If nothing find, return empty list
	   */

	private List<String> findSynonyms(String gene,String scope){
		
		List<String> li = new ArrayList<String> ();
		
		if (this.termSet.contains(gene)){
			for (String str:this.synonymDictionary.get(gene).get(scope)){
				li.add(str);
			};
		}
		return li;
	
	}
	
	 /**
	   * Find synonyms that exact matches the term 
	   * @param Gene
	   *          Gene specifies the term you are up to.
	   * @return Return the synonyms in a List. If nothing find, return empty list
	   */

	public List<String> findExactSynonyms(String gene){
		
		return findSynonyms(gene, "exact");
	}
	
	 /**
	   * Find the related terms to the given term 
	   * @param Gene
	   *          Gene specifies the term you are up to.
	   * @return Return the synonyms in a List. If nothing find, return empty list
	   */

	public List<String> findRelatedSynonyms(String gene){
		
		return findSynonyms(gene, "related");
	}
	
	 /**
	   * Find terms that are covered by the given term 
	   * @param Gene
	   *          Gene specifies the term you are up to.
	   * @return Return the synonyms in a List. If nothing find, return empty list
	   */
	
	public List<String> findNarrowSynonyms(String gene){
		
		return findSynonyms(gene, "narrow");
	}
	
	
	 /**
	   * Find terms that covers the given term. 
	   * @param Gene
	   *          Gene specifies the term you are up to.
	   * @return Return the synonyms in a List. If nothing find, return empty list
	   */

	public List<String> findBroadSynonyms(String gene){
		
		return findSynonyms(gene, "broad");
	}
	
	
	 /**
	   * Find all the synonyms regardless of scope
	   * @param Gene
	   *          Gene specifies the term you are up to.
	   * @return Return the synonyms in a List. If nothing find, return empty list
	   */
	public List<String> findAllSynonyms(String gene){
		List<String> synonyms = new ArrayList<String> ();
		if (this.termSet.contains(gene)){
			Map<String,List<String>> geneSynonyms = this.synonymDictionary.get(gene);
			Set<String> keys = geneSynonyms.keySet();
			for(String k:keys){
				List<String> subList = geneSynonyms.get(k);
				for(String s:subList){
					synonyms.add(s);
				}
			}
		}
		return synonyms;
	}
	
	
	 /**
	   * Find all the synonyms regardless of scope
	   * @param Gene
	   *          Gene specifies the term you are up to.
	   * @return Return the synonyms in a List. If nothing find, return empty list
	   */

	public List<String> exhaustedSearchSynonyms(String gene){
		for(String term:this.termSet){
			if (term.contains(gene)){
				return findAllSynonyms(term);
			}
		}
		return new ArrayList<String> ();
	}
	

	 /**
	   * Return the dictionary
	   * @param None
	   * @return Return dictionary
	   */
	public Map<String,Map<String,List<String>>> getDictionary(){
		
		return this.synonymDictionary;
	}
	
	
	
}
