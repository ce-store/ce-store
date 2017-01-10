package com.ibm.ets.ita.ce.store.agents.general;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CURRPROP;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_CURRTIMEOV;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_DATAFMT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_DAY;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_DURPROP;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_ENDPROP;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_GENCURR;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_MONTH;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_STARTPROP;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_UNENDDURPROP;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_YEAR;
import static com.ibm.ets.ita.ce.store.names.MiscNames.DEFAULT_DATEFMT;
import static com.ibm.ets.ita.ce.store.names.MiscNames.TRIGTYPE_CON;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.ibm.ets.ita.ce.store.agents.CeNotifyHandler;
import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.model.CeConcept;
import com.ibm.ets.ita.ce.store.model.CeInstance;
import com.ibm.ets.ita.ce.store.model.CeProperty;
import com.ibm.ets.ita.ce.store.model.CePropertyInstance;
import com.ibm.ets.ita.ce.store.utilities.GeneralUtilities;

public class DurationCalculator extends CeNotifyHandler {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

    /*  Example trigger:

        conceptualise a ~ duration event ~ DE that
        is a CE triggered event and
        has the value FC as ~ from concept ~ and
        has the value FI as ~ from instance ~ and
        has the value SP as ~ start property ~ and
        has the value EP as ~ end property ~ and
        has the value DP as ~ duration property ~ and
        has the value UP as ~ unended duration property ~ and
        has the value CP as ~ current property ~ and
        has the value GC as ~ generate current ~ and
        has the value TO as ~ current time override ~.

        there is a duration event named 'trig_duration’ that
        has 'com.ibm.ets.ita.ce.trigger.temporal.DurationCalculator' as class to notify and
        has 'person' as concept name and
        has 'start date' as start property and
        has 'end date' as end property and
        has 'duration' as duration property and
        has 'current' as current property and
        has 'true' as generate current.

        Example formatted property in meta model

        the formatted property ‘person:start date:value’ has ‘dd, MMM, yy’ as data format.
     */
	
	private static final String DEFAULT_MONTHVAL = "1";
	private static final String DEFAULT_DAYVAL = "1";

    private String getPropertyFormat(ActionContext pAc, CeProperty targetProp) {
        if (targetProp != null) {
            // should always have a meta model
            CeInstance metaModel = targetProp.getMetaModelInstance(pAc);
            CePropertyInstance dataFormat = metaModel.getPropertyInstanceNamed(PROP_DATAFMT);

            // if property has a data format then return format
            if (dataFormat != null) {
                String format = dataFormat.getSingleOrFirstValue();
                return format;
            }
        }

        // if no format provided we should assume millisecond timestamp
        return null;
    }

    /**
     * @param time: time to convert
     * @param format: format to convert time into
     * @return converted time
     *
     * @example the formatted property ‘person triggered event:duration property:value’ has ‘D’ as data format.
     *
     * Supported formats:
     *
     * S: milliseconds
     * s: seconds
     * m: minutes
     * h: hours
     * D: days
     * W: weeks
     * M: months
     * Y: years
     *
     * if not supported returns time
     *
     * TODO Extend data format to include extra characters such as 'D days'
     */
    private long convertToFormat(ActionContext pAc, long duration, String format) {
        long daysInWeek = 7;
        float daysInMonth = 30.4167f;
        float daysInYear = 365.25f;

        if (format.equals("S")) {
            return duration;
        } else if (format.equals("s")) {
            return TimeUnit.MILLISECONDS.toSeconds(duration);
        } else if (format.equals("m")) {
            return TimeUnit.MILLISECONDS.toMinutes(duration);
        } else if (format.equals("h")) {
            return TimeUnit.MILLISECONDS.toHours(duration);
        } else if (format.equals("D")) {
            return TimeUnit.MILLISECONDS.toDays(duration);
        } else if (format.equals("W")) {
            return TimeUnit.MILLISECONDS.toDays(duration) / daysInWeek;
        } else if (format.equals("M")) {
            return (long) (((float) TimeUnit.MILLISECONDS.toDays(duration)) / daysInMonth);
        } else if (format.equals("Y")) {
            return (long) (((float) TimeUnit.MILLISECONDS.toDays(duration)) / daysInYear);
        } else {
            reportWarning("Attempted to convert duration property to unsupported format", pAc);
            return duration;
        }
    }

