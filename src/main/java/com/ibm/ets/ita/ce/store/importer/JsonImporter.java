package com.ibm.ets.ita.ce.store.importer;

import java.util.ArrayList;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.client.rest.CeStoreRestApiSpecialImport;
import com.ibm.ets.ita.ce.store.utilities.GeneralUtilities;
import static com.ibm.ets.ita.ce.store.names.RestNames.STYLE_NORMALISED;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

public class JsonImporter {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	private static final String CONNECTOR_THAT = "that\n";
	private static final String CONNECTOR_AND = "and\n";
	private static final String CONNECTOR_FINISH = ".\n\n";
	private static final String DEFAULT_DETERMINER = "an";
	private static final String DEFAULT_CON_NAME = "extracted json object";

	private CeStoreRestApiSpecialImport importer = null;
	private JsonImporter parent = null;
	private TreeMap<String, ArrayList<JsonImporter>> children = new TreeMap<String, ArrayList<JsonImporter>>();
	private String id = null;
	private String determiner = null;
	private String conceptName = null;
	private String originalConceptName = null;
	private boolean isSuppressed = false;
	private ArrayList<ArrayList<String>> attributes = new ArrayList<ArrayList<String>>();

	public JsonImporter(CeStoreRestApiSpecialImport pImporter, String pId, String pDet, String pConceptName, boolean pIsSuppressed) {
		this.importer = pImporter;
		this.id = GeneralUtilities.encodeForCe(pId);
		this.isSuppressed = pIsSuppressed;

		if (pDet == null) {
			this.determiner = DEFAULT_DETERMINER;
		} else {
			this.determiner = pDet;
		}

		if (pConceptName == null) {
			this.conceptName = DEFAULT_CON_NAME;
		} else {
			this.conceptName = pConceptName;
		}
	}

	public static JsonImporter createWithParent(CeStoreRestApiSpecialImport pImporter, String pId, JsonImporter pParent, String pPropName, boolean pIsSuppressed, String pDet, String pConceptName) {
		JsonImporter ji = new JsonImporter(pImporter, pId, pDet, pConceptName, pIsSuppressed);

		ji.parent = pParent;
		pParent.addChild(pPropName, ji);

		return ji;
	}

	public boolean isEmpty() {
		return !hasAttributes() && !hasChildren();
	}

	public boolean isSuppressed() {
		return this.isSuppressed;
	}

	public String getId() {
		if (this.id == null) {
			this.id = GeneralUtilities.encodeForCe(this.importer.generateNewInstanceId());
		}
		return this.id;
	}

	public void setId(String pId) {
		this.id = pId;
	}

	public String getConceptName() {
		return this.conceptName;
	}

	public void setConceptName(String pConName, String pDet) {
		this.conceptName = pConName;
		this.determiner = pDet;
	}

	public String getOriginalConceptName() {
		return this.originalConceptName;
	}

	public void setOriginalConceptName(String pOrigConName) {
		this.originalConceptName = pOrigConName;
	}

