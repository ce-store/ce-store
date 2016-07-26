package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.client.web.WebActionContext;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.hudson.helper.AnswerReply;
import com.ibm.ets.ita.ce.store.hudson.helper.ConvConfig;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class SameProcessor {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private WebActionContext wc = null;
	ConvConfig cc = null;
//	private ArrayList<ProcessedWord> allWords = null;
	private ArrayList<CeInstance> instList = null;
	private ArrayList<CeProperty> propList = null;
	private ArrayList<CeConcept> conList = null;
	private ArrayList<CeProperty> matchedProps = null;

	public SameProcessor(WebActionContext pWc, ConvConfig pCc, ArrayList<ProcessedWord> pAllWords, ArrayList<CeInstance> pInstList, ArrayList<CeProperty> pPropList, ArrayList<CeConcept> pConList) {
		this.wc = pWc;
		this.cc = pCc;
//		this.allWords = pAllWords;
		this.instList = pInstList;
		this.propList = pPropList;
		this.conList = pConList;
	}

	public ArrayList<CeInstance> doSameProcessing(AnswerReply pReply) {
		ArrayList<CeInstance> result = null;
		boolean indirect = false;

		this.matchedProps = seekMatchingPropertiesFor(this.instList, pReply);

		for (CeProperty thisProp : this.matchedProps) {
			reportDebug("Matched on property: " + thisProp.toString(), this.wc);
		}

		if (this.matchedProps.isEmpty()) {
			//No properties were matched so walk out one set any try again
			indirect = true;

			for (CeInstance thisInst : this.instList) {
				ArrayList<CeInstance> linkedInsts = new ArrayList<CeInstance>(thisInst.getAllRelatedInstances(this.wc));
				matchedProps.addAll(seekMatchingPropertiesFor(linkedInsts, pReply));
			}
		}

		if (!this.matchedProps.isEmpty()) {
			result = sameProcessingFor(pReply, indirect);
		} else {
			reportError("Could not do 'same' processing as no properties could be matched", this.wc);
			result = new ArrayList<CeInstance>();
		}

		return result;
	}

	private ArrayList<CeProperty> seekMatchingPropertiesFor(ArrayList<CeInstance> pInstList, AnswerReply pReply) {
		ArrayList<CeProperty> matchedProps = new ArrayList<CeProperty>();

		if (pInstList.isEmpty()) {
			reportError("Unable to locate instance in 'same' processing, for: " + pReply.getOriginalQuestion().getQuestionText(), this.wc);
		} else {
			if (this.propList.isEmpty() && this.conList.isEmpty()) {
				reportError("No concepts or properties identified in 'same' processing, for: " + pReply.getOriginalQuestion().getQuestionText(), this.wc);
			} else {
				for (CeInstance thisInst : pInstList) {
					//First try concept-based matching
					if (!this.conList.isEmpty()) {
						for (CeConcept thisCon : this.conList) {
							for (CePropertyInstance thisPi : thisInst.getPropertyInstances()) {
								CeProperty relProp = thisPi.getRelatedProperty();
								
								if (relProp.isObjectProperty()) {
									CeConcept rangeCon = relProp.getRangeConcept();
									
									if (rangeCon.equalsOrHasParent(thisCon)) {
										reportDebug("   Concept-matched PI for " + thisInst.getInstanceName() + " is: " + thisPi.toString(), this.wc);
										if (!matchedProps.contains(relProp)) {
											matchedProps.add(relProp);
										}
									}
								}
							}
						}
					}
					
					//Next try property-based matching
					if (!this.propList.isEmpty()) {
						for (CePropertyInstance thisPi : thisInst.getPropertyInstances()) {
							for (CeProperty thisProp : this.propList) {
								CeProperty rootProp = null;

								if (thisProp.isInferredProperty()) {
									rootProp = thisProp.getStatedSourceProperty();
								} else {
									rootProp = thisProp;
								}
								
								CeProperty relProp = thisPi.getRelatedProperty();
								CeProperty rootRelProp = null;
								
								if (relProp.isInferredProperty()) {
									rootRelProp = relProp.getStatedSourceProperty();
								} else {
									rootRelProp = relProp;
								}
								
								if (rootProp.equals(rootRelProp)) {
									reportDebug("   Property-matched PI for " + thisInst.getInstanceName() + " is: " + thisPi.toString(), this.wc);
									if (!matchedProps.contains(rootProp)) {
										matchedProps.add(rootProp);
									}
								}
							}
						}
					}
				}				
			}
		}

		return matchedProps;
	}

	private ArrayList<CeInstance> sameProcessingFor(AnswerReply pReply, boolean pIndirect) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		if (pIndirect) {
			for (CeInstance thisInst : this.instList) {
				ArrayList<CeInstance> linkedInsts = new ArrayList<CeInstance>(thisInst.getAllRelatedInstances(this.wc));

				for (CeInstance linkedInst : linkedInsts) {
					for (CeInstance retInst : sameProcessing(pReply, linkedInst, thisInst)) {
						if (!result.contains(retInst)) {
							result.add(retInst);
						}
					}
				}
			}
		} else {
			for (CeInstance thisInst : this.instList) {
				for (CeInstance retInst : sameProcessing(pReply, thisInst, thisInst)) {
					if (!result.contains(retInst)) {
						result.add(retInst);
					}						
				}
			}
		}

		return result;
	}
	
	private ArrayList<CeInstance> sameProcessing(AnswerReply pReply, CeInstance pInst, CeInstance pParentInst) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();

		for (CeProperty thisProp : this.propList) {
			String propName = thisProp.getPropertyName();

			if (thisProp.isDatatypeProperty()) {
				String tgtVal = pInst.getSingleValueFromPropertyNamed(propName);

				if ((tgtVal != null) && (!tgtVal.isEmpty())) {
					ArrayList<CeInstance> seekResults = null;

//					if (isDatabaseSeekable(thisProp)) {
//						seekResults = databaseSameProcessingForValue(pReply, pInst, thisProp, tgtVal);
//					} else {
						seekResults = localSameProcessingForValue(pInst, thisProp, tgtVal);
//					}

					if (!seekResults.isEmpty()) {
						if (pParentInst == null) {
							result.addAll(seekResults);
						} else {
							for (CeInstance seekInst : seekResults) {
								if (!seekInst.equals(pParentInst)) {
									result.add(seekInst);
								}
							}
						}
					}
				}
			} else {
				CeInstance tgtInst = pInst.getSingleInstanceFromPropertyNamed(this.wc, propName);

				if (tgtInst != null) {
					ArrayList<CeInstance> seekResults = null;
					
//					if (isDatabaseSeekable(thisProp)) {
//						seekResults = databaseSameProcessingForInstance(pReply, pInst, thisProp, tgtInst);
//					} else {
						seekResults = localSameProcessingForInstance(pInst, thisProp, tgtInst);
//					}

					if (!seekResults.isEmpty()) {
						if (pParentInst == null) {
							result.addAll(seekResults);
						} else {
							for (CeInstance seekInst : seekResults) {
								if (!seekInst.equals(pParentInst)) {
									result.add(seekInst);
								}
							}
						}
					}
				}
			}
		}

		return result;
	}
	
	private ArrayList<CeInstance> localSameProcessingForValue(CeInstance pMatchedInst, CeProperty pMatchedProp, String pTgtVal) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		CeConcept domainCon = pMatchedProp.getDomainConcept();
		ArrayList<CeInstance> possInsts = this.wc.getModelBuilder().getAllInstancesForConceptNamed(this.wc, domainCon.getConceptName());
		String lcTgtVal = pTgtVal.trim().toLowerCase();

		reportDebug("Doing 'same' (value) processing for [matchedInst='" + pMatchedInst.getInstanceName() + "', matchedProp='" + pMatchedProp.formattedFullPropertyName() + "', tgtVal='" + pTgtVal + "']", this.wc);

		for (CeInstance possInst : possInsts) {
			if (possInst != pMatchedInst) {
				for (String possVal : possInst.getValueListFromPropertyNamed(pMatchedProp.getPropertyName())) {
					String lcPossVal = possVal.trim().toLowerCase();

					if (lcPossVal.equals(lcTgtVal)) {
						result.add(possInst);
					}
				}
			}
		}

		return result;
	}

