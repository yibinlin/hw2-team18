package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

/**
 * The WordCount class that contains a word, and its count in Gigaword corpus.
 * @author Yibin Lin
 *
 */
public class WordCount {

  /**
   * The word.
   */
  String word;
  /**
   * The count of the word.
   */
  Integer cnt;
  
  public WordCount(String word, Integer cnt)
  {
    this.word = word;
    this.cnt = cnt;
  }
  
  public WordCount(String word)
  {
    this.word = word;
    this.cnt = 0;
  }
}
