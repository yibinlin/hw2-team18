package edu.cmu.lti.oaqa.openqa.test.team18.keyterm;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import edu.cmu.lti.oaqa.ecd.log.AbstractLoggedComponent;
import edu.cmu.lti.oaqa.framework.BaseJCasHelper;
import edu.cmu.lti.oaqa.framework.QALogEntry;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.KeytermList;
import edu.cmu.lti.oaqa.framework.types.InputElement;

public abstract class AbstractKeytermExtractor extends AbstractLoggedComponent {
  protected abstract List<Keyterm> getKeyterms(String question);

  @Override
  public final void process(JCas jcas) throws AnalysisEngineProcessException {
    super.process(jcas);
    try {
      // prepare input
      InputElement input = (InputElement) BaseJCasHelper.getAnnotation(jcas, InputElement.type);
      String question = input.getQuestion();
      // do task
      List<Keyterm> keyterms = getKeyterms(question);
      log(keyterms.toString());
      // save output
      KeytermList.storeKeyterms(jcas, keyterms);
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  protected final void log(String message) {
    super.log(QALogEntry.KEYTERM, message);
  }
}