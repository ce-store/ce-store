package com.ibm.ets.ita.ce.store.api;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Properties;

import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeConceptualModel;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CeQuery;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.CeSource;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerSearchResult;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.model.rationale.CeRationaleReasoningStep;

/*
 * Things to discuss in JavaDoc
 * 
 * command sentences
 * conceptual model sentences
 * concept
 * shadow concept
 * instance
 * property
 * fact (concept instance and relationship/property) sentences
 * rule sentences
 * query sentences
 * annotations
 * direct relationships
 * primary and secondary relationships
 * reasoning steps
 */
/**
 * The CEStore interface provides the primary entry point for users embedding
 * the CEStore.
 * 
 * TODO - add details of use
 * 
 * @author IBM
 * @version $Rev$
 * @since $Date$
 */
public interface CEStore {

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

	// MANAGEMENT

	/**
	 * Remove all entries from the CE Store. This includes all concepts, rules
	 * and instances.
	 */
	ContainerSentenceLoadResult resetStore(String pStartingUid);

	/**
	 * Remove all instances from the CE Store
	 * 
	 * @return object containing statistics relating to the CE sentence load
	 *         operation
	 */
	ContainerSentenceLoadResult emptyInstances();

	/**
	 * Prints various store statistics to standard out
	 */
	void showStoreStatistics();

	/**
	 * Prints various store statistics to standard out
	 */
	void runStoreStatistics();

	// MODEL SENTENCE LOADING

	/**
	 * Parse and store CE sentences from the provided URL.
	 * 
	 * @param pUrl
	 *            a URL that points to a collection of CE sentences
	 * @return object containing statistics relating to the CE sentence load
	 *         operation
	 */
	ContainerSentenceLoadResult loadSentencesFromUrl(String pUrl, String pSrcName);

	/**
	 * Parse and store the provided CE model sentence text.
	 * 
	 * @param pCeText
	 *            the CE sentence
	 * @param pFormName
	 *            the name used as the source of the CE sentence
	 * @param pMode
	 *            StoreActions.MODE_NORMAL or StoreActions.MODE_VALIDATE. If set
	 *            to StoreActions.MODE_VALIDATE the sentence is just validates
	 *            and the results are returned, the sentence is not stored.
	 * @return object containing statistics relating to the CE sentence load
	 *         operation
	 */
	ContainerSentenceLoadResult loadSentencesFromForm(String pCeText, String pFormName, int pMode);

	ContainerSentenceLoadResult loadSentencesFromFormForSpecifiedSource(String pCeText, CeSource pTgtSrc, int pMode);

	/**
	 * Parse and store CE sentences from the named file.
	 * 
	 * @param pFullyQualifiedFilename
	 *            the name of the file containing CE setnences
	 * @return object containing statistics relating to the CE sentence load
	 *         operation
	 */
	ContainerSentenceLoadResult loadSentencesFromFile(String pFullyQualifiedFilename);

	// CONCEPTUAL MODEL

	/**
	 * Return a list of all of the concepts that the store knows about.
	 * 
	 * @return the list of concepts
	 */
	ArrayList<CeConcept> listAllConcepts();

	/**
	 * Return a list of all of the concepts that are children of the named
	 * concept. By child here we mean that a returned concept will be related to
	 * the named concept by an "is-a" relationship.
	 * 
	 * @param pConceptName
	 *            the name of the parent concept
	 * @return the names of the child concept. Each child "is-a" parent concept
	 */
	ArrayList<CeConcept> listAllChildConceptsFor(String pConceptName);

	/**
	 * Return a list of all of the concepts that are directly related to the
	 * named concept by the "is-a" relationship. Direct here means that the
	 * relationship between the child and the named concept is a single step,
	 * for example, consider the relationships:
	 * </p>
	 * A is-a B <br/>
	 * B is-a C
	 * <p/>
	 * A is related directly to B but is related indirectly to C
	 * 
	 * @param pConceptName
	 *            the name of the parent concept
	 * @return the names of the child concept. Each child "is-a" parent concept
	 *         directly
	 */
	ArrayList<CeConcept> listDirectChildConceptsFor(String pConceptName);