//	private ArrayList<CeInstance> databaseSameProcessingForValue(AnswerReply pReply, CeInstance pMatchedInst, CeProperty pMatchedProp, String pTgtVal) {
//		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
//
//		CeInstance ssInst = sqlSourceForProperty(pMatchedProp);
//		
//		if (ssInst != null) {
//			String tableName = ssInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_TNAME);
//			String columnName = ssInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_CNAME);
//			boolean caseSen = new Boolean(ssInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_CASESEN)).booleanValue();
//			String forcedCase = ssInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_FCASE);
//
//			ArrayList<String> newInstIds = ModifierHandler.makeDatabaseSearchCall(this.wc, this.cc, pReply, this.allWords, tableName, columnName, caseSen, forcedCase, pTgtVal);
//
//			if (newInstIds != null) {
//				for (String thisInstId : newInstIds) {
//					CeInstance newInst = this.wc.getModelBuilder().getInstanceNamed(this.wc, thisInstId);
//					
//					if (newInst != null) {
//						result.add(newInst);
//					}
//				}
//			}
//		} else {
//			reportError("Could not find corresponding SQL source instance for property '" + pMatchedProp + "'", this.wc);
//		}
//
//		return result;
//	}

	private ArrayList<CeInstance> localSameProcessingForInstance(CeInstance pMatchedInst, CeProperty pMatchedProp, CeInstance pTgtInst) {
		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
		CeConcept domainCon = pMatchedProp.getDomainConcept();
		ArrayList<CeInstance> possInsts = this.wc.getModelBuilder().getAllInstancesForConceptNamed(this.wc, domainCon.getConceptName());
		ArrayList<String> propList = new ArrayList<String>();
		
		//TODO: These should not be hardcoded
		propList.add("postcode");
		propList.add("line 1");
		
		reportDebug("Doing 'same' (instance) processing for [matchedInst='" + pMatchedInst.getInstanceName() + "', matchedProp='" + pMatchedProp.formattedFullPropertyName() + "', tgtInst='" + pTgtInst + "']", this.wc);

		for (CeInstance possInst : possInsts) {
			for (CeInstance relInst : possInst.getInstanceListFromPropertyNamed(this.wc, pMatchedProp.getPropertyName())) {
				for (CeInstance matchedRelInst : pMatchedInst.getInstanceListFromPropertyNamed(this.wc, pMatchedProp.getPropertyName())) {
					if (relInst != matchedRelInst) {
						if (relInst.hasSameValuesAs(this.wc, matchedRelInst, propList)) {
							if (!result.contains(possInst)) {
								result.add(possInst);
							}
						}
					}
				}
			}
		}

		return result;
	}
	
