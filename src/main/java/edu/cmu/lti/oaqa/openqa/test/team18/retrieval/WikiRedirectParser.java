package edu.cmu.lti.oaqa.openqa.test.team18.retrieval;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * used to connect to server to get Wikipedia page redirects.
 * 
 * The file was extracted by the wikipedia_redirect project by Hideki Shima @CMU @LTI.
 * http://code.google.com/p/wikipedia-redirect/
 * 
 * Connects to a Tomcat application server and fetches the wikipedia redirect.
 * 
 * @author Yibin Lin
 * 
 */
public class WikiRedirectParser {
  public static final String PAGE_URL = "http://reap.cs.cmu.edu:8080/WikiRedirect/demo?";

  public static final String HOST = "reap.cs.cmu.edu";

  public static final int PORT = 8080;

  /**
   * Find synonyms. If a keyterm is a alias (redirected word) in Wikipedia, return the 
   * authentic main word in Wikipedia.<br>
   * 
   * If a keyterm is an authentic main word in Wikipediam, then return all its aliases (redirects).
   * 
   * @param aKeyterm a keyterm produced in keytern extraction phase.
   * @return its synonyms as described above.
   */
  public List<String> findSynonyms(String aKeyterm) {
    List<String> res = new LinkedList<String>();
    
    if (isReachable(HOST, PORT)) { //if it is not reachable, then we return an empty list.
      HttpURLConnection connection = null;
      OutputStreamWriter wr = null;
      BufferedReader rd = null;
      StringBuilder sb = null;
      String line = null;

      URL serverAddress = null;

      try {
        serverAddress = new URL(PAGE_URL + "str[]=" + URLEncoder.encode(aKeyterm, "UTF-8"));
        // set up out communications stuff
        connection = null;

        // Set up the initial connection
        connection = (HttpURLConnection) serverAddress.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setReadTimeout(100000);

        connection.connect();

        // get the output stream writer and write the output to the server
        // not needed in this example
        // wr = new OutputStreamWriter(connection.getOutputStream());
        // wr.write("");
        // wr.flush();

        // read the result from the server
        rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        sb = new StringBuilder();

        while ((line = rd.readLine()) != null) {
          String[] synonymPair = line.split(";");
          for (String s : synonymPair) {
            if (!s.equalsIgnoreCase(aKeyterm)) {
              res.add(s);
            }
          }
        }

      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (ProtocolException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        // close the connection, set all objects to null
        connection.disconnect();
        rd = null;
        sb = null;
        wr = null;
        connection = null;
      }
    }
    return res;
  }

  /**
   * Test if a specific host and port is available for the program. Is used to detect whether the
   * Tomcat server is running.
   * 
   * @param host
   *          host name
   * @param port
   *          port number, probably 8080 in this case.
   * @return true if the port+host is reachable, false otherwise.
   */
  boolean isReachable(String host, int port) {
    Socket socket = null;
    boolean reachable = false;
    try {
      socket = new Socket(host, port);
      reachable = true;
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (socket != null)
        try {
          socket.close();
        } catch (IOException e) {
        }
    }
    return reachable;
  }

  /**
   * for testing
   * 
   * @param args
   * 
   *          public static void main(String[] args) { System.out.println(new
   *          WikiRedirectParser().findSynonyms("China")); }
   */
}
