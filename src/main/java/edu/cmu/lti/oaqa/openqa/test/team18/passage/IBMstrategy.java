package edu.cmu.lti.oaqa.openqa.test.team18.passage;

import java.util.ArrayList;
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

  public List<PassageCandidate> extractPassages(String[] keyterms) {
    List<List<PassageSpan>> matchingSpans = new ArrayList<List<PassageSpan>>();
    List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
    List<PassageSpan> pivots = new ArrayList<PassageSpan>();
    // List<Integer> weightP = new ArrayList<Integer>();

    // Find all keyterm matches.
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
    List<PassageSpan> pspan = getPassageSentences();
    // Iterator<PassageSpan> it = pspan.iterator();
    // while (it.hasNext()){
    // PassageSpan a = it.next();
    // System.out.println("wo shi span de jieguo:"+a.getBegin()+" "+a.getEnd());
    // }
    //
    for (PassageSpan ps : pspan) {
      Integer leftEdge = ps.begin;
      Integer rightEdge = ps.end;
      if (!leftEdges.contains(leftEdge))
        leftEdges.add(leftEdge);
      if (!rightEdges.contains(rightEdge))
        rightEdges.add(rightEdge);
    }

    // For every possible window, calculate keyterms found, matches found; score window, and create
    // passage candidate.
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    for (Integer begin : leftEdges) {
      for (Integer end : rightEdges) {
        if (end <= begin)
          continue;
        if (end-begin>500)
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
              boolean find=false;
              for (PassageSpan ps1 : pivots) {
                if (ps1.getBegin() == keytermMatch.getBegin() && ps1.getEnd() == keytermMatch
                        .getEnd()) {
                  find=true;
                }
              }
              if (!find){
                pivots.add(keytermMatch);
              }

            }
          }
          if (thisKeytermFound)
            keytermsFound++;
        }
        double score = scorer.scoreWindow(begin, end, matchesFound, totalMatches, keytermsFound,
                totalKeyterms, textSize);

        // pivot score
        double avgP = 0, avgA = 0;
        double maxP = 0, maxA = 0;
        double totalP = 0, totalA = 0;
        double rP = (double)pivots.size() / (double)matchingSpans.size();
        double dist = 0;
        // if (pivots.size()>100){
        // System.out.println(pivots.size());
        // }
        for (PassageSpan ps1 : pivots) {
          for (PassageSpan ps2 : pivots) {
            if (ps1 != ps2) {
              totalP += Math.abs(ps2.getBegin() - ps1.getBegin());
              if (maxP < Math.abs(ps2.getBegin() - ps1.getBegin())) {
                maxP = Math.abs(ps2.getBegin() - ps1.getBegin());
              }
              // System.out.println("I am running");
            }
          }
          totalA += Math.abs(ps1.getBegin() - begin);
          if (maxA < Math.abs(ps1.getBegin() - begin)) {
            maxA = Math.abs(ps1.getBegin() - begin);
          }
        }
        avgP = totalP / pivots.size();
        avgA = totalA / pivots.size();
        String k = null;
        int w = 0;
//        for (PassageSpan ps1 : pivots) {
//          System.out.println("what!"+ps1.getBegin() + " " + ps1.getEnd());
//        }
        for (PassageSpan ps1 : pivots) {
          k = text.substring(ps1.getBegin(), ps1.getEnd());
//          System.out.println(k);
//          for (String a : keyterms) {
//            System.out.print(a + " ");
//          }

          // Keyterm nk = new Keyterm(k);
          for (int i = 0; i <= keyterms.length; i++) {
            if (keyterms[i].equals(k)) {
              w = i+1;
              break;
            }
          }
          dist += (250 / w) * (1 - (ps1.getBegin() - begin) / maxA);
        }
       // System.out.println("dist"+dist);
        double scorePivot = 0;
        double pSize = pivots.size();
//        System.out.println(avgP);
//        System.out.println(totalP);
//        System.out.println(rP);
//        System.out.println(pSize);
//        System.out.println("--------------");
        scorePivot = rP * (avgP / totalP) * (1 / pSize) * dist;
        //System.out.println("scorePivot"+scorePivot);
        // end of pivot score

        PassageCandidate window = null;
        if (dist!=0.0){
          score=score*(scorePivot+1000);
        }
        else{
          score=0;
        }
//        if (scorePivot <1) {
//          score = 0;
//        }
        
        //System.out.println(score);
        if (score >= 0) {
          // score=0;
          try {
            window = new PassageCandidate(docId, begin, end, (float) score, null);

          } catch (AnalysisEngineProcessException e) {
            e.printStackTrace();
          }
          result.add(window);
        }
      }
    }

    // Sort the result in order of decreasing score.
    // Collections.sort ( result , new PassageCandidateComparator() );
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
    // int s=0, e=0;
    // int spanNumber = 0;
    char[] punc = { '.', '?', '!' };
    while (j < textSize - 2) {
      if (text.charAt(j) == '.' && text.charAt(j + 2) >= 'A' && text.charAt(j + 2) <= 'Z') {
        PassageSpan a = new PassageSpan(i, j);
        span.add(a);
        i = j + 2;
        // spanNumber++;
      }
      j++;
    }
    PassageSpan a = new PassageSpan(i, textSize - 1);
    span.add(a);
    // Iterator <PassageSpan> it = span.iterator();
    // while (it.hasNext()){
    // PassageSpan a1=it.next();
    // System.out.print("Punctuation of Passage"+a1.getBegin()+" "+a1.getEnd()+"\t");
    // int ii=0;
    // for (ii=a1.getBegin();ii<=a1.getEnd();ii++){
    // System.out.print(text.charAt(ii));
    // }
    // System.out.println();
    // }
    return span;
  }
}