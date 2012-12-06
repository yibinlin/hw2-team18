package edu.cmu.lti.oaqa.openqa.test.team18.passage;

import java.util.ArrayList;
import java.util.List;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
/**
 * @deprecated
 * @author Haohan Wang
 *
 */
public class WordNetImpl {

  public static Synset[] wordnet(String s, SynsetType type) {
    String OS = System.getProperty("os.name").toLowerCase();
    
    if(OS.indexOf("win") >= 0)
    {
      System.setProperty("wordnet.database.dir", "lexicon/dict/");
    }
    else
    {
      System.setProperty("wordnet.database.dir", "lexicon/dict");
    }
    

    WordNetDatabase database = WordNetDatabase.getFileInstance();

    Synset[] synsets = database.getSynsets(s, SynsetType.NOUN);

    //String[] synsets2 = database.getBaseFormCandidates(s, type);

//    System.out.println(synsets2.length);
//    for (String s1:synsets2){
//      System.out.println(s1);
//    }
    
    return synsets;
  }
  
//  public static List<String> searchForSynonyms(String word){
//    //Synset[] synsets=WordNetImpl.wordnet(word);
//    List<String> result = new ArrayList<String>();
//    for (Synset ss:synsets){
//      String s=ss.toString();
//      String r=null;
//      int start=0, end=0;
//      for (int i=0;i<=s.length()-1;i++){
//        if (s.charAt(i)=='['){
//          start=i;
//        }
//        if (s.charAt(i)==']'){
//          end=i;
//          break;
//        }
//      }
//      start=start+1;
//      for (int i=start;i<=end;i++){
//        if (s.charAt(i)==','){
//          r=s.substring(start, i);
//          //System.out.println(r.compareTo(word));
//          if (r.compareTo(word)!=0){
//            result.add(r);
//          }
//          start=i+1;
//        }
//      }
//      r=s.substring(start,end);
//      if (r.compareTo(word)!=0){
//        result.add(r);
//      }
//    }
//    return result;
//  }
  
//  public static void main (String args[]){
//    String m="be";
//    Synset [] synsets = WordNetImpl.wordnet(m);
//    for (Synset ss:synsets){
//      System.out.println(ss);
//    }
//    List<String> ls = WordNetImpl.searchForSynonyms(m);
//    for (String s:ls){
//      System.out.println(s);
//    }
//  }
}
