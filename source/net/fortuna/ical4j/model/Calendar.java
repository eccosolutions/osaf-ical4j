/*
 * $Id: Calendar.java,v 1.16 2005/12/10 08:57:17 fortuna Exp $ [Apr 5, 2004]
 *
 * Copyright (c) 2004, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
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
package net.fortuna.ical4j.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.TreeSet;

import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.filter.OutputFilter;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.util.PropertyValidator;

/**
 * Defines an iCalendar calendar.
 * 
 * <pre>
 *    4.6 Calendar Components
 *    
 *       The body of the iCalendar object consists of a sequence of calendar
 *       properties and one or more calendar components. The calendar
 *       properties are attributes that apply to the calendar as a whole. The
 *       calendar components are collections of properties that express a
 *       particular calendar semantic. For example, the calendar component can
 *       specify an event, a to-do, a journal entry, time zone information, or
 *       free/busy time information, or an alarm.
 *    
 *       The body of the iCalendar object is defined by the following
 *       notation:
 *    
 *         icalbody   = calprops component
 *    
 *         calprops   = 2*(
 *    
 *                    ; 'prodid' and 'version' are both REQUIRED,
 *                    ; but MUST NOT occur more than once
 *    
 *                    prodid /version /
 *    
 *                    ; 'calscale' and 'method' are optional,
 *                    ; but MUST NOT occur more than once
 *    
 *                    calscale        /
 *                    method          /
 *    
 *                    x-prop
 *    
 *                    )
 *    
 *         component  = 1*(eventc / todoc / journalc / freebusyc /
 *                    / timezonec / iana-comp / x-comp)
 *    
 *         iana-comp  = &quot;BEGIN&quot; &quot;:&quot; iana-token CRLF
 *    
 *                      1*contentline
 *    
 *                      &quot;END&quot; &quot;:&quot; iana-token CRLF
 *    
 *         x-comp     = &quot;BEGIN&quot; &quot;:&quot; x-name CRLF
 *    
 *                      1*contentline
 *    
 *                      &quot;END&quot; &quot;:&quot; x-name CRLF
 * </pre>
 * 
 * Example 1 - Creating a new calendar:
 * 
 * <pre><code>
 * Calendar calendar = new Calendar();
 * calendar.getProperties().add(new ProdId(&quot;-//Ben Fortuna//iCal4j 1.0//EN&quot;));
 * calendar.getProperties().add(Version.VERSION_2_0);
 * calendar.getProperties().add(CalScale.GREGORIAN);
 * 
 * // Add events, etc..
 * </code></pre>
 * 
 * @author Ben Fortuna
 */
public class Calendar implements Serializable {

    private static final long serialVersionUID = -1654118204678581940L;

    public static final String BEGIN = "BEGIN";

    public static final String VCALENDAR = "VCALENDAR";

    public static final String END = "END";

    private PropertyList properties;

    private ComponentList components;

    /**
     * Default constructor.
     */
    public Calendar() {
        this(new PropertyList(), new ComponentList());
    }

    /**
     * Constructs a new calendar with no properties and the specified
     * components.
     * 
     * @param components
     *            a list of components to add to the calendar
     */
    public Calendar(final ComponentList components) {
        this(new PropertyList(), components);
    }

    /**
     * Constructor.
     * 
     * @param p
     *            a list of properties
     * @param c
     *            a list of components
     */
    public Calendar(final PropertyList p, final ComponentList c) {
        this.properties = p;
        this.components = c;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(BEGIN);
        buffer.append(':');
        buffer.append(VCALENDAR);
        buffer.append("\r\n");
        buffer.append(getProperties());
        buffer.append(getComponents());
        buffer.append(END);
        buffer.append(':');
        buffer.append(VCALENDAR);
        buffer.append("\r\n");

        return buffer.toString();
    }

