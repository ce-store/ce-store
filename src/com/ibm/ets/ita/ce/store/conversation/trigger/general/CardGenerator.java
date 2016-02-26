package com.ibm.ets.ita.ce.store.conversation.trigger.general;

import static com.ibm.ets.ita.ce.store.utilities.FileUtilities.appendToSb;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteCeParameters;

import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.StoreActions;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeSource;

public class CardGenerator {

	private static final String CARD_TELL = "tell card";

	private static final String UID_PREFIX = "msg_";
	private static final String SRC_PREFIX = "conv_";

	private ActionContext ac;

	// Tell cards are used to add valid CE to the store
	public CardGenerator(ActionContext ac) {
		this.ac = ac;
	}

	public void generateTellCard(CeInstance cardInst, String convText, String fromService, String toService) {
		System.out.println("\nGenerate tell card:");
		StringBuilder sb = new StringBuilder();
		TreeMap<String, String> ceParms = new TreeMap<String, String>();

		appendToSb(sb, "there is a %CARD_TYPE% named '%CARD_NAME%' that");
		appendToSb(sb, "  has the timestamp '{now}' as timestamp and");
		appendToSb(sb, "  has '%CONV_TEXT%' as content and");
		appendToSb(sb, "  is from the service '%FROM_SERV%' and");
		appendToSb(sb, "  is to the service '%TO_SERV%'.");

		ceParms.put("%CARD_TYPE%", CARD_TELL);
		ceParms.put("%CARD_NAME%", generateNewUid());
		ceParms.put("%CONV_TEXT%", convText);
		ceParms.put("%FROM_SERV%", fromService);
		ceParms.put("%TO_SERV%", toService);

		String ceSentence = substituteCeParameters(sb.toString(), ceParms);
		String source = generateSrcName(fromService);
		System.out.println(ceSentence);
		saveCeCardText(ceSentence, source);
	}

	private void saveCeCardText(String ceSentence, String source) {
		StoreActions sa = StoreActions.createUsingDefaultConfig(ac);

		CeSource tgtSrc = CeSource.createNewFormSource(ac, source, source);
		sa.saveCeText(ceSentence, tgtSrc);
	}

	private String generateNewUid() {
		return ac.getModelBuilder().getNextUid(ac, UID_PREFIX);
	}

	private String generateSrcName(String sender) {
		return SRC_PREFIX + sender;
	}
}
