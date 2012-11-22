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

  List<String> candidates;

  public KeytermCandidateFinder() {
    candidates = new LinkedList<String>();
  }

  synchronized void addCandidate(String phrase) {
    candidates.add(phrase);
  }

  synchronized void clearCandidates() {
    candidates.clear();
  }
  
  public abstract List<String> getKeytermCandidates(String text);
}
