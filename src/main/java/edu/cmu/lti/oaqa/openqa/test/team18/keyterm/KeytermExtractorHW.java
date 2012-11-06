package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class KeytermExtractorHW extends AbstractKeytermExtractor {

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    int MAX_N_BEST_CHUNKS = 0;
    List<Keyterm> ktl=new LinkedList<Keyterm>();
    ConfidenceChunker chunker = null; // lingpipe unit
    double confidence = 0; // confidence for whether a word/phrase is the target
    char[] cha;
    String str;
    Keyterm kt = new Keyterm();
    int startoffset;
    int endoffset;
    cha = question.toCharArray();
    try {
      // from lingpipe
      // import the trained data, biogenetag data
      chunker = (ConfidenceChunker) AbstractExternalizable.readObject(new File(
              "./src/main/resources/ne-en-bio-genetag.HmmChunker"));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    // from lingpipe
    // for each input string, generate the five words most likely to be the target words.
    Iterator<Chunk> it = (Iterator) chunker.nBestChunks(cha, 0, cha.length, MAX_N_BEST_CHUNKS);
    while (it.hasNext()) {
      Chunk c = it.next();
      // organize the confidence for the words
      confidence = Math.pow(2.0, c.score());
      // only generate the words with the confidence greater than the threshold, i.e. 0.6
      if (confidence > 0.6) {
        startoffset=c.start();
        endoffset=c.end();
        str="";
        for (int i=startoffset;i<=endoffset;i++){
          str+=cha[i];
        }
        kt.setComponentId(str);
        ktl.add(kt);
      }
    }
    return ktl;
  }
}
