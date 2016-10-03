package com.ibm.ets.ita.ce.store.client.web.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_UID_PREFIX;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_UID_BATCHSTART;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_UID_BATCHEND;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_UID_BATCHSIZE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INSTANCE_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RESULTS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_PROP_NAME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_STOREPROPS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_VALUE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_RANGE;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_FREQUENCY;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_COUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CRESENS;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_EXECTIME;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_CMDCOUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_VALCOUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_INVCOUNT;
import static com.ibm.ets.ita.ce.store.names.JsonNames.JSON_NEWINSTS;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.TreeMap;

import com.ibm.ets.ita.ce.store.client.rest.ApiHandler;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonArray;
import com.ibm.ets.ita.ce.store.client.web.json.CeStoreJsonObject;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.StoreConfig;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.model.CeSentence;
import com.ibm.ets.ita.ce.store.model.container.ContainerSentenceLoadResult;

public class CeWebSpecial extends CeWebObject {
    public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

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
    // JSON_RESULTS[]
    // JSON_INSTANCE_NAME
    // JSON_PROP_NAME
    // JSON_INSTANCE_NAME
    public static CeStoreJsonObject generateReferenceListFrom(String pInstName, ArrayList<ArrayList<String>> pRefList) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        jObj.put(JSON_RESULTS, processRefListRows(pRefList));
        jObj.put(JSON_INSTANCE_NAME, pInstName);

        return jObj;
    }

    private static CeStoreJsonArray processRefListRows(ArrayList<ArrayList<String>> pRefList) {
        CeStoreJsonArray jRows = new CeStoreJsonArray();

        for (ArrayList<String> thisRow : pRefList) {
            CeStoreJsonObject jRow = new CeStoreJsonObject();

            String instName = thisRow.get(0);
            String propName = thisRow.get(1);

            putStringValueIn(jRow, JSON_INSTANCE_NAME, instName);
            putStringValueIn(jRow, JSON_PROP_NAME, propName);

            jRows.add(jRow);
        }

        return jRows;
    }

    // UID batch response:
    // JSON_UID_PREFIX
    // JSON_UID_BATCHSTART
    // JSON_UID_BATCHEND
    // JSON_UID_BATCHSIZE
    public static CeStoreJsonObject generateUidBatchFrom(Properties pUidProps, long pBatchSize) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        putStringValueIn(jObj, JSON_UID_PREFIX, pUidProps.getProperty(JSON_UID_PREFIX, ""));
        putStringValueIn(jObj, JSON_UID_BATCHSTART, pUidProps.getProperty(JSON_UID_BATCHSTART, ""));
        putStringValueIn(jObj, JSON_UID_BATCHEND, pUidProps.getProperty(JSON_UID_BATCHEND, ""));
        putLongValueIn(jObj, JSON_UID_BATCHSIZE, pBatchSize);

        return jObj;
    }

    public CeStoreJsonObject generateSentenceLoadResultsFrom(ContainerSentenceLoadResult pSenStats, String[] pOnlyProps, boolean pSuppPropTypes) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();
        CeWebInstance instWeb = new CeWebInstance(this.ac);
        long execTime = System.currentTimeMillis() - this.ac.getStartTime();

        putIntValueIn(jObj, JSON_INVCOUNT, pSenStats.getInvalidSentenceCount());
        putIntValueIn(jObj, JSON_CMDCOUNT, pSenStats.getCommandCount());
        putIntValueIn(jObj, JSON_VALCOUNT, pSenStats.getValidSentenceCount());
        putLongValueIn(jObj, JSON_EXECTIME, execTime);

        if ((pSenStats.getNewInstances() != null) && (!pSenStats.getNewInstances().isEmpty())) {
            putArrayValueIn(jObj, JSON_NEWINSTS, instWeb.generateSummaryListJsonFor(pSenStats.getNewInstances(), pOnlyProps, pSuppPropTypes));
        }

        if (pSenStats.getCreatedSentences() != null) {
            CeStoreJsonArray jSenArr = new CeStoreJsonArray();
            CeWebSentence senWeb = new CeWebSentence(this.ac);

            for (CeSentence thisSen : pSenStats.getCreatedSentences()) {
                jSenArr.add(senWeb.generateSummaryNormalJson(thisSen, null));
            }

            putArrayValueIn(jObj, JSON_CRESENS, jSenArr);
        }

        return jObj;
    }

    // Generic count response structure:
    // JSON_COUNT
    public static CeStoreJsonObject generateCountSingleFrom(int pCount) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        putIntValueIn(jObj, JSON_COUNT, pCount);

        return jObj;
    }

    // Generic single value response structure:
    // JSON_VALUE
    public static CeStoreJsonObject generateSingleValueFrom(Object pVal) {
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        putObjectValueIn(jObj, JSON_VALUE, pVal);

        return jObj;
    }

    // Generic multiple concept instance list response structure:
    // JSON_COUNT
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

                        if ((pApiHandler.isDefaultStyle()) || (pApiHandler.isSummaryStyle())) {
                            jArr.add(instWeb.generateSummaryDetailsJsonFor(thisInst, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes));
                        } else if (pApiHandler.isFullStyle()) {
                            jArr.add(instWeb.generateFullDetailsJsonFor(thisInst, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels, pSuppPropTypes));
                        } else {
                            jArr.add(instWeb.generateMinimalDetailsJsonFor(thisInst, pOnlyProps, pNumSteps, pRelInsts, pRefInsts, pLimRels));
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
    // JSON_FREQUENCY
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

                pObj.put(JSON_RANGE, range);
                pObj.put(JSON_FREQUENCY, 0);

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

                    int frequency = (pObj.getInt(JSON_FREQUENCY)) + 1;
                    pObj.put(JSON_FREQUENCY, frequency);
                }
            }
        }

        return jArr;
    }

    // Store Config response:
    // JSON_STOREPROPS[]
    // {KEY}
    public static CeStoreJsonObject generateStoreConfigListFrom(ActionContext pAc) {
        StoreConfig config = pAc.getCeConfig();
        CeStoreJsonObject jObj = new CeStoreJsonObject();

        putObjectValueIn(jObj, JSON_STOREPROPS, createJsonObjectsFor(config.getAllProperties()));

        return jObj;
    }

}
