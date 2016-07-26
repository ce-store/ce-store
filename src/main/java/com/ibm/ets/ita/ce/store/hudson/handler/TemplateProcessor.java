package com.ibm.ets.ita.ce.store.hudson.handler;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;
import java.util.Collections;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;

public class TemplateProcessor {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	//TODO: Abstract this out into CE
	private static final String DEFAULT_NEXT_SEPARATOR = ", ";
	private static final String DEFAULT_MULTI_VALUE_SEPARATOR = "|";

	private static final String MODE_STANDARD = "STANDARD";
	private static final String MODE_HEADLINE = "HEADLINE";
	private static final String MODE_TEMPLATE = "TEMPLATE";
	private static final String MODE_NESTED = "NESTED";
	private static final String MODE_NONE = "NONE";

	private static final String FORMAT_PRETTYDAYS = "pretty days";
	
	private ActionContext ac = null;
	private ArrayList<ProcessedWord> allWords = null;
	
	public TemplateProcessor(ActionContext pAc, ArrayList<ProcessedWord> pWords) {
		this.ac = pAc;
		this.allWords = pWords;
	}

	public String createAnswerFor(ArrayList<CeInstance> pInstCluster) {
		return createAnswerFor(pInstCluster, MODE_STANDARD);
	}

	private String createAnswerFor(ArrayList<CeInstance> pInstCluster, String pMode) {
		CeInstance mainInst = pInstCluster.get(0);

		String instName = mainInst.getInstanceName();
		String id = mainInst.getFirstInstanceIdentifier(this.ac);
		String result = null;
		String idBit = id;
		String instBit = id;
		String conBit = computeConBit(pInstCluster);
		
		if ((conBit != null) && (!conBit.isEmpty())) {
			if (!instBit.equals(instName)) {
				instBit += " (" + instName + ")";
			}

			if (pMode.equals(MODE_NONE)) {
				result = instBit;
			} else if (pMode.equals(MODE_STANDARD) && ModifierHandler.isExpandQuestion(this.ac, this.allWords)) {
				result = untemplatedTextFor(pInstCluster, idBit, conBit);
			} else {
				if (isTemplated(mainInst)) {
					CeInstance templateInst = templateFor(mainInst, pMode);
	
					if (templateInst != null) {
						result = doTemplateProcessing(idBit, instBit, conBit, pInstCluster, templateInst, pMode);
					} else {
						//No template is defined so just use the id and concepts and list the properties
						reportWarning("No entity template defined for the templated thing '" + idBit + "'", this.ac);
						if (pMode.equals(MODE_STANDARD)) {
							result = untemplatedTextFor(pInstCluster, idBit, conBit);
						}
					}
				} else {
					//A non-templated concept does not return anything extra
					//i.e. No properties or relations are mentioned
					if (pMode.equals(MODE_NESTED)) {
						result = instBit;
					} else {
						result = instBit + conBit + ".";
					}
				}
			}
		} else {
			//No matching concept was found so return a null result
			result = null;
		}

		return result;
	}
	
	private String getNextModeFor(String pMode) {
		String nextMode = null;

		if (pMode.equals(MODE_NESTED)) {
			nextMode = MODE_NONE;
		} else if (pMode.equals(MODE_STANDARD)) {
			nextMode = MODE_NESTED;
		} else if (pMode.equals(MODE_HEADLINE)) {
			nextMode = MODE_NESTED;
		} else if (pMode.equals(MODE_TEMPLATE)) {
			nextMode = MODE_NESTED;
		} else {
			nextMode = pMode;
		}

		return nextMode;
	}

	private ArrayList<CeConcept> computeLeavesFor(CeInstance pInst) {
		ArrayList<CeConcept> result = new ArrayList<CeConcept>();

//		for (CeConcept thisCon : pInst.listAllConcepts()) {
//			if (!thisCon.hasAnyChildren()) {
//				if (!result.contains(thisCon)) {
//					result.add(thisCon);
//				}
//			}
//		}

		result = pInst.getAllLeafConcepts();

		return result;
	}

