package edu.cmu.lti.oaqa.openqa.test.team18.passage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorer;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerProduct;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;
import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;
import edu.cmu.lti.oaqa.openqa.test.team18.passage.IBMstrategy.PassageSpan;

//import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder.PassageSpan;
/**
 * First temp of using IBM strategy in trec06 name this class for that
 * Then try to implement siteQ
 * Finally abandon this class
 * @deprecated
 * @author Haohan Wang
 * 
 */
public class IBMstrategy {
  private String text;

  private String docId;

  private int textSize; // values for the entire text

  private int totalMatches;

  private int totalKeyterms;

  private KeytermWindowScorer scorer;

  public IBMstrategy(String docId, String text, KeytermWindowScorer scorer) {
    super();
    this.text = text;
    this.docId = docId;
    this.textSize = text.length();
    this.scorer = scorer;
  }

  @SuppressWarnings("unchecked")
  public List<PassageCandidate> extractPassages(String[] keyterms) {
    List<List<PassageSpan>> matchingSpans = new ArrayList<List<PassageSpan>>();
    List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
    List<PassageSpan> pivots = new ArrayList<PassageSpan>();

    for (String keyterm : keyterms) {
      Pattern p = Pattern.compile(keyterm);
      Matcher m = p.matcher(text);
      while (m.find()) {
        PassageSpan match = new PassageSpan(m.start(), m.end());
        matchedSpans.add(match);
        totalMatches++;
      }
      if (!matchedSpans.isEmpty()) {
        matchingSpans.add(matchedSpans);
        totalKeyterms++;
      }
    }

    // create set of left edges and right edges which define possible windows.
    List<Integer> leftEdges = new ArrayList<Integer>();
    List<Integer> rightEdges = new ArrayList<Integer>();
    // generate the passages with start and end with a natural sentence
    List<PassageSpan> pspan = getPassageSentences();
    // Generate all the passagespans
    // passagespans are divided by natural sentences
    for (PassageSpan ps : pspan) {
      Integer leftEdge = ps.begin;
      Integer rightEdge = ps.end;
      if (!leftEdges.contains(leftEdge))
        leftEdges.add(leftEdge);
      if (!rightEdges.contains(rightEdge))
        rightEdges.add(rightEdge);
    }
    // Generate all the passagespans
    // passagespans are divided by keyterms
//    for ( List<PassageSpan> keytermMatches : matchingSpans ) {
//      for ( PassageSpan keytermMatch : keytermMatches ) {
//        Integer leftEdge = keytermMatch.begin;
//        Integer rightEdge = keytermMatch.end; 
//        if (! leftEdges.contains( leftEdge ))
//          leftEdges.add( leftEdge );
//        if (! rightEdges.contains( rightEdge ))
//          rightEdges.add( rightEdge );
//      }
//    }
    // For every possible window, calculate keyterms found, matches found; score window, and create
    // passage candidate.
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    for (Integer begin : leftEdges) {
      // end cannot be smaller than great
      for (Integer end : rightEdges) {
        if (end <= begin)
          continue;
        // In the retrieved passage, a window greater than 500 characters is rarely seen
        if (end - begin > 800)
          continue;
        // This code runs for each window.
        pivots.clear();
        int keytermsFound = 0;
        int matchesFound = 0;
        for (List<PassageSpan> keytermMatches : matchingSpans) {
          boolean thisKeytermFound = false;
          for (PassageSpan keytermMatch : keytermMatches) {
            if (keytermMatch.containedIn(begin, end)) {
              matchesFound++;
              thisKeytermFound = true;
              boolean find = false;
              // search for if the pivot has already stores the keyterm
              for (PassageSpan ps1 : pivots) {
                if (ps1.getBegin() == keytermMatch.getBegin()
                        && ps1.getEnd() == keytermMatch.getEnd()) {
                  find = true;
                }
              }
              // pivot stores the offset of the matched keyterm
              if (!find) {
                pivots.add(keytermMatch);
              }
            }
          }
          if (thisKeytermFound)
            keytermsFound++;
        }
//        for (PassageSpan ps: pivots){
//          System.out.println(ps.getBegin()+" "+ps.getEnd());
//        }
        double score = scorer.scoreWindow(begin, end, matchesFound, totalMatches, keytermsFound,
                totalKeyterms, textSize);
        //System.out.println(pivots.size());
        // calculate pivot score, which is named scorePivot
        // avgP is the average distance of matched keyterms in the sentence
        // avgA is the avearge distance of all the keyterms
        double avgP = 0, avgA = 0;
        // the following two lines are for the temp variables for calculate the score
        double maxP = 1, maxA = 5000;
        double totalP = 0, totalA = 0;
        // rP is the ratio of matched Keyterms
        double rP = (double) pivots.size() / (double) matchingSpans.size();
        // dist is the sum of the weight of each keyterm multiplied by the the ratio of the
        // positions of this keyterm in the answer candidate
        double dist = 0;
        // calculate for avgP and avgA
        int i, j;
        for (i=0;i<=pivots.size()-1;i++){
          for (j=i+1;j<=pivots.size()-1;j++){
            PassageSpan ps1 = pivots.get(i);
            PassageSpan ps2 = pivots.get(j);
            int begin1=ps1.getBegin();
            int begin2=ps2.getBegin();
//            int end1=ps1.getEnd();
//            int end2=ps2.getEnd();
            totalP += Math.abs(begin2 - begin1);
            if (maxP < Math.abs(begin2 - begin1)) {
              maxP = Math.abs(begin2 - begin1);
            }
            totalA += Math.abs(begin1 - begin);
            if (maxA < Math.abs(begin1 - begin)) {
              maxA = Math.abs(begin1 - begin);
            }
          }
        }

        avgP = totalP / pivots.size();
        avgA = totalA / pivots.size();
        String k = null;
        // calculate for dist
        // w is the position of this word in the Keyterm, which can reflect the weight
        int w = 0;
        for (PassageSpan ps1 : pivots) {
          k = text.substring(ps1.getBegin(), ps1.getEnd());
          for (int m = 0; m <= keyterms.length; m++) {
            if (keyterms[m].equals(k)) {
              w = m + 1;
              break;
            }
          }
//          System.out.println(k);
//          System.out.println("w"+w);
          dist += ((end-begin) / w) * (1 - (ps1.getBegin() - begin) / maxA);
        }
        double scorePivot = 0;
        // pSize is the number of all the matched keyterms for this sentence
        double pSize = pivots.size();
//        if (pSize>1){
//          System.out.println("rp"+rP);
//          System.out.println("avgP"+avgP);
//          System.out.println("totalP"+totalP);
//          System.out.println("maxP"+maxP);
//          System.out.println("dist"+dist);
//          System.out.println("pSize"+pSize);
//          System.out.println("length"+(end-begin));
//        }
        // calculate scorePivot
        scorePivot = rP * (avgP / maxP) * (1 / pSize) * dist;
        PassageCandidate window = null;
        // if scorePivot is meaningful, calculate a new score
        // otherwise, set the score to 0
//        if (pSize>1){
//          System.out.println("scorePivot:"+scorePivot);
//          System.out.println("score:"+score);
//        }
        if (dist != 0.0) {
          score = score*scorePivot*100;
        } else {
          score = 0;
        }
        // for the score with a weight greater than 0
        // generate a result
        if (score >= 0) {
          try {
            window = new PassageCandidate(docId, begin, end, (float) scorePivot, null);
          } catch (AnalysisEngineProcessException e) {
            e.printStackTrace();
          }
          result.add(window);
        }
      }
    }
    // Sort the result in order of decreasing score.
    Collections.sort(result, new PassageCandidateComparator());
    return result;

  }

