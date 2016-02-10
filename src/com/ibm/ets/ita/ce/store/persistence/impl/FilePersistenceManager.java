package com.ibm.ets.ita.ce.store.persistence.impl;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.close;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.persistence.PersistenceManager;

public class FilePersistenceManager
  extends AbstractPersistenceManager
  implements PersistenceManager
{
  public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

  private final String ENCODING = "UTF-8";
  private final String COMMENT_HEADER = "--";
  private final String SENTENCE_ID_HEADER = "-- sen id ";
  private final int SENTENCE_ID_HEADER_LENGTH = SENTENCE_ID_HEADER.length();
  private final String SOURCE_ID_HEADER = "-- src id ";
  //private final int SOURCE_ID_HEADER_LENGTH = SOURCE_ID_HEADER.length();

  File getFile(String storeName) {
    String persistPath = actionContext.getCeConfig().getPersistPath();
    return new File(persistPath + File.separator + storeName + "-sentences.ce");
  }
  
  @Override
  int getLastSavedSentenceId(String storeName) {
    int result;
    FileInputStream fis = null;
    BufferedReader br = null;
    try {
      File file = getFile(storeName);
      if (!file.exists()) {
        result = -1;
      } else {
        fis = new FileInputStream(file); 
        InputStreamReader isr = new InputStreamReader(fis, ENCODING);
        br = new BufferedReader(isr);
        String lastLine = null;
        for(String line=br.readLine(); line!=null; line=br.readLine()) {
          lastLine = line;
        }
        lastLine = lastLine.substring(SENTENCE_ID_HEADER_LENGTH);
        result = Integer.parseInt(lastLine);
      }
    } catch (Exception e) {
      System.out.println(e); // TODO
      result = -1;
    } finally {
      close(br);
      close(fis);
    }
    return result;
  }

  @Override
  int writeSentences(String storeName, List<CeSentence> newSentences) {
    int result = 0;
    FileOutputStream fos = null;
    BufferedWriter bw = null;
    try {
      File file = getFile(storeName);
      if (!file.exists()) {
        file.createNewFile();
      }
      fos = new FileOutputStream(file, true);
      OutputStreamWriter osw = new OutputStreamWriter(fos, ENCODING);
      bw = new BufferedWriter(osw);
      for(CeSentence sentence : newSentences) {
        String sentenceText = sentence.getCeText(actionContext);
        int sentenceId = sentence.getId();
        String sourceId = sentence.getSource().getId();
        bw.newLine();
        bw.write(sentenceText);
        bw.newLine();
        bw.write(SOURCE_ID_HEADER + sourceId);
        bw.newLine();
        bw.write(SENTENCE_ID_HEADER + sentenceId);
        bw.newLine();
        result++;
      }
    } catch (Exception e) {
      System.out.println(e); // TODO
      // swallow ?
    } finally {
      close(bw);
      close(fos);
    }
    return result;
  }

  @Override
  List<String> readSentences(String storeName) {
    List<String> result = Collections.emptyList();
    FileInputStream fis = null;
    BufferedReader br = null;
    try {
      File file = getFile(storeName);
      if (file.exists()) {
        result = new ArrayList<String>();
        fis = new FileInputStream(file); 
        InputStreamReader isr = new InputStreamReader(fis, ENCODING);
        br = new BufferedReader(isr);
        // a sentence may be split across multiple lines, but is guaranteed to
        // have comment(s) following it.
        String sentence = "";
        for(String line=br.readLine(); line!=null; line=br.readLine()) {
          if (!line.isEmpty()) {
            if (line.startsWith(COMMENT_HEADER)) {
              if (!sentence.isEmpty()) {
                result.add(sentence);
                sentence = "";
              } else {
                // skip the comments for now TODO
              }
            } else {
            	sentence += line;
            }
          }
        }
      }
    } catch (Exception e) {
        System.out.println(e); // TODO
    } finally {
      close(br);
      close(fis);
    }
    return result;
  }

  @Override
  void deleteSentences(String storeName) {
    File file = getFile(storeName);
    if (file.exists()) {
      boolean success = file.delete();
      if (!success) {
        // TODO
      }
    }
  }

}