	private String computeConBit(ArrayList<CeInstance> pInstCluster) {
		String result = null;
		
		ArrayList<CeConcept> sortedCons = new ArrayList<CeConcept>();
		
		for (CeInstance thisInst : pInstCluster) {
			for (CeConcept thisCon : computeLeavesFor(thisInst)) {
				if (!sortedCons.contains(thisCon)) {
					sortedCons.add(thisCon);
				}
			}
		}
		
		Collections.sort(sortedCons);

		for (CeConcept thisCon : sortedCons) {
			if (!thisCon.equalsOrHasParentNamed(this.ac, GenericHandler.CON_CONFCON)) {
				if (result == null) {
					result = "";
				} else {
					result += " and ";
				}

				if (thisCon.isQualifiedWithAn()) {
					result += "an ";
				} else {
					result += "a ";
				}
				
				result += thisCon.getConceptName();
			}
		}

		if ((result != null) && (!result.isEmpty())) {
			result = " is " + result;
		}

		return result;
	}

	private String untemplatedTextFor(ArrayList<CeInstance> pInstCluster, String pIdBit, String pConBit) {
		String result = pIdBit + pConBit;

		String props = listPropertiesFor(pInstCluster, MODE_TEMPLATE);

		if (!props.isEmpty()) {
			result += "\n" + props;
		}

		return result;
	}

	private String doTemplateProcessing(String pIdBit, String pInstBit, String pConBit, ArrayList<CeInstance> pInstCluster, CeInstance pTplInst, String pMode) {
		String overallResult = "";
		boolean warnAboutMissedProps = pTplInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_WAMP).toUpperCase().equals("TRUE");

