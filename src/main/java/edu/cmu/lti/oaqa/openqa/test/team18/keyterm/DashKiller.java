package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.util.List;
import java.util.StringTokenizer;

/**
 * It is a simple keyterm extractor unit as it only seeks words with dash inside and split them. If
 * the given question doesn't have any dash words, it returns an empty list.
 * 
 * @author Yibin Lin
 * 
 */
public class DashKiller extends KeytermCandidateFinder {

  @Override
  /**
   * get list of keyterm candidates that are only the parts of a dash word, 
   * for example, we return ["pep", "bes"] if there is a dash word "pep-bes".
   * Note that in the DashKiller we don't return the word "pep-bes" itself. 
   * It is supposed to be handled by other KeytermCandidateFinder's.
   */
  public List<String> getKeytermCandidates(String text) {
    this.clearCandidates();
    text = text.replaceAll("[0-9]+\\|", "");

    StringTokenizer st = new StringTokenizer(text);
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (token.contains("-")) {
        String[] splitted = token.split("-", 2);
        if (addable(splitted)) {
          for(String s: splitted)
          {
            this.addCandidate(s);
          }
        }
      }
    }
    return candidates;
  }

  /**
   * Test if a splitted by dash word is addable. 
   * 
   * <p>
   * 1. If the dash word looks like 
   * a gene name (typically it has many upper case letters, and digits), then 
   * we don't want to split it (in this case, return false).
   * </p>
   * 
   * <p>
   * 2. If the dash word looks like a usual word: such as after-hours, then we don't 
   * want to split (we return false). 
   * </p>
   * 
   * 
   * @param splitted
   * @return true if it is suitable to be added as a candidate
   */
  private boolean addable(String[] splitted) {
    int score = 0;
    int singleCapitalScore = 0;
    for (String s : splitted) {
      if (s.matches(".*[0-9].*")) {
        score++;
      }
      if (s.matches(".*[A-Z].*[A-Z].*")) {
        score++;
      }
      if(s.matches(".*[A-Z].*"))
      {
        singleCapitalScore++;
      }
    }
    if (score < 2 && singleCapitalScore > 1) {
      return true;
    } else {
      return false;
    }
  }

}