	/**
	 * Return a list of all of the concepts that are parents of the named
	 * concept. By parent here we mean that the named concept will be related to
	 * a returned concept by an "is-a" relationship.
	 * 
	 * @param pConceptName
	 *            the name of the child concept
	 * @return the names of the parent concepts.
	 */
	ArrayList<CeConcept> listAllParentConceptsFor(String pConceptName);

	/**
	 * Return a list of all of the concepts that are directly related to the
	 * named concept by the "is-a" relationship. Direct here means that the
	 * relationship between the child and the named concept is a single step,
	 * for example, consider the relationships:
	 * </p>
	 * A is-a B <br/>
	 * B is-a C
	 * <p/>
	 * A is related directly to B but is related indirectly to C
	 * 
	 * @param pConceptName
	 *            the name of the parent concept
	 * @return the names of the child concept.
	 */
	ArrayList<CeConcept> listDirectParentConceptsFor(String pConceptName);

	/**
	 * Return the detals of the named concept.
	 * 
	 * @param pConceptName
	 *            the concept for which details will be returned
	 * @return object containing the details of the concept
	 */
	CeConcept getConceptDetails(String pConceptName);

	/**
	 * Return a list of the properties defined between two named concepts. For
	 * example,
	 * <p/>
	 * the person fred is the brother of the person bill. <br/>
	 * DOMAIN:person / PROPERTY:is the brother of / RANGE:person
	 * <p/>
	 * the person fred has 27 as age. <br/>
	 * DOMAIN:person / PROPERTY:age / RANGE:constant
	 * 
	 * @param pDomainName
	 *            the name of the concept on which the property is defined
	 * @param pRangeName
	 *            the name of the concept to which the property refers. This
	 *            name can be set to "constant" which means that the property
	 *            refers to a constant value and not a concept instance
	 * @return list of properties.
	 */
	ArrayList<CeProperty> listProperties(String pDomainName, String pRangeName);

	/**
	 * Shadow concepts are concepts that are generated by implication rather
	 * than by explicit statement. For example, if the user submits the
	 * sentence:
	 * <p/>
	 * conceptualise the ~ person ~ P that is an agent.
	 * <p/>
	 * Without conceptualising the concept agent then agent is a shadow concept.
	 * A shadow concept behaves in the same way as any other concept it's just
	 * not defined explicitly by the user.
	 * 
	 * @return list of shadow concepts.
	 */
	ArrayList<CeConcept> listShadowConcepts();

	// CONCEPT INSTANCES / PROPERTIES

	/**
	 * Return a count of the number of instances that the store holds for the
	 * named concept
	 * 
	 * @param pConceptName
	 *            the concept for which instances will be counted
	 * @return instance count
	 */
	int countInstances(String pConceptName);

	/**
	 * Return a list of the concept instances for the named concept.
	 * 
	 * @param pConceptName
	 *            the concept for which instances will be returned
	 * @return list of concept instances
	 */
	ArrayList<CeInstance> listInstances(String pConceptName);

	/**
	 * Return a list of the concept instances given a concept object.
	 * 
	 * @param pTargetConcept
	 *            the concept for which instances will be returned
	 * @return list of concept instances
	 */
	ArrayList<CeInstance> listAllInstanceDetails(CeConcept pTargetConcept);

	/**
	 * Return a list of the exact concept instances given a concept object. i.e.
	 * those that are instances of only that single concept, and are not also
	 * classified as another concept.
	 * 
	 * @param pTargetConcept
	 *            the concept for which instances will be returned
	 * @return list of concept instances
	 */
	ArrayList<CeInstance> listAllExactInstanceDetails(CeConcept pTargetConcept);

	/**
	 * Return a list of instance name/property pairs name that refer to the
	 * named instance
	 * 
	 * @param pInstName
	 *            the instance name for which references will be returned
	 * @return list of instance name/property pairs
	 */
	ArrayList<ArrayList<String>> listReferences(String pInstName);

	/**
	 * Shadow instances are instances that are generated by implication rather
	 * than by explicit statement. For example, if the user submits the
	 * sentence:
	 * <p/>
	 * the person fred is the brother of the person bill.
	 * <p/>
	 * Without providing a sentence that defined bill and its properties then
	 * bill is a shadow instance. A shadow instance behaves in the same way as
	 * any other instance it's just not defined explicitly by the user.
	 * 
	 * @return list of shadow instances.
	 */
	ArrayList<CeInstance> listShadowInstances();

