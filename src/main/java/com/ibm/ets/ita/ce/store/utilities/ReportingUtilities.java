package com.ibm.ets.ita.ce.store.utilities;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.SUFFIX_DEFAULT;
import static com.ibm.ets.ita.ce.store.names.MiscNames.SUFFIX_TIMING;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_DOT;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.substituteNormalParameters;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.ibm.ets.ita.ce.store.core.ActionContext;
import com.ibm.ets.ita.ce.store.core.ActionResponse;

public class ReportingUtilities {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CLASS_NAME = ReportingUtilities.class.getName();
	private static final String PACKAGE_NAME = ReportingUtilities.class.getPackage().getName();
	private static final Logger thisLogger = Logger.getLogger(PACKAGE_NAME);

	public static final String CE_ROOT_LOGGER_NAME = "com.ibm.ets.ita.ce";
	private static final Level DEBUG_LEVEL = Level.FINER;
	private static final Level MICRO_LEVEL = Level.FINEST;
	private static final Level TIMING_LEVEL = Level.FINEST;
	private static final Logger ceRootLogger = Logger.getLogger(CE_ROOT_LOGGER_NAME);
	private static final Logger defaultLogger = Logger.getLogger(CE_ROOT_LOGGER_NAME + SUFFIX_DEFAULT);
	private static final Logger defaultTimingLogger = Logger.getLogger(CE_ROOT_LOGGER_NAME + SUFFIX_TIMING);

	private static Map<String, Level> preDebugLoggerLevels = null;
	private static Handler debugHandler = null;

	/*
	 * Log at Level.SEVERE using the default logger and always add to action
	 * response if non-null.
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportError(String message, ActionContext actionContext) {
		reportError(message, null, actionContext, defaultLogger, null, null);
	}

	/*
	 * Log at Level.SEVERE using the default logger and always add to action
	 * response if non-null.
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportError(String message, TreeMap<String, String> pParms, ActionContext actionContext) {
		reportError(message, pParms, actionContext, defaultLogger, null, null);
	}

	/*
	 * Log at Level.SEVERE using the given logger and always add to action
	 * response if non-null.
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportError(String message, ActionContext actionContext, Logger logger, String className,
			String methodName) {
		reportError(message, null, actionContext, defaultLogger, className, methodName);
	}

	/*
	 * Log at Level.SEVERE using the given logger and always add to action
	 * response if non-null.
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportError(String message, TreeMap<String, String> pParms, ActionContext actionContext,
			Logger logger, String className, String methodName) {
		logger.logp(Level.SEVERE, className, methodName, message);
		ActionResponse actionResponse = actionContext.getActionResponse();
		if (actionResponse != null) {
			String msgText = null;

			if (pParms != null) {
				msgText = substituteNormalParameters(message, pParms);
			} else {
				msgText = message;
			}

			actionResponse.addErrorMessage(msgText);
		}
	}

	/*
	 * Log at Level.WARNING using the default logger and always add to action
	 * response if non-null.
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportWarning(String message, ActionContext actionContext) {
		reportWarning(message, null, actionContext, defaultLogger, null, null);
	}

	/*
	 * Log at Level.WARNING using the default logger and always add to action
	 * response if non-null.
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportWarning(String message, TreeMap<String, String> pParms, ActionContext actionContext) {
		reportWarning(message, pParms, actionContext, defaultLogger, null, null);
	}

	/*
	 * Log at Level.WARNING using the given logger and always add to action
	 * response if non-null.
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportWarning(String message, ActionContext actionContext, Logger logger, String className,
			String methodName) {
		reportWarning(message, null, actionContext, defaultLogger, className, methodName);
	}

	/*
	 * Log at Level.WARNING using the given logger and always add to action
	 * response if non-null.
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportWarning(String message, TreeMap<String, String> pParms, ActionContext actionContext,
			Logger logger, String className, String methodName) {
		logger.logp(Level.WARNING, className, methodName, message);
		ActionResponse actionResponse = actionContext.getActionResponse();
		if (actionResponse != null) {
			String msgText = null;

			if (pParms != null) {
				msgText = substituteNormalParameters(message, pParms);
			} else {
				msgText = message;
			}

			actionResponse.addWarningMessage(msgText);
		}
	}

	/*
	 * Log at Level.INFO using the default logger and always add to action
	 * response if non-null.
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportInfo(String message, ActionContext actionContext) {
		reportInfo(message, actionContext, defaultLogger, null, null);
	}

	/*
	 * Log at Level.INFO using the given logger and always add to action
	 * response if non-null.
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportInfo(String message, ActionContext actionContext, Logger logger, String className,
			String methodName) {
		logger.logp(Level.INFO, className, methodName, message);
		ActionResponse actionResponse = actionContext.getActionResponse();
		if (actionResponse != null) {
			actionResponse.addInfoMessage(message);
		}
	}

	/*
	 * Log at DEBUG_LEVEL using the default logger and add to action response if
	 * logging at DEBUG_LEVEL is configured, and if non-null. See also
	 * isReportDebug().
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportDebug(String message, ActionContext actionContext) {
		reportDebug(message, null, actionContext, defaultLogger, null, null);
	}

	/*
	 * Log at DEBUG_LEVEL using the default logger and add to action response if
	 * logging at DEBUG_LEVEL is configured, and if non-null. See also
	 * isReportDebug().
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportDebug(String message, TreeMap<String, String> pParms, ActionContext actionContext) {
		reportDebug(message, pParms, actionContext, defaultLogger, null, null);
	}

	/*
	 * Log at DEBUG_LEVEL using the given logger and add to action response if
	 * logging at DEBUG_LEVEL is configured, and if non-null. See also
	 * isReportDebug().
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportDebug(String message, ActionContext actionContext, Logger logger, String className,
			String methodName) {
		reportDebug(message, null, actionContext, defaultLogger, className, methodName);
	}

	/*
	 * Log at DEBUG_LEVEL using the given logger and add to action response if
	 * logging at DEBUG_LEVEL is configured, and if non-null. See also
	 * isReportDebug().
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportDebug(String message, TreeMap<String, String> pParms, ActionContext actionContext,
			Logger logger, String className, String methodName) {
		if (isReportDebug(logger)) {
			logger.logp(DEBUG_LEVEL, className, methodName, message);
			ActionResponse actionResponse = actionContext.getActionResponse();
			if (actionResponse != null) {
				String msgText = null;

				if (pParms != null) {
					msgText = substituteNormalParameters(message, pParms);
				} else {
					msgText = message;
				}

				actionResponse.addDebugMessage(msgText);
			}
		}
	}

	/*
	 * Return a boolean indicating whether invoking the reportDebug() method
	 * that uses the default logger would result in output being produced
	 * (useful if the message that would be output is expensive to construct).
	 * See also reportDebug().
	 * 
	 * Input parameter must be non-null.
	 */
	public static boolean isReportDebug() {
		return defaultLogger.isLoggable(DEBUG_LEVEL);
	}

