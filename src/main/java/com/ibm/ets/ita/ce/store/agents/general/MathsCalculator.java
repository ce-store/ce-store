package com.ibm.ets.ita.ce.store.agents.general;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import static com.ibm.ets.ita.ce.store.names.MiscNames.TRIGTYPE_CON;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import com.ibm.ets.ita.ce.store.agents.CeNotifyHandler;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeInstance;

public class MathsCalculator extends CeNotifyHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";
	
	public static final String PROP_CONNAME = "concept name";
	public static final String PROP_IPNAME = "input property name";
	public static final String PROP_OPNAME = "output property name";
	public static final String PROP_ALG = "algorithm";
	public static final String OPERATOR_MULTIPLY = "*";
	public static final String OPERATOR_DIVIDE = "/";
	public static final String OPERATOR_ADD = "+";
	public static final String OPERATOR_SUBTRACT = "-";

    @Override
    public void notify(ActionContext pAc, String pThingType, String pThingName, String pTriggerName, String pSourceId, String pRuleOrQuery, String pRuleOrQueryName) {
    	
    	if (pThingType.equals(TRIGTYPE_CON)) {
            CeInstance trigger = pAc.getModelBuilder().getInstanceNamed(pAc, pTriggerName);
            String conName = trigger.getSingleValueFromPropertyNamed(PROP_CONNAME);

            if (conName.equalsIgnoreCase(pThingName)) {
//	            System.out.println(trigger.getInstanceName() + "(" + pSourceId + ") " + pThingName);

	            doMathsProcessing(pAc, trigger);
            }
    	}
    }

    private void doMathsProcessing(ActionContext pAc, CeInstance pTrigger) {
    	ArrayList<String> inputPropList = pTrigger.getValueListFromPropertyNamed(PROP_IPNAME);
    	String outputProp = pTrigger.getSingleValueFromPropertyNamed(PROP_OPNAME);
    	String rawAlgorithm = pTrigger.getSingleValueFromPropertyNamed(PROP_ALG);
    	String[] algParts = rawAlgorithm.split(" ");

        for (CeInstance thisInst : pAc.getSessionCreations().getNewInstances()) {
        	if (thisInst.getValueListFromPropertyNamed(outputProp).isEmpty()) {
            	doMathsFor(pAc, thisInst, inputPropList, outputProp, algParts);
        	}
        }
    }

    private void doMathsFor(ActionContext pAc, CeInstance pInst, ArrayList<String> pIpList, String pOp, String[] pAlgParts) {
//    	System.out.println(pInst.getInstanceName());
//    	System.out.println(pIpList);
//    	System.out.println(pOp);
    	
    	ArrayList<Double> values = new ArrayList<Double>();
    	ArrayList<String> operators = new ArrayList<String>();

    	for (String ap : pAlgParts) {
//        	System.out.println("  " + ap);

        	if (ap.startsWith("[")) {
    			String propIdx = ap.replace("[", "").replace("]", "");
    			String propName = pIpList.get(new Integer(propIdx)-1);

    			ArrayList<String> propVals = pInst.getValueListFromPropertyNamed(propName);
    			
    			double total = 0;
    			for (String thisPv : propVals) {
    				total += new Double(thisPv);
    			}
    			values.add(total);
//            	System.out.println("(" + propName + " -> " + propVals + ")");
    		} else {
    			operators.add(ap);
    		}
    	}

//    	System.out.println(values);
//    	System.out.println(operators);

    	Double firstVal = values.get(0);
    	Double secondVal = values.get(1);
    	double result = 0;
    	
    	for (String op : operators) {
        	if (op.equals(OPERATOR_MULTIPLY)) {
        		result = firstVal * secondVal;
    		} else if (op.equals(OPERATOR_DIVIDE)) {
        		result = firstVal / secondVal;
    		} else if (op.equals(OPERATOR_ADD)) {
        		result = firstVal + secondVal;
    		} else if (op.equals(OPERATOR_SUBTRACT)) {
        		result = firstVal - secondVal;
    		} else {
    			reportError("Unknown operator '" + op + "'", pAc);
    		}
    	}
    	
    	String ceText = "the " + pInst.getFirstLeafConceptName() + " '" + pInst.getInstanceName() + "' has '" + result + "' as " + pOp + ".";

    	saveCeText(pAc, ceText);
    }

}
