package com.ibm.ets.ita.ce.store.client.web.model;

import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2015
 * All Rights Reserved
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.ActionContext;
import com.ibm.ets.ita.ce.store.ApiHandler;
import com.ibm.ets.ita.ce.store.StoreConfig;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;
import com.ibm.ets.ita.ce.store.uid.UidManager;

public class CeWebSpecial extends CeWebObject {
    public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2015";

    // JSON Response Keys
    private static final String KEY_INSTANCE_NAME = "instance_name";
    private static final String KEY_RESULTS = "results";
    private static final String KEY_PROP_NAME = "property_name";

    private static final String KEY_ENV_PROPS = "environment_properties";
    private static final String KEY_GEN_PROPS = "general_properties";

    private static final String KEY_VALUE = "value";
    private static final String KEY_RANGE = "range";
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_COUNT = "count";
    private static final String KEY_UID_PREFIX = "prefix";
    private static final String KEY_UID_BATCHSTART = "batch_start";
    private static final String KEY_UID_BATCHEND = "batch_end";
    private static final String KEY_UID_BATCHSIZE = "batch_size";

    private static final String KEY_CRESENS = "created_sentences";
    private static final String KEY_EXECTIME = "execution_time";
    private static final String KEY_CMDCOUNT = "command_count";
    private static final String KEY_VALCOUNT = "valid_sentences";
    private static final String KEY_INVCOUNT = "invalid_sentences";

    private static final String KEY_NEWINSTS = "new_instances";

    private enum Temporal {
        TIMESTAMP, QUARTER, DATE
    }

    public CeWebSpecial(ActionContext pAc) {
        super(pAc);
    }

    // Diverse concept details response:
    // See CeWebInstance.generateDiverseConceptInstanceJson()
    public static CeStoreJsonArray generateDiverseConceptInstanceListFrom(
            TreeMap<CeInstance, ArrayList<CeConcept>> pInstList) {
        CeStoreJsonArray jArr = new CeStoreJsonArray();

        if (pInstList != null) {
            for (CeInstance thisInst : pInstList.keySet()) {
                addObjectValueTo(jArr,
                        CeWebInstance.generateDiverseConceptDetailsJson(thisInst, pInstList.get(thisInst)));
            }
        }

        return jArr;
    }

    // Reference list response:
    // KEY_RESULTS[]
    // KEY_INSTANCE_NAME
    // KEY_PROP_NAME
    // KEY_INSTANCE_NAME
    public static CeStoreJsonObject generateReferenceListFrom(String pInstName, ArrayList<ArrayList<String>> pRefList) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        jObj.put(KEY_RESULTS, processRefListRows(pRefList));
        jObj.put(KEY_INSTANCE_NAME, pInstName);

