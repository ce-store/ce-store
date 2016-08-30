package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.timestampNow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ModelBuilder;

public abstract class CeModelEntity implements Comparable<CeModelEntity> {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	protected static final String PROP_PLURAL = "plural form";
	protected static final String PROP_PAST = "past tense";

	private static AtomicLong seqNumCtr = new AtomicLong(0);

	protected String name = null;		//No getters and setters on this class... should be implemented (with more specific names) by extending classes
	private long seqNum = CeModelEntity.seqNumCtr.getAndIncrement();
	private long creationDate = ModelBuilder.NO_TS;
	private boolean metaModelGenerated = false;
	private CeAnnotation[] annotations = new CeAnnotation[0];
	private CeSentence[] primarySentences = new CeSentence[0];

	public abstract HashSet<CeSentence> listAllSentences();
	public abstract ArrayList<CeSentence> listSecondarySentences();
	public abstract ArrayList<ArrayList<CeSentence>> listAllSentencesAsPair();

	protected CeModelEntity() {
		this.creationDate = timestampNow();
	}

	public static void resetCounter() {
		seqNumCtr = new AtomicLong(0);
	}

	public static void sortInstancesBySequenceNumber(List<CeInstance> pList) {
		Comparator<CeInstance> comp = new Comparator<CeInstance>() {
			@Override
			public int compare(CeInstance pInst1, CeInstance pInst2) {
				return new Long(pInst1.getSequenceNumber() - pInst2.getSequenceNumber()).intValue();
			}
		};

		Collections.sort(pList, comp);
	}

	public boolean isConcept() {
		return false;
	}

	public boolean isConceptualModel() {
		return false;
	}

	public boolean isInstance() {
		return false;
	}

	public boolean isProperty() {
		return false;
	}

	public boolean isQuery() {
		return false;
	}

	public boolean isRule() {
		return false;
	}

	public boolean isSource() {
		return false;
	}

	public long getSequenceNumber() {
		return this.seqNum;
	}

	public long getCreationDate() {
		return this.creationDate;
	}

	public boolean metaModelHasBeenGenerated() {
		return this.metaModelGenerated;
	}

	public void markAsMetaModelGenerated() {
		this.metaModelGenerated = true;
	}

	public CeAnnotation[] getAnnotations() {
		return this.annotations;
	}

	public int countAnnotations() {
		return this.annotations.length;
	}

	public void addAnnotation(ActionContext pAc, String pAnnoLabel, String pAnnoText, CeSentence pAnnoSen) {
		int currLen = 0;
		CeAnnotation newAnno = CeAnnotation.createAnnotationFrom(pAc, pAnnoLabel, pAnnoText, pAnnoSen);

		currLen = this.annotations.length;
		CeAnnotation[] newArray = new CeAnnotation[currLen + 1];
		System.arraycopy(this.annotations, 0, newArray, 0, currLen);
		this.annotations = newArray;

		this.annotations[currLen] = newAnno;
	}

	private void removeAnnotation(CeAnnotation pAnno) {
		if (hasAnnotation(pAnno)) {
			CeAnnotation[] newArray = new CeAnnotation[this.annotations.length - 1];

			int ctr = 0;
			for (CeAnnotation thisAnno : this.annotations) {
				if (thisAnno != pAnno) {
					newArray[ctr++] = thisAnno;
				}
			}

			this.annotations = newArray;
		}
	}

	public boolean hasAnnotation(CeAnnotation pAnno) {
		boolean result = false;

		for (CeAnnotation thisAnno : this.annotations) {
			if (!result) {
				if (thisAnno == pAnno) {
					result = true;
				}
			}
		}

		return result;
	}

	public CeSentence[] getPrimarySentences() {
		return this.primarySentences;
	}

	public int countPrimarySentences() {
		return this.primarySentences.length;
	}

	public ArrayList<CeSentence> listPrimarySentences() {
		ArrayList<CeSentence> result = new ArrayList<CeSentence>();

		for (CeSentence thisSen : this.primarySentences) {
			result.add(thisSen);
		}

		return result;
	}

	public void addPrimarySentence(CeSentence pSen) {
		if (!hasPrimarySentence(pSen)) {
			int currLen = 0;

			currLen = this.primarySentences.length;
			CeSentence[] newArray = new CeSentence[currLen + 1];
			System.arraycopy(this.primarySentences, 0, newArray, 0, currLen);

			this.primarySentences = newArray;
			this.primarySentences[currLen] = pSen;			
		}
	}

	public void removePrimarySentence(CeSentence pSen) {
		if (hasPrimarySentence(pSen)) {
			CeSentence[] newArray = new CeSentence[this.primarySentences.length - 1];

			int ctr = 0;
			for (CeSentence priSen : this.primarySentences) {
				if (priSen != pSen) {
					newArray[ctr++] = priSen;
				}
			}

			this.primarySentences = newArray;
		}
	}

	public boolean hasPrimarySentence(CeSentence pSen) {
		boolean result = false;

		for (CeSentence priSen : this.primarySentences) {
			if (!result) {
				if (priSen == pSen) {
					result = true;
				}
			}
		}

		return result;
	}

	public boolean hasPrimarySentences() {
		return (getPrimarySentences().length > 0);
	}

	public boolean hasAnySentences() {
		return listAllSentences().isEmpty();
	}

	public HashSet<CeSentence> calculateAnnotationSentences() {
		HashSet<CeSentence> result = new HashSet<CeSentence>();

		for (CeAnnotation thisAnno : getAnnotations()) {
			result.add(thisAnno.getAnnotationSentence());
		}

		return result;
	}

	public void removeAnnotationUsingSentence(CeSentence pSen) {
		CeAnnotation annoToDelete = null;

		for (CeAnnotation thisAnno : getAnnotations()) {
			CeSentence annoSen = thisAnno.getAnnotationSentence();

			if (annoSen.equals(pSen)) {
				annoToDelete = thisAnno;
			}
		}

		if (annoToDelete != null) {
			removeAnnotation(annoToDelete);
		}

	}

	public int countAnnotationSentences() {
		//In this case the count of the sentences is the same as the count of the annotations
		return countAnnotations();
	}

	public boolean isShadowEntity() {
		return !hasPrimarySentences();
	}

	@Override
	public boolean equals(Object pObj) {
		boolean result = false;

		if (pObj != null) {
			if (pObj.hashCode() == hashCode()) {
				//The hash codes match so these objects MAY be the same.  Now check on the strings
				CeModelEntity pMe = (CeModelEntity)pObj;
				result = (identityKey().equals(pMe.identityKey()));
			}
		}
		
		return result;
	}

	@Override
	public int compareTo(CeModelEntity pOtherEntity) {
		return identityKey().compareTo(pOtherEntity.identityKey());
	}

	@Override
	public int hashCode() {
		return identityKey().hashCode();
	}

	public String identityKey() {
		return this.name;
	}

	public String toString() {
	  return (this.name==null) ? super.toString() : this.name;
	}
}