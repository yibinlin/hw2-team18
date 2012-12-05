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
/**
 * 
 * @author Haohan Wang
 * implementation of siteQ with matrix of keyterms
 * siteQ is from TREC10
 * the matrix reflects different weight for weight from concept level and word level from TREC2006
 * and some our own trials to improve MAP
 */
public class SiteQwithMatrix {
  private String text;

  private String docId;

  private int textSize; // values for the entire text

  public SiteQwithMatrix(String docId, String text, KeytermWindowScorer scorer) {
    super();
    this.text = text;
    this.docId = docId;
    this.textSize = text.length();
  }

  @SuppressWarnings("unchecked")
  /**
   * extractPassages
   * 
   * @param keytermM
   * input is a matrix of Keyterm
   * each line of the matrix maitain the same concept of Keyterm, i.e. the synonyms of each keyterm
   * each line is ordered as the weight of keyterm
   * each row of each line is the keyterms of same concept, listed in tho order of weight
   * 
   * @return the extracted List of PassageCandidate
   * the rusult is sorted by a score
   * The score is mainly implemented with SiteQ, with weights of concept and word. 
   * Also with my modifications explained in detail in below
   * 
   */
  public List<PassageCandidate> extractPassages(List<List<String>> keytermM) {
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
    // For every possible window, start to calculate
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    for (Integer begin : leftEdges) {
      // end cannot be smaller than great
      for (Integer end : rightEdges) {
        if (end <= begin)
          continue;
        // In the retrieved passage, a window greater than 500 characters is rarely seen
        if (end - begin > 500)
          continue;
        String textLine = text.substring(begin, end);
        //replace the html tags with useful information like Greek characters in the format of .gif with text of Greek characters
        //traditional parser cannot do this
        textLine = replaceDotGif(textLine);
        //clean html tags
        String temp = Jsoup.parse(textLine).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
        textLine = temp;
        List<KeytermHit> khl = new ArrayList<KeytermHit>();
        float hitNumber = 0;//the times of the keyterm matched for each concept
        //match the keyterm
        //for each matched keyterm, store it in a list of KeytermHit with the useful information: offset and weight
        //for each concept, maintain the increase of hitNumber
        for (int i = 0; i <= keytermM.size() - 1; i++) {
          boolean find = false;
          for (int j = 0; j <= keytermM.get(i).size() - 1; j++) {
            String keyterm = keytermM.get(i).get(j);
            Pattern p = Pattern.compile(keyterm);
            Matcher m = p.matcher(textLine);
            while (m.find()) {
              find = true;
              KeytermHit kh = new KeytermHit(m.start(), m.end(), i + 1, j + 1);
              khl.add(kh);
            }
          }
          if (find) {
            hitNumber += 1;
          }
        }
        if (hitNumber > 1) { // a passage can be a candidate only at least two concepts are matched
          //mainly implementation for SiteQ with modification
          double avgE = 0, totalE = 0, maxE = 0; //E means the distance of each mateched keyterm
          double avgB = 0, totalB = 0, maxB = 0; //B means the distance of matched keyterm to the begin of this candidate answer
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
          double sigmaD = 0; //sigmaD is the sigma of the weighted quoation of distance of this keyterm to the begin of candidate.
          for (int i = 0; i <= khl.size() - 1; i++) {
            double b = khl.get(i).begin;
            double r = khl.get(i).row;
            double l = khl.get(i).line;
            double w = (100 / r + 1 / l);
            double textLength = textLine.length();
            sigmaD += (w / (textLength)) * (1 - (b - begin) / avgB);
            //the weight is calculated with a combination of concept weight and word weight divided by the length of each candidate
            //this is different from SiteQ, but somehow, gives a better performance
          }

          double keytermMsize = keytermM.size();
          hitNumber = hitNumber - 1;
          //rp should be the all matched keyterm divided by all the keyterms
          //we increase the impact of concept hit of the keyterm
          //and ignore the word weight
          double rp = Math.pow(hitNumber, 3) / keytermMsize;
          double khlsize = khl.size();
          //final score is calculated by all the params
          double scoreMatrix = (rp * (avgE / maxE) * (1 / khlsize) * (sigmaD));
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
    //sorted the result
    Collections.sort(result, new PassageCandidateComparator());
    return result;
  }
  /**
   * the default class of PassageCandidateComparator
   * 
   */
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
  /**
   * default class of PassageSpan
   *
   */
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
  /**
   * class KeytermHit
   * @author Haohan Wang
   * this class is a structure to restore the information of a matched Keyterm
   * the information are the offset(begin, end) of this keyterm in the target sentence
   * and the concept weight(row) and word weight(line) of this keyterm
   */
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

  /**
   * getPassageSentences
   * 
   * this function is called to return the PassageSpan of divided by natural sentences and semisentences.
   * in html form, it also return the span divided by html tags
   * @return a list of PassageSpan
   */
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
    //the last span needs to be generated out of the loop
    PassageSpan a = new PassageSpan(i, textSize - 1);
    span.add(a);
    return span;
  }
  /**
   * replaceDotGif
   * this function is called to replace the meaningful html tags with texts
   * traditional parser will leave these information out
   * 
   * @param text
   * input is a string with html tags
   * 
   * @return String
   * output is a string, all the html tags with useful information in .gif format are extracted
   */
  public String replaceDotGif(String text) {
    String result = text;
    Pattern p = Pattern.compile(".gif");
    Matcher m = p.matcher(text);
    //not necessary to add all the Greek characters into this dict based on the resource
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
}