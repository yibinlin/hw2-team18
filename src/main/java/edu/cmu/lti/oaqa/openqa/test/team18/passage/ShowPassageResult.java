package edu.cmu.lti.oaqa.openqa.test.team18.passage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.jsoup.Jsoup;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;
/**
 * This class is used to extract the correct answer
 * only used when we need figure out better ways of extraction
 * @deprecated
 * @author Haohan Wang
 *
 */
public class ShowPassageResult extends SimplePassageExtractor {
  @SuppressWarnings("null")
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    FileOutputStream fos = null;
    BufferedWriter bw = null;
//     try {
//     String html = wrapper.getDocText("9182672");
//     System.out.println("-----------------------");
//     System.out.println(html.length());
//     System.out.println("----------------------");
//     } catch (SolrServerException e5) {
//     // TODO Auto-generated catch block
//     e5.printStackTrace();
//     }

    File file = new File("./src/main/resources/gs/passageresult.txt");
    try {
      fos = new FileOutputStream(file);
    } catch (FileNotFoundException e4) {
      e4.printStackTrace();
    }
    bw = new BufferedWriter(new OutputStreamWriter(fos));
    // BufferedReader ar = null;
    // try {
    // ar = new BufferedReader(new FileReader("./src/main/resources/input/back.txt"));
    // } catch (FileNotFoundException e3) {
    // e3.printStackTrace();
    // }
    // String m = null;
    // String no = null;
    // try {
    // m = ar.readLine();
    // } catch (IOException e3) {
    // e3.printStackTrace();
    // }
    // while (m != null) {
    // String f = m.substring(4, m.length());
    // // System.out.println(f);
    // if (f.equals(question)) {
    // no = m.substring(0, 3);
    // }
    // try {
    // m = ar.readLine();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    // System.out.println(no);
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader("./src/main/resources/gs/trecgen06.passage"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    String str = null;

    // String id = document.getDocID();
    String htmlText = null;
    try {
      str = br.readLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
    while (str != null) {
      String NO = null, ID = null;
      int index = 0;
      if (str.charAt(11) >= '0' && str.charAt(11) <= '9') {
        index = 12;
      } else {
        index = 11;
      }
      NO = str.substring(0, 3);
      ID = str.substring(4, index);
      int s1 = 0, e1 = 0, s2 = 0, e2 = 0;
      String no1, no2;
      try {
        htmlText = wrapper.getDocText(ID);
        //String text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
        //htmlText=text;
//        if (NO.equals("172")&&ID.equals("14600272")){
//          System.out.println(htmlText.length());
//        }
      } catch (SolrServerException e3) {
        // TODO Auto-generated catch block
        e3.printStackTrace();
      }
      int length = htmlText.length() - 1;
      if (length >5) {
        boolean first = true;
        boolean second = true;
        boolean blank = true;
        for (int i = index + 1; i <= str.length() - 1; i++) {
          if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
            if (blank) {
              if (first) {
                s1 = i;
                first = false;
              } else {
                e1 = i;
              }
            } else {
              if (second) {
                s2 = i;
                second = false;
              } else {
                e2 = i;
              }
            }
          } else {
            if (blank) {
              blank = false;
            } else {
              break;
            }
          }
        }
        no1 = str.substring(s1, Math.min(e1 + 1, length));
        no2 = str.substring(s2, Math.min(e2 + 1, length));
        int n1 = Integer.parseInt(no1);
        int n2 = Integer.parseInt(no2);

        String result = NO + " " + no1 + " " + no2 + " "
                + htmlText.substring(Math.min(n1, length - 1), Math.min(n2 + 1, length - 1));
        try {
          bw.write(result);
          bw.newLine();
          System.out.println(result);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      try {
        str = br.readLine();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      bw.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (RetrievalResult document : documents) {
      System.out.println("Yeah");
    }
    return null;
  }
}
