package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class GoldenStandard extends AbstractKeytermExtractor{
  protected List<Keyterm> getKeyterms(String question) {
    List<Keyterm> kl = new ArrayList<Keyterm>();
    BufferedReader ar = null;
    try {
      ar = new BufferedReader(new FileReader("input/back.txt"));
    } catch (FileNotFoundException e3) {
      // TODO Auto-generated catch block
      e3.printStackTrace();
    }
    String m=null;
    String no = null;
    try {
      m=ar.readLine();
    } catch (IOException e3) {
      // TODO Auto-generated catch block
      e3.printStackTrace();
    }
    while (m!=null){
      String f=m.substring(4, m.length());
      //System.out.println(f);
      if (f.equals(question)){
        no=m.substring(0,3);
      }
      try {
        m=ar.readLine();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    System.out.println(no);
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader("./src/main/resources/gs/yibin.keyterm"));
    } catch (FileNotFoundException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    String r=null;
    try {
      r = br.readLine();
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    while (r != null) {
      String No=r.substring(0, 3);
      int index = 0;
      String kt;
      if (No.equals(no)){
        for (int i=3;i<=r.length()-1;i++){
          if (r.charAt(i)=='|'){
            index=i;
          }
        }
        kt=r.substring(index+1,r.length());
        Keyterm KT = new Keyterm(kt);
        kl.add(KT);
      }
      try {
        r = br.readLine();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
//    for (Keyterm a:kl){
//      System.out.println(a);
//    }
//    for (Keyterm k:kl){
//      System.out.println(k.toString());
//    }
    return kl;
  }
}
