package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.util.LinkedList;
import java.util.List;

/**
 * This abstract class is corresponding to the concept of finding keyterm candidates (but 
 * not the final answer set). Therefore, it has a List of candidates, and some methods to 
 * add a new keyterm candiate, and clear the list.
 * 
 * @author Yibin Lin
 *
 */
public abstract class KeytermCandidateFinder {

  /**
   * possible keyterms as String objects.
   */
  List<String> candidates;

  /**
   * constructor
   */
  public KeytermCandidateFinder() {
    candidates = new LinkedList<String>();
  }
  
  /**
   * This synchronized method adds String keyterm candidates to 
   * the head of the candidate list. But it is not currently used.
   * 
   * @param phrase one keyterm candidate in String object format.
   */
  synchronized void addCandidateToHead(String phrase) {
    ((LinkedList<String>)candidates).addFirst(phrase);
  }

  /**
   * This synchronized method adds String keyterm candidates to 
   * the end of the candidate list. 
   * 
   * @param phrase one keyterm candidate in String object format.
   */
  synchronized void addCandidate(String phrase) {
    candidates.add(phrase);
  }

  /**
   * This synchronized method clears the candidate list.
   */
  synchronized void clearCandidates() {
    candidates.clear();
  }
  
  /**
   * check is the current candidate already contains (include) a string s as the substring/itself
   * @param s the string
   * @return true if so.
   */
  boolean candidateContains(String s)
  {
    for (String c : candidates)
    {
      if (c.contains(s))
        return true;
    }
    return false;
  }
  
  /**
   * An interface that are implemented by the subclasses. The AggregatedExtractor will call
   * this method to get the String list of keyterm candidates and then try to convert them to 
   * Keyterm objects.  
   * 
   * @param text typically the question
   * @return a list of keyterm candidates
   */
  public abstract List<String> getKeytermCandidates(String text);
}