	/*
	 * Return a boolean indicating whether invoking the reportDebug() method
	 * would result in output being produced (useful if the message that would
	 * be output is expensive to construct). See also reportDebug().
	 * 
	 * Input parameter must be non-null.
	 */
	public static boolean isReportDebug(Logger logger) {
		return logger.isLoggable(DEBUG_LEVEL);
	}

	/*
	 * Log at MICRO_LEVEL using the default logger and add to action response if
	 * logging at MICRO_LEVEL is configured, and if non-null. See also
	 * isReportMicroDebug().
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportMicroDebug(String message, ActionContext actionContext) {
		reportMicroDebug(message, actionContext, defaultLogger, null, null);
	}

	/*
	 * Log at MICRO_LEVEL using the given logger and add to action response if
	 * logging at MICRO_LEVEL is configured, and if non-null. See also
	 * isReportMicroDebug().
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportMicroDebug(String message, ActionContext actionContext, Logger logger, String className,
			String methodName) {
		if (isReportMicroDebug(logger)) {
			logger.logp(MICRO_LEVEL, className, methodName, message);
			ActionResponse actionResponse = actionContext.getActionResponse();
			if (actionResponse != null) {
				actionResponse.addDebugMessage(message);
			}
		}
	}

	/*
	 * Return a boolean indicating whether invoking the reportMicroDebug()
	 * method that uses the default logger would result in output being produced
	 * (useful if the message that would be output is expensive to construct).
	 * See also reportMicroDebug().
	 * 
	 * Input parameter must be non-null.
	 */
	public static boolean isReportMicroDebug() {
		return defaultLogger.isLoggable(MICRO_LEVEL);
	}

