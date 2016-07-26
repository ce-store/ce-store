package com.ibm.ets.ita.ce.store.hudson.helper;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

public class Duration<S, E> {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private final S start;
	private final E end;

	public Duration(S start, E end) {
		this.start = start;
		this.end = end;
	}

	public S getStart() {
		return start;
	}

	public E getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "Duration (" + start + "-" + end + ")";
	}

}
