package com.ibm.ets.ita.ce.store.core;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.PREFIX_SEN;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.model.CeSentence;

/*
 * A list of CeSentence objects in their natural order (which is defined in the
 * CeSentence class as the order of the sentence id number), with all elements
 * non-null.
 * 
 * Is managing our own array really more efficient than just using ArrayList ?
 */
public class SentenceList {
  public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

  private static final String CLASS_NAME = SentenceList.class.getName();
  private static final String PACKAGE_NAME = SentenceList.class.getPackage().getName();
  private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

  private static final int INITIAL_LENGTH = 40;
  private static final int INCREMENT_LENGTH = 20;
  private static final String MSG_DUPLICATE_ID_NOT_ADDED = "sentence has duplicate id %d, new text is '%s', existing text is '%s'.";
  
  // Accesses to the sentence array and the last index integer are guarded via
  // the read-write lock. Lock acquisition occurs in public methods of this
  // class that access these fields, or where these methods only perform 
  // parameter checking and transformation, in a called method. Also note that
  // acquiring the lock ensures "up to date", coherent values for both variables
  // are seen as per the Java memory model.
  private ReadWriteLock rwLock = new ReentrantReadWriteLock();
  private CeSentence[] sentences = new CeSentence[INITIAL_LENGTH];
  private int lastUsedIndex = -1;

  // Source of incrementing event ids starting from 0, across all SentenceList
  // objects, for the lifetime of the virtual machine. An id < 0 indicates no
  // such event has taken place for the current list.
  // The maximum of these ids indicates the last event, and the maximum of the
  // ids for any event across several lists acts as the id for that event for
  // the union of the lists.
  private static final AtomicInteger sharedEventId = new AtomicInteger(0);
  private volatile int   createEventId = sharedEventId.incrementAndGet();
  private volatile int      addEventId = -1;
  private volatile int   removeEventId = -1;

  // assumes o1 and o2 are non-null.
  // can't instantiate a CeSentence with a chosen sentence id to use when
  // searching for a sentence with a given id, so use the following comparator,
  // this must use the same ordering as CeSentence.compareTo(CeSentence).
  private static final Comparator<Object> searchComparator = new Comparator<Object>() {
    @Override
    public int compare(Object o1, Object o2) {
      int value1 = (o1 instanceof Integer) ? ((Integer)o1).intValue() : ((CeSentence)o1).getId();
      int value2 = (o2 instanceof Integer) ? ((Integer)o2).intValue() : ((CeSentence)o2).getId();
      return value1 - value2;
    }
  };

  // constructor, diagnostic output.
  SentenceList() {
    String objectId = Integer.toHexString(System.identityHashCode(this));
    logger.logp(Level.FINER, CLASS_NAME, "SentenceList", "constructor, " + objectId + ", " + this.createEventId);
  }
  
  // assumes integerId is non-null.
  // from Arrays javadoc : returns index of the search key, if it is contained
  // in the/ array; otherwise, (-(insertion point) - 1). The insertion point is
  // defined as the point at which the key would be inserted into the array: the
  // index of the first element greater than the key, or a.length if all
  // elements in the array are less than the specified key. Note that this
  // guarantees that the return value will be >= 0 if and only if the key is
  // found.
  private int findSentenceIndex(Integer integerId) {
    return Arrays.binarySearch(this.sentences, 0, this.lastUsedIndex+1, integerId, searchComparator);
  }
  
  private static int toInsertionIndex(int index) {
    return (-index-1);
  }

  // assumes formattedId is non-null.
  // returns null if formattedId not in correct format.
  private static Integer formattedIdToIntegerId(String formattedId) {
    Integer result = null;
    try {
      if (formattedId.startsWith(PREFIX_SEN)) {
        String idNumberString = formattedId.substring(PREFIX_SEN.length());
        result = new Integer(Integer.parseInt(idNumberString));
      }
    } catch (NumberFormatException nfe) {
      // swallow, unlikely to happen if string begins with correct prefix 
    }
    return result;
  }
  