	/**
	 * Unreferenced instances are instances that are not referenced by any
	 * other, instance
	 *
	 * @return list of unreferenced instances.
	 */
	ArrayList<CeInstance> listUnreferencedInstances(boolean pIgnoreMetaModel);

	/**
	 * Remove all the instances for the named concept from the store.
	 * 
	 * @param pConceptName
	 *            the concept for which instances will be removed.
	 */
	void deleteAllInstancesForConceptNamed(String pConceptName);

	/**
	 * Remove the named instance from the store.
	 * 
	 * @param pInstName
	 *            the name of the instance to remove.
	 */
	void deleteInstanceNamed(String pInstName);

	// SENTENCES

	/**
	 * Return a list of all the CE sentences.
	 * 
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listAllSentences();

	/**
	 * Return a list of all the CE sentences used to create the conceptual
	 * model.
	 * 
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listAllModelSentences();

	/**
	 * Return a list of all the CE sentences used to create facts (concept
	 * instances and relationships).
	 * <p/>
	 * A normal, or unqualified, sentence reads as follows:
	 * <p/>
	 * the person fred is the brother of the person jim.
	 * <p/>
	 * A qualified sentence reads something like:
	 * <p/>
	 * it is true that the person fred is the brother of the person jim.
	 * 
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listAllNormalFactSentences();

	/**
	 * Return a list of all the CE sentences used to create qualified facts
	 * (concept instances and relationships).
	 * <p/>
	 * A normal, or unqualified, sentence reads as follows:
	 * <p/>
	 * the person fred is the brother of the person jim.
	 * <p/>
	 * A qualified sentence reads something like:
	 * <p/>
	 * it is true that the person fred is the brother of the person jim.
	 * 
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listAllQualifiedFactSentences();

	/**
	 * Return a list of all the CE sentences used to create rules
	 * 
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listAllRuleSentences();

	/**
	 * Return a list of all the CE sentences used to create queries
	 * 
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listAllQuerySentences();

	/**
	 * Return a list of all the CE sentences used to create annotations
	 * 
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listAllAnnotationSentences();

	/**
	 * Return a list of all the CE sentences that describe commands
	 * 
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listAllCommandSentences();

	/**
	 * Return a list of all the CE sentences that store has parsed that are
	 * valid
	 * 
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listAllValidSentences();

	/**
	 * Return a list of all the CE sentences that store has parsed that are
	 * invalid
	 * 
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listAllInvalidSentences();

	/**
	 * Return a list of all the CE sentences that are used in some way to define
	 * the instance: For example,
	 * <p/>
	 * the person fred is the brother of the person bill.
	 * <p/>
	 * For the instance "fred" this is considered to be a primary sentence. For
	 * the instance "bill" this is considered to be a secondary sentence. This
	 * is because it defines a property on fred but not on bill.
	 * 
	 * @param pInstName
	 *            the name of the instance for which sentences should be
	 *            returned
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listPrimarySentencesForInstance(String pInstName);

	/**
	 * Return a list of all the CE sentences that refer to, but don't define,
	 * the instance: For example,
	 * <p/>
	 * the person fred is the brother of the person bill.
	 * <p/>
	 * For the instance "fred" this is considered to be a primary sentence. For
	 * the instance "bill" this is considered to be a secondary sentence. This
	 * is because it defines a property on fred but not on bill.
	 * 
	 * @param pInstName
	 *            the name of the instance for which sentences should be
	 *            returned
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listSecondarySentencesForInstance(String pInstName);

	/**
	 * Return all the sentences that refer to the named instance regardless of
	 * whether they are primary or secondary sentences.
	 * 
	 * @param pInstName
	 *            the name of the instance for which sentences should be
	 *            returned
	 * @return list containing two lists. One of primary sentences on of
	 *         secondary sentences.
	 */
	ArrayList<ArrayList<CeSentence>> listAllSentencesForInstance(String pInstName);

	/**
	 * Return a list of all the CE sentences that are used in some way to define
	 * the concept: For example,
	 * <p/>
	 * conceptualise the ~ person ~ P that is an agent.
	 * <p/>
	 * For the concept "person" this is considered to be a primary sentence. For
	 * the concept "agent" this is considered to be a secondary sentence. This
	 * is because it defines person but not agent.
	 * 
	 * @param pConceptName
	 *            the name of the concept for which sentences should be returned
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listPrimarySentencesForConcept(String pConceptName);

	/**
	 * Return a list of all the CE sentences that refer to, but don't define,
	 * the concept: For example,
	 * <p/>
	 * conceptualise the ~ person ~ P that is an agent.
	 * <p/>
	 * For the concept "person" this is considered to be a primary sentence. For
	 * the concept "agent" this is considered to be a secondary sentence. This
	 * is because it defines person but not agent.
	 * 
	 * @param pConceptName
	 *            the name of the concept for which sentences should be returned
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listSecondarySentencesForConcept(String pConceptName);

	/**
	 * Return all the sentences that refer to the named concept regardless of
	 * whether they are primary or secondary sentences.
	 * 
	 * @param pConceptName
	 *            the name of the concept for which sentences should be returned
	 * @return list containing two lists. One of primary sentences on of
	 *         secondary sentences.
	 */
	ArrayList<ArrayList<CeSentence>> listAllSentencesForConcept(String pConceptName);

	/**
	 * CE sentences come from somewhere. This somewhere is called a source. A
	 * source could be, for example, a file, a URL or a user interface form.
	 * This method returns a list of all of the sources that the store knows
	 * about. From the sources the sentences that the source provided can be
	 * retrieved.
	 * 
	 * @return list of CE sentence sources
	 */
	ArrayList<CeSource> listAllSources();

	/**
	 * CE sentences come from somewhere. This somewhere is called a source. A
	 * source could be, for example, a file, a URL or a user interface form.
	 * This method returns a list of all of the sources that the store knows
	 * about. From the sources the sentences that the source provided can be
	 * retrieved.
	 * 
	 * @return the specified CE Source
	 */
	CeSource getSourceDetailsFor(String pSrcId);

	/**
	 * CE model sentences define concepts within a conceptual model. Each source
	 * that contains model sentences can optionally define the conceptual model
	 * to which the conceptualise sentences apply, or a default conceptual model
	 * is used.
	 * 
	 * @return list of conceptual models
	 */
	ArrayList<CeConceptualModel> listAllConceptualModels();

	/**
	 * CE model sentences define concepts within a conceptual model. Each source
	 * that contains model sentences can optionally define the conceptual model
	 * to which the conceptualise sentences apply, or a default conceptual model
	 * is used.
	 * 
	 * @return details for conceptual model
	 */
	CeConceptualModel getConceptualModelDetails(String pCmName);

	/**
	 * Return all the CE setences that came from the named source.
	 * 
	 * @param pSourceId
	 *            the name of the source for which sentences should be returned
	 * @return list of CE sentences
	 */
	ArrayList<CeSentence> listAllSentencesForSource(String pSourceId);

	/**
	 * Remove all the sentence with the provided source id from the store.
	 * 
	 * @param pSrcId
	 *            the id of the source for which sentences are to be removed.
	 * @return object containing statistics relating to the remove operation
	 */
	ContainerSentenceLoadResult deleteSentencesFromSourceById(String pSrcId);

	/**
	 * Remove all the sentence from the named agent from the store.
	 * 
	 * @param pSrcAgentId
	 *            the name of the agent for which sentences are to be removed.
	 * @return object containing statistics relating to the remove operation
	 */
	ContainerSentenceLoadResult deleteSentencesFromSourceByAgentId(String pSrcAgentId);

	/**
	 * Remove all the sentence with the provided source name from the store.
	 * 
	 * @param pSrcName
	 *            the name of the source for which sentences are to be removed.
	 * @return object containing statistics relating to the remove operation
	 */
	ContainerSentenceLoadResult deleteSentencesFromSourceByName(String pSrcName);

	/**
	 * Remove the named sentence from the store.
	 * 
	 * @param pSenId
	 *            the id of the sentence to remove.
	 */
	void deleteSentence(String pSenId);

