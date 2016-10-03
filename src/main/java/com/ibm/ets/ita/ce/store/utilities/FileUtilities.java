package com.ibm.ets.ita.ce.store.utilities;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.NL;
import static com.ibm.ets.ita.ce.store.names.MiscNames.BS;
import static com.ibm.ets.ita.ce.store.names.MiscNames.ENCODING;
import static com.ibm.ets.ita.ce.store.names.MiscNames.URL_SEP;
import static com.ibm.ets.ita.ce.store.names.MiscNames.URL_EQUALS;
import static com.ibm.ets.ita.ce.store.names.MiscNames.URL_AMPERSAND;
import static com.ibm.ets.ita.ce.store.names.MiscNames.FILE_SEP;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_CEFOLDER;
import static com.ibm.ets.ita.ce.store.names.RestNames.HDR_AUTH;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.isReportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportMicroDebug;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportError;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportException;
import static com.ibm.ets.ita.ce.store.utilities.ReportingUtilities.reportWarning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public abstract class FileUtilities {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String CLASS_NAME = FileUtilities.class.getName();
	private static final String PACKAGE_NAME = FileUtilities.class.getPackage().getName();
	private static final Logger logger = Logger.getLogger(PACKAGE_NAME);

	public static void appendToSb(StringBuilder pSb, String pText) {
		appendToSbNoNl(pSb, pText);
		pSb.append(NL);
	}

	public static void appendToSbNoNl(StringBuilder pSb, String pText) {
		pSb.append(pText);
	}

	public static void appendNewLineToSb(StringBuilder pSb) {
		pSb.append(NL);
	}

	public static String listTextFor(Object[] pList) {
		StringBuilder sb = new StringBuilder();
		String separator = "";

		for (Object thisObj : pList) {
			appendToSbNoNl(sb, separator);
			appendToSbNoNl(sb, thisObj.toString());

			separator = ", ";
		}

		return sb.toString();
	}

	public static String convertToString(ActionContext pAc, BufferedReader pReader) {
		final String METHOD_NAME = "convertToString";

		StringBuilder sb = new StringBuilder();
		String thisLine = "";

		try {
			while ((thisLine = pReader.readLine()) != null) {
				if (sb.length() != 0) {
					sb.append(NL);
				}

				appendToSbNoNl(sb, thisLine);
			}
		} catch (IOException e) {
			reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
		}

		return sb.toString();
	}

	// close stream, ignoring any exception (stream may be null).
	public static void close(InputStream stream) {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (IOException ioe) {
			// swallow
		}
	}

	// close reader, ignoring any exception (reader may be null).
	public static void close(Reader reader) {
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (IOException ioe) {
			// swallow
		}
	}

	// close stream, ignoring any exception (stream may be null).
	public static void close(OutputStream stream) {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (IOException ioe) {
			// swallow
		}
	}

	// close writer, ignoring any exception (writer may be null).
	public static void close(Writer writer) {
		try {
			if (writer != null) {
				writer.close();
			}
		} catch (IOException ioe) {
			// swallow
		}
	}

	public static String calculateFullFilenameFrom(ActionContext pAc, String pFilename) {
		String result = pFilename;

		if (pFilename.contains(BS)) {
			reportWarning("Unexpected file separator encountered in path name '" + pFilename + "' (The forward slash character is operating system independent as is preferred by the CE Store)", pAc);
		}

		if (result.contains(TOKEN_CEFOLDER)) {
			//Replace the CE folder token with the rootFolder name - try all three combinations of possible filenames
			result = result.replace(TOKEN_CEFOLDER + FILE_SEP, pAc.getCeConfig().getRootFolder());
			result = result.replace(TOKEN_CEFOLDER + BS, pAc.getCeConfig().getRootFolder());
			result = result.replace(TOKEN_CEFOLDER, pAc.getCeConfig().getRootFolder());
		}

		return result;
	}

	public static void writeToFile(ActionContext pAc, StringBuilder pSb, String pFilename) {
		final String METHOD_NAME = "writeToFile";

		String tgtFilename = calculateFullFilenameFrom(pAc, pFilename);

		if (pSb != null) {
			int sbLen = pSb.length();
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tgtFilename), ENCODING));
				try {
					//TODO: Pick a better chunk size than 1, e.g. 1024
					for (int i = 0; i < sbLen; i++) {
						out.write(pSb.charAt(i));
					}

					out.flush();
				} catch (IOException e) {
					reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
				}
			} catch (UnsupportedEncodingException e) {
				reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
			} catch (FileNotFoundException e) {
				reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					reportException(e, "close", pAc, logger, CLASS_NAME, METHOD_NAME);
				}
			}
		} else {
			reportError("Unable to write to file (contents is null), filename=" + tgtFilename, pAc);
		}
	}

	public static void writeToFile(ActionContext pAc, String pContents, String pFilename) {
		writeToFile(pAc, pContents, pFilename, false); 
	}

	public static void writeToFile(ActionContext pAc, String pContents, String pFilename, boolean pAppend) {
		final String METHOD_NAME = "writeToFile";

		String tgtFilename = calculateFullFilenameFrom(pAc, pFilename);

		if (pContents != null) {
			try {
				BufferedWriter out = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(tgtFilename, pAppend), ENCODING));
				try {
					out.write(pContents);
					out.flush();
				} catch (IOException e) {
					reportException(e, "flush", pAc, logger, CLASS_NAME, METHOD_NAME);
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {
						reportException(e, "close", pAc, logger, CLASS_NAME, METHOD_NAME);
					}
				}
			} catch (UnsupportedEncodingException e) {
				reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
			} catch (FileNotFoundException e) {
				reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
			}
		} else {
			reportError("Unable to write to file (contents is null), filename=" + tgtFilename, pAc);
		}
	}

	public static void writeToFile(ActionContext pAc, InputStream pIn, String pFilename) {
		final String METHOD_NAME = "writeToFile";

		String tgtFilename = calculateFullFilenameFrom(pAc, pFilename);
		try {
			FileOutputStream out = new FileOutputStream(tgtFilename);
			try {
				final int BUF_SIZE = 1 << 8;
				byte[] buffer = new byte[BUF_SIZE];
				int bytesRead = -1;
				while((bytesRead = pIn.read(buffer)) > -1) {
					out.write(buffer, 0, bytesRead);
				}
				out.flush();
			} catch (IOException e) {
				reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					reportException(e, "close", pAc, logger, CLASS_NAME, METHOD_NAME);
				}
			}
		} catch (FileNotFoundException e) {
			reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
		}
	}

	public static void createFolderIfNeeded(ActionContext pAc, String pFolderName) {
		String tgtFolderName = calculateFullFilenameFrom(pAc, pFolderName);

		File f = new File(tgtFolderName);
		if (!f.exists()) {
			createFolder(tgtFolderName);
		}
	}

	public static void checkForFolderOnStartup(ActionContext pAc, String pFolderName) {
		String tgtFolderName = calculateFullFilenameFrom(pAc, pFolderName);

		File f = new File(tgtFolderName);
		if (!f.exists()) {
			reportDebug("The folder '" + tgtFolderName + "' does not exist and must be created.", pAc);
		}
	}

	private static boolean createFolder(String pFolderName) {
		return new File(pFolderName).mkdir();
	}

	public static void deleteFile(ActionContext pAc, String pFilename) { 
		String tgtFilename = calculateFullFilenameFrom(pAc, pFilename);

		File f = new File(tgtFilename);
		if (f.exists()) {
			f.delete();
		}
	}

	public static void emptyFolder(ActionContext pAc, String pFolderName) {
		String tgtFolderName = calculateFullFilenameFrom(pAc, pFolderName);

		File dir = new File(tgtFolderName);
		if (!dir.exists()) {
			reportError("Unable to empty the specified folder (" + tgtFolderName + ") since it does not exist", pAc);
		}

		String[] filenameList = dir.list();
		for (int i = 0; i < filenameList.length; i++) {
			String thisFilename = filenameList[i];
			File f = new File(getFolderValueFor(pAc, tgtFolderName) + thisFilename);

			//Only attempt to remove files
			if (f.isFile()) {
				boolean success = f.delete();

				if (!success) {
					reportError("Could not delete file: " + f.getPath(), pAc);
				}
			}
		}
	}

	public static boolean fileExists(ActionContext pAc, String pFilename) {
		String tgtFilename = calculateFullFilenameFrom(pAc, pFilename);

		return new File(tgtFilename).exists();
	}

	public static ArrayList<String> listFullFilenamesInFolder(ActionContext pAc, String pFolderName) {
		ArrayList<String> result = new ArrayList<String>();
		String tgtFolderName = calculateFullFilenameFrom(pAc, pFolderName);

		File folder = new File(tgtFolderName);
		File[] fileArray = folder.listFiles();

		if (fileArray != null) {
			for (File thisFile : fileArray) {
				result.add(tgtFolderName + thisFile.getName());
			}
		} else {
			reportError("Folder '" + pFolderName + "' could not be found", pAc);
		}

		return result;
	}

	public static ArrayList<String> listRawFilenamesInFolder(ActionContext pAc, String pFolderName) {
		ArrayList<String> result = new ArrayList<String>();
		String tgtFolderName = calculateFullFilenameFrom(pAc, pFolderName);

		File folder = new File(tgtFolderName);
		File[] fileArray = folder.listFiles();

		if (fileArray != null) {
			for (File thisFile : fileArray) {
				result.add(thisFile.getName());
			}
		}

		return result;
	}

	public static String readTextFromFile(ActionContext pAc, String pFilename) {
		final String METHOD_NAME = "readTextFromFile";
		
		String tgtFilename = calculateFullFilenameFrom(pAc, pFilename);
		StringBuilder sb = new StringBuilder();
		String thisLine = "";

		try {
			BufferedReader brd = new BufferedReader(new InputStreamReader(new FileInputStream(tgtFilename), ENCODING));
			while ((thisLine = brd.readLine()) != null) {
				sb.append(thisLine + NL);
			}
			brd.close();
		} catch (IOException e) {
			reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
		}

		return sb.toString();
	}

	public static BufferedReader bufferedReaderFromFile(ActionContext pAc, String pFilename) {
		final String METHOD_NAME = "bufferedReaderFromFile";
		
		String tgtFilename = calculateFullFilenameFrom(pAc, pFilename);
		BufferedReader brd = null;

		try {
			brd = new BufferedReader(new InputStreamReader(new FileInputStream(tgtFilename), ENCODING));
		} catch (IOException e) {
			reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
		}

		return brd;
	}

	public static void appendToFileWithNewlines(ActionContext pAc, String pFilename, String pContent) {
		writeToFile(pAc, pContent + NL + NL, pFilename, true);
	}

	public static String urlEncode(ActionContext pAc, String pValue) {
		final String METHOD_NAME = "urlEncode";
		
		String result = "";

		try {
			result = URLEncoder.encode(pValue, ENCODING);
		} catch (UnsupportedEncodingException e) {
			reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
		}

		return result;
	}

	public static String urlDecode(ActionContext pAc, String pValue) {
		final String METHOD_NAME = "urlDecode";
		
		String result = "";

		if (pValue != null) {
			try {
				result = URLDecoder.decode(pValue, ENCODING);
			} catch (UnsupportedEncodingException e) {
				reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
			}
		}

		return result;
	}

	private static URLConnection createUrlConnection(ActionContext pAc, String pEncUrl) {
		final String METHOD_NAME = "createUrlConnection";

		URL url = null;
		URLConnection conn = null;

		try {
			url = new URL(pEncUrl);
			conn = url.openConnection();
			
			//Specify the credentials (provided in the original HTTP request that started this action) if they have been provided
			if (pAc.hasCredentials()) {
				conn.setRequestProperty(HDR_AUTH, pAc.getCredentials());
			}
		} catch (IOException e) {
			reportException(e, pEncUrl, pAc, logger, CLASS_NAME, METHOD_NAME);
		}

		return conn;
	}

	public static BufferedReader bufferedReaderFromUrlWithApplicationTextHeader(ActionContext pAc, String pUrl) {
		TreeMap<String, String> headers = new TreeMap<String, String>();
		
		headers.put("Accept", "application/text");

		return makeUrlRequest(pAc, pUrl, null, headers, false, false);
	}

	public static BufferedReader bufferedReaderFromUrl(ActionContext pAc, String pUrl) {
		return makeUrlRequest(pAc, pUrl, null, null, false, false);
	}

	private static BufferedReader makeUrlRequest(ActionContext pAc, String pUrl, String pPostParms, TreeMap<String, String> pHdrs, boolean pIsPost, boolean pQuiet) {
		final String METHOD_NAME = "bufferedReaderFromUrl";
		
		URLConnection conn = null;
		BufferedReader brd = null;
		String encUrl = null;

		//Manual encoding of spaces characters due to http 505 issue with Tomcat
		encUrl = pUrl.replaceAll(" ", "%20");

		if (encUrl.contains(TOKEN_CEFOLDER)) {
			encUrl = encUrl.replace(TOKEN_CEFOLDER, pAc.getCeConfig().getRootFolder());
		}

		if (isReportMicroDebug()) {
			reportMicroDebug("Sending HTTP request to '" + encUrl + "'", pAc);
		}

		conn = createUrlConnection(pAc, encUrl);

		// Deal with the response
		if (conn != null) {
			try {
				if (pIsPost) {
					//POST specific stuff
					conn.setDoOutput(true);
					
					OutputStreamWriter wr;
					wr = new OutputStreamWriter(conn.getOutputStream());
					wr.write(pPostParms);
					wr.flush();
					wr.close();
				}

				//Process any headers
				if (pHdrs != null) {
					for (String key : pHdrs.keySet()) {
						conn.setRequestProperty(key, pHdrs.get(key));
					}
				}

				brd = new BufferedReader(new InputStreamReader(conn.getInputStream(), ENCODING));
			} catch (IOException e) {
				if (!pQuiet) {
					reportException(e, encUrl, pAc, logger, CLASS_NAME, METHOD_NAME);
				}
			}
		} else {
			reportError("Unable to open URL '" + encUrl + "' (null connection)", pAc);
		}

		return brd;
	}
	
	public static BufferedReader bufferedReaderFromString(String pText) {
		return new BufferedReader(new StringReader(pText));
	}

	public static String getFilenameFromUrl(String pUrl) {
		String result = "";

		if (pUrl != null) {
			String urlBits[] = pUrl.split(URL_SEP);
			//TODO: Add error checking
			result = urlBits[urlBits.length - 1];

			//Ensure any parameters on the url are removed
			String parmBits[] = result.split("\\?");

			result = parmBits[0];
		} else {
			result = "";
		}

		return result;
	}

	public static String getFilenameFrom(String pFullFilename) {
		String result = "";

		if (pFullFilename != null) {
			String fnBits[] = pFullFilename.split(FILE_SEP);
			//TODO: Add error checking
			result = fnBits[fnBits.length - 1];
		} else {
			result = "";
		}

		return result;
	}

	public static String getFolderValueFor(ActionContext pAc, String pFolderName) {
		//Using a hardcoded '/' character instead of File.Separator since this is
		//overall consistent and works in all three operating systems that we are
		//targeting
		String result = calculateFullFilenameFrom(pAc, pFolderName);

		if ((result != null) && (!result.isEmpty())) {
			//Append a folder separator if needed
			if (!result.endsWith(FILE_SEP)) {
				result += FILE_SEP;
			}
		}

		return result;
	}

	public static String encodeForFilename(String pFilename) {
		//Remove any characters that are not alphanumeric or underscore
		return pFilename.replaceAll("[^A-Za-z0-9_]", "");
	}

	public static String encodeForBatchFile(String pValue) {
		//Characters need to be escaped
		return pValue
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("%", "%%")
				.replace("^", "^^")
				.replace(">", "^>")
				.replace("<", "^<")
				.replace("|", "^|")
				.replace("'", "^'")
				.replace("`", "^`")
				.replace(",", "^,")
				.replace(";", "^;")
				.replace("=", "^=")
				.replace("(", "^(")
				.replace(")", "^)")
				.replace("!", "^!")
				.replace("[", "\\[")
				.replace("]", "\\]")
				.replace("&", "^&");
	}

	public static String joinFolderAndFilename(ActionContext pAc, String pFolderName, String pFilename) {
		return getFolderValueFor(pAc, pFolderName) + pFilename;
	}

	public static void copyFile(ActionContext pAc, File pSrcFile, File pTgtFile) {
		final String METHOD_NAME = "copyFile";
		
		try {
			if (pSrcFile.isFile()) {
				if ((!pTgtFile.exists()) || (pTgtFile.isFile())) {
					if (!pTgtFile.exists()) {
						pTgtFile.createNewFile();
					}
		
					FileInputStream sfis = null;
					FileOutputStream tfis = null;
					FileChannel srcChannel = null;
					FileChannel tgtChannel = null;
		
					try {
						sfis = new FileInputStream(pSrcFile);
						tfis = new FileOutputStream(pTgtFile);
						
						if (sfis != null) {
							srcChannel = sfis.getChannel();
							
							if (tfis != null) {
								tgtChannel = tfis.getChannel();
								tgtChannel.transferFrom(srcChannel, 0, srcChannel.size());
							}
						}
					} finally {
						if (sfis != null) {
							sfis.close();
						}
						
						if (tfis != null) {
							tfis.close();
						}

						if (srcChannel != null) {
							srcChannel.close();
						}

						if (tgtChannel != null) {
							tgtChannel.close();
						}
					}
				} else {
					reportError("Cannot copy file from '" + pSrcFile.getName() + "' to '" + pTgtFile.getName() + "' as target destination is not a file", pAc);
				}
			} else {
				reportError("Cannot copy file from '" + pSrcFile.getName() + "' to '" + pTgtFile.getName() + "' as source destination is not a file", pAc);
			}
		} catch (IOException e) {
			reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
		}
	}

	public static void unzipFile(ActionContext pAc, String pFilename, String pOutputFolder) {
		final String METHOD_NAME = "unzipFile";
		
		createFolderIfNeeded(pAc, pOutputFolder);

		try {
			ZipFile zSrcFile = new ZipFile(pFilename);

			@SuppressWarnings("unchecked")
			Enumeration<ZipEntry> zfEntries = (Enumeration<ZipEntry>)zSrcFile.entries();

			while (zfEntries.hasMoreElements()) {
				ZipEntry thisZfEntry = zfEntries.nextElement();

				if (thisZfEntry.isDirectory()) {
					reportWarning("Inner directory structures are not currently exported (" + thisZfEntry.getName() + ")", pAc);
				} else {
					String targetFilename = pOutputFolder + thisZfEntry.getName();
					writeToFile(pAc, zSrcFile.getInputStream(thisZfEntry), targetFilename);
				}
			}

			zSrcFile.close();
		} catch (IOException e) {
			reportException(e, pAc, logger, CLASS_NAME, METHOD_NAME);
		}
	}

	public static String sendHttpGetRequest(ActionContext pAc, String pTargetUrl, TreeMap<String, String> pUrlParms, boolean pQuiet) {
		return sendHttpRequest(pAc, pTargetUrl, pUrlParms, false, pQuiet);
	}

	public static String sendHttpPostRequest(ActionContext pAc, String pTargetUrl, TreeMap<String, String> pUrlParms, boolean pQuiet) {
		return sendHttpRequest(pAc, pTargetUrl, pUrlParms, true, pQuiet);
	}

	private static String sendHttpRequest(ActionContext pAc, String pTargetUrl, TreeMap<String, String> pPostParms, boolean pIsPost, boolean pQuiet) {
		final String METHOD_NAME = "sendHttpRequest";
		
		String response = null;
		String parmText = null;
		
		try {
			parmText = constructParameterString(pPostParms);
		} catch (UnsupportedEncodingException e) {
			reportException(e, pTargetUrl, pAc, logger, CLASS_NAME, METHOD_NAME);
		}

		response = sendAndExtractResponseFrom(pAc, pTargetUrl, parmText, pIsPost, pQuiet);

		if (isReportMicroDebug()) {
			reportMicroDebug("HTTP response received: " + response, pAc);
		}

		return response;
	}
	
	private static String constructParameterString(TreeMap<String, String> pPostParms) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		
		if (pPostParms != null) {
			String cp = ENCODING;

			for (String thisKey : pPostParms.keySet()) {
				String thisVal = pPostParms.get(thisKey);

				if (!thisVal.isEmpty()) {
					if (!(sb.length() == 0)) {
						sb.append(URL_AMPERSAND);
					}
					sb.append(URLEncoder.encode(thisKey, cp));
					sb.append(URL_EQUALS);
					sb.append(URLEncoder.encode(thisVal, cp));
				}
			}
		}

		return sb.toString();
	}

	private static String sendAndExtractResponseFrom(ActionContext pAc, String pUrl, String pPostParms, boolean pIsPost, boolean pQuiet) {
		final String METHOD_NAME = "sendAndExtractResponseFrom";

		StringBuilder sb = new StringBuilder();

		try {
			BufferedReader rd = makeUrlRequest(pAc, pUrl, pPostParms, null, pIsPost, pQuiet);

			if (rd != null) {
				String line = null;

				while ((line = rd.readLine()) != null) {
					sb.append(line);
					sb.append(NL);
				}

				rd.close();
			}
		} catch (IOException e) {
			reportException(e, pUrl, pAc, logger, CLASS_NAME, METHOD_NAME);
		}

		return sb.toString();
	}

}