  private class PassageCandidateComparator implements Comparator {
    // Ranks by score, decreasing.
    public int compare(Object o1, Object o2) {
      PassageCandidate s1 = (PassageCandidate) o1;
      PassageCandidate s2 = (PassageCandidate) o2;
      if (s1.getProbability() < s2.getProbability()) {
        return 1;
      } else if (s1.getProbability() > s2.getProbability()) {
        return -1;
      }
      return 0;
    }
  }

  class PassageSpan {
    private int begin, end;

    public int getBegin() {
      return begin;
    }

    public void setBegin(int begin) {
      this.begin = begin;
    }

    public int getEnd() {
      return end;
    }

    public void setEnd(int end) {
      this.end = end;
    }

    public PassageSpan(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }

    public boolean containedIn(int begin, int end) {
      if (begin <= this.begin && end >= this.end) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static void main(String[] args) {
    PassageCandidateFinder passageFinder1 = new PassageCandidateFinder("1",
            "The quick brown fox jumped over the quick brown fox.",
            new KeytermWindowScorerProduct());
    PassageCandidateFinder passageFinder2 = new PassageCandidateFinder("1",
            "The quick brown fox jumped over the quick brown fox.", new KeytermWindowScorerSum());
    String[] keyterms = { "quick", "jumped" };
    List<PassageCandidate> windows1 = passageFinder1.extractPassages(keyterms);
    System.out.println("Windows (product scoring): " + windows1);
    List<PassageCandidate> windows2 = passageFinder2.extractPassages(keyterms);
    System.out.println("Windows (sum scoring): " + windows2);
  }

  public List<PassageSpan> getPassageSentences() {
    List<PassageSpan> span = new ArrayList<PassageSpan>();
    int i = 0, j = 1;
    // char[] punc = { '.', '?', '!' };
    // generate the span of natural sentences
    while (j < textSize - 2) {
      if ((text.charAt(j) == '.'||text.charAt(j) == ',') && text.charAt(j + 2) >= 'A' && text.charAt(j + 2) <= 'Z') {
        PassageSpan a = new PassageSpan(i, j);
        span.add(a);
        i = j + 2;
      }
      j++;
    }
    PassageSpan a = new PassageSpan(i, textSize - 1);
    span.add(a);
    return span;
  }
}