package com.ibm.ets.ita.ce.store.conversation.processor;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.CON_INTERESTING;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFact;
import com.ibm.ets.ita.ce.store.parsing.processor.ProcessorCe;

public class InterestingThingsProcessor {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	ActionContext ac = null;

	private InterestingThingsProcessor(ActionContext pAc) {
		this.ac = pAc;
	}

	public static String generateInterestingThingsFromNl(ActionContext pAc, ResultOfAnalysis pResult) {
		InterestingThingsProcessor ip = new InterestingThingsProcessor(pAc);

		return ip.doNlProcessing(pResult);
	}
	
	public static String generateInterestingThingsFromCe(ActionContext pAc, String pCeText) {
		InterestingThingsProcessor ip = new InterestingThingsProcessor(pAc);

		return ip.doCeProcessing(pCeText);
	}
	
	private String doCeProcessing(String pCeText) {
		String result = null;

		//Process the specified text
		StoreActions sa = StoreActions.createUsingDefaultConfig(this.ac);
		ProcessorCe procCe = sa.parseCeSentence(pCeText);

		if (procCe != null) {
			ArrayList<CeInstance> mainInstances = listMainInstancesFor(procCe);
			ArrayList<CeInstance> mentionedInstances = listMentionedInstancesFor(procCe);

			result = calculateInterestingContentFor(mentionedInstances, mainInstances);
		} else {
			result = ES;
		}

		return result;
	}
	
	private ArrayList<CeInstance> listMainInstancesFor(ProcessorCe pProcCe) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		ArrayList<BuilderSentence> valSens = pProcCe.getValidatedSentences();
		
		if (valSens != null) {
			for (BuilderSentence thisBs : valSens) {
				if (thisBs.isFactSentence()) {
					BuilderSentenceFact fBs = (BuilderSentenceFact)thisBs;

					//Only add the main (subject) instance name
					String instName = fBs.getInstanceName();
					CeInstance mainInst = this.ac.getModelBuilder().getInstanceNamed(this.ac, instName);

					if (mainInst != null) {
						result.add(mainInst);
					}
				}
			}
		}