  // can choose whether to let returned value be below initial length or not.
  private static int calculateOptimalLength(int nSentences) {
    //return Math.max(INITIAL_LENGTH, INCREMENT_LENGTH*((nSentences/INCREMENT_LENGTH)+1));
    return INCREMENT_LENGTH*((nSentences/INCREMENT_LENGTH)+1);
  }
  
  private void ensureAdditionalCapacity(int nSentencesToAdd) {
    int nSentencesNow = this.lastUsedIndex + 1;
    int nSentencesAfterAdd = nSentencesNow + nSentencesToAdd;
    if (nSentencesAfterAdd > this.sentences.length) {
      int newLength = calculateOptimalLength(nSentencesAfterAdd);
      CeSentence[] newSentences = Arrays.copyOf(this.sentences, newLength);
      this.sentences = newSentences;
    }
  }
  
  /*
  // might want to use this or make this public at some point, if public then
  // write some tests in the SentenceListTest class. Also add lock acquisition.
  private void trimToSize() {
    int optimalLength = calculateOptimalLength(lastUsedIndex+1);
    if (optimalLength != sentences.length) {
      CeSentence[] newSentences = Arrays.copyOf(sentences, optimalLength);
      sentences = newSentences;
    }
  }
  */
  
  // get an id that identifies the event that created this list.
  public int getCreateEventId() {
    return this.createEventId;
  }
  
  // get an id that identifies the last event that added sentence(s).
  public int getAddEventId() {
    return this.addEventId;
  }
  
  // get an id that identifies the last event that removed sentence(s).
  public int getRemoveEventId() {
    return this.removeEventId;
  }
  
  public int getSentenceCount() {
    this.rwLock.readLock().lock();
    try {
      return this.lastUsedIndex+1;
    } finally {
      this.rwLock.readLock().unlock();
    }
  }
  
  public int getLastSentenceId() {
    this.rwLock.readLock().lock();
    try {
      return (this.lastUsedIndex>=0) ? this.sentences[this.lastUsedIndex].getId() : -1;
    } finally {
      this.rwLock.readLock().unlock();
    }
  }
  
  public String getLastSentenceFormattedId() {
    this.rwLock.readLock().lock();
    try {
      return (this.lastUsedIndex>=0) ? this.sentences[this.lastUsedIndex].formattedId() : null;
    } finally {
      this.rwLock.readLock().unlock();
    }
  }
  
  public CeSentence[] getSentences() {
    this.rwLock.readLock().lock();
    try {
      return Arrays.copyOf(this.sentences, this.lastUsedIndex+1);
    } finally {
      this.rwLock.readLock().unlock();
    }
  }
  
  public List<CeSentence> getSentencesAsList() {
    this.rwLock.readLock().lock();
    try {
      List<CeSentence> list = new ArrayList<CeSentence>(this.lastUsedIndex+1);
      for(int i=0; i<this.lastUsedIndex+1; i++) {
        list.add(this.sentences[i]);
      }
      return list;
    } finally {
      this.rwLock.readLock().unlock();
    }
  }
  
  public ArrayList<CeSentence> getSentencesAsArrayList() {
    // lock taken in called method
    return (ArrayList<CeSentence>)getSentencesAsList();
  }
  
  // assumes startIntegerId is non-null.
  private CeSentence[] getSentencesFrom(Integer startIntegerId) {
    this.rwLock.readLock().lock();
    try {
      CeSentence[] result;
      int index = findSentenceIndex(startIntegerId);
      if (index < 0) {                   // sentence with startIntegerId not found
        index = toInsertionIndex(index); // next sentence after startIntegerId
      }
      if (index > this.lastUsedIndex) {       // no more sentences after startIntegerId
        result = new CeSentence[0];
      } else {                           // get sentences from startIntegerId or next id
        result = Arrays.copyOfRange(this.sentences, index, this.lastUsedIndex+1);
      }
      return result;
    } finally {
      this.rwLock.readLock().unlock();
    }
  }

