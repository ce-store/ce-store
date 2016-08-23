package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.hudson.handler.GenericHandler;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;

public class ChosenWord {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final int TYPE_MOD_START = 1;
	private static final int TYPE_MOD_END = 2;
	private static final int TYPE_GRP = 3;
	private static final int TYPE_ORD = 4;
	private static final int TYPE_DBCONC = 5;
	private static final int TYPE_DBTAB = 6;
	private static final int TYPE_DBCOL = 7;
	private static final int TYPE_DBCONS = 8;
	private static final int TYPE_CEINST = 9;
	private static final int TYPE_CECON = 10;
	private static final int TYPE_CEPROP = 11;

	private ProcessedWord coreWord = null;
	private int type = -1;
	private CeInstance primaryInstance = null;
	private CeInstance secondaryInstance = null;

	private ChosenWord(ProcessedWord pCoreWord, int pType) {
		this.coreWord = pCoreWord;
		this.type = pType;
	}

	public static ChosenWord createAsStartModifier(ProcessedWord pWord, CeInstance pInst) {
		ChosenWord cw = new ChosenWord(pWord, TYPE_MOD_START);

		cw.primaryInstance = pInst;

		return cw;
	}

	public static ChosenWord createAsEndModifier(ProcessedWord pWord, CeInstance pInst) {
		ChosenWord cw = new ChosenWord(pWord, TYPE_MOD_END);

		cw.primaryInstance = pInst;

		return cw;
	}

	public static ChosenWord createAsOrderer(ProcessedWord pWord, CeInstance pInst) {
		ChosenWord cw = new ChosenWord(pWord, TYPE_ORD);

		cw.primaryInstance = pInst;

		return cw;
	}

	public static ChosenWord createAsGrouper(ProcessedWord pWord, CeInstance pInst) {
		ChosenWord cw = new ChosenWord(pWord, TYPE_GRP);

		cw.primaryInstance = pInst;

		return cw;
	}

	public static ChosenWord createAsDatabaseConcept(ProcessedWord pWord, CeInstance pPriInst, CeInstance pSecInst) {
		ChosenWord cw = new ChosenWord(pWord, TYPE_DBCONC);

		cw.primaryInstance = pPriInst;
		cw.secondaryInstance = pSecInst;

		return cw;
	}

	public static ChosenWord createAsDatabaseTable(ProcessedWord pWord, CeInstance pPriInst, CeInstance pSecInst) {
		ChosenWord cw = new ChosenWord(pWord, TYPE_DBTAB);

		cw.primaryInstance = pPriInst;
		cw.secondaryInstance = pSecInst;

		return cw;
	}

	public static ChosenWord createAsDatabaseColumn(ProcessedWord pWord, CeInstance pInst) {
		ChosenWord cw = new ChosenWord(pWord, TYPE_DBCOL);

		cw.primaryInstance = pInst;

		return cw;
	}

	public static ChosenWord createAsDatabaseConstraint(ProcessedWord pWord, CeInstance pInst) {
		ChosenWord cw = new ChosenWord(pWord, TYPE_DBCONS);

		cw.primaryInstance = pInst;

		return cw;
	}

	public static ChosenWord createAsCeInstance(ProcessedWord pWord, CeInstance pInst) {
		ChosenWord cw = new ChosenWord(pWord, TYPE_CEINST);

		cw.primaryInstance = pInst;

		return cw;
	}

	public static ChosenWord createAsCeConcept(ActionContext pAc, ProcessedWord pWord, CeConcept pCon) {
		ChosenWord cw = null;

		CeInstance mmInst = pCon.retrieveMetaModelInstance(pAc);

		if (mmInst != null) {
			cw = new ChosenWord(pWord, TYPE_CECON);
			cw.primaryInstance = mmInst;
		}

		return cw;
	}

	public String getConceptName() {
		return this.primaryInstance.getInstanceName();
	}

