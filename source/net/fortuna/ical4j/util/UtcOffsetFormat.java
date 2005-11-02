/*
 * $Id: UtcOffsetFormat.java,v 1.4 2005/06/26 12:33:48 fortuna Exp $ [15-May-2004]
 *
 * Copyright (c) 2004, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 	o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 	o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 	o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fortuna.ical4j.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Defines the format used by all iCalendar UTC offsets.
 * 
 * @author benfortuna
 * @deprecated use <code>net.fortuna.ical4j.model.UtcOffset</code> instead.
 */
public final class UtcOffsetFormat {

    private static final long MILLIS_PER_SECOND = 1000;

    private static final long MILLIS_PER_MINUTE = 60000;

    private static final long MILLIS_PER_HOUR = 3600000;

    private static final int HOUR_START_INDEX = 1;

    private static final int HOUR_END_INDEX = 3;

    private static final int MINUTE_START_INDEX = 3;

    private static final int MINUTE_END_INDEX = 5;

    private static final int SECOND_START_INDEX = 5;

    private static final int SECOND_END_INDEX = 7;

    private static final NumberFormat HOUR_FORMAT = new DecimalFormat("00");

    private static final NumberFormat MINUTE_FORMAT = new DecimalFormat("00");

    private static final NumberFormat SECOND_FORMAT = new DecimalFormat("00");

    private static Log log = LogFactory.getLog(UtcOffsetFormat.class);

    private static UtcOffsetFormat instance = new UtcOffsetFormat();

    /**
     * Constructor made private to enforce singleton.
     */
    private UtcOffsetFormat() {
    }

    /**
     * @return Returns the instance.
     */
    public static UtcOffsetFormat getInstance() {
        return instance;
    }

    /**
     * Parses an iCalendar utc-offset string.
     * 
     * @param aString
     *            the utc-offset string to parse
     * @return the timezone offset in milliseconds
     */
    public long parse(final String aString) {

        // debugging..
        if (log.isDebugEnabled()) {
            log.debug("Parsing string [" + aString + "]");
        }

        long offset = 0;

        boolean negative = aString.startsWith("-");

        offset += Integer.parseInt(aString.substring(HOUR_START_INDEX,
                HOUR_END_INDEX))
                * MILLIS_PER_HOUR;

        offset += Integer.parseInt(aString.substring(MINUTE_START_INDEX,
                MINUTE_END_INDEX))
                * MILLIS_PER_MINUTE;

        try {

            offset += Integer.parseInt(aString.substring(SECOND_START_INDEX,
                    SECOND_END_INDEX))
                    * MILLIS_PER_SECOND;
        }
        catch (Exception e) {
            // seconds not supplied..
            log.debug("Seconds not specified", e);
        }

        if (negative) { return -offset; }

        return offset;
    }

    /**
     * Returns a string representation of an UTC offset.
     * 
     * @param offset
     *            an UTC offset in milliseconds
     * @return a string
     */
    public String format(final long offset) {

        StringBuffer b = new StringBuffer();

        long remainder = Math.abs(offset);

        if (offset < 0) {
            b.append('-');
        }
        else {
            b.append('+');
        }

        b.append(HOUR_FORMAT.format(remainder / MILLIS_PER_HOUR));

        remainder = remainder % MILLIS_PER_HOUR;

        b.append(MINUTE_FORMAT.format(remainder / MILLIS_PER_MINUTE));

        remainder = remainder % MILLIS_PER_MINUTE;

        if (remainder > 0) {
            b.append(SECOND_FORMAT.format(remainder / MILLIS_PER_SECOND));
        }

        return b.toString();
    }
}