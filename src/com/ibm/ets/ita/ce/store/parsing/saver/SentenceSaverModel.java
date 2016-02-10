package com.ibm.ets.ita.ce.store.parsing.saver;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceModel;

public class SentenceSaverModel extends SentenceSaver {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	public SentenceSaverModel(ActionContext pAc, BuilderSentence pSentence) {
		super (pAc, pSentence);
	}

	protected BuilderSentenceModel getTargetModelSentence() {
		return (BuilderSentenceModel)this.targetSentence;
	}

	protected void saveModelSentence() {
		if (this.targetConcept != null) {
			if (!this.targetConcept.getConceptName().isEmpty()) {
				storeValidSentence();

				if (this.ac.getCeConfig().isSavingCeSentences()) {
					this.targetConcept.addPrimarySentence(this.sentenceInstance);
				}

				saveProperties();
				saveParents();
			} else {
				reportError("Concept name is empty, for sentence: " + this.sentenceText, this.ac);
			}
		} else {
			reportError("Unexpected null targetConcept in SentenceSaverModel, for sentence:" + this.targetSentence.getSentenceText(), this.ac);
		}
	}

	private void saveProperties() {
		ArrayList<CeProperty> propList = null;

		if (getTargetModelSentence().getNewConcept() == null) {
			//Ensure that only the new properties for this concept are processed (not all - they may have come from earlier sentences)
			propList = getTargetModelSentence().getNewProperties();
		} else {
			//This is a new concept so all properties must be processed
			propList = new ArrayList<CeProperty>();
			for (CeProperty thisProp : getTargetModelSentence().getNewConcept().getDirectProperties()) {
				propList.add(thisProp);
			}
		}

		for (CeProperty thisProp : propList) {
			//Before inserting the property ensure that the range concept has been created
			if (thisProp.isObjectProperty()) {
				CeConcept.createOrRetrieveConceptNamed(this.ac, thisProp.getRangeConceptName());
			}

			if (!thisProp.isInferredProperty()) {
				if (this.ac.getCeConfig().isSavingCeSentences()) {
					thisProp.addPrimarySentence(this.sentenceInstance);
				}
			}

			if(thisProp.isObjectProperty()) {
				CeConcept thisConcept = CeConcept.createOrRetrieveConceptNamed(this.ac, thisProp.getRangeConceptName());

				if (thisConcept != null) {
					thisConcept.addSecondarySentence(this.sentenceInstance);
				} else {
					reportError("Unable to locate concept named '" + thisProp.getRangeConceptName() + "' during secondary sentence saving.", this.ac);
				}
			}
		}
	}

	private void saveParents() {
		//Ensure that only the new parents for this concept are processed (not all - they may have come from earlier sentences)
		for (CeConcept thisParent : getTargetModelSentence().getNewParents()) {
			//Before inserting the inheritance ensure that the parent concept has been created
			CeConcept thisConcept = CeConcept.createOrRetrieveConceptNamed(this.ac, thisParent.getConceptName());

			if (thisConcept != null) {
				thisConcept.addSecondarySentence(this.sentenceInstance);
			} else {
				reportError("Unable to locate concept named '" + thisParent.getConceptName() + "' during secondary sentence saving.", this.ac);
			}
		}
	}

}