	public String getConceptPluralName(ActionContext pAc) {
		String result = null;
		String conName = this.primaryInstance.getInstanceName();

		CeConcept tgtCon = pAc.getModelBuilder().getConceptNamed(pAc, conName);

		if (tgtCon != null) {
			result = tgtCon.pluralFormName(pAc);
		} else {
			result = getConceptName();
		}

		return result;
	}

	public static ChosenWord createAsCeProperty(ActionContext pAc, ProcessedWord pWord, CeProperty pProp) {
		ChosenWord cw = null;

		CeInstance mmInst = pProp.getMetaModelInstance(pAc);

		if (mmInst != null) {
			cw = new ChosenWord(pWord, TYPE_CEPROP);
			cw.primaryInstance = mmInst;
		}

		return cw;
	}

	public int originalWordPos() {
		return this.coreWord.getWordPos();
	}

	public CeInstance getSecondaryInstance() {
		return this.secondaryInstance;
	}

	public boolean hasPropertyQualifier() {
		boolean result = false;

		if (this.secondaryInstance != null) {
			result = !this.secondaryInstance.getSingleValueFromPropertyNamed(GenericHandler.PROP_PROPQUAL).isEmpty();
		}

		return result;
	}

	public String getPropertyQualifier() {
		String result = null;

		if (this.secondaryInstance != null) {
			result = this.secondaryInstance.getSingleValueFromPropertyNamed(GenericHandler.PROP_PROPQUAL);
		}

		return result;
	}

	public boolean isStartModifier() {
		return this.type == TYPE_MOD_START;
	}

	public boolean isEndModifier() {
		return this.type == TYPE_MOD_END;
	}

	public boolean isModifier() {
		return isStartModifier() || isEndModifier();
	}

	public boolean isGrouper() {
		return this.type == TYPE_GRP;
	}

	public boolean isOrderer() {
		return this.type == TYPE_ORD;
	}

	public boolean isCeInstance() {
		return this.type == TYPE_CEINST;
	}

	public boolean isCeConcept() {
		return this.type == TYPE_CECON;
	}

	public boolean isCeProperty() {
		return this.type == TYPE_CEPROP;
	}

	public String interpretationText(ActionContext pAc, boolean pPluralise) {
		String result = "";

		if (isNamedConcept()) {
			if (this.secondaryInstance != null) {
				String conName = this.secondaryInstance.getInstanceName();

				CeConcept tgtCon = pAc.getModelBuilder().getConceptNamed(pAc, conName);

				if (tgtCon != null) {
					if (pPluralise) {
						result += tgtCon.pluralFormName(pAc);
					} else {
						result += tgtCon.getConceptName();
					}
				} else {
					result += "???";
				}
			}
		} else if (isCeInstance()) {
			result += this.primaryInstance.getFirstInstanceIdentifier(pAc);
		} else {
			if (this.primaryInstance != null) {
				result += this.primaryInstance.getFirstInstanceIdentifier(pAc);
			}
		}

		return result;
	}

	public String interpretationPropertyTextFor(ActionContext pAc, CeConcept pCon, String pLastPropName, boolean pPluralise) {
		String result = "";

		if (pCon == null) {
			result = interpretationText(pAc, pPluralise);
		} else {
			CeConcept thisCon = getConcept(pAc);

			for (CeProperty possProp : pCon.calculateAllDirectProperties(false)) {
				if (possProp.isObjectProperty()) {
					if (!possProp.getPropertyName().equals(pLastPropName)) {
						CeConcept rangeCon = possProp.getRangeConcept();

						if (!rangeCon.isThing()) {
							if (rangeCon.equalsOrHasParent(thisCon)) {
								if (pPluralise) {
									String pf = possProp.pluralFormName(pAc);

									if (!pf.isEmpty()) {
										result += pf;
									} else {
										result += possProp.getPropertyName();
									}
								} else {
									result += possProp.getPropertyName();
								}
							}

							if (thisCon.hasParent(rangeCon)) {
								String pf = possProp.pluralFormName(pAc);
	
								if (!pf.isEmpty()) {
									result += pf;
								} else {
									result += possProp.getPropertyName();
								}
							}
						}
					}
				}
			}
		}

		return result;
	}

