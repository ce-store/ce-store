package com.ibm.ets.ita.ce.store.parsing.saver;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;

public class SentenceSaverGeneral extends SentenceSaver {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	protected SentenceSaverGeneral(ActionContext pAc, BuilderSentence pSentence) {
		super(pAc, pSentence);
	}

}
