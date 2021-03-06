/*
 * $Id: PropertyList.java,v 1.8 2005/09/28 09:03:53 fortuna Exp $ [Apr 5, 2004]
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

import net.fortuna.ical4j.model.filter.OutputFilter;

/**
 * Defines a list of iCalendar properties.
 * 
 * @author Ben Fortuna
 */
public class PropertyList extends ArrayList implements Serializable {

    private static final long serialVersionUID = -8875923766224921031L;

    /**
     * Default constructor.
     */
    public PropertyList() {
    }

    /**
     * Creates a new instance with the specified initial capacity.
     * 
     * @param initialCapacity
     *            the initial capacity of the list
     */
    public PropertyList(final int initialCapacity) {
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
     * Write the property list to a string filtering the properties according to
     * the supplied filter.
     * 
     * @param filter
     *            filter to use.
     * @return iCalendar data written.
     */
    public final String toString(OutputFilter filter) {

        // Short cut for all properties
        if (filter.isAllProperties())
            return toString();
        else if (filter.hasPropertyFilters()) {
            StringBuffer buffer = new StringBuffer();
            for (Iterator i = iterator(); i.hasNext();) {
                // Test each property to see whether it is in the filter
                Property p = (Property) i.next();
                boolean[] filterit = filter.testPropertyValue(p.getName());

                // Check whether to write it out
                if (filterit[0]) {
                    // Check whether no-value is set
                    if (filterit[1]) {
                        // Write without the value
                        buffer.append(p.toStringNoValue());
                    } else {
                        buffer.append(p.toString());
                    }
                }
            }
            return buffer.toString();
        } else
            return "";
    }

    /**
     * Right all properties to string in special 'flat' format.
     * 
     * @param prefix
     * @return
     */
    public final String toStringFlat(String prefix) {
        StringBuffer buffer = new StringBuffer();
        for (Iterator i = iterator(); i.hasNext();) {
            buffer.append(((Property)i.next()).toStringFlat(prefix));
        }
        return buffer.toString();
    }

    /**
     * Returns the first property of specified name.
     * 
     * @param aName
     *            name of property to return
     * @return a property or null if no matching property found
     */
    public final Property getProperty(final String aName) {
        for (Iterator i = iterator(); i.hasNext();) {
            Property p = (Property) i.next();
            if (p.getName().equals(aName)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Returns a list of properties with the specified name.
     * 
     * @param name
     *            name of properties to return
     * @return a property list
     */
    public final PropertyList getProperties(final String name) {
        PropertyList list = new PropertyList();
        for (Iterator i = iterator(); i.hasNext();) {
            Property p = (Property) i.next();
            if (p.getName().equals(name)) {
                list.add(p);
            }
        }
        return list;
    }

    /**
     * Add a property to the list.
     * 
     * @param property
     *            the property to add
     * @return true
     * @see List#add(java.lang.Object)
     */
    public final boolean add(final Property property) {
        return add((Object) property);
    }

    /**
     * Overrides superclass to throw an <code>IllegalArgumentException</code>
     * where argument is not a <code>net.fortuna.ical4j.model.Property</code>.
     * 
     * @see List#add(E)
     */
    public final boolean add(final Object arg0) {
        if (!(arg0 instanceof Property)) {
            throw new IllegalArgumentException("Argument not a "
                    + Property.class.getName());
        }
        return super.add(arg0);
    }

    /**
     * @return boolean indicates if the list is empty
     * @see List#isEmpty()
     */
    // public final boolean isEmpty() {
    // return properties.isEmpty();
    // }
    /**
     * @return an iterator
     * @see List#iterator()
     */
    // public final Iterator iterator() {
    // return properties.iterator();
    // }
    /**
     * Remove a property from the list.
     * 
     * @param property
     *            the property to remove
     * @return true if the list contained the specified property
     * @see List#remove(java.lang.Object)
     */
    public final boolean remove(final Property property) {
        return remove((Object) property);
    }

    /**
     * @return the number of properties in the list
     * @see List#size()
     */
    // public final int size() {
    // return properties.size();
    // }
}
