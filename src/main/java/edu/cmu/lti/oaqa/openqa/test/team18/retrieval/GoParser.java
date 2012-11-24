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
	
	private Map<String,Map<String,List<String>>> buildDictionary(Document doc){
		
		Element root = doc.getDocumentElement();
		
		Map<String,Map<String,List<String>>> dictionary = new HashMap<String,Map<String,List<String>>>();
		
		
		NodeList termList = doc.getElementsByTagName("term");
		for(int i = 0; i < termList.getLength(); i++){
			Element termElement = (Element) termList.item(i);
			Element nameElement = (Element) termElement.getElementsByTagName("name").item(0);
			String name = nameElement.getTextContent();
			//System.out.println(name);

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
	
	public GoParser (String filename) throws ParserConfigurationException, SAXException, IOException{
		
        DocumentBuilderFactory dfb = DocumentBuilderFactory.newInstance();  
        DocumentBuilder db = dfb.newDocumentBuilder();
        Document dom = db.parse(new File(filename));
        this.synonymDictionary = buildDictionary(dom);
	}
	
	private List<String> findSynonyms(String gene,String scope){
		
		return this.synonymDictionary.get(gene).get(scope);
		
	}
	
	public List<String> findExactSynonyms(String gene){
		
		return findSynonyms(gene, "exact");
	}
	
	public List<String> findRelatedSynonyms(String gene){
		
		return findSynonyms(gene, "related");
	}
	
	public List<String> findNarrowSynonyms(String gene){
		
		return findSynonyms(gene, "narrow");
	}
	
	public List<String> findBroadSynonyms(String gene){
		
		return findSynonyms(gene, "broad");
	}
	
	public List<String> findAllSynonyms(String gene){
		List<String> synonyms = new ArrayList<String> ();
		Map<String,List<String>> geneSynonyms = this.synonymDictionary.get(gene);
		Set<String> keys = geneSynonyms.keySet();
		for(String k:keys){
			List<String> subList = geneSynonyms.get(k);
			for(String s:subList){
				synonyms.add(s);
			}
		}
		return synonyms;
	}
	
	public Map<String,Map<String,List<String>>> getDictionary(){
		
		return this.synonymDictionary;
	}
	
	
	
}
