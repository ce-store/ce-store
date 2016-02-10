package com.ibm.ets.ita.ce.store.conversation.model;

import java.util.ArrayList;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

public class FinalItem extends GeneralItem {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	private ArrayList<ExtractedItem> exItemList = null;

	public FinalItem(ExtractedItem pExItem) {
		this.exItemList = new ArrayList<ExtractedItem>();
		
		this.exItemList.add(pExItem);
	}

	public ArrayList<ExtractedItem> getExtractedItems() {
		return this.exItemList;
	}

	public ExtractedItem getFirstExtractedItem() {
		ExtractedItem result = null;

		if (!this.exItemList.isEmpty()) {
			result = this.exItemList.get(0);
		}

		return result;
	}

	public ExtractedItem getLastExtractedItem() {
		ExtractedItem result = null;

		if (!this.exItemList.isEmpty()) {
			result = this.exItemList.get(this.exItemList.size() - 1);
		}

		return result;
	}

	public void addExtractedItem(ExtractedItem pEi) {
		if (!this.exItemList.contains(pEi)) {
			this.exItemList.add(pEi);
		}
	}
	
	public boolean isConceptItem() {
		boolean result = false;
		ExtractedItem fei = getFirstExtractedItem();

		if (fei != null) {
			result = fei.isConceptItem();
		}

		return result;
	}

	public boolean isPropertyItem() {
		boolean result = false;
		ExtractedItem fei = getFirstExtractedItem();

		if (fei != null) {
			result = fei.isPropertyItem();
		}

		return result;
	}

	public boolean isInstanceItem() {
		boolean result = false;
		ExtractedItem fei = getFirstExtractedItem();

		if (fei != null) {
			result = fei.isInstanceItem();
		}

		return result;
	}

}