		for (CeInstance mainInst : pInstCluster) {
			String result = null;

			for (CeConcept possCon : mainInst.listAllConcepts()) {
				CeInstance mmConInst = possCon.retrieveMetaModelInstance(this.ac);
				String nextSep = "";
				
				if (mmConInst.isConceptNamed(this.ac, GenericHandler.CON_TEMPTHING)) {
					result = templateStringFor(pTplInst);
					result = result.replace("{ID}", pIdBit);
					result = result.replace("{INST}", pInstBit);
					result = result.replace("{CONS}", pConBit);
					
					for (CeProperty thisProp : possCon.calculateAllProperties().values()) {
						CeInstance propTplInst = propertyTemplateInstanceFor(pTplInst, thisProp, mainInst);
						String multiValSep = "";
						String filterOp = null;
						String filterVal = null;
						String dataFormat = null;
						
						if (propTplInst != null) {
							multiValSep = propTplInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_MVS);
							nextSep = propTplInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_PROPSEP);
							filterOp = propTplInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_FILTOP);
							filterVal = propTplInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_FILTVAL);
							dataFormat = propTplInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_DATAFORMAT);
						} else {
							multiValSep = DEFAULT_MULTI_VALUE_SEPARATOR;
							nextSep = DEFAULT_NEXT_SEPARATOR;
						}
	
						String propMarker = "{" + thisProp.getPropertyName() + "}";
						String valList = "";
	
						if (thisProp.isDatatypeProperty()) {
							//Datatype property
							for (String thisVal : mainInst.getValueListFromPropertyNamed(thisProp.getPropertyName())) {
								if (!valList.isEmpty()) {
									valList += multiValSep;
								}
	
								if ((filterOp != null) && (!filterOp.isEmpty())) {
									if ((filterVal != null) && (!filterVal.isEmpty())) {
										//TODO: Support more filter types and abstract these values
										if (filterOp.equals(">")) {
											//TODO: Try converting to int
											if (thisVal.compareTo(filterVal) > 0) {
												valList += formatValue(thisVal, dataFormat);
											}
										}
									}
								} else {
									valList += formatValue(thisVal, dataFormat);
								}
							}
						} else {
							if (!thisProp.getPropertyName().equals(GenericHandler.PROP_SAMEAS)) {
								ArrayList<String> previousVals = new ArrayList<String>();

								//Object property
								for (CeInstance thisInst : mainInst.getInstanceListFromPropertyNamed(this.ac, thisProp.getPropertyName())) {
									ArrayList<CeInstance> wrappedInst = new ArrayList<CeInstance>();
									wrappedInst.add(thisInst);
									wrappedInst.addAll(thisInst.getInstanceListFromPropertyNamed(this.ac, GenericHandler.PROP_SAMEAS));

									String nextMode = getNextModeFor(pMode);
									String thisVal = createAnswerFor(wrappedInst, nextMode);
									
									if (!previousVals.contains(thisVal)) {
										if (!valList.isEmpty()) {
											valList += multiValSep;
										}
		
										valList += thisVal;
										previousVals.add(thisVal);
									}
									
								}
							}
						}
	
						if (!valList.isEmpty()) {
							if (warnAboutMissedProps) {
								if (!result.contains(propMarker)) {
									reportWarning("No template marker found for '" + thisProp.formattedFullPropertyName() + "' (value is " + valList + ")", this.ac);
								}
							}
	
							if (!valList.isEmpty()) {
								valList += nextSep;
							}
							
							String preText = null;
							String suffText = null;
							
							if (propTplInst != null) {
								preText = propTplInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_PRETEXT);
								suffText = propTplInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_SUFFTEXT);
							} else {
								preText = "";
								suffText = "";
							}
	
							if (!preText.isEmpty()) {
								valList = preText + valList;
							}
	
							if (!suffText.isEmpty()) {
								valList += suffText;
							}
	
							result = result.replace(propMarker, valList);
						} else {
							//There is no value for this property so just replace it
							result = result.replace(propMarker, "");
						}
					}
				}
			}

			overallResult += result;
		}

		return overallResult;
	}

	private String formatValue(String pRawValue, String pDataFormat) {
		String result = null;

		if ((pDataFormat == null) || (pDataFormat.isEmpty())) {
			result = pRawValue;
		} else {
			if (pDataFormat.equals(FORMAT_PRETTYDAYS)) {
				result = formatPrettyDays(pRawValue);
			} else {
				reportWarning("Value formatting not yet implemented, for '" + pRawValue + "' with format '" + pDataFormat + "'", this.ac);
				result = pRawValue;
			}
		}

		return result;
	}

	private String formatPrettyDays(String pRawValue) {
		String result = "";
		double daysPerYear = 365.242;	//TODO: There must be a better way of calculating an exact value
		int daysPerMonth = 30;
		
		//TODO: This assumes the value is already in days.  Should really check the data format...
		int intVal = new Integer(pRawValue).intValue();
		int daysVal = 0;
		int monthsVal = 0;
		int yearsVal = 0;

		if (intVal > 30) {
			int daysLeft = 0;

			if (intVal > 365) {
				yearsVal = new Double(Math.floor(intVal / daysPerYear)).intValue();
				daysLeft = new Double(intVal - (yearsVal * daysPerYear)).intValue();				
			} else {
				daysLeft = intVal;
			}
			
			if (daysLeft > 30) {
				monthsVal = new Double(Math.floor(daysLeft / daysPerMonth)).intValue();
				daysLeft = new Double(daysLeft - (monthsVal * daysPerMonth)).intValue();				
			} else {
				monthsVal = 0;
				daysLeft = intVal;
			}
			
			daysVal = daysLeft;
		} else {
			daysVal = intVal;
		}
		
		if (yearsVal > 0) {
			result += yearsVal + " year";
			
			if (yearsVal > 1) {
				result += "s";
			}
		}
		
		if (monthsVal > 0) {
			if (!result.isEmpty()) {
				result += "{sep}";
			}
			result += monthsVal + " month";

			if (monthsVal > 1) {
				result += "s";
			}
		}
		
		//Only show days if no years
		if (yearsVal == 0) {
			if ((monthsVal == 0) || (daysVal > 0)) {
				result = result.replace("{sep}", " ");
				if (!result.isEmpty()) {
					result += "{sep}";
				}
			}
			result += daysVal + " day";
			
			if (daysVal != 1) {
				result += "s";
			}
		}

		result = result.replace("{sep}", " and ");

		return result;
	}
	
	private CeInstance propertyTemplateInstanceFor(CeInstance pEntTpl, CeProperty pTgtProp, CeInstance pTgtInst) {
		CeInstance result = null;
		
		for (CeInstance propTplInst : pEntTpl.getInstanceListFromPropertyNamed(this.ac, GenericHandler.PROP_PROPTPL)) {
			CeInstance corrTo = propTplInst.getSingleInstanceFromPropertyNamed(this.ac, GenericHandler.PROP_CORRTO);
			
			if (corrTo.getInstanceName().equals(pTgtProp.formattedFullPropertyName())) {
				CeInstance boundDomCon = propTplInst.getSingleInstanceFromPropertyNamed(this.ac, GenericHandler.PROP_APPWHEN);
				
				if (boundDomCon != null) {
					CeConcept tgtCon = this.ac.getModelBuilder().getConceptNamed(this.ac, boundDomCon.getInstanceName());
					
					if (tgtCon != null) {
						if (pTgtInst.isConceptNamed(this.ac, tgtCon.getConceptName())) {
							result = propTplInst;
							break;
						}
					}
						
				} else {
					result = propTplInst;
					break;
				}
			}
		}
		
		return result;
	}
	
	private String templateStringFor(CeInstance pTplInst) {
		return pTplInst.getSingleValueFromPropertyNamed(GenericHandler.PROP_TPLSTR);
	}
	
	private boolean isTemplated(CeInstance pInst) {
		boolean result = false;
		CeConcept tcCon = this.ac.getModelBuilder().getConceptNamed(this.ac, GenericHandler.CON_TEMPTHING);

		for (CeConcept thisCon : pInst.listAllConcepts()) {
			CeInstance mmInst = thisCon.retrieveMetaModelInstance(this.ac);
			
			if (mmInst != null) {
				if (mmInst.isDirectConcept(tcCon)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	private CeInstance templateFor(CeInstance pInst, String pMode) {
		CeInstance result = null;
		CeInstance defaultInst = null;
		CeConcept tcCon = this.ac.getModelBuilder().getConceptNamed(this.ac, GenericHandler.CON_TEMPTHING);

		for (CeConcept thisCon : pInst.listAllConcepts()) {
			CeInstance mmInst = thisCon.retrieveMetaModelInstance(this.ac);

			if (mmInst != null) {
				if (mmInst.isDirectConcept(tcCon)) {
					for (CeInstance tplInst : mmInst.getInstanceListFromPropertyNamed(this.ac, GenericHandler.PROP_ENTTEMP)) {
						defaultInst = tplInst;
						
						for (CeInstance tplMode : tplInst.getInstanceListFromPropertyNamed(this.ac, GenericHandler.PROP_CORRTO)) {
							if (tplMode.getInstanceName().toLowerCase().equals(pMode.toLowerCase())) {
								result = tplInst;
								break;
							}
						}
					}
				}
			}
		}
		
		//If no result was found then use the default instance (the last one processed)
		if (result == null) {
//			reportDebug("Using default template instance (" + defaultInst.getInstanceName() + ") for '" + pInst.getInstanceName() + "', mode: " + pMode, this.ac);
			result = defaultInst;
		}

		return result;
	}
	
	private String listPropertiesFor(ArrayList<CeInstance> pInstCluster, String pMode) {
		StringBuilder sb = new StringBuilder();

		for (CeInstance thisInst : pInstCluster) {
			for (CePropertyInstance thisPi : thisInst.getPropertyInstances()) {
				if (thisPi.getRelatedProperty().isDatatypeProperty()) {
					String valList = "";
					
					for (String thisVal : thisPi.getValueList()) {
						if (!valList.isEmpty()) {
							valList += ", ";
						}
	
						valList += thisVal;
					}
	
					sb.append("  " + thisPi.getRelatedProperty().getPropertyName() + " -> " + valList + "\n");
				} else {
					String idList = "";
					
					for (CeInstance relInst : thisPi.getValueInstanceList(this.ac)) {
						ArrayList<CeInstance> wrappedInst = new ArrayList<CeInstance>();
						wrappedInst.add(relInst);
						wrappedInst.addAll(relInst.getInstanceListFromPropertyNamed(this.ac, GenericHandler.PROP_SAMEAS));
	
						if (!idList.isEmpty()) {
							idList += ", ";
						}
	
						idList += createAnswerFor(wrappedInst, pMode);
					}
	
					sb.append("  " + thisPi.getRelatedProperty().getPropertyName() + " -> " + idList + "\n");
				}
			}
		}

		return sb.toString();
	}

}
