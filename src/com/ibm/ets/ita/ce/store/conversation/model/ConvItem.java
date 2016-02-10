package com.ibm.ets.ita.ce.store.conversation.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.ActionContext;

public abstract class ConvItem extends GeneralItem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private static final String DETERMINER = "a";
	private static final String UID_PREFIX = "conv";

	protected String conceptName = null;
	protected String itemText = null;
	protected String[] delimiterList = null;

	protected abstract void initialise();
	protected abstract void parse(ActionContext pAc);

	public static String getDeterminer() {
		return DETERMINER;
	}
	
	protected ConvItem(ActionContext pAc, String pConName, String pItemText) {
		this.id = pAc.getModelBuilder().getNextUid(pAc, UID_PREFIX);
		this.conceptName = pConName;
		this.itemText = pItemText;
	}

	public String getItemText() {
		return this.itemText;
	}

	protected static ArrayList<String> splitUsing(String pDelim, ArrayList<String> pCurrentList) {
		ArrayList<String> result = new ArrayList<String>();

		for (String thisPart : pCurrentList) {
			String[] splitParts = thisPart.split(pDelim);

			if (splitParts.length == 0) {
				splitParts = new String[1];
				splitParts[0] = thisPart;
			}

			for (String splitPart : splitParts) {
				if (!splitPart.trim().isEmpty()) {
					result.add(splitPart.trim());
				}
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return "(" + this.conceptName + ": " + this.id + ") " + this.itemText;
	}

}