	/**
	 * Validate that the provided text is a valid CE sentence, or otherwise.
	 * 
	 * @param pCeText
	 *            the text to validate
	 * @return object containing statistics relating to validation operation
	 */
	public ContainerSentenceLoadResult validateCeSentence(String pCeText);

	// QUERIES / RULES

	/**
	 * Return details of the named query.
	 * 
	 * @param pName
	 *            the named of the query.
	 * @return object containing the details of the query.
	 */
	CeQuery getQueryDetails(String pName);

	/**
	 * Return details of the named rule.
	 * 
	 * @param pName
	 *            the named of the rule.
	 * @return object containing the details of the rule.
	 */
	CeRule getRuleDetails(String pName);

	/**
	 * Return a list of all of the queries that the store knows about.
	 * 
	 * @return list of queries
	 */
	ArrayList<CeQuery> listAllQueries();

	/**
	 * Return a list of all of the rules that the store knows about.
	 * 
	 * @return list of rules
	 */
	ArrayList<CeRule> listAllRules();

	/**
	 * Takes a CE query sentence as a string an executes it returning the
	 * results.
	 * 
	 * @param pCeQuery
	 *            the query CE sentence
	 * @param pStartTs
	 *            start time stamp
	 * @param pEndTs
	 *            end time stamp
	 * @return object containing the results.
	 */
	ContainerCeResult executeUserSpecifiedCeQuery(String pCeQuery, String pStartTs, String pEndTs);

	/**
	 * Takes a CE query sentence as a string an executes it returning the
	 * results.
	 * 
	 * @param pCeQuery
	 *            the query CE sentence
	 * @param pSuppressCE
	 *            whether or not to suppress the CE column (default=false)
	 * @param pStartTs
	 *            start time stamp
	 * @param pEndTs
	 *            end time stamp
	 * @return object containing the results.
	 */
	ContainerCeResult executeUserSpecifiedCeQuery(String pCeQuery, boolean pSuppressCE, String pStartTs, String pEndTs);

	/**
	 * Executes a named (and previously saved) CE query sentence.
	 * 
	 * @param pCeQueryName
	 *            the name of the query CE sentence
	 * @param pStartTs
	 *            start time stamp
	 * @param pEndTs
	 *            end time stamp
	 * @return object containing the results.
	 */
	ContainerCeResult executeUserSpecifiedCeQueryByName(String pCeQueryName, String pStartTs, String pEndTs);

	/**
	 * Executes a named (and previously saved) CE query sentence.
	 * 
	 * @param pCeQueryName
	 *            the name of the query CE sentence
	 * @param pSuppressCE
	 *            whether or not to suppress the CE column (default=false)
	 * @param pStartTs
	 *            start time stamp
	 * @param pEndTs
	 *            end time stamp
	 * @return object containing the results.
	 */
	ContainerCeResult executeUserSpecifiedCeQueryByName(String pCeQueryName, boolean pSuppressCE, String pStartTs,
			String pEndTs);

	/**
	 * Takes a CE rule sentence as a string an executes it returning the
	 * results.
	 * 
	 * @param pCeRule
	 *            the rule CE sentence
	 * @param pStartTs
	 *            start time stamp
	 * @param pEndTs
	 *            end time stamp
	 * @return object containing the results.
	 */
	ContainerCeResult executeUserSpecifiedCeRule(String pCeRule, String pStartTs, String pEndTs);

	/**
	 * Executes a named (and previously saved) CE rule sentence.
	 * 
	 * @param pCeRuleName
	 *            the name of the query CE sentence
	 * @param pStartTs
	 *            start time stamp
	 * @param pEndTs
	 *            end time stamp
	 * @return object containing the results.
	 */
	ContainerCeResult executeUserSpecifiedCeRuleByName(String pCeRuleName, String pFormat, String pStartTs,
			String pEndTs);

	/**
	 * Execute all of the CE rule sentences that the store knows about.
	 * Resulting conclusions will be written back into the store.
	 * 
	 * @return object containing statistics relating to the rule execution
	 */
	ContainerSentenceLoadResult executeInferenceRules();

	// RATIONALE

	/**
	 * Return a list of all of the rationale reasoning steps that the store
	 * knows about.
	 * 
	 * @return list of rationale reasoning steps.
	 */
	ArrayList<CeRationaleReasoningStep> listAllRationale();

	/**
	 * Return a list the rationale reasoning steps that were created due to the
	 * specified sentence
	 * 
	 * @param pSen
	 *            reasoning steps created by the sentence with this id are
	 *            returned.
	 * @return list of rationale reasoning steps.
	 */
	ArrayList<CeRationaleReasoningStep> listRationaleForSentence(CeSentence pSen);

	/**
	 * Return a list the rationale reasoning steps that were created due to the
	 * named rule
	 * 
	 * @param pRuleName
	 *            reasoning steps created by this named rule are returned.
	 * @return list of rationale reasoning steps.
	 */
	ArrayList<CeRationaleReasoningStep> listRationaleForRule(String pRuleName);

	/**
	 * Return a list the rationale reasoning steps that mention the named
	 * concept.
	 * 
	 * @param pConName
	 *            reasoning steps that mention this concept name are returned
	 * @param pCheckPremise
	 *            when set true the premise sentences of each reasoning step are
	 *            considered when looking for the concept name otherwise the
	 *            conclusion sentence is considered.
	 * @return list of rationale reasoning steps.
	 */
	ArrayList<CeRationaleReasoningStep> listRationaleForConcept(String pConName, boolean pCheckPremise);

	/**
	 * Return a list the rationale reasoning steps that mention the named
	 * property.
	 * 
	 * @param pPropName
	 *            reasoning steps that mention this property name are returned
	 * @param pCheckPremise
	 *            when set true the premise sentences of each reasoning step are
	 *            considered when looking for the property name otherwise the
	 *            conclusion sentence is considered.
	 * @return list of rationale reasoning steps.
	 */
	ArrayList<CeRationaleReasoningStep> listRationaleForProperty(String pPropName, boolean pCheckPremise);

	/**
	 * Return a list the rationale reasoning steps that mention the named
	 * instance.
	 * 
	 * @param pInstName
	 *            reasoning steps that mention this instance name are returned
	 * @param pCheckPremise
	 *            when set true the premise sentences of each reasoning step are
	 *            considered when looking for the instance name otherwise the
	 *            conclusion sentence is considered.
	 * @return list of rationale reasoning steps.
	 */
	ArrayList<CeRationaleReasoningStep> listRationaleForInstance(String pInstName, boolean pCheckPremise);

	/**
	 * Return a list the rationale reasoning steps that mention the named
	 * property value.
	 * 
	 * @param pInstName
	 *            reasoning steps that mention this instance name and...
	 * @param pPropName
	 *            mention this property name and...
	 * @param pValue
	 *            mention this property value are returned
	 * @param pCheckPremise
	 *            when set true the premise sentences of each reasoning step are
	 *            considered otherwise the conclusion sentence is considered.
	 * @return list of rationale reasoning steps.
	 */
	ArrayList<CeRationaleReasoningStep> listRationaleForPropertyValue(String pInstName, String pPropName, String pValue,
			boolean pCheckPremise);

	// OTHER

	/**
	 * Searches all of the instances of the named concept looking for property
	 * values that contain the search terms. Returns an object containing the
	 * matches in terms of the concept/instance/property/value.
	 * 
	 * @param pTerms
	 *            the list of strings to search for
	 * @param pConceptNames
	 *            the names of the concepts to search. If null all concepts are
	 *            searched.
	 * @param pPropertyNames
	 *            the names of the properties to search. If null all properties
	 *            are searched.
	 * @param pCaseSensitive
	 *            whether the search is case sensitive or not
	 * @return Object containing the matches in terms of the
	 *         concept/instance/property/value.
	 */
	ArrayList<ContainerSearchResult> keywordSearch(ArrayList<String> pSearchTerms, String[] pConceptNames,
			String[] pPropertyNames, boolean pCaseSensitive);

	/**
	 * Get a single Unique Identifier
	 * 
	 * @return the generated unique identifier
	 */
	String getUidSingle();

	/**
	 * Get multiple Unique Identifiers
	 * 
	 * @param pBatchSize
	 *            the number of identifiers to generate
	 * @return the batch of generated unique identifiers
	 */
	Properties getUidBatch(long pBatchSize);

	/**
	 * Reset the algorithm that generates unique identifiers
	 */
	void resetUids();
}
