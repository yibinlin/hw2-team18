package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class finds keyterm candidates using LingPipe HMM
 * Gene tagger. It means that it only finds gene mentions. 
 * It doesn't find other nouns or verbs or adjectives.
 * 
 * @author Yibin Lin
 *
 */
public class LingPipeNER extends KeytermCandidateFinder{

  int MAX_N_BEST_CHUNKS = 10;

  ConfidenceChunker chunker; // lingpipe unit

  public LingPipeNER() {
    super();
    try {
      // from lingpipe
      // import the trained data, biogenetag data
      chunker = (ConfidenceChunker) AbstractExternalizable.readObject(new File(
              "src/main/resources/model/ne-en-bio-genetag.HmmChunker"));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * @override
   * It uses HMM Chunker of the LingPipe to get gene mentions.
   * With confidence > 0.6, we think that it is a gene mention.
   * 
   */
  public List<String> getKeytermCandidates(String text) {
    this.clearCandidates();
    text = text.replaceAll("[0-9]+\\|", "");
    double confidence = 0; // confidence for whether a word/phrase is the target
    char[] cha;
    
    int startoffset;
    int endoffset;
    cha = text.toCharArray();
    
    Iterator<Chunk> it = (Iterator) chunker.nBestChunks(cha, 0, cha.length, MAX_N_BEST_CHUNKS);
    while (it.hasNext()) {
      StringBuffer sb = new StringBuffer();
      Chunk c = it.next();
      // organize the confidence for the words
      confidence = Math.pow(2.0, c.score());
      // only generate the words with the confidence greater than the threshold, i.e. 0.6
      if (confidence > 0.6) {
        startoffset = c.start();
        endoffset = c.end();
        for (int i = startoffset; i <= endoffset; i++) {
          sb.append(cha[i]);
        }
        String cnddt = sb.toString().trim();
        if(!filterCandidate(cnddt))
        {
          this.addCandidate(cnddt);
        }
      }
    }
    return candidates;
  }
  
  /**
   * filter candidate string, it will return true if and only if:
   * 1. The string contains any parenthesis
   * 2. The string starts with a special character.
   * 3. The string ends with a special character. 
   * 
   * @param string the string candidate
   * @return true if the string is better considered to be filtered.
   */
  private boolean filterCandidate(String string) {
    if(string.contains("(") || string.contains(")")) //filter left/right parenthesis..
    {
      return true;
    }
    else if(!string.matches(".*[a-zA-Z0-9]")) //if ends with a special character, we filter it..
    {
      return true;
    }
    else if(!string.matches("[a-zA-Z0-9].*")) //if starts with a special character, we filter it..
    {
      return true;
    }
    
    return false;
      
  }
}
