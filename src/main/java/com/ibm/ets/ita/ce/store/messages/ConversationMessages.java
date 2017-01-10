package com.ibm.ets.ita.ce.store.messages;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

public class ConversationMessages {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

	public static final String MSG_UNEXPECTEDTRIGGER = "Unexpected trigger type (%01) for %02";
	public static final String MSG_IGNORINGMSG = "Ignoring message '%01' as it was not directed to me";
	public static final String MSG_EXECTIME = "Conversation processing took %01 seconds";
	public static final String MSG_NOTRIGINST = "No trigger instance found";
	public static final String MSG_NOTRIGDETAILS = "Unable to get trigger details for: %01";
	public static final String MSG_CANNOTSAVE = "Cannot save conversation card as no card type is specified, in response to: %01";
	public static final String MSG_THANKYOU = "Thank you for your message";
	public static final String MSG_SAVED = "I have saved that to the knowledge base";
	public static final String MSG_DECLINED = "I'm sorry but I can't help you.  You don't have the right authorisation.";
	public static final String MSG_LOGIN = "You must log in before you can interact with the system";
	public static final String MSG_NOTAUTH = "I'm sorry but the user named '%01' is not authorised to interact with the system.  Please log in as an authorised user.";
	public static final String MSG_BADACT = "I cannot respond to that request.\nThe '%01' speech act is not authorised for user '%02'";
	public static final String MSG_NOTUNDERSTOOD = "I wasn't able to understand any of that, sorry.";
	public static final String MSG_NOTHINGDONE = "I didn't do anything because I don't think you said anything";
	public static final String MSG_REPEATED = "You've said that already!";
	public static final String MSG_NEGATION = "I'm not able to handle negative statements such as 'no', 'not' and 'doesn't'";
	public static final String MSG_NOTHINGELSE = "Sorry, I don't have anything else to tell you";
	public static final String MSG_NOFURTHER = "Sorry, I don't have any further explanation for that";
	public static final String MSG_NOEXPAND = "You didn't tell me what to expand on";
	public static final String MSG_NOCMD = "Command handling is not yet implemented";
	public static final String MSG_CARDTYPE_NOTSUPP = "Processing for this card type is not yet implemented: %01 (%02)";
	public static final String MSG_LOCCARD = "Error: Unable to locate card '%01' (%02) when processing confirm card '%03'";


}
