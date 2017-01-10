package com.ibm.ets.ita.ce.store.parsing.saver;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_COLON;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_TILDE;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.util.ArrayList;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeModelEntity;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentence;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceFact;
import com.ibm.ets.ita.ce.store.parsing.builder.BuilderSentenceRuleOrQuery;

public class SentenceSaverAnnotation extends SentenceSaver {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private BuilderSentence lastSentence = null;

	public SentenceSaverAnnotation(ActionContext pAc, BuilderSentence pSentence, BuilderSentence pLastSentence) {
		super (pAc, pSentence);

		this.lastSentence = pLastSentence;
	}

	protected void saveAnnotationSentence() {
		storeValidSentence();

		String words[] = this.sentenceText.split(TOKEN_COLON);
		String annoLabel = words[0] + TOKEN_COLON;
		String annoText = this.sentenceText.substring(annoLabel.length(), this.sentenceText.length()).trim();

		if (this.lastSentence == null) {
			//There is no last sentence so save this to the source
			CeSource tgtSource = this.ac.getLastSource();
			if (tgtSource != null) {
				saveAnnotation(tgtSource, annoLabel, annoText);
			} else {
				reportWarning("Unable to link annotation text '" + annoText + "' to source because it is null", this.ac);
			}
		} else {
			//There is a last sentence so determine the type
			this.lastSentence.addAnnotation(this.ac, this.sentenceInstance);

			if (this.lastSentence.isModelSentence()) {
				//Model sentence - save annotation to the concept or property
				CeConcept tgtCon = this.lastSentence.getTargetConcept();

				if (tgtCon != null) {
					if (isPropertyAnnotation(annoText)) {
						CeProperty tgtProp = extractTargetPropertyFrom(annoText, tgtCon);

						if (tgtProp != null) {
							//The property matches one defined against the concept, so save against that property
							saveAnnotation(tgtProp, annoLabel, extractPropertyAnnotationFrom(annoText, tgtProp));
						}
					} else {
						//Note a property annotation, save save against the concept
						saveAnnotation(tgtCon, annoLabel, annoText);
					}
				} else {
					reportWarning("Unable to link annotation text '" + annoText + "' to concept because it is null (" + this.lastSentence.getSentenceText() + ")", this.ac);
				}
			} else if (this.lastSentence.isFactSentence()) {
				if (this.lastSentence.isValid()) {
					//Fact sentence - save to the instance
					String tgtInstName = ((BuilderSentenceFact)this.lastSentence).getInstanceName();					
					CeInstance tgtInst = this.ac.getModelBuilder().getInstanceNamed(this.ac, tgtInstName);

					if (tgtInst != null) {
						//Save against the instance
						//TODO: Is this correct?  It is more likely that the annotation applies to the sentence rather than the instance...
						saveAnnotation(tgtInst, annoLabel, annoText);
					} else {
						reportWarning("Unable to link annotation text '" + annoText + "' to instance named '" + tgtInstName + "' because it could not be located", this.ac);
					}
				} else {
					reportWarning("Unable to link annotation text '" + annoText + "' to fact sentence because the sentence was not valid", this.ac);
				}
			} else if (this.lastSentence.isQuerySentence()) {
				//Query sentence - save to the query
				CeQuery tgtQuery = ((BuilderSentenceRuleOrQuery)this.lastSentence).getQuery();

				if (tgtQuery != null) {
					saveAnnotation(tgtQuery, annoLabel, annoText);
				} else {
					reportWarning("Unable to link annotation text '" + annoText + "' to query because it could not be located", this.ac);
				}
			} else if (this.lastSentence.isRuleSentence()) {
				//Rule sentence - save to the rule
				CeRule tgtRule = ((BuilderSentenceRuleOrQuery)this.lastSentence).getRule();

				if (tgtRule != null) {
					saveAnnotation(tgtRule, annoLabel, annoText);
				} else {
					reportWarning("Unable to link annotation text '" + annoText + "' to rule because it could not be located", this.ac);
				}
			} else {
				reportWarning("Unable to determine preceeding sentence type for annotation sentence '" + annoText + "', so linking to source instead", this.ac);

				CeSource targetSource = this.ac.getLastSource();
				if (targetSource != null) {
					targetSource.addAnnotation(this.ac, annoLabel, annoText, this.sentenceInstance);
				} else {
					reportWarning("Unable to link annotation text '" + annoText + "' to source because it is null", this.ac);
				}
			}
		}
	}

	private void saveAnnotation(CeModelEntity pTgtEntity, String pAnnoLabel, String pAnnoText) {
		pTgtEntity.addAnnotation(this.ac, pAnnoLabel, pAnnoText, this.sentenceInstance);
	}

	private static boolean isPropertyAnnotation(String pAnnoText) {
		return pAnnoText.startsWith(TOKEN_TILDE);
	}

	private CeProperty extractTargetPropertyFrom(String pAnnoText, CeConcept pTgtCon) {
		CeProperty result = null;
		String[] words = pAnnoText.split(TOKEN_TILDE);

		if (words.length > 1) {
			String tgtPropName = words[1].trim();

			ArrayList<CeProperty> propList = pTgtCon.calculatePropertiesNamed(tgtPropName);

			if (!propList.isEmpty()) {
				result = propList.get(0);
				if (propList.size() > 1) {
					reportWarning("Multiple properties named '" + tgtPropName + "' on concept '" + pTgtCon.getConceptName() + "' for property annotation: " + pAnnoText, this.ac);
				}
			} else {
				reportWarning("Unable to find property named '" + tgtPropName + "' on concept '" + pTgtCon.getConceptName() + "' for property annotation: " + pAnnoText, this.ac);
			}
		}

		return result;
	}

	private static String extractPropertyAnnotationFrom(String pAnnoText, CeProperty pProp) {
		String propAnnoTag = TOKEN_TILDE + " " + pProp.getPropertyName() + " " + TOKEN_TILDE;
		return pAnnoText.replace(propAnnoTag, "").trim();
	}

}