    /**
     * Write calendar component to string and filter the sub-components and
     * properties as requested.
     * 
     * @param filter
     *            the filter to apply to the sub-components and properties.
     * @return the iCalendar data written out.
     */
    public final String toString(OutputFilter filter) {

        Calendar calendar = this;

        // If expansion of recurrence is required what we have to do is create a
        // whole new calendar object with the new expanded components in it and
        // then write that one out
        if (filter.getExpand() != null) {
            calendar = createExpanded(filter);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(BEGIN);
        buffer.append(':');
        buffer.append(VCALENDAR);
        buffer.append("\r\n");
        buffer.append(calendar.getProperties().toString(filter));
        buffer.append(calendar.getComponents().toString(filter));
        buffer.append(END);
        buffer.append(':');
        buffer.append(VCALENDAR);
        buffer.append("\r\n");

        return buffer.toString();
    }

    private Calendar createExpanded(OutputFilter filter) {

        // Create a new calendar with the same top-level properties as this one
        Calendar newCal = new Calendar();
        newCal.getProperties().addAll(getProperties());

        // Now look at each component and determine whether expansion is
        // required
        InstanceList instances = new InstanceList();
        ComponentList overrides = new ComponentList();
        Component master = null;
        for (Iterator iter = getComponents().iterator(); iter.hasNext();) {
            Component comp = (Component) iter.next();

            if ((comp instanceof VEvent) || (comp instanceof VJournal)
                    || (comp instanceof VToDo)) {
                // See if this is the master instance
                if (comp.getProperties().getProperty(Property.RECURRENCE_ID) == null) {
                    master = comp;
                    instances.addComponent(comp, filter.getExpand().getStart(),
                            filter.getExpand().getEnd());
                } else {
                    overrides.add(comp);
                }
            } else if (comp instanceof VTimeZone) {
                // Ignore VTIMEZONEs as we convert all date-time properties to
                // UTC
            } else {
                // Create new component and convert properties to UTC
                Component newcomp = comp.copy();
                componentToUTC(newcomp);
                newCal.getComponents().add(newcomp);
            }
        }

        for (Iterator iterator = overrides.iterator(); iterator.hasNext();) {
            Component comp = (Component) iterator.next();
            instances.addComponent(comp, null, null);
        }

        // Create a copy of the master with recurrence properties removed
        boolean isRecurring = false;
        Component masterCopy = master.copy();
        for (Iterator iter = masterCopy.getProperties().iterator(); iter
                .hasNext();) {
            Property prop = (Property) iter.next();
            if ((prop instanceof RRule) || (prop instanceof RDate)
                    || (prop instanceof ExRule) || (prop instanceof ExDate)) {
                iter.remove();
                isRecurring = true;
            }
        }

        // Expand each instance within the requested range
        TreeSet sortedKeys = new TreeSet(instances.keySet());
        for (Iterator iter = sortedKeys.iterator(); iter.hasNext();) {
            String ikey = (String) iter.next();
            Instance instance = (Instance) instances.get(ikey);

            // Make sure this instance is within the requested range
            if ((filter.getExpand().getStart().compareTo(instance.getEnd()) >= 0)
                    || (filter.getExpand().getEnd().compareTo(
                            instance.getStart()) <= 0))
                continue;
            
            // Create appropriate copy
            Component copy = null;
            if (instance.getComp() == master) {
                copy = masterCopy.copy();
            } else {
                copy = instance.getComp().copy();
            }
            componentToUTC(copy);

            // Adjust the copy to match the actual instance info
            if (isRecurring) {
                // Add RECURRENCE-ID, replacing existing if present
                RecurrenceId rid = (RecurrenceId) copy.getProperties()
                        .getProperty(Property.RECURRENCE_ID);
                if (rid != null) {
                    copy.getProperties().remove(rid);
                }
                rid = new RecurrenceId(instance.getRid());
                copy.getProperties().add(rid);

                // Adjust DTSTART (in UTC)
                DtStart olddtstart = (DtStart) copy.getProperties().getProperty(
                        Property.DTSTART);
                if (olddtstart != null) {
                    copy.getProperties().remove(olddtstart);
                }
                DtStart newdtstart = new DtStart(instance.getStart());
                if ((newdtstart.getDate() instanceof DateTime) && (((DateTime)newdtstart.getDate()).getTimeZone() != null)) {
                    newdtstart.setUtc(true);
                }
                copy.getProperties().add(newdtstart);

                // If DTEND present, replace it (in UTC)
                DtEnd olddtend = (DtEnd) copy.getProperties().getProperty(
                        Property.DTEND);
                if (olddtend != null) {
                    copy.getProperties().remove(olddtend);
                    DtEnd newdtend = new DtEnd(instance.getEnd());
                    if ((newdtend.getDate() instanceof DateTime) && (((DateTime)newdtend.getDate()).getTimeZone() != null)) {
                        newdtend.setUtc(true);
                    }
                    copy.getProperties().add(newdtend);
                }
            }
            
            // Now have a valid expanded instance so add it
            newCal.getComponents().add(copy);
        }

        return newCal;
    }

    /**
     * Convert all DATE-TIME properties to UTC, remvoing timezone references.
     * 
     * @param comp
     */
    private void componentToUTC(Component comp) {
        
        // Do to each top-level property
        for (Iterator iter = comp.getProperties().iterator(); iter.hasNext();) {
            Property prop = (Property) iter.next();
            if (prop instanceof DateProperty)
            {
                DateProperty dprop = (DateProperty) prop;
                if ((dprop.getDate() instanceof DateTime)
                        && (((DateTime) dprop.getDate()).getTimeZone() != null)) {
                    dprop.setUtc(true);
                }
            }
        }
        
        // Do to each embedded component
        ComponentList subcomps = null;
        if (comp instanceof VEvent) {
            subcomps = ((VEvent)comp).getAlarms();
        } else if (comp instanceof VToDo) {
            subcomps = ((VToDo)comp).getAlarms();
        }
        
        if (subcomps != null) {
            for (Iterator iter = subcomps.iterator(); iter.hasNext();) {
                Component subcomp = (Component) iter.next();
                componentToUTC(subcomp);
            }
        }
    }

    /**
     * Write calendar component to string using the special 'flat' format.
     * 
     * @return the iCalendar data written out.
     */
    public final String toStringFlat() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(VCALENDAR);
        buffer.append(":BEGIN\n");
        buffer.append(getProperties().toStringFlat(VCALENDAR));
        buffer.append(getComponents().toStringFlat(VCALENDAR));

        return buffer.toString();
    }