        return jObj;
    }

    private static CeStoreJsonArray processRefListRows(ArrayList<ArrayList<String>> pRefList) {
        CeStoreJsonArray jRows = new CeStoreJsonArray();

        for (ArrayList<String> thisRow : pRefList) {
            CeStoreJsonObject jRow = new CeStoreJsonObject();

            String instName = thisRow.get(0);
            String propName = thisRow.get(1);

            putStringValueIn(jRow, KEY_INSTANCE_NAME, instName);
            putStringValueIn(jRow, KEY_PROP_NAME, propName);

            jRows.add(jRow);
        }

        return jRows;
    }

    // UID batch response:
    // KEY_UID_PREFIX
    // KEY_UID_BATCHSTART
    // KEY_UID_BATCHEND
    // KEY_UID_BATCHSIZE
    public static CeStoreJsonObject generateUidBatchFrom(Properties pUidProps, long pBatchSize) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        putStringValueIn(jObj, KEY_UID_PREFIX, pUidProps.getProperty(UidManager.KEY_UID_PREFIX, ""));
        putStringValueIn(jObj, KEY_UID_BATCHSTART, pUidProps.getProperty(UidManager.KEY_UID_BATCHSTART, ""));
        putStringValueIn(jObj, KEY_UID_BATCHEND, pUidProps.getProperty(UidManager.KEY_UID_BATCHEND, ""));
        putLongValueIn(jObj, KEY_UID_BATCHSIZE, pBatchSize);

        return jObj;
    }

    public CeStoreJsonObject generateSentenceLoadResultsFrom(ContainerSentenceLoadResult pSenStats, String[] pOnlyProps, boolean pSuppPropTypes) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();
        CeWebInstance instWeb = new CeWebInstance(this.ac);
        long execTime = System.currentTimeMillis() - this.ac.getStartTime();

        putIntValueIn(jObj, KEY_INVCOUNT, pSenStats.getInvalidSentenceCount());
        putIntValueIn(jObj, KEY_CMDCOUNT, pSenStats.getCommandCount());
        putIntValueIn(jObj, KEY_VALCOUNT, pSenStats.getValidSentenceCount());
        putLongValueIn(jObj, KEY_EXECTIME, execTime);

        if ((pSenStats.getNewInstances() != null) && (!pSenStats.getNewInstances().isEmpty())) {
            putArrayValueIn(jObj, KEY_NEWINSTS, instWeb.generateSummaryListJsonFor(pSenStats.getNewInstances(), pOnlyProps, pSuppPropTypes));
        }

        if (pSenStats.getCreatedSentences() != null) {
            CeStoreJsonArray jSenArr = new CeStoreJsonArray();
            CeWebSentence senWeb = new CeWebSentence(this.ac);

            for (CeSentence thisSen : pSenStats.getCreatedSentences()) {
                jSenArr.add(senWeb.generateSummaryNormalJson(thisSen, null));
            }

            putArrayValueIn(jObj, KEY_CRESENS, jSenArr);
        }

        return jObj;
    }

    // Generic count response structure:
    // KEY_COUNT
    public static CeStoreJsonObject generateCountSingleFrom(int pCount) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        putIntValueIn(jObj, KEY_COUNT, pCount);

        return jObj;
    }

    // Generic single value response structure:
    // KEY_VALUE
    public static CeStoreJsonObject generateSingleValueFrom(Object pVal) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        putObjectValueIn(jObj, KEY_VALUE, pVal);

        return jObj;
    }

    // Generic multiple concept instance list response structure:
    // KEY_COUNT
    public static CeStoreJsonObject generateMultipleConceptInstanceListFrom(ActionContext pAc, ApiHandler pApiHandler,
            TreeMap<String, ArrayList<CeInstance>> pList, String[] pOnlyProps, int pNumSteps, boolean pRelInsts, boolean pRefInsts, String[] pLimRels, boolean pSuppPropTypes) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        if (pList != null) {
            for (String conName : pList.keySet()) {
                CeStoreJsonArray jArr = new CeStoreJsonArray();
                ArrayList<CeInstance> instList = pList.get(conName);

                if (instList != null) {
                    for (CeInstance thisInst : instList) {
                        CeWebInstance instWeb = new CeWebInstance(pAc);

                        if ((pApiHandler.isDefaultStyle()) || (pApiHandler.isFullStyle())) {
                            jArr.add(instWeb.generateFullDetailsJsonFor(thisInst, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes));
                        } else {
                            jArr.add(instWeb.generateSummaryDetailsJsonFor(thisInst, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes));
                        }
                    }
                }

                jObj.put(conName, jArr);
            }
        }

        return jObj;
    }

    private static long parseProperty(ActionContext pAc, String propertyValue, Temporal type,
            SimpleDateFormat formatter) {
        long property = -1;

        switch (type) {
        case TIMESTAMP:
            property = Long.parseLong(propertyValue);
            break;
        case DATE:
            Date date;
            try {
                date = formatter.parse(propertyValue);
                property = date.getTime();
            } catch (ParseException e) {
                reportError("Parse error: incorrect format in start property", pAc);
                e.printStackTrace();
            }

            break;
        case QUARTER:
            String quarterNum = propertyValue.substring(1);
            property = Integer.parseInt(quarterNum);
            break;
        default:
            break;
        }

        return property;
    }

    // Generate JSON array frequency response:
    // KEY_FREQUENCY
    public static CeStoreJsonArray generateFrequencyArrayFrom(ActionContext pAc, CeConcept pCon,
            ArrayList<CeInstance> instList, String temporalProperty, String buckets) {
        CeStoreJsonArray jArr = new CeStoreJsonArray();
        long numBuckets;
        Temporal type = null;

        // Find temporal property from meta model
        Collection<CeProperty> map = pCon.calculateAllProperties().values();

        for (CeProperty thisProp : map) {
            CeInstance mm = thisProp.getMetaModelInstance(pAc);

            if (mm.isConceptNamed(pAc, "temporal property")) {
                // Found temporal property
                temporalProperty = thisProp.getPropertyName();
                break;
            }
        }

        // Else set to default
        if (temporalProperty == null || temporalProperty.isEmpty()) {
            temporalProperty = "timestamp";
        }

        // Set default buckets
        if (buckets == null || buckets.isEmpty()) {
            numBuckets = 10;
        } else {
            numBuckets = Integer.parseInt(buckets);
        }

        long minValue = 0;
        long maxValue = 0;
        long bucketSize;

        String propertyValue = null;
        CePropertyInstance pi;

        String format = null;

        // Get temporal property and work out type
        for (int index = 0; index < instList.size() && (propertyValue == null); ++index) {
            pi = instList.get(index).getPropertyInstanceNamed(temporalProperty);

            if (pi != null) {
                // Get property
                propertyValue = pi.getSingleOrFirstValue();

                // Get format
                CeProperty p = pi.getRelatedProperty();

                if (p != null) {
                    CeInstance metaModel = p.getMetaModelInstance(pAc);
                    if (metaModel != null) {
                        CePropertyInstance dataFormat = metaModel.getPropertyInstanceNamed("data format");

                        // If property has a data format then return format
                        if (dataFormat != null) {
                            format = dataFormat.getSingleOrFirstValue();
                        }
                    }
                }

                if (format != null) {
                    type = Temporal.DATE;
                } else {
                    if (propertyValue.startsWith("Q")) {
                        String quarterNum = propertyValue.substring(1);

                        try {
                            minValue = Integer.parseInt(quarterNum);
                        } catch (NumberFormatException e1) {
                            System.out.println("Property not acceptable format");
                            return jArr;
                        }

                        type = Temporal.QUARTER;
                    } else {
                        try {
                            Long.parseLong(propertyValue);
                        } catch (NumberFormatException e) {
                            System.out.println("Property not acceptable format");
                            return jArr;
                        }

                        type = Temporal.TIMESTAMP;
                    }
                }
            }
        }

        if (propertyValue == null) {
            System.out.println("Property not found");
            return jArr;
        }

        // Parse initial property value
        SimpleDateFormat formatter = null;
        if (format != null) {
            formatter = new SimpleDateFormat(format);
        }

        minValue = parseProperty(pAc, propertyValue, type, formatter);
        maxValue = minValue;

        if (instList != null && minValue > -1) {
            // Calculate minimum and maximum values
            for (CeInstance thisInst : instList) {
                pi = thisInst.getPropertyInstanceNamed(temporalProperty);

                if (pi != null) {
                    propertyValue = pi.getSingleOrFirstValue();
                    long inst = parseProperty(pAc, propertyValue, type, formatter);

                    if (inst > -1) {
                        if (inst < minValue) {
                            minValue = inst;
                        } else if (inst > maxValue) {
                            maxValue = inst;
                        }
                    }
                }
            }

            // Calculate bucket size
            bucketSize = (long) Math.ceil((maxValue - minValue + 1) / (double) numBuckets);

            // Generate JSON array with 0 frequency
            for (int i = 0; i < numBuckets; ++i) {
                long startOfRange = minValue + (bucketSize * i);
                long endOfRange = startOfRange + bucketSize - 1;

                String labelFormat = "";
                String startLabel;
                String endLabel;

                // Create range label
                if (type == Temporal.TIMESTAMP || type == Temporal.DATE) {
                    // if > 1 day in range
                    long oneSecond = 1000;
                    long oneMinute = oneSecond * 60;
                    long oneHour = oneMinute * 60;
                    long oneDay = oneHour * 24;

                    if (endOfRange - startOfRange < oneSecond) {
                        labelFormat = "HH:mm:ss.SSS d MMM, ''yy";
                    } else if (endOfRange - startOfRange < oneMinute) {
                        labelFormat = "HH:mm:ss d MMM, ''yy";
                    } else if (endOfRange - startOfRange < oneDay) {
                        labelFormat = "HH:mm d MMM, ''yy";
                    } else {
                        labelFormat = "d MMM, ''yy";
                    }

                    SimpleDateFormat labelFormatter = new SimpleDateFormat(labelFormat);

                    startLabel = labelFormatter.format(new Date(startOfRange));
                    endLabel = labelFormatter.format(new Date(endOfRange));
                } else {
                    startLabel = "Q" + new Long(startOfRange).toString();
                    endLabel = "Q" + new Long(endOfRange).toString();
                }

                CeStoreJsonObject pObj = new CeStoreJsonObject();

                String range;
                if (startOfRange == endOfRange) {
                    range = startLabel;
                } else {
                    range = startLabel + " - " + endLabel;
                }

                pObj.put(KEY_RANGE, range);
                pObj.put(KEY_FREQUENCY, 0);

                jArr.add(pObj);
            }

            // Populate frequencies
            for (CeInstance thisInst : instList) {
                pi = thisInst.getPropertyInstanceNamed(temporalProperty);

                if (pi != null) {
                    propertyValue = pi.getSingleOrFirstValue();
                    long timestamp = parseProperty(pAc, propertyValue, type, formatter);
                    int bucket = (int) Math.floor((timestamp - minValue) / (double) bucketSize);

                    CeStoreJsonObject pObj = (CeStoreJsonObject) jArr.get(bucket);

                    int frequency = (pObj.getInt(KEY_FREQUENCY)) + 1;
                    pObj.put(KEY_FREQUENCY, frequency);
                }
            }
        }

        return jArr;
    }

    // Store Config response:
    // KEY_ENV_PROPS[]
    // {KEY}
    // KEY_GEN_PROPS[]
    // {KEY}
    public static CeStoreJsonObject generateStoreConfigListFrom(ActionContext pAc) {
        StoreConfig config = pAc.getCeConfig();
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        putObjectValueIn(jObj, KEY_ENV_PROPS, createJsonObjectsFor(config.getAllEnvironmentProperties()));
        putObjectValueIn(jObj, KEY_GEN_PROPS, createJsonObjectsFor(config.getAllGeneralProperties()));

        return jObj;
    }

}
