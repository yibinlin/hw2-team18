package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.util.*;
import java.util.Map.Entry;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.*;

public class KeytermExtractorHW2 extends AbstractKeytermExtractor{
  private StanfordCoreNLP pipeline;

  public KeytermExtractorHW2() {
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos");
    pipeline = new StanfordCoreNLP(props);
  }

  public Map<Integer, Integer> getGeneSpans(String text) {
    Map<Integer, Integer> begin2end = new HashMap<Integer, Integer>();
    Annotation document = new Annotation(text);
    pipeline.annotate(document);
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      List<CoreLabel> candidate = new ArrayList<CoreLabel>();
      for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
        String pos = token.get(PartOfSpeechAnnotation.class);
        if (pos.startsWith("NN")) {
          candidate.add(token);
        } else if (candidate.size() > 0) {
          int begin = candidate.get(0).beginPosition();
          int end = candidate.get(candidate.size() - 1).endPosition();
          begin2end.put(begin, end);
          candidate.clear();
        }
      }
      if (candidate.size() > 0) {
        int begin = candidate.get(0).beginPosition();
        int end = candidate.get(candidate.size() - 1).endPosition();
        begin2end.put(begin, end);
        candidate.clear();
      }
    }
    return begin2end;
    // how does this work?
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    List<Keyterm> kl = new LinkedList<Keyterm>();
    Keyterm kt = new Keyterm();
    Map<Integer, Integer> bd = new HashMap<Integer, Integer>();
    bd=getGeneSpans(question);
    Iterator<Entry<Integer, Integer>> it = bd.entrySet().iterator();
    while (it.hasNext()){
      @SuppressWarnings("unchecked")
      Map.Entry<Integer, Integer> mp = (Entry<Integer, Integer>)bd;
      kt.setComponentId(question.substring(mp.getKey(),mp.getValue()));
      kl.add(kt);
    }
    return kl;
  }
}
