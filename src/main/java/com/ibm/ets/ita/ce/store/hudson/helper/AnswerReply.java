package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class AnswerReply {
//	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";
//
//	private Question originalQuestion = null;
//	private TreeMap<String, Answer> answers = null;
//
//	private AnswerReply(Question pQuestion) {
//		this.originalQuestion = pQuestion;
//		this.answers = new TreeMap<String, Answer>();
//	}
//
//	public static AnswerReply create(Question pQuestion) {
//		return new AnswerReply(pQuestion);
//	}
//
//	public Question getOriginalQuestion() {
//		return this.originalQuestion;
//	}
//
//	public TreeMap<String, Answer> getAnswers() {
//		return this.answers;
//	}
//
//	public boolean hasAnswers() {
//		return !this.answers.keySet().isEmpty();
//	}
//
//	public ArrayList<Answer> sortedAnswers() {
//		ArrayList<Answer> result = new ArrayList<Answer>(this.answers.values());
//
//		Collections.sort(result);
//
//		return result;
//	}
//
//	public void addAnswer(ActionContext pAc, Answer pAnswer) {
//		String key = pAnswer.getKey();
//
//		if (this.answers.containsKey(key)) {
//			reportError("Overwriting answer in reply, for key:" + key, pAc);
//		}
//
//		this.answers.put(key, pAnswer);
//	}
//
//	public boolean hasAnswer(String pAnswerKey) {
//		return this.answers.containsKey(pAnswerKey);
//	}
//
//	public void trimAnswersUsing(ArrayList<CeInstance> pInsts) {
//		Answer setAnswer = this.answers.get("cons");
//
//		if (setAnswer != null && setAnswer.hasAnswerSet()) {
//			AnswerResultSet ars = setAnswer.getAnswerSet();
//			ArrayList<String> existingVals = new ArrayList<String>();
//			ArrayList<ArrayList<String>> newRows = new ArrayList<ArrayList<String>>();
//
//			for (ArrayList<String> thisRow : ars.getRows()) {
//				if (thisRow.size() > 1 && thisRow.get(0) != "duration") {
//					String rowInstId = thisRow.get(0);
//					String rowDesc = thisRow.get(1);
//
//					if (pInsts == null) {
//						if (!existingVals.contains(rowDesc)) {
//							existingVals.add(rowDesc);
//
//							ArrayList<String> newRow = new ArrayList<String>();
//							newRow.add(rowDesc);
//							newRows.add(newRow);
//						}
//					} else {
//						for (CeInstance thisInst : pInsts) {
//							if (thisInst.getInstanceName().equals(rowInstId)) {
//								if (!existingVals.contains(rowDesc)) {
//									existingVals.add(rowDesc);
//
//									ArrayList<String> newRow = new ArrayList<String>();
//									newRow.add(rowDesc);
//									newRows.add(newRow);
//								}
//							}
//						}
//					}
//				} else {
//					newRows.add(thisRow);
//				}
//			}
//
//			ars.setRows(newRows);
//		}
//	}
//
}