	public String interpretationValueTextFor(ActionContext pAc) {
		String propName = "";
		String result = "";

		if (this.secondaryInstance != null) {
			CeInstance optUses = this.secondaryInstance.getSingleInstanceFromPropertyNamed(pAc, GenericHandler.PROP_OPTUSES);

			if (optUses != null) {
				propName = optUses.getSingleValueFromPropertyNamed(GenericHandler.PROP_PROPNAME);
			}
		}

		if (this.primaryInstance != null) {
			if (propName.isEmpty()) {
				result += this.primaryInstance.getFirstInstanceIdentifier(pAc);
			} else {
				String tgtVal = this.primaryInstance.getSingleValueFromPropertyNamed(propName);

				if (tgtVal.isEmpty()) {
					result += this.primaryInstance.getFirstInstanceIdentifier(pAc);
				} else {
					result += tgtVal;
				}
			}
		}

		return result;
	}

	public CeConcept getConcept(ActionContext pAc) {
		CeConcept result = null;

		if (this.secondaryInstance != null) {
			String conName = this.secondaryInstance.getInstanceName();

			result = pAc.getModelBuilder().getConceptNamed(pAc, conName);
		}

		return result;
	}

	public boolean isNamedConcept() {
		//A named concept has no primary instance but has a secondary instance
		return (this.primaryInstance == null) && (this.secondaryInstance != null);
	}

	public String referredInstanceText() {
		String result = "";

		for (CeInstance thisInst : this.coreWord.listReferredExactInstances()) {
			result += thisInst.getInstanceName();
		}

		if (result.isEmpty()) {
			ArrayList<CeInstance> mis = this.coreWord.getMatchingInstances();

			if (mis != null) {
				for (CeInstance thisInst : mis) {
					result += thisInst.getInstanceName();
				}
			}
		}

		return result;
	}

	public String cePropertyText() {
		String result = "";

		if (this.primaryInstance != null) {
			result = this.primaryInstance.getSingleValueFromPropertyNamed(GenericHandler.PROP_PROPNAME);
		}

		return result;
	}

	public boolean cePropertyAppliesTo(ActionContext pAc, CeInstance pInst) {
		boolean result = false;

		CeProperty thisProp = pAc.getModelBuilder().getPropertyFullyNamed(this.primaryInstance.getInstanceName());

		for (CeConcept tgtCon : pInst.getDirectConcepts()) {
			//TODO: Is this the right way round?
			if (thisProp.getDomainConcept().equalsOrHasParent(tgtCon)) {
				result = true;
				break;
			}
		}

		return result;
	}

	public CeInstance ceInstance() {
		return this.primaryInstance;
	}

	@Override
	public String toString() {
		String result = null;
		String firstName = null;
		String cwText = null;

		if (this.primaryInstance != null) {
			firstName = this.primaryInstance.getInstanceName();
		}

		if (this.coreWord != null) {
			cwText = this.coreWord.getWordText();
		}

		if (isModifier()) {
			result = "(Modifier) " + firstName + " [" + cwText + "]";
		} else if (isGrouper()) {
			result = "(Grouper) " + firstName + " [" + cwText + "]";
		} else if (isOrderer()) {
			result = "(Orderer) " + firstName + " [" + cwText + "]";
		} else if (isCeInstance()) {
			result = "(CeInstance) " + firstName + " [" + cwText + "]";
		} else if (isCeConcept()) {
			result = "(CeConcept) " + firstName + " [" + cwText + "]";
		} else if (isCeProperty()) {
			result = "(CeProperty) " + firstName + " [" + cwText + "]";
		} else {
			result = "Unknown type of ChosenWord: " + this.type;
		}

		return result;
	}

	public CeProperty getCeProperty(ActionContext pAc) {
		CeProperty result = pAc.getModelBuilder().getPropertyFullyNamed(this.primaryInstance.getInstanceName());

		return result;
	}

}
