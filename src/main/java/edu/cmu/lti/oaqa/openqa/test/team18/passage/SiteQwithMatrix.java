package edu.cmu.lti.oaqa.openqa.test.team18.passage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorer;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerProduct;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;
import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;
//import edu.cmu.lti.oaqa.openqa.test.team18.passage.SiteQwithMatrix.PassageCandidateComparator;
import edu.cmu.lti.oaqa.openqa.test.team18.passage.SiteQwithMatrix.PassageSpan;
import edu.cmu.lti.oaqa.openqa.test.team18.retrieval.GoParser;
import edu.cmu.lti.oaqa.openqa.test.team18.retrieval.NihParser;

public class SiteQwithMatrix {
  private String text;

  private String docId;

  private int textSize; // values for the entire text

  private int totalMatches;

  private int totalKeyterms;

  private KeytermWindowScorer scorer;

  public SiteQwithMatrix(String docId, String text, KeytermWindowScorer scorer) {
    super();
    this.text = text;
    this.docId = docId;
    this.textSize = text.length();
    this.scorer = scorer;
  }

  @SuppressWarnings("unchecked")
  public List<PassageCandidate> extractPassages(List<List<String>> keytermM) {
    // List<List<PassageSpan>> matchingSpans = new ArrayList<List<PassageSpan>>();
    // List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
    // List<List<String>> keytermM = getSynonyms(keyterms);
    for (List<String> ls : keytermM) {
      for (String s : ls) {
        System.out.println(s);
      }
    }
    // for (String keyterm : keyterms) {
    // Pattern p = Pattern.compile(keyterm);
    // Matcher m = p.matcher(text);
    // while (m.find()) {
    // PassageSpan match = new PassageSpan(m.start(), m.end());
    // matchedSpans.add(match);
    // totalMatches++;
    // }
    // if (!matchedSpans.isEmpty()) {
    // matchingSpans.add(matchedSpans);
    // totalKeyterms++;
    // }
    // }

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
    // for ( List<PassageSpan> keytermMatches : matchingSpans ) {
    // for ( PassageSpan keytermMatch : keytermMatches ) {
    // Integer leftEdge = keytermMatch.begin;
    // Integer rightEdge = keytermMatch.end;
    // if (! leftEdges.contains( leftEdge ))
    // leftEdges.add( leftEdge );
    // if (! rightEdges.contains( rightEdge ))
    // rightEdges.add( rightEdge );
    // }
    // }
    // For every possible window, calculate keyterms found, matches found; score window, and create
    // passage candidate.
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    for (Integer begin : leftEdges) {
      // end cannot be smaller than great
      for (Integer end : rightEdges) {
        if (end <= begin)
          continue;
        // In the retrieved passage, a window greater than 500 characters is rarely seen
        if (end - begin > 500)
          continue;
        // System.out.println(begin+" "+end);
        String textLine = text.substring(begin, end);
        textLine = replaceDotGif(textLine);
        String temp = Jsoup.parse(textLine).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
        textLine = temp;
        List<KeytermHit> khl = new ArrayList<KeytermHit>();
        List<KeytermHit> khl2 = new ArrayList<KeytermHit>();
        float hitNumber = 0;
        for (int i = 0; i <= keytermM.size() - 1; i++) {
          boolean find = false;
          for (int j = 0; j <= keytermM.get(i).size() - 1; j++) {
            String keyterm = keytermM.get(i).get(j);
            Pattern p = Pattern.compile(keyterm);
            Matcher m = p.matcher(textLine);
            while (m.find()) {
              find = true;
              // boolean addin = true;
              KeytermHit kh = new KeytermHit(m.start(), m.end(), i + 1, j + 1);
              khl.add(kh);
              // for (KeytermHit ke: khl2){
              // if (ke.line==kh.line&&ke.row==kh.row){
              // addin=false;
              // }
              // }
              // if (addin){
              // khl2.add(kh);
              // }
            }
          }
          if (find) {
            hitNumber += 1;
          }
        }
        if (hitNumber > 1) {
          double avgE = 0, totalE = 0, maxE = 0;
          double avgB = 0, totalB = 0, maxB = 0;
          double hitsize = khl.size();
          for (int i = 0; i <= khl.size() - 1; i++) {
            int b1 = khl.get(i).begin;
            for (int j = i + 1; j <= khl.size() - 1; j++) {
              int b2 = khl.get(j).begin;
              totalE += Math.abs(b2 - b1);
              if (maxE < Math.abs(b2 - b1)) {
                maxE = Math.abs(b2 - b1);
              }
            }
            totalB += Math.abs(b1 - begin);
            if (maxB < Math.abs(b1 - begin)) {
              maxB = Math.abs(b1 - begin);
            }
          }
          avgE = totalE / hitsize;
          avgB = totalB / hitsize;
          double sigmaD = 0;
          for (int i = 0; i <= khl.size() - 1; i++) {
            double b = khl.get(i).begin;
            double r = khl.get(i).row;
            double l = khl.get(i).line;
            double w2 = keytermM.size() + 5;
            double w = (100 / r + 1 / l);
            double textLength = textLine.length();
            sigmaD += (w / (textLength)) * (1 - (b - begin) / avgB);
          }
          double sigmaE = 0;
          double [] sigmaF= {1000,1000,1000,1000,1000,1000,1000,1000,1000,1000};
          double dis = 0;
          for (int i = 0; i <= khl.size() - 1; i++) {
            for (int j = i + 1; j <= khl.size() - 1; j++) {
              int sRow = khl.get(i).row;
              if (sRow != khl.get(j).row&&sRow<10) {
                dis = Math.abs(khl.get(i).begin - khl.get(j).begin);
                if (sigmaF[sRow] > dis) {
                  sigmaF[sRow] = dis;
                }
              }
            }
          }
          for (int i=0;i<=9;i++){
            if (sigmaF[i]!=1000){
              sigmaE+=sigmaF[i];
            }
          }
          double keytermMsize = keytermM.size();
          hitNumber = hitNumber - 1;
          double rp = Math.pow(hitNumber, 3) / keytermMsize;
          double khlsize = khl.size();
          double scoreMatrix = (rp * (avgE / maxE) * (1 / khlsize) * (sigmaD));
                  /// (sigmaE);
          System.out.println("rp" + rp);
          System.out.println("avgE" + avgE);
          System.out.println("maxE" + maxE);
          System.out.println("khlsize" + khlsize);
          System.out.println("sigmaD" + (sigmaD));
          System.out.println("sigmaE" + sigmaE);
          System.out.println("scoreM" + scoreMatrix);
          PassageCandidate window = null;
          try {
            window = new PassageCandidate(docId, begin, end, (float) scoreMatrix, null);
          } catch (AnalysisEngineProcessException e) {
            e.printStackTrace();
          }
          result.add(window);
        }
      }
    }
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

  class KeytermHit {
    private int begin, end;

    private int row, line;

    public KeytermHit(int b, int e, int r, int l) {
      this.begin = b;
      this.end = e;
      this.row = r;
      this.line = l;
    }
  }

  public static void main(String[] args) {
    String a = "number of <IMG SRC=\"/math/12pt/normal/beta.gif\" ALIGN=BOTTOM ALT=\"beta \">-sheets and a diminished <IMG160 5360 6172 eases,<SUP> </SUP>among them Creutzfeldt&#150;Jakob disease (CJD) of man, bovine<SUP> </SUP>spongiform encephalopathy (BSE) and scrapie of sheep (see reviews<SUP> </SUP>by Prusiner, 1998";
    SiteQwithMatrix sqm = new SiteQwithMatrix(a, a, null);
    String b = sqm.replaceDotGif(a);
    System.out.println(b);
  }

  public List<PassageSpan> getPassageSentences() {
    List<PassageSpan> span = new ArrayList<PassageSpan>();
    int i = 0, j = 1;
    // generate the span of natural sentences
    while (j < textSize - 2) {
      if ((text.charAt(j) == '.' || text.charAt(j) == ',' || text.charAt(j) == '>')
              && ((text.charAt(j + 2) >= 'A' && text.charAt(j + 2) <= 'Z')
                      || (text.charAt(j + 2) >= 'a' && text.charAt(j + 2) <= 'z') || text
                      .charAt(j + 2) == '<')) {
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

  public String replaceDotGif(String text) {
    String result = text;
    Pattern p = Pattern.compile(".gif");
    Matcher m = p.matcher(text);
    String[] dict = { "alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota",
        "kappa", "lambda", "sigma" };
    int s = 0, e = 0;
    boolean last = true;
    if (!m.find()) {
      return text;
    } else {
      for (int i = 1; i <= text.length() - 2; i++) {
        if (text.charAt(i) == '<') {
          s = i;
        } else if (text.charAt(i) == '>') {
          e = i;
        }
        if ((s != 0) && (e != 0) && (e > s)) {
          String middle = text.substring(s, e + 1);
          result = text.substring(0, s) + replaceDotGif(middle)
                  + text.substring(e + 1, text.length() - 1);
          i = i - (text.length() - result.length());
          text = result;
          last = false;
          s = 0;
          e = 0;
        }
      }
      if (last) {
        for (String str : dict) {
          Pattern pp = Pattern.compile(str);
          Matcher mm = pp.matcher(text);
          if (mm.find()) {
            return str;
          }
        }
      }
    }
    return result;
  }

  // public List<List<String>> getSynonyms(String[] keyterms) {
  // List<List<String>> result = new ArrayList<List<String>>();
  // for (String k : keyterms) {
  // List<String> kl = new ArrayList<String>();
  // List<String> nk = new ArrayList<String>();
  // List<String> r = new ArrayList<String>();
  // kl.add(k);
  // k = k.toLowerCase();
  // try {
  // GoParser gp = new GoParser("src/main/resources/dict/synonym.xml");
  // r = gp.findAllSynonyms(k);
  // for (String s : r) {
  // nk.add(s);
  // }
  // } catch (ParserConfigurationException e) {
  // e.printStackTrace();
  // } catch (SAXException e) {
  // e.printStackTrace();
  // } catch (IOException e) {
  // e.printStackTrace();
  // }
  // try {
  // NihParser np = new NihParser("src/main/resources/dict/nih.txt");
  // r = np.findSynonyms(k);
  // for (String s : r) {
  // nk.add(s);
  // }
  // } catch (IOException e) {
  // e.printStackTrace();
  // }
  // boolean exist = false;
  // for (String s1 : nk) {
  // exist = false;
  // for (String s2 : kl) {
  // if (s1.equals(s2) || s1.contains("\\")) {
  // exist = true;
  // break;
  // }
  // }
  // if (!exist) {
  // kl.add(s1);
  // }
  // }
  // result.add(kl);
  // }
  // return result;
  // }
}