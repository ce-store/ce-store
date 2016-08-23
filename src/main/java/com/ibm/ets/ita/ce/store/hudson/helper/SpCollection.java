package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.HashMap;

import com.ibm.ets.ita.ce.store.conversation.model.ProcessedWord;
import com.ibm.ets.ita.ce.store.model.CeModelEntity;

public class SpCollection extends SpThing {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private ProcessedWord firstWord = null;
	private ProcessedWord connectorWord = null;
	private HashMap<ProcessedWord, ArrayList<CeModelEntity>> items = new HashMap<ProcessedWord, ArrayList<CeModelEntity>>();

	public SpCollection(ProcessedWord pFirstWord) {
		this.firstWord = pFirstWord;
	}

	public ProcessedWord getFirstWord() {
		return this.firstWord;
	}

	public HashMap<ProcessedWord, ArrayList<CeModelEntity>> getItems() {
		return this.items;
	}

	public void addItem(ProcessedWord pWord, ArrayList<CeModelEntity> pMe) {
		this.items.put(pWord, pMe);
	}

	public ProcessedWord getConnectorWord() {
		return this.connectorWord;
	}

	public void setConnectorWord(ProcessedWord pWord) {
		this.connectorWord = pWord;
	}
	
	public String computeLabel() {
		String result = "";

		result += this.firstWord.getWordText();

		for (ProcessedWord thisWord : this.items.keySet()) {
			result += " " + this.connectorWord.getWordText() + " ";
			result += thisWord.getWordText();
		}

		return result;
	}
}
