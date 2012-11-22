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

  public List<String> getKeytermCandidates(String text) {
    this.clearCandidates();
    text = text.replaceAll("[0-9]+\\|", "");
    double confidence = 0; // confidence for whether a word/phrase is the target
    char[] cha;
    StringBuffer sb = new StringBuffer();
    
    int startoffset;
    int endoffset;
    cha = text.toCharArray();
    
    Iterator<Chunk> it = (Iterator) chunker.nBestChunks(cha, 0, cha.length, MAX_N_BEST_CHUNKS);
    while (it.hasNext()) {
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
        this.addCandidate(sb.toString().trim());
      }
    }
    return candidates;
  }
}
