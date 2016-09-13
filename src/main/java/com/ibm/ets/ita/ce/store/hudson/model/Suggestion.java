package com.ibm.ets.ita.ce.store.hudson.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class Suggestion implements Comparable<Suggestion> {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private String originalText = null;
	private String beforeText = null;
	private String afterText = null;
	private String wholeText = null;

	public static Suggestion create(String pOt, String pBt, String pAt) {
		return new Suggestion(pOt, pBt, pAt);
	}

	private Suggestion(String pOt, String pBt, String pAt) {
		//Private to ensure static creators are used
		this.originalText = pOt;
		this.beforeText = pBt;
		this.afterText = pAt;
		this.wholeText = "";

		if (pBt != null) {
			this.wholeText += pBt;
		}
		
		if (pOt != null) {
			this.wholeText += pOt;
		}

		if (pAt != null) {
			this.wholeText += pAt;
		}
	}

	public String getOriginalText() {
		return this.originalText;
	}

	public boolean hasOriginalText() {
		return (this.originalText != null) && (!this.originalText.isEmpty());
	}

	public String getBeforeText() {
		return this.beforeText;
	}

	public boolean hasBeforeText() {
		return (this.beforeText != null) && (!this.beforeText.isEmpty());
	}

	public String getAfterText() {
		return this.afterText;
	}

	public boolean hasAfterText() {
		return (this.afterText != null) && (!this.afterText.isEmpty());
	}

	public String getWholeText() {
		return this.wholeText;
	}

	public int compareTo(Suggestion pOtherSugg) {
		return this.wholeText.length() - pOtherSugg.getWholeText().length();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("(Suggestion): ");

		if (hasBeforeText()) {
			sb.append(this.beforeText);
		}

		sb.append("|");

		if (hasOriginalText()) {
			sb.append(this.originalText);
		}
		
		sb.append("|");

		if (hasAfterText()) {
			sb.append(this.afterText);
		}

		return sb.toString();
	}

}