    private long getTimePropertyValue(ActionContext pAc, CePropertyInstance pi) {
        int numDates = pi.countUniquePropertyValues();

        if (numDates > 1) {
            reportWarning("Multiple dates found in instance '" + pi.getRelatedInstance().getInstanceName() + "', using first date", pAc);
        }

        CeProperty relatedProp = pi.getRelatedProperty();
        long time = -1;
        String value = null;
        String format = getPropertyFormat(pAc, relatedProp);

        if (relatedProp.isDatatypeProperty()) {
            value = pi.getSingleOrFirstValue();
        } else {
            ArrayList<CeInstance> insts = new ArrayList<CeInstance>(pi.getValueInstanceList(pAc));
            CeInstance inst = null;

            if (!insts.isEmpty()) {
                if (insts.size() > 1) {
                    reportWarning("Multiple dates found in instance, using first date", pAc);
                }

                inst = insts.get(0);
            }

            value = getValueFromInstance(pAc, inst);
            format = DEFAULT_DATEFMT;
        }

        if (value != null) {
            // if no format then assume timestamp
            if (format == null) {
                time = Long.parseLong(value);
            } else {
                SimpleDateFormat startFormatter = new SimpleDateFormat(format);

                Date date;
                try {
                    date = startFormatter.parse(value);
                } catch (ParseException e) {
                    reportError("Parse error: incorrect format in start property", pAc);
                    e.printStackTrace();
                    return -1;
                }
                time = date.getTime();
            }
        }

        return time;
    }

    private String getValueFromInstance(ActionContext pAc, CeInstance pInst) {
        //TODO: Implement this properly... for now it assumes properties named year, month, day
    	String result = "";
        String yearVal = pInst.getSingleValueFromPropertyNamed(PROP_YEAR);
        String monthVal = pInst.getSingleValueFromPropertyNamed(PROP_MONTH);
        String dayVal = pInst.getSingleValueFromPropertyNamed(PROP_DAY);

        if (yearVal.isEmpty()) {
        	//Assume it is the id instead
        	result = pInst.getInstanceName();
        } else {
            if (monthVal.isEmpty()) {
                monthVal = DEFAULT_MONTHVAL;
            }

            if (dayVal.isEmpty()) {
                dayVal = DEFAULT_DAYVAL;
            }

            result = yearVal + "-" + monthVal + "-" + dayVal;
        }

        return result;
    }

    private String calculateDuration(ActionContext pAc, CePropertyInstance tdpi, CeConcept con, long startTime,
            long endTime) {
        String duration;
        long durationTime = Math.abs(endTime - startTime);

        if (tdpi != null) {
            String durationFormat = null;
            String value = tdpi.getSingleOrFirstValue();

            if (value != null) {
                ArrayList<CeProperty> matchedProps = con.calculatePropertiesNamed(value);
                CeProperty targetProp = null;

                if (!matchedProps.isEmpty()) {
                    targetProp = matchedProps.get(0);
                }

                durationFormat = getPropertyFormat(pAc, targetProp);
            }

            // if no format then assume duration in milliseconds
            if (durationFormat == null) {
                duration = Long.toString(durationTime);
            } else {
                duration = Long.toString(convertToFormat(pAc, durationTime, durationFormat));
            }
        } else {
            duration = Long.toString(durationTime);
        }

        return duration;
    }