	/*
	 * Return a boolean indicating whether invoking the reportMicroDebug()
	 * method would result in output being produced (useful if the message that
	 * would be output is expensive to construct). See also reportMicroDebug().
	 * 
	 * Input parameter must be non-null.
	 */
	public static boolean isReportMicroDebug(Logger logger) {
		return logger.isLoggable(MICRO_LEVEL);
	}

	/*
	 * Version of reportException() that doesn't include a message parameter.
	 */
	public static void reportException(Exception exception, ActionContext actionContext, Logger logger,
			String className, String methodName) {
		reportException(exception, null, actionContext, logger, className, methodName);
	}

	/*
	 * Log the given exception and message (if non-null) at Level.SEVERE using
	 * the given logger and always add to action response if non-null.
	 *
	 * Input parameters must be non-null, except for message.
	 */
	public static void reportException(Exception exception, String message, ActionContext actionContext, Logger logger,
			String className, String methodName) {
		logger.logp(Level.SEVERE, className, methodName, message, exception);

		String exceptionType = exception.getClass().getSimpleName();
		String errorMessage = "Exception " + exceptionType + " in " + className + TOKEN_DOT + methodName + " : '"
				+ exception.getMessage() + "'";
		if (message != null && !message.isEmpty()) {
			errorMessage += " (" + message + ")";
		}

		ActionResponse actionResponse = actionContext.getActionResponse();
		if (actionResponse != null) {
			actionResponse.addErrorMessage(errorMessage);
		}

		exception.printStackTrace();
	}

	/*
	 * Log at TIMING_LEVEL using the default timing logger and add to action
	 * response if logging at TIMING_LEVEL is configured, and if non-null. See
	 * also isReportTiming().
	 * 
	 * Input parameters must be non-null.
	 */
	public static void reportTiming(String message, ActionContext actionContext, String className, String methodName) {
		reportTiming(message, actionContext, defaultTimingLogger, className, methodName);
	}

	/*
	 * Log at TIMING_LEVEL using the given logger and add to action response if
	 * logging at TIMING_LEVEL is configured, and if non-null. See also
	 * isReportTiming().
	 * 
	 * Input parameters must be non-null.
	 * 
	 * private, as so far, all timing logging is done using the default timing
	 * logger. Make public if a specific logger is desired.
	 */
	private static void reportTiming(String message, ActionContext actionContext, Logger logger, String className,
			String methodName) {
		if (isReportTiming(logger)) {
			logger.logp(TIMING_LEVEL, className, methodName, message);
			ActionResponse actionResponse = actionContext.getActionResponse();
			if (actionResponse != null) {
				actionResponse.addDebugMessage(message + " (" + className + TOKEN_DOT + methodName + ")");
			}
		}
	}

	/*
	 * Return a boolean indicating whether invoking the reportTiming() method
	 * that uses the default timing logger would result in output being produced
	 * (useful if the message that would be output is expensive to construct).
	 * See also reportTiming().
	 * 
	 * Input parameter must be non-null.
	 */
	public static boolean isReportTiming() {
		return defaultTimingLogger.isLoggable(TIMING_LEVEL);
	}

	/*
	 * Return a boolean indicating whether invoking the reportDebug() method
	 * would result in output being produced (useful if the message that would
	 * be output is expensive to construct). See also reportTiming().
	 * 
	 * Input parameter must be non-null.
	 * 
	 * private, as so far, all timing logging is done using the default timing
	 * logger. Make public if a specific logger is desired.
	 */
	private static boolean isReportTiming(Logger logger) {
		return logger.isLoggable(TIMING_LEVEL);
	}

	/*
	 * Return a map of logger name to logger level for all CE loggers (any that
	 * have a name beginning with CE_ROOT_LOGGER) that are set to a level less
	 * than the given level.
	 */
	private static Map<String, Level> getCELoggerLevelsLessThan(Level level) {
		LogManager logManager = LogManager.getLogManager();
		Map<String, Level> loggerNamesToLevels = new HashMap<String, Level>();
		for (Enumeration<String> loggerNames = logManager.getLoggerNames(); loggerNames.hasMoreElements();) {
			String loggerName = loggerNames.nextElement();
			if (loggerName.startsWith(CE_ROOT_LOGGER_NAME)) {
				Logger logger = Logger.getLogger(loggerName);
				if (!logger.isLoggable(level)) {
					loggerNamesToLevels.put(loggerName, logger.getLevel());
				}
			}
		}
		return loggerNamesToLevels;
	}

