package edu.cmu.lti.oaqa.openqa.test.team18.retrieval;

import java.io.*;
import java.util.*;

public class NihParser {

  private Map<String, List<String>> dictionary;

  /**
   * Construct dictionary from nih dataset
   * 
   * @param absolute
   *          resource Path to nih.txt
   * @return Instance of NihParser
   */
  public NihParser(String fileuri) throws IOException {

    BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass()
            .getResourceAsStream(fileuri)));
    String line;
    this.dictionary = new HashMap<String, List<String>>();
    while ((line = br.readLine()) != null) {
      line = line.replace("\n", "");
      String[] fields = line.split("\t");
      // System.out.println(fields.length);
      if (fields.length != 2) {
        continue;
      } else {
        // System.out.println(fields[1]);
        String[] synonyms = fields[1].split("\\|");
        // System.out.println(synonyms.length);
        List<String> li = new ArrayList<String>();
        for (String str : synonyms) {
          li.add(str);
        }
        if (li.size() > 0) {
          this.dictionary.put(fields[0], li);
        }
      }

    }
  }

  public List<String> findSynonyms(String gene) {
    List<String> li = new ArrayList<String>();
    try {
      List<String> synonyms = this.dictionary.get(gene);
      for (String syno : synonyms) {
        li.add(syno);
      }

    } catch (Exception e) {
    }
    return li;
  }

}
