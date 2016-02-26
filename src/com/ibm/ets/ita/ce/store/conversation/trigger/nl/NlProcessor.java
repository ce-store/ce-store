package com.ibm.ets.ita.ce.store.conversation.trigger.nl;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.conversation.trigger.general.CardGenerator;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public class NlProcessor {

	private static final String PROP_CONTENT = "content";

	private ActionContext ac = null;
	private CardGenerator cg = null;
	private NlTriggerHandler th = null;

	public NlProcessor(ActionContext ac, NlTriggerHandler th) {
		this.ac = ac;
		this.th = th;
		cg = new CardGenerator(ac);
	}

	// Process NL card
	public void process(CeInstance cardInst) {
		String convText = cardInst.getSingleValueFromPropertyNamed(PROP_CONTENT);
		convText = appendDotIfNeeded(convText);
		System.out.println("Conv text: " + convText);

		if (isValidCe(convText)) {
			// Valid CE - generate Tell card
			System.out.println("Valid CE");
			cg.generateTellCard(cardInst, convText, th.getFromInstName(), th.getTellServiceName());
		} else {
			// NL - determine meaning
			System.out.println("Not valid CE");
		}
	}

	// Trim leading and trailing whitespace and append full stop if needed
	private String appendDotIfNeeded(String text) {
		String result = text.trim();

		if (!result.endsWith(".")) {
			result += ".";
		}

		return result;
	}

	// Test text for valid CE
	private boolean isValidCe(String text) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(ac);

		ContainerSentenceLoadResult result = sa.validateCeSentence(text);

		return (result.getInvalidSentenceCount() == 0) && (result.getValidSentenceCount() > 0);
	}
}
