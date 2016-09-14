package com.ibm.ets.ita.ce.store.parsing.saver;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ModelBuilder;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CePropertyValue;
import com.ibm.ets.ita.ce.store.model.CeSequence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFact;

public class SentenceSaverFact extends SentenceSaver {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	protected SentenceSaverFact(ActionContext pAc, BuilderSentence pSentence) {
		super (pAc, pSentence);
	}

	private BuilderSentenceFact getTargetFactSentence() {
		return (BuilderSentenceFact)this.targetSentence;
	}

	public void saveFactSentence() {
		storeValidSentence();

		if (this.targetConcept == null) {
			if (this.targetSentence.isRationaleProcessing()) {
				//Nothing needs to be done here...
				//rationale sentences can have null target concepts (e.g. when the subject is "the value"
				//and they do not need to be saved or processed
			} else {
				//Unable to process sentence due to null target concept
				reportError("Unexpected null targetConcept, for sentence: " + this.sentenceText, this.ac);
			}
		} else {
			processMainInstance();
		}
	}

	private void processMainInstance() {
		String instName = getTargetFactSentence().getInstanceName();
		
		if (!instName.isEmpty()) {
			CeInstance newInst = this.ac.getModelBuilder().getOrCreateInstanceNamed(this.ac, instName);
			this.ac.getSessionCreations().recordNewInstance(newInst);

			//First Save any secondary instances (from "is a" statements)
			processSecondaryInstances(newInst);

			//Save the instance
			saveInstance(this.targetConcept, newInst);

			//Process the properties
			processProperties(newInst);

			//Process the sequences
			processSequences();
		} else {
			reportError("Instance not saved as no name was specified.  For sentence: " + getTargetFactSentence().getSentenceText(), this.ac);
		}
	}

	private void processSecondaryInstances(CeInstance pInst) {
		//Inserts for any other specified concepts (from "is a" statements)
		for (CeConcept thisSecConcept : getTargetFactSentence().getAllSecondaryConcepts()) {
			//Save the instance (abstract and concrete) for each secondary concept
			saveInstance(thisSecConcept, pInst);
		}
	}

	private void processSequences() {
		//TODO: Replace this with the final version
		for (CeSequence thisSeq : getTargetFactSentence().getSequences()) {
			thisSeq.tempDebug(this.ac);
		}
	}

	private void processProperties(CeInstance pInst) {
		for (CePropertyInstance thisPi : getTargetFactSentence().retrieveAllProperties()) {
			thisPi.setRelatedInstance(this.ac, pInst);
			processPropertyValuesFor(thisPi);
			mergePropertyInstance(pInst, thisPi);
		}
	}

	private void processPropertyValuesFor(CePropertyInstance pPi) {
		CeProperty thisProp = pPi.getRelatedProperty();

		if (thisProp != null) {
			if (thisProp.isSingleCardinality()) {
				//There is only one property value to be processed
				processPropertyValue(pPi, pPi.getSingleOrFirstValue());
			} else {
				//There are multiple property values so process each one in turn
				for (String thisVal : pPi.getValueListWithDuplicates()) {
					processPropertyValue(pPi, thisVal);
				}
			}
		} else {
			reportError("Unexpected null property encountered, for sentence: " + this.sentenceText, this.ac);
		}
	}

	private void processPropertyValue(CePropertyInstance pPropInst, String pValue) {
		if (!pValue.isEmpty()) {
			CeConcept rangeConcept = pPropInst.getSingleOrFirstRangeConcept(this.ac);

			if (rangeConcept != null) {
				if (!pValue.isEmpty()) {
					CeInstance relInst = this.ac.getModelBuilder().getOrCreateInstanceNamed(this.ac, pValue);
					this.ac.getSessionCreations().recordNewInstance(relInst);
					relInst.addConceptAndParents(rangeConcept);

					if (this.ac.getCeConfig().isSavingCeSentences()) {
						relInst.addSecondarySentence(this.sentenceInstance);
					}

					//Save the instance (regardless of whether it is new or not)
					getModelBuilder().saveInstance(this.ac, relInst);

					CeConcept[] directConcepts = relInst.getDirectConcepts();

			        for (CeConcept concept : directConcepts) {
			            this.ac.getCurrentSource().addAffectedConcept(concept);
			        }
				} else {
					reportError("Property not saved as no name was specified for target instance.  For property '" + pPropInst.getPropertyName() + "' in sentence: " + getTargetFactSentence().getSentenceText(), this.ac);
				}
			} else {
				//Constant values can be ignored as there is nothing extra to create in the model
			}
		}
	}

	private void mergePropertyInstance(CeInstance pInst, CePropertyInstance pPi) {
		//The builder sentence will always have separate property instances, but these need to be merged
		//as multiple values on a single property instance on the final instance
		CePropertyInstance existingPropInst = pInst.getPropertyInstanceNamed(pPi.getPropertyName());
		if (existingPropInst == null) {
			pInst.addPropertyInstance(pPi);
			existingPropInst = pPi;

			//Notify the action context that this property has been affected by this execution
			this.ac.getCurrentSource().addAffectedProperty(pPi.getRelatedProperty());
		} else {
			if (!pPi.isSingleCardinality()) {
				for (CePropertyValue thisPv : pPi.getPropertyValues()) {
					existingPropInst.addPropertyValue(this.ac, thisPv);

					//Notify the action context that this property has been affected by this execution
					this.ac.getCurrentSource().addAffectedProperty(pPi.getRelatedProperty());
				}
			} else {
				CePropertyValue existingPv = existingPropInst.getPropertyValues()[0];
				CePropertyValue thisPv = pPi.getPropertyValues()[0];

				if (!existingPv.getValue().equals(thisPv.getValue())) {
					reportWarning("Adding unmatching property value on single cardinality property (" + pPi.toString() + ") for instance '" + pInst.getInstanceName() + "'.", this.ac);
				}

				existingPropInst.addPropertyValue(this.ac, thisPv);

				//Notify the action context that this property has been affected by this execution
				this.ac.getCurrentSource().addAffectedProperty(pPi.getRelatedProperty());
			}
		}
		
		if (existingPropInst.getRelatedProperty().isObjectProperty()) {
			//Add the referring property instance to each of the related instances
			ModelBuilder mb = this.ac.getModelBuilder();
			for (String instName : existingPropInst.getValueList()) {
				if (!instName.isEmpty()) {
					CeInstance relInst = mb.getInstanceNamed(this.ac, instName);
					
					if (relInst != null) {
						relInst.addReferringPropertyInstance(existingPropInst);
					} else {
						reportWarning("No instance named '" + instName + "' found during setting up of property references", this.ac);
					}
				}
			}
		}
	}

	private void saveInstance(CeConcept pConcept, CeInstance pInst) {
        //Add the concept and parents and record this primary sentence
        pInst.addConceptAndParents(pConcept);
        if (this.ac.getCeConfig().isSavingCeSentences()) {
            pInst.addPrimarySentence(this.sentenceInstance);
        }

        CeConcept[] directConcepts = pInst.getDirectConcepts();

        for (CeConcept concept : directConcepts) {
            this.ac.getCurrentSource().addAffectedConcept(concept);
        }
    }

	@Override
	public String toString() {
		return "SentenceSaverFact for: " + this.targetSentence.toString();
	}

}