  public CeSentence[] getSentencesFrom(int startId) {
    // lock taken in called method
    Integer startIntegerId = new Integer(startId);
    return getSentencesFrom(startIntegerId);
  }
  
  public CeSentence[] getSentencesFrom(String startFormattedId) {
    // lock taken in called method
    CeSentence[] result;
    if (startFormattedId==null) {
      result = new CeSentence[0];
    } else {
      Integer startIntegerId = formattedIdToIntegerId(startFormattedId);
      if (startIntegerId==null) {
        result = new CeSentence[0];
      } else {
        result = getSentencesFrom(startIntegerId);
      }
    }
    return result;
  }
  
  public List<CeSentence> getSentencesOfTypeAsList(int type) {
    this.rwLock.readLock().lock();
    try {
      List<CeSentence> result = new ArrayList<CeSentence>();
      for(int i=0; i<this.lastUsedIndex+1; i++) {
        if (this.sentences[i].getSentenceType() == type) {
          result.add(this.sentences[i]);
        }
      }
      return result;
    } finally {
      this.rwLock.readLock().unlock();
    }
  }
 
  // assumes integerId is non-null.
  private CeSentence getSentence(Integer integerId) {
    this.rwLock.readLock().lock();
    try {
      CeSentence result;
      int index = findSentenceIndex(integerId);
      if (index < 0) {
        result = null;
      } else {
        result = this.sentences[index];
      }
      return result;
    } finally {
      this.rwLock.readLock().unlock();
    }
  }
  
  public CeSentence getSentence(int id) {
    // lock taken in called method
    Integer integerId = new Integer(id);
    return getSentence(integerId);
  }
  
  public CeSentence getSentence(String formattedId) {
    // lock taken in called method
    CeSentence result;
    if (formattedId==null) {
      result = null;
    } else {
      Integer integerId = formattedIdToIntegerId(formattedId);
      if (integerId==null) {
        result = null;
      } else {
        result = getSentence(integerId);
      }
    }
    return result;
  }
  
  // assumes integerId is non-null.  
  private boolean containsSentence(Integer integerId) {
    this.rwLock.readLock().lock();
    try {
      boolean result;
      int index = findSentenceIndex(integerId);
      if (index < 0) {
        result = false;
      } else {
        result = true;
      }
      return result;
    } finally {
      this.rwLock.readLock().unlock();
    }
  }
  
  public boolean containsSentence(String formattedId) {
    // lock taken in called method
    boolean result;
    if (formattedId==null) {
      result = false;
    } else {
      Integer integerId = formattedIdToIntegerId(formattedId);
      if (integerId==null) {
        result = false;
      } else {
        result = containsSentence(integerId);
      }
    }
    return result;
  }
  
  public boolean containsSentence(int id) {
    // lock taken in called method
    Integer integerId = new Integer(id);
    return containsSentence(integerId);
  }
  
  public boolean containsText(String text, ActionContext actionContext) {
    this.rwLock.readLock().lock();
    try {
      boolean result = false;
      if (text!=null) {
        for(int i=0; i<this.lastUsedIndex+1; i++) { // brute-force, improve if used frequently
          if (this.sentences[i].getCeText(actionContext).equals(text)) {
            result = true;
            break;
          }
        }
      }
      return result;
    } finally {
      this.rwLock.readLock().unlock();
    }
  }
  
  // expect almost all adds to be to the end of the sentences array, in which 
  // case the System.arraycopy will do nothing (last parameter will be zero).
  public void addSentence(CeSentence newSentence, ActionContext actionContext) {
    final String METHOD_NAME = "addSentence";
    logger.entering(CLASS_NAME, METHOD_NAME, (newSentence==null ? "0" : "1"));
    if (newSentence != null) {
      this.rwLock.writeLock().lock();
      try {
        int index = findSentenceIndex(new Integer(newSentence.getId()));
        if (index < 0) {
          index = toInsertionIndex(index);
          ensureAdditionalCapacity(1);
          System.arraycopy(this.sentences, index, this.sentences, index+1, this.lastUsedIndex-index+1); // shift elements up (if required)
          this.sentences[index] = newSentence;
          this.lastUsedIndex++;
          this.addEventId = sharedEventId.incrementAndGet();
        } else {
          if (actionContext!=null) {
            String message = String.format(MSG_DUPLICATE_ID_NOT_ADDED,
             new Integer(newSentence.getId()), newSentence.getCeText(actionContext), this.sentences[index].getCeText(actionContext));
            reportWarning(message, actionContext, logger, CLASS_NAME, METHOD_NAME);
          }
        }
      } finally {
        this.rwLock.writeLock().unlock();
      }
    }
    logger.exiting(CLASS_NAME, METHOD_NAME);
  }