	/*
	 * Set the logger levels of the the loggers named in the given map to the
	 * corresponding values in the map.
	 */
	private static void setLoggerLevels(Map<String, Level> loggerNamesToLevels) {
		if (loggerNamesToLevels != null) {
			for (Map.Entry<String, Level> loggerNameToLevel : loggerNamesToLevels.entrySet()) {
				Logger logger = Logger.getLogger(loggerNameToLevel.getKey());
				logger.setLevel(loggerNameToLevel.getValue());
			}
		}
	}

	/*
	 * Indicate whether debug mode is on or off. When debug mode is on, loggers
	 * are configured to ensure all DEBUG_LEVEL log messages are output. Code
	 * may also switch on the value of this state to perform extra debug
	 * processing.
	 * 
	 * Code that guards calls to reportDebug() should use isReportDebug() so
	 * that loggers that are configured to output DEBUG_LEVEL log messages by
	 * other means are still called.
	 * 
	 * Return true if debug mode is on and false otherwise.
	 */
	public static boolean isDebugOn() {
		return (preDebugLoggerLevels != null);
	}

	/*
	 * If not in debug mode, configure logging such that any CE loggers (any
	 * that have a name beginning with CE_ROOT_LOGGER) with levels that do not
	 * already resolve to DEBUG_LEVEL are set to DEBUG_LEVEL, and if a debug log
	 * filename is given, add a suitable handler so log records are written by
	 * the CE root logger to that file.
	 * 
	 * This overrides the existing logger configuration, for example that set by
	 * a web-container or a logging.properties file.
	 * 
	 * Return true if successful (regardless of whether a change occurred), and
	 * false otherwise (indicating an error occurred).
	 */
	public synchronized static boolean setDebugOn(String debugLogFilename) {
		final String METHOD_NAME = "setDebugOn";
		boolean success = true;
		if (!isDebugOn()) {
			try {
				LogManager logManager = LogManager.getLogManager();
				logManager.checkAccess(); // throws SecurityException if not
											// allowed
				// ensure all CE loggers are set to DEBUG_LEVEL at least.
				preDebugLoggerLevels = getCELoggerLevelsLessThan(DEBUG_LEVEL);
				Map<String, Level> postDebugLoggerLevels = new HashMap<String, Level>(preDebugLoggerLevels);
				Set<Entry<String, Level>> levelsEntrySet = postDebugLoggerLevels.entrySet();
				for (Entry<String, Level> levelsEntry : levelsEntrySet) {
					levelsEntry.setValue(DEBUG_LEVEL);
				}
				setLoggerLevels(postDebugLoggerLevels);
				// add a handler if a debug log filename is given.
				if (debugHandler == null && debugLogFilename != null) {
					debugHandler = new FileHandler(debugLogFilename); // true
																		// means
																		// 'append'
					ceRootLogger.addHandler(debugHandler);
				}
				thisLogger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "CE loggers set to debug levels.");
			} catch (SecurityException se) {
				// swallow, no change to logging possible
				thisLogger.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
						"cannot update logger configuration dynamically.");
				success = false;
			} catch (IOException ioe) {
				// swallow, could not set-up file handler.
				thisLogger.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "cannot configure log file dynamically.");
				success = false;
			}
		}
		return success;
	}

	/*
	 * If in debug mode, configure logging such that any CE loggers return to
	 * the previous levels before debug mode was activated (note that this will
	 * override any intervening changes to these loggers from other sources),
	 * and close the debug handler if present.
	 * 
	 * Return true if successful (regardless of whether a change occurred), and
	 * false otherwise (indicating an error occurred).
	 */
	public synchronized static boolean setDebugOff() {
		final String METHOD_NAME = "setDebugOff";
		boolean success = true;
		if (isDebugOn()) {
			try {
				LogManager logManager = LogManager.getLogManager();
				logManager.checkAccess(); // throws SecurityException if not
											// allowed
				// close the debug handler if it exists.
				if (debugHandler != null) {
					debugHandler.close();
					ceRootLogger.removeHandler(debugHandler);
					debugHandler = null;
				}
				// revert any changed CE loggers to to pre-debug levels
				setLoggerLevels(preDebugLoggerLevels);
				preDebugLoggerLevels = null;
				thisLogger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "CE loggers set to pre-debug levels.");
			} catch (SecurityException se) {
				// swallow, no change to logging possible
				thisLogger.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
						"cannot update logger configuration dynamically.");
				success = false;
			}
		}
		return success;
	}

}
