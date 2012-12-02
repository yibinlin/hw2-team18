package edu.cmu.lti.oaqa.openqa.test.team18.retrieval;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import java.util.List;

public class testGo {
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException{
		
		NihParser nih = new NihParser("src/main/resources/dict/nih.txt");
		List<String> s = nih.findSynonyms("");
		
		for(String str:s){
			System.out.println(str);
		}
		
		System.out.println(s.size());
		
	}

}
