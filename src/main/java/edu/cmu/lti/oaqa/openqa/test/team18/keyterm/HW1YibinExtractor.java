package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * 
 * @deprecated no longer used in our CSE pipeline
 * @author Yibin Lin
 *
 */
public class HW1YibinExtractor extends AbstractKeytermExtractor {

  /**
   * instance of Chunker object from LingPipe.
   */
  Chunker chunker;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      chunker = (Chunker) AbstractExternalizable.readObject(new File(
              "src/main/resources/model/ne-en-bio-genetag.HmmChunker"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  /**
   * As is for HW1, I used Lingpipe to find the gene names for 
   * the questions.
   * Last modified: Nov. 5, 2012
   */
  protected List<Keyterm> getKeyterms(String question) {
    Chunking chunking = chunker.chunk(question);
    Set<Chunk> chunkRes = chunking.chunkSet();
    List<Keyterm> keyterms = new ArrayList<Keyterm>();

    for (Chunk c : chunkRes) {
      // filtering
      String possibleGene = question.substring(c.start(), c.end());
      if (possibleGene.length() == 1 && !possibleGene.equals(possibleGene.toUpperCase()))
        continue;

      keyterms.add(new Keyterm(possibleGene));
    }
    
    return keyterms;
  }
}