		return result;
	}

	private ArrayList<CeInstance> listMentionedInstancesFor(ProcessorCe pProcCe) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		ArrayList<BuilderSentence> valSens = pProcCe.getValidatedSentences();
		
		if (valSens != null) {
			for (BuilderSentence thisBs : pProcCe.getValidatedSentences()) {
				if (thisBs.isFactSentence()) {
					BuilderSentenceFact fnBs = (BuilderSentenceFact)thisBs;
					
					//Only add instance names of mentioned instances
					for (CePropertyInstance oPi : fnBs.getObjectProperties()) {
						for (String instName : oPi.getValueList()) {
							CeInstance thisInst = this.ac.getModelBuilder().getInstanceNamed(this.ac, instName);
							
							if (thisInst != null) {
								result.add(thisInst);
							}
						}
					}
				}
			}
		}

		return result;
	}

	private String doNlProcessing(ResultOfAnalysis pResult) {
		return calculateInterestingContentFor(pResult.getMatchedInstances().values(), null);
	}

	private String calculateInterestingContentFor(Collection<CeInstance> pInstances, Collection<CeInstance> pSuppressedInstances) {
		String result = null;

		for (CeInstance thisInst : pInstances) {
			String directInstText = calculateDirectInstanceTextFor(this.ac, thisInst, pSuppressedInstances);
			String relInstText = calculateRelatedInstanceTextFor(this.ac, thisInst, pSuppressedInstances);
			String refInstText = calculateReferencedInstanceTextFor(this.ac, thisInst, pSuppressedInstances);

			if ((!directInstText.isEmpty()) || (!relInstText.isEmpty()) || (!refInstText.isEmpty())) {
				if (result == null) {
					result = "I also found the following interesting things:\n";
				} else {
					result += "\n";
				}
				
				if (!directInstText.isEmpty()) {
					result += directInstText;

					if ((relInstText != null) && (!relInstText.isEmpty())) {
						result += " that\n";
					} else {
						result += ".\n";
					}

					if (!relInstText.isEmpty()) {
						result += relInstText;
					}
				}

				if (!refInstText.isEmpty()) {
					result += "\n" + refInstText;
				}
			}
		}

		return result;
	}
	
	private static String calculateDirectInstanceTextFor(ActionContext pAc, CeInstance pInst, Collection<CeInstance> pSuppressedInstances) {
		String result = "";

		if ((pSuppressedInstances == null) || (!pSuppressedInstances.contains(pInst))) {
			if (pInst.isConceptNamed(pAc, CON_INTERESTING)) {
				//Check whether the instance itself is interesting
				String intConNames = extractInterestingConceptNamesFrom(pAc, pInst);
				
				if (intConNames != null) {
					String qualifier = null;	//TODO: Abstract this
					if ((intConNames.startsWith("a") || intConNames.startsWith("e") || intConNames.startsWith("i") || intConNames.startsWith("o") || intConNames.startsWith("u"))) {
						qualifier = "an";
					} else {
						qualifier = "a";
					}
					
					result += pInst.getInstanceName() + " is " + qualifier + " " + intConNames;
				}
			}
		}

		return result;
	}

	private static String calculateRelatedInstanceTextFor(ActionContext pAc, CeInstance pInst, Collection<CeInstance> pSuppressedInstances) {
		String result = "";

		//Check for related instances that are interesting
		for (CePropertyInstance relPi : pInst.getPropertyInstances()) {
			CeProperty relProp = relPi.getRelatedProperty();

			if (relProp.isObjectProperty()) {
				for (CeInstance relInst : relPi.getValueInstanceList(pAc)) {
					if ((pSuppressedInstances == null) || (!pSuppressedInstances.contains(relInst))) {
						String intConNames = extractInterestingConceptNamesFrom(pAc, relInst);
	
						if (intConNames != null) {
							String rangeText = null;
							
							if (intConNames.equals(pInst.getFirstLeafConceptName())) {
								rangeText = "";
							} else {
								rangeText = " the " + intConNames;
							}
	
							if (relProp.isFunctionalNoun()) {
								//Functional noun
								result += "  has " + rangeText + " " + relInst.getInstanceName() + " as " + relPi.getPropertyName();
							} else {
								//Verb singular
								result += "  " + relPi.getPropertyName() + rangeText + " " + relInst.getInstanceName();
							}
							result += "\n";
						}
					}
				}
			}
		}

		return result;
	}

	private String calculateReferencedInstanceTextFor(ActionContext pAc, CeInstance pInst, Collection<CeInstance> pSuppressedInstances) {
		String result = "";

		//Check for referring instances that are interesting
		TreeMap<CeProperty, ArrayList<CeInstance>> intRefPis = new TreeMap<CeProperty, ArrayList<CeInstance>>();
		for (CePropertyInstance thisPi : pInst.getReferringPropertyInstances()) {
			if (thisPi.getRelatedProperty().isObjectProperty()) {
				CeInstance refInst = thisPi.getRelatedInstance();
				if (refInst != pInst) {
					if (refInst.isConceptNamed(this.ac, CON_INTERESTING)) {
						if ((pSuppressedInstances == null) || (!pSuppressedInstances.contains(refInst))) {
							ArrayList<CeInstance> currList = intRefPis.get(thisPi.getRelatedProperty());
							
							if (currList == null) {
								currList = new ArrayList<CeInstance>();
								intRefPis.put(thisPi.getRelatedProperty(), currList);
							}
		
							if (!currList.contains(refInst)) {
								currList.add(refInst);
							}
						}
					}
				}
			}
		}

		for (CeProperty thisProp : intRefPis.keySet()) {
			ArrayList<CeInstance> instList = intRefPis.get(thisProp);
			
			if (instList.size() > 5) {
				//More than 5
				CeInstance firstInst = instList.get(0);
				result += "there are " + instList.size() + " " + pluralFormForConceptName(firstInst.getFirstLeafConceptName()) + " that";
				if (thisProp.isFunctionalNoun()) {
					//Functional noun
					result += " have " + pInst.getInstanceName() + " as " + thisProp.pluralFormName(pAc);
				} else {
					//Verb singular
					result += " " + thisProp.pluralFormName(pAc) + " " + pInst.getInstanceName();
				}
				result += ".\n";
			} else {
				//5 or less
				for (CeInstance refInst : instList) {
					String intConNames = extractInterestingConceptNamesFrom(pAc, refInst);

					if (intConNames != null) {
						result += "the " + intConNames + " " + refInst.getInstanceName();
						if (thisProp.isFunctionalNoun()) {
							//Functional noun
							result += " has the " + pInst.getFirstLeafConceptName() + " " + pInst.getInstanceName() + " as " + thisProp.getPropertyName();
						} else {
							//Verb singular
							result += " " + thisProp.getPropertyName() + " the " + pInst.getFirstLeafConceptName() + " " + pInst.getInstanceName();
						}
						result += ".\n";
					}
				}
			}
		}

		return result;
	}
	
	private String pluralFormForConceptName(String pConName) {
		String result = null;
		
		if ((pConName != null) && (!pConName.isEmpty())) {
			CeConcept thisCon = this.ac.getModelBuilder().getConceptNamed(this.ac, pConName);
			
			if (thisCon != null) {
				result = thisCon.pluralFormName(this.ac);
			} else {
				result = "";
			}
		} else {
			result = "";
		}

		return result;
	}

	private static String extractInterestingConceptNamesFrom(ActionContext pAc, CeInstance pInst) {
		String result = null;
		boolean moreThanOne = false;
		CeConcept intCon = pAc.getModelBuilder().getConceptNamed(pAc, CON_INTERESTING);
		
		if (intCon != null) {
			for (CeConcept dirCon : pInst.getDirectConcepts()) {
				if (dirCon.equalsOrHasParent(intCon)) {
					if (result == null) {
						result = "";
					} else {
						result += ", ";
						moreThanOne = true;
					}
					result += dirCon.getConceptName();
				}
			}
		} else {
			reportWarning("Unable to locate '" + CON_INTERESTING + "' concept so interesting concept names cannot be calculated", pAc);
		}
		
		if (moreThanOne) {
			result = "(" + result + ")";
		}
		
		return result;
	}

}