    /**
     * @return Returns the components.
     */
    public final ComponentList getComponents() {
        return components;
    }

    /**
     * @return Returns the properties.
     */
    public final PropertyList getProperties() {
        return properties;
    }

    /**
     * Perform validation on the calendar, its properties and its components in
     * its current state.
     * 
     * @throws ValidationException
     *             where the calendar is not in a valid state
     */
    public final void validate() throws ValidationException {
        validate(true);
    }

    /**
     * Perform validation on the calendar in its current state.
     * 
     * @param recurse
     *            indicates whether to validate the calendar's properties and
     *            components
     * @throws ValidationException
     *             where the calendar is not in a valid state
     */
    public final void validate(final boolean recurse)
            throws ValidationException {
        // 'prodid' and 'version' are both REQUIRED,
        // but MUST NOT occur more than once
        PropertyValidator.getInstance().assertOne(Property.PRODID, properties);
        PropertyValidator.getInstance().assertOne(Property.VERSION, properties);

        // 'calscale' and 'method' are optional,
        // but MUST NOT occur more than once
        PropertyValidator.getInstance().assertOneOrLess(Property.CALSCALE,
                properties);
        PropertyValidator.getInstance().assertOneOrLess(Property.METHOD,
                properties);

        // must contain at least one component
        if (getComponents().isEmpty()) {
            throw new ValidationException(
                    "Calendar must contain at least one component");
        }

        // validate properties..
        for (Iterator i = getProperties().iterator(); i.hasNext();) {
            Property property = (Property) i.next();

            if (!(property instanceof XProperty)
                 && !property.isCalendarProperty()) {
                throw new ValidationException(
                    "Invalid property: " + property.getName());
            }
        }

        // validate components..
        for (Iterator i = getComponents().iterator(); i.hasNext();) {
            Component component = (Component) i.next();

            if (!component.isCalendarComponent()) {
                throw new ValidationException(
                    "Not a valid calendar component: " + component.getName());
            }
        }

        if (recurse) {
            validateProperties();
            validateComponents();
        }
    }

    /**
     * Invoke validation on the calendar properties in its current state.
     * 
     * @throws ValidationException
     *             where any of the calendar properties is not in a valid state
     */
    private void validateProperties() throws ValidationException {
        for (Iterator i = getProperties().iterator(); i.hasNext();) {
            Property property = (Property) i.next();
            property.validate();
        }
    }

    /**
     * Invoke validation on the calendar components in its current state.
     * 
     * @throws ValidationException
     *             where any of the calendar components is not in a valid state
     */
    private void validateComponents() throws ValidationException {
        for (Iterator i = getComponents().iterator(); i.hasNext();) {
            Component component = (Component) i.next();
            component.validate();
        }
    }
    
    /**
     * Two calendars are equal if and only if their property lists and component
     * lists are equal.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public final boolean equals(final Object arg0) {
        if (arg0 instanceof Calendar) {
            Calendar calendar = (Calendar) arg0;
            return getProperties().equals(calendar.getProperties())
                    && getComponents().equals(calendar.getComponents());
        }
        return super.equals(arg0);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public final int hashCode() {
        return getProperties().hashCode() + getComponents().hashCode();
    }
}