	public boolean hasParent() {
		return this.parent != null;
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	private boolean hasAttributes() {
		return (this.attributes != null) && !this.attributes.isEmpty();
	}

	public JsonImporter getParent() {
		return this.parent;
	}

	public TreeMap<String, ArrayList<JsonImporter>> getChildren() {
		return this.children;
	}

	public ArrayList<String> getAttributeNames() {
		ArrayList<String> result = new ArrayList<String>();

		for (ArrayList<String> thisPair : this.attributes) {
			String propName = thisPair.get(0);

			if (!result.contains(propName)) {
				result.add(propName);
			}
		}

		return result;
	}

	public void addAttribute(String pPropName, String pValue, String pRangeCon) {
		ArrayList<String> pair =  new ArrayList<String>();

		pair.add(pPropName);
		pair.add(pValue);		
		pair.add(pRangeCon);

		this.attributes.add(pair);
	}

	public void addChild(String pPropName, JsonImporter pChild) {
		ArrayList<JsonImporter> theseChildren = this.children.get(pPropName);

		if (theseChildren == null) {
			theseChildren = new ArrayList<JsonImporter>();
			this.children.put(pPropName, theseChildren);
		}

		theseChildren.add(pChild);
	}
	
	public String calculateCeText(String pParmStyle, boolean pIgnoreBlanks) {
		String result = null;

		if ((pParmStyle != null) && (pParmStyle.equals(STYLE_NORMALISED))) {
			result = calculateNormalisedCeText(pIgnoreBlanks);
		} else {
			result = calculateDefaultCeText(pIgnoreBlanks);
		}

		return result;
	}
	
	private String calculateNormalisedCeText(boolean pIgnoreBlanks) {
		StringBuilder sb = new StringBuilder();
		String connector = CONNECTOR_THAT;

		sb.append("there is ");
		sb.append(this.determiner);
		sb.append(" ");
		sb.append(this.conceptName);
		sb.append(" named '");
		sb.append(GeneralUtilities.encodeForCe(getId()));
		sb.append("'");

		connector = calculateNormalisedValues(sb, connector, pIgnoreBlanks);
		calculateNormalisedChildren(sb, connector);

		sb.append(CONNECTOR_FINISH);

		return sb.toString();
	}

	private String calculateDefaultCeText(boolean pIgnoreBlanks) {
		StringBuilder sb = new StringBuilder();
		String connector = CONNECTOR_THAT;

		sb.append("there is ");
		sb.append(this.determiner);
		sb.append(" ");
		sb.append(this.conceptName);
		sb.append(" named '");
		sb.append(GeneralUtilities.encodeForCe(getId()));
		sb.append("'");

		connector = calculateDefaultValues(sb, connector, pIgnoreBlanks);
		calculateDefaultChildren(sb, connector);

		sb.append(CONNECTOR_FINISH);

		return sb.toString();
	}

	private String calculateNormalisedValues(StringBuilder pSb, String pConnector, boolean pIgnoreBlanks) {
		String connector = pConnector;

		if (hasAttributes()) {
			for (ArrayList<String> pair : this.attributes) {
				String propName = pair.get(0);
				String propVal = GeneralUtilities.encodeForCe(pair.get(1));
				String propRange = pair.get(2);

				if (!propVal.isEmpty() || !pIgnoreBlanks) {
					pSb.append(" ");
					pSb.append(connector);
					pSb.append("  has");

					if (propRange != null) {
						pSb.append(" the ");
						pSb.append(propRange);
					}

					pSb.append(" '");
					pSb.append(GeneralUtilities.encodeForCe(propVal));
					pSb.append("' as ");
					pSb.append(propName);
	
					connector = CONNECTOR_AND;
				}
			}
		}
		
		return connector;
	}

	private void calculateNormalisedChildren(StringBuilder pSb, String pConnector) {
		String connector = pConnector;

		if (hasChildren()) {
			for (String propName : this.children.keySet()) {
				for (JsonImporter thisChild : this.children.get(propName)) {
					if (!thisChild.isSuppressed()) {
						String propVal = GeneralUtilities.encodeForCe(thisChild.getId());
	
						pSb.append(" ");
						pSb.append(connector);
						pSb.append("  has the ");
						pSb.append(thisChild.conceptName);
	
						pSb.append(" '");
						pSb.append(GeneralUtilities.encodeForCe(propVal));
						pSb.append("' as ");
						pSb.append(propName);
	
						connector = CONNECTOR_AND;
					}
				}
			}
		}
	}

	private String calculateDefaultValues(StringBuilder pSb, String pConnector, boolean pIgnoreBlanks) {
		String connector = pConnector;
		
		//TODO: Implement this

		if (hasAttributes()) {
			for (ArrayList<String> pair : this.attributes) {
				String propName = pair.get(0);
				String propVal = GeneralUtilities.encodeForCe(pair.get(1));
				String propRange = pair.get(2);
				
				if (!propVal.isEmpty() || !pIgnoreBlanks) {
					pSb.append(" ");
					pSb.append(connector);
					pSb.append("  has");
	
					if (propRange != null) {
						pSb.append(" the ");
						pSb.append(propRange);
					}

					pSb.append(" '");
					pSb.append(GeneralUtilities.encodeForCe(propVal));
					pSb.append("' as ");
					pSb.append(propName);
	
					connector = CONNECTOR_AND;
				}
			}
		}
		
		return connector;
	}

	private void calculateDefaultChildren(StringBuilder pSb, String pConnector) {
		String connector = pConnector;

		//TODO: Implement this

		if (hasChildren()) {
			for (String propName : this.children.keySet()) {
				for (JsonImporter thisChild : this.children.get(propName)) {
					if (!thisChild.isSuppressed) {
						String propVal = GeneralUtilities.encodeForCe(thisChild.getId());
		
						pSb.append(" ");
						pSb.append(connector);
						pSb.append("  has the ");
						pSb.append(thisChild.conceptName);
		
						pSb.append(" '");
						pSb.append(GeneralUtilities.encodeForCe(propVal));
						pSb.append("' as ");
						pSb.append(propName);
		
						connector = CONNECTOR_AND;
					}
				}
			}
		}
	}

}
