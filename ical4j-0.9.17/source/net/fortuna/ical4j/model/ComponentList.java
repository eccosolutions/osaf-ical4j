/*
 * $Id: ComponentList.java,v 1.8 2005/09/28 09:03:53 fortuna Exp $ [Apr 5, 2004]
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
import java.util.ArrayList;
import java.util.Iterator;

import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.filter.OutputFilter;
import net.fortuna.ical4j.model.parameter.Range;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.util.Dates;

/**
 * Defines a list of iCalendar components.
 * 
 * @author Ben Fortuna
 */
public class ComponentList extends ArrayList implements Serializable {

    private static final long serialVersionUID = 7308557606558767449L;

    /**
     * Default constructor.
     */
    public ComponentList() {
    }

    /**
     * Creates a new instance with the specified initial capacity.
     * 
     * @param initialCapacity
     *            the initial capacity of the list
     */
    public ComponentList(final int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * @see java.util.AbstractCollection#toString()
     */
    public final String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator i = iterator(); i.hasNext();) {
            buffer.append(i.next().toString());
        }
        return buffer.toString();
    }

    /**
     * Write the component list to a string filtering the components according
     * to the supplied filter.
     * 
     * @param filter
     *            filter to use.
     * @return iCalendar data written.
     */
    public final String toString(OutputFilter filter) {

        // Short cut for all components
        if (filter.isAllSubComponents() && (filter.getLimit() == null))
            return toString();
        else if (filter.hasSubComponentFilters() || filter.isAllSubComponents()) {
            StringBuffer buffer = new StringBuffer();
            for (Iterator i = iterator(); i.hasNext();) {
                // Test each property to see whether it is in the filter
                Component c = (Component) i.next();

                // Check for limit
                if ((filter.getLimit() != null) && (c instanceof VEvent)) {

                    // Policy: if component has a Recurrence-ID property then
                    // include it if:
                    //
                    // a) If start/end are within limit range
                    // b) else if r-id + duration is within the limit range
                    // c) else if r-id is before limit start and
                    // range=thisandfuture
                    // d) else if r-id is after limit end and range=thisandprior
                    //
                    
                    RecurrenceId rid = (RecurrenceId) c.getProperties().getProperty(Property.RECURRENCE_ID);
                    if (rid != null) {
                        DtStart dtstart = ((VEvent)c).getStartDate();
                        DtEnd dtend = ((VEvent)c).getEndDate();
                        DateTime start = new DateTime(dtstart.getDate());
                        DateTime end = null;
                        if (dtend != null) {
                            end = new DateTime(dtend.getDate());
                        } else {
                            Dur duration = null;
                            if (start instanceof DateTime) {
                                // Its a timed event with no duration
                                duration = new Dur(0, 0, 0, 0);
                            } else {
                                // Its an all day event so duration is one day
                                duration = new Dur(1, 0, 0, 0);
                            }
                            end = (DateTime)Dates.getInstance(duration.getTime(start), start);
                        }
                        
                        Period p = new Period(start, end);
                        if (!p.intersects(filter.getLimit())) {
                            Dur duration = new Dur(start, end);
                            start = new DateTime(rid.getDate());
                            end = (DateTime)Dates.getInstance(duration.getTime(start), start);
                            p = new Period(start, end);
                            if (!p.intersects(filter.getLimit())) {
                                if (Range.THISANDFUTURE.equals(rid.getParameters().getParameter(Parameter.RANGE))) {
                                    if (start.compareTo(filter.getLimit().getEnd()) >= 0)
                                        continue;
                                }
                                else if (Range.THISANDPRIOR.equals(rid.getParameters().getParameter(Parameter.RANGE))) {
                                    if (start.compareTo(filter.getLimit().getStart()) < 0)
                                        continue;
                                } else
                                    continue;
                            }
                        } 
                    }
                }

                // Write all or some
                if (filter.isAllSubComponents()) {
                    buffer.append(c.toString());
                } else {
                    OutputFilter subfilter = filter.getSubComponentFilter(c);

                    // Check whether to write it out
                    if (subfilter != null) {
                        buffer.append(c.toString(subfilter));
                    }
                }
            }
            return buffer.toString();
        } else
            return "";
    }

    /**
     * Write component to string in special flat mode.
     * 
     * @param prefix
     * @return
     */
    public final String toStringFlat(String prefix) {
        StringBuffer buffer = new StringBuffer();
        for (Iterator i = iterator(); i.hasNext();) {
            buffer.append(((Component) i.next()).toStringFlat(prefix));
        }
        return buffer.toString();
    }

    /**
     * Returns the first component of specified name.
     * 
     * @param aName
     *            name of component to return
     * @return a component or null if no matching component found
     */
    public final Component getComponent(final String aName) {
        for (Iterator i = iterator(); i.hasNext();) {
            Component c = (Component) i.next();
            if (c.getName().equals(aName)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Returns a list containing all components with specified name.
     * 
     * @param name
     *            name of components to return
     * @return a list of components with the matching name
     */
    public final ComponentList getComponents(final String name) {
        ComponentList components = new ComponentList();
        for (Iterator i = iterator(); i.hasNext();) {
            Component c = (Component) i.next();
            if (c.getName().equals(name)) {
                components.add(c);
            }
        }
        return components;
    }

    /**
     * Add a component to the list.
     * 
     * @param component
     *            the component to add
     * @return true
     * @see List#add(java.lang.Object)
     */
    public final boolean add(final Component component) {
        return add((Object) component);
    }

    /**
     * Overrides superclass to throw an <code>IllegalArgumentException</code>
     * where argument is not a <code>net.fortuna.ical4j.model.Component</code>.
     * 
     * @see List#add(E)
     */
    public final boolean add(final Object arg0) {
        if (!(arg0 instanceof Component)) {
            throw new IllegalArgumentException("Argument not a "
                    + Component.class.getName());
        }
        return super.add(arg0);
    }

    /**
     * @return boolean indicates if the list is empty
     * @see List#isEmpty()
     */
    // public final boolean isEmpty() {
    // return components.isEmpty();
    // }
    /**
     * @return an iterator
     * @see List#iterator()
     */
    // public final Iterator iterator() {
    // return components.iterator();
    // }
    /**
     * Remove a component from the list.
     * 
     * @param component
     *            the component to remove
     * @return true if the list contained the specified component
     * @see List#remove(java.lang.Object)
     */
    public final boolean remove(final Component component) {
        return remove((Object) component);
    }

    /**
     * @return the number of components in the list
     * @see List#size()
     */
    // public final int size() {
    // return components.size();
    // }
    /**
     * Provides a list containing all components contained in this component
     * list.
     * 
     * @return a list
     */
    // public final List toList() {
    // return new ArrayList(components);
    // }
}