  // expect almost all adds to be to the end of the sentences array, with new
  // sentences in an ordered collection (e.g. a list), in id order.
  public void addSentences(Collection<CeSentence> newSentences, ActionContext actionContext) {
    final String METHOD_NAME = "addSentences";
    logger.entering(CLASS_NAME, METHOD_NAME, (newSentences==null ? "null" : new Integer(newSentences.size())));
    if (newSentences != null && !newSentences.isEmpty()) {
      this.rwLock.writeLock().lock();
      try {
        // add new sentences to the end of the sentences array, noting if in-order.
        int lastUsedIndexBeforeAdd = this.lastUsedIndex;
        ensureAdditionalCapacity(newSentences.size());
        boolean sortRequired = false;
        int lastSentenceId = getLastSentenceId();
        for(CeSentence newSentence : newSentences) {
          if (newSentence != null) {  // don't add nulls to sentences list
            int newSentenceId = newSentence.getId();
            if (!sortRequired && (newSentenceId > lastSentenceId)) {
              this.sentences[++this.lastUsedIndex] = newSentence;
              lastSentenceId = newSentenceId;
            } else {                  // no longer adding sentences with incrementing ids
              int index = findSentenceIndex(new Integer(newSentence.getId()));
              if (index < 0) {
                this.sentences[++this.lastUsedIndex] = newSentence;
                sortRequired = true;
              } else {                // but skip duplicates
                if (actionContext!=null) {
                  String message = String.format(MSG_DUPLICATE_ID_NOT_ADDED,
                   new Integer(newSentence.getId()), newSentence.getCeText(actionContext), this.sentences[index].getCeText(actionContext));
                  reportWarning(message, actionContext, logger, CLASS_NAME, METHOD_NAME);
                }              
              }
            }
          }
        }
        // sort just the newly added sentences, if not enough then sort all.
        if (sortRequired) {
          Arrays.sort(this.sentences, lastUsedIndexBeforeAdd+1, this.lastUsedIndex+1);        
          if (lastUsedIndexBeforeAdd>=0 && this.lastUsedIndex>lastUsedIndexBeforeAdd) {
            int lastIdBeforeAdd = this.sentences[lastUsedIndexBeforeAdd].getId();
            int firstIdOfAdd = this.sentences[lastUsedIndexBeforeAdd+1].getId();
            if (lastIdBeforeAdd > firstIdOfAdd) {
              Arrays.sort(this.sentences, 0, this.lastUsedIndex+1);
            }
          }
        }
        // bump add event id if one or more sentences were added.
        if (this.lastUsedIndex > lastUsedIndexBeforeAdd) {
          this.addEventId = sharedEventId.incrementAndGet();
        }
      } finally {
        this.rwLock.writeLock().unlock();
      }
    }
    logger.exiting(CLASS_NAME, METHOD_NAME);
  }
  
  private CeSentence removeSentence(Integer integerId) {
    CeSentence result;
    this.rwLock.writeLock().lock();
    try {
      int index = findSentenceIndex(integerId);
      if (index >= 0) {
        result = this.sentences[index];
        System.arraycopy(this.sentences, index+1, this.sentences, index, this.lastUsedIndex-index);
        this.sentences[this.lastUsedIndex] = null;
        this.lastUsedIndex--;
        this.removeEventId = sharedEventId.incrementAndGet();
      } else {
        result = null;
      }
    } finally {
      this.rwLock.writeLock().unlock();
    }
    return result;
  }
  
