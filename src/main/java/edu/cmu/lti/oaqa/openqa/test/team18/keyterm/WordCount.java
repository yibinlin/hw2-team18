package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

public class WordCount {

  String word;
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