//	private ArrayList<CeInstance> databaseSameProcessingForInstance(AnswerReply pReply, CeInstance pMatchedInst, CeProperty pMatchedProp, CeInstance pTgtInst) {
//		ArrayList<CeInstance> result = new ArrayList<CeInstance>();
//
//		CeInstance ssInst = sqlSourceForProperty(pMatchedProp);
//		
//		if (ssInst != null) {
//			String tableName = ssInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_TNAME);
//			ArrayList<CeInstance> usedProps = ssInst.getInstanceListFromPropertyNamed(this.wc, GenericHandler.PROP_MUO);
//			ArrayList<ArrayList<String>> filterInfo = calculateFilterInfoFor(usedProps, pTgtInst);
//			boolean caseSen = new Boolean(ssInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_CASESEN)).booleanValue();
//			String forcedCase = ssInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_FCASE);
//
//			ArrayList<String> newInstIds = ModifierHandler.makeDatabaseFilterCall(this.wc, this.cc, pReply, this.allWords, tableName, filterInfo, caseSen, forcedCase);
//
//			if (newInstIds != null) {
//				for (String thisInstId : newInstIds) {
//					CeInstance newInst = this.wc.getModelBuilder().getInstanceNamed(this.wc, thisInstId);
//					
//					if (newInst != null) {
//						result.add(newInst);
//					}
//				}
//			}
//		} else {
//			reportError("Could not find corresponding SQL source instance for property '" + pMatchedProp + "'", this.wc);
//		}
//
//		return result;
//	}

//	private ArrayList<ArrayList<String>> calculateFilterInfoFor(ArrayList<CeInstance> pSqlMappedPropInsts, CeInstance pTgtInst) {
//		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
//
//		for (CeInstance thisInst : pSqlMappedPropInsts) {
//			CeProperty tgtProp = this.wc.getModelBuilder().getPropertyFullyNamed(thisInst.getInstanceName());
//			String colName = thisInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_COLNAME);
//			String colVal = pTgtInst.getSingleValueFromPropertyNamed(tgtProp.getPropertyName());
//
//			ArrayList<String> newRow = new ArrayList<String>();
//
//			newRow.add(colName);
//			newRow.add(colVal);
//
//			result.add(newRow);
//		}
//		
//		return result;
//	}

//	private boolean isDatabaseSeekable(CeProperty pProp) {
//		return sqlSourceForProperty(pProp) != null;
//	}

//	private CeInstance sqlSourceForProperty(CeProperty pProp) {
//		CeInstance result = null;
//		CeInstance mmInst = pProp.getMetaModelInstance(this.wc);
//
//		if (mmInst != null) {
//			for (CeInstance thisInst : this.wc.getModelBuilder().getAllInstancesForConceptNamed(this.wc, GenericHandler.CON_SQLSRC)) {
//				for (CeInstance propInst : thisInst.getInstanceListFromPropertyNamed(this.wc, GenericHandler.PROP_CORRTO)) {
//					if (propInst.equals(mmInst)) {
//						result = thisInst;
//						break;
//					}
//				}
//			}
//		}
//
//		return result;
//	}

}