    @Override
    public void notify(ActionContext pAc, String pThingType, String pThingName, String pTriggerName, String pSourceId,
            String pRuleOrQuery, String pRuleOrQueryName) {

    	StringBuilder sb = new StringBuilder();
        CeInstance trigger = pAc.getModelBuilder().getInstanceNamed(pAc, pTriggerName);

        // TODO Allow linked instances

        // get property instances
        CePropertyInstance tspi = trigger.getPropertyInstanceNamed(PROP_STARTPROP);
        CePropertyInstance tepi = trigger.getPropertyInstanceNamed(PROP_ENDPROP);
        CePropertyInstance tdpi = trigger.getPropertyInstanceNamed(PROP_DURPROP);
        CePropertyInstance udpi = trigger.getPropertyInstanceNamed(PROP_UNENDDURPROP);
        CePropertyInstance tcpi = trigger.getPropertyInstanceNamed(PROP_CURRPROP);
        CePropertyInstance tgci = trigger.getPropertyInstanceNamed(PROP_GENCURR);
        CePropertyInstance ctoi = trigger.getPropertyInstanceNamed(PROP_CURRTIMEOV);

        // get property values if not null
        String sp = tspi != null ? tspi.getSingleOrFirstValue() : "";
        String ep = tepi != null ? tepi.getSingleOrFirstValue() : "";
        String dp = tdpi != null ? tdpi.getSingleOrFirstValue() : "";
        String up = udpi != null ? udpi.getSingleOrFirstValue() : "";
        String cp = tcpi != null ? tcpi.getSingleOrFirstValue() : "";
        String gc = tgci != null ? tgci.getSingleOrFirstValue() : "";
        String cto = ctoi != null ? ctoi.getSingleOrFirstValue() : "";

        Boolean setCurrent = Boolean.parseBoolean(gc);

        // TODO Look for new properties added to current instances and recalculate duration

        // get new instances
        ArrayList<CeInstance> insts = new ArrayList<CeInstance>();        
        insts.addAll(pAc.getSessionCreations().getNewInstances());

        for (CeInstance inst : insts) {

            // check instance is of correct type
        	String conName = null;
        	
        	if (pThingType.equals(TRIGTYPE_CON)) {
        		conName = pThingName;
        	} else {
        		ArrayList<CeProperty> propList = pAc.getModelBuilder().getPropertiesNamed(pThingName);

        		if (!propList.isEmpty()) {
        			for (CeProperty thisProp : propList) {
        				if (thisProp.isStatedProperty()) {
                			conName = thisProp.getDomainConcept().getConceptName();
                			break;
        				}
        			}
        		}
        	}
        	
        	if (conName != null) {
                if (inst.isConceptNamed(pAc, conName)) {
                    CePropertyInstance dpi = inst.getPropertyInstanceNamed(dp);
                    CePropertyInstance cpi = inst.getPropertyInstanceNamed(cp);

                    // check if property instances are already set && duration or current property exists
                    if (dpi == null && cpi == null && (!dp.equals("") || !cp.equals(""))) {
                        CePropertyInstance spi = inst.getPropertyInstanceNamed(sp);
                        CePropertyInstance epi = inst.getPropertyInstanceNamed(ep);

                        if ((spi != null) && (epi != null)) {
    	                    String instName = inst.getInstanceName();
    	
    	                    String encThingName = GeneralUtilities.encodeForCe(conName);
    	                    String encName = GeneralUtilities.encodeForCe(instName);
    	
    	                    // if start is set we can calculate either duration or current
    	                    if (spi != null) {
    	                        long startTime = getTimePropertyValue(pAc, spi);
    	
    	                        if (startTime < 0) {
    	                            // parse error
    	                            return;
    	                        }
    	
    	                        // if end is set and duration property exists we can calculate duration
    	                        if (epi != null && !dp.equals("")) {
    	                            long endTime = getTimePropertyValue(pAc, epi);
    	
    	                            if (endTime < 0) {
    	                                // parse error
    	                                return;
    	                            }
    	
    	                            CeConcept tgtCon = pAc.getModelBuilder().getConceptNamed(pAc, conName);
    	
    	                            String duration = calculateDuration(pAc, tdpi, tgtCon, startTime, endTime);
    	                            String encDurationProp = GeneralUtilities.encodeForCe(dp);
    	                            String encDuration = GeneralUtilities.encodeForCe(duration);

                                    CeProperty tgtProp = findPropertyFor(pAc, conName, dp);
                                    
                                    if (tgtProp != null) {
                                    	//TODO: Replace with standard CE generation
                                    	String rangePart = "";
    	                            
	    	                            if (tgtProp.isObjectProperty()) {
	    	                            	rangePart = "the " + tgtProp.getRangeConceptName() + " ";
	    	                            }
	    	
	    	    	                    sb.append("the " + encThingName + " '" + encName + "'");	    	    	                	
	    	                            sb.append(" has " + rangePart + "'" + encDuration + "' as " + encDurationProp + ".\n\n");
                                    }
    	                        }
    	
    	                        // if setCurrent is true and current property exists we can set current
    	                        else if (setCurrent && !cp.equals("")) {
    	                            // TODO Create 'current duration property' that holds the current number of days in office for non-persistent models
    	                            String encCurrentProp = GeneralUtilities.encodeForCe(cp);
    	
    	    	                    sb.append("the " + encThingName + " '" + encName + "'");    	    	                	
    	                            sb.append(" has 'true' as " + encCurrentProp + ".\n\n");
    	                        }
    	                    }
    	                }
                    }

                    //Now try for unended durations
                    CePropertyInstance epi = null;

                    if (!ep.isEmpty()) {
                        epi = inst.getPropertyInstanceNamed(ep);
                    }

                    if (epi == null) {
                        CePropertyInstance upi = inst.getPropertyInstanceNamed(up);

                        if (upi == null) {
                            //TODO: Refactor so there is less repition of code from above
                            CePropertyInstance spi = inst.getPropertyInstanceNamed(sp);
                            String instName = inst.getInstanceName();

                            String encThingName = GeneralUtilities.encodeForCe(conName);
                            String encName = GeneralUtilities.encodeForCe(instName);

                            // if start is set we can calculate either duration or current
                            if (spi != null) {
                                long startTime = getTimePropertyValue(pAc, spi);
                                long endTime = -1;

                                if (cto.isEmpty()) {
                                	//There is no 'current time override' so use the current time
                                	endTime = System.currentTimeMillis();
                                } else {
                                	endTime = new Long(cto).longValue();
                                }

                                CeConcept tgtCon = pAc.getModelBuilder().getConceptNamed(pAc, conName);

                                String duration = calculateDuration(pAc, udpi, tgtCon, startTime, endTime);

                                String encDurationProp = GeneralUtilities.encodeForCe(up);
                                String encDuration = GeneralUtilities.encodeForCe(duration);

                                CeProperty tgtProp = findPropertyFor(pAc, conName, up);
                                
                                if (tgtProp != null) {
                                	//TODO: Replace with standard CE generation
    	                            String rangePart = "";
    	                            
    	                            if (tgtProp.isObjectProperty()) {
    	                            	rangePart = "the " + tgtProp.getRangeConceptName() + " ";
    	                            }

    	                            sb.append("the " + encThingName + " '" + encName + "'");
    	                            sb.append(" has " + rangePart + "'" + encDuration + "' as " + encDurationProp + ".\n\n");
                                }
                            }
                        }
                    }
                }
        	}
        }
        
        if (sb.length() > 0) {
            CeNotifyHandler.saveCeText(pAc, sb.toString());
        }
    }
    
    private CeProperty findPropertyFor(ActionContext pAc, String pConName, String pPropName) {
    	CeProperty matchedProp = null;
    	CeConcept tgtCon = pAc.getModelBuilder().getConceptNamed(pAc, pConName);
    	
    	if (tgtCon != null) {
    		ArrayList<CeProperty> allProps = new ArrayList<CeProperty>();
    		allProps.addAll(tgtCon.calculateAllProperties().values());

        	for (CeProperty thisProp : allProps) {
    			if (thisProp.getPropertyName().equals(pPropName)) {
    				matchedProp = thisProp;
    				break;
    			}
        	}
    	}
    	
    	return matchedProp;
    }
    
}
