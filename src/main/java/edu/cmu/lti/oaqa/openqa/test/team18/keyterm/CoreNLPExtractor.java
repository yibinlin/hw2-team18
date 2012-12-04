package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;


/**
 * 
 * @deprecated No longer used in the CSE pipeline. 
 * @author Yuchen Tian
 *
 */
public class CoreNLPExtractor extends AbstractKeytermExtractor{

	@Override
	protected List<Keyterm> getKeyterms(String question) {
	
		try {
			
			PosTagNamedEntityRecognizer postagger = new PosTagNamedEntityRecognizer();
			Map<Integer, Integer> begin2end = postagger.getGeneSpans(question);
			
			List<Keyterm> li = new ArrayList<Keyterm>();
			Iterator iter = begin2end.keySet().iterator();
			while (iter.hasNext()) { 
			    int begin = (Integer) iter.next(); 
			    int end = (Integer) begin2end.get(begin);
			    String kt = question.substring(begin, end);
			    li.add(new Keyterm(kt));
			} 
			return li;			
			
		} catch (ResourceInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	
	}
	

}