  public CeSentence removeSentence(int id) {
    // lock taken in called method
    final String METHOD_NAME = "removeSentence";
    logger.entering(CLASS_NAME, METHOD_NAME, new Integer(id));
    Integer integerId = new Integer(id);
    CeSentence result = removeSentence(integerId);
    logger.exiting(CLASS_NAME, METHOD_NAME, (result==null ? "0" : "1"));
    return result;
  }
  
  public CeSentence removeSentence(String formattedId) {
    // lock taken in called method
    final String METHOD_NAME = "removeSentence";
    logger.entering(CLASS_NAME, METHOD_NAME, formattedId);
    CeSentence result;
    if (formattedId==null) {
      result = null;
    } else {
      Integer integerId = formattedIdToIntegerId(formattedId);
      if (integerId==null) {
        result = null;
      } else {
        result = removeSentence(integerId);
      }
    }
    logger.exiting(CLASS_NAME, METHOD_NAME, (result==null ? "0" : "1"));
    return result;
  }
  
  public CeSentence[] removeSentencesOfType(int type) {
    // lock taken in called method
    final String METHOD_NAME = "removeSentencesOfType";
    logger.entering(CLASS_NAME, METHOD_NAME, new Integer(type));
    CeSentence[] result = removeSentencesOfTypes(new int[]{type});
    logger.exiting(CLASS_NAME, METHOD_NAME, new Integer(result.length));
    return result;
  }
  
  // improve if used frequently (could do this in-place by moving chunks in the
  // array as required ?, in which case might want to call or make trimToSize()
  // public).
  public CeSentence[] removeSentencesOfTypes(int[] types) {
    final String METHOD_NAME = "removeSentencesOfTypes";
    logger.entering(CLASS_NAME, METHOD_NAME, Arrays.toString(types));
    CeSentence[] result = new CeSentence[0];
    if (types!=null && types.length>0) {
      this.rwLock.writeLock().lock();
      try {
        Arrays.sort(types); // using binarySearch is a bit extreme, but it'll work
        boolean isRemovingSingleType = types.length==1;
        List<CeSentence> keptSentences = new ArrayList<CeSentence>();
        ArrayList<CeSentence> removedSentences = new ArrayList<CeSentence>();
        for(int i=0; i<this.lastUsedIndex+1; i++) {
          int sentenceType = this.sentences[i].getSentenceType();
          boolean isToBeKept;
          if (isRemovingSingleType) {
            isToBeKept = (sentenceType!=types[0]); 
          } else {
            isToBeKept = (Arrays.binarySearch(types, sentenceType) < 0);
          }
          if (isToBeKept) {
            keptSentences.add(this.sentences[i]);
          } else {
            removedSentences.add(this.sentences[i]);
          }
        }
        if (removedSentences.size() > 0) {
          this.sentences = new CeSentence[INITIAL_LENGTH];
          this.lastUsedIndex = -1;
          ensureAdditionalCapacity(keptSentences.size());
          for(int i=0; i<keptSentences.size(); i++) {
            this.sentences[i] = keptSentences.get(i);
          }
          this.lastUsedIndex = keptSentences.size()-1;
          this.removeEventId = sharedEventId.incrementAndGet();
        }
        result = removedSentences.toArray(result);
      } finally {
        this.rwLock.writeLock().unlock();
      }
    }
    logger.exiting(CLASS_NAME, METHOD_NAME, new Integer(result.length));
    return result;
  }
  
  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(super.toString());
    stringBuilder.append(" : [" );
    this.rwLock.readLock().lock();
    try {      
      for (int i=0; i<=this.lastUsedIndex; i++) {
        stringBuilder.append(this.sentences[i].formattedId());
        stringBuilder.append(", ");
      }
      if (this.lastUsedIndex >= 0) {
        stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());
      }
    } finally {
      this.rwLock.readLock().unlock();
    }
    stringBuilder.append(']');
    return stringBuilder.toString();
  }

}
