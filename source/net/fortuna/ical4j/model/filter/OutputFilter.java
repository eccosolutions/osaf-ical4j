package net.fortuna.ical4j.model.filter;

import java.util.HashMap;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Period;

/**
 * @author cyrusdaboo
 * 
 * Created on Oct 29, 2005
 * 
 * This is a filter object that allows filtering the output writer by component
 * type or property. It follows the model defined by the CalDAV <calendar-data>
 * XML element including support of the no-value attribute. When no-value is
 * used, a property is written out up to the ':' in the stream, and then no more
 * (i.e. the value is skipped).
 * 
 */

public class OutputFilter {

    /**
     * The name of the component being filtered by this filter object.
     */
    protected String mComponentName;

    /**
     * True if all sub-components of this component should be written out. False
     * if only specified sub-components should be written out.
     */
    protected boolean mAllSubComponents;

    /**
     * List of specific sub-component names to write out when not writing them
     * all out. Others are not written out.
     */
    protected HashMap mSubComponents;

    /**
     * True if all properties in the current component should be written out.
     * False if only specified properties should be written out.
     */
    protected boolean mAllProperties;

    /**
     * List of specific properties to write out when not writing them all out.
     * Others are not written out.
     */
    protected HashMap mProperties;

    /**
     * Expand all recurrence instances within the specified period.
     */
    protected Period expand;

    /**
     * Limit which overridden recurrence instances are returned to those falling
     * in the specified period.
     */
    protected Period limit;

    /**
     * Limit the range of free busy information to only that within the
     * specified period.
     */
    protected Period limitfb;

    /**
     * Initialise the filter element.
     * 
     * @param name
     *            the name of the component represented by this filter.
     */
    public OutputFilter(String name) {
        mComponentName = name;
    }

    /**
     * @return the name of the component represented by the filter.
     */
    public String getComponentName() {
        return mComponentName;
    }

    /**
     * Test to see if a component can be written out.
     * 
     * @param comp
     *            component to test.
     * @return true if the specified component is to be written out, false if
     *         the specified component is not written out.
     */
    public boolean testComponent(Component comp) {

        return mComponentName.equalsIgnoreCase(comp.getName());
    }

    /**
     * @return true if all sub-components are to be written out, false if
     *         specified sub-components only should be written out.
     */
    public boolean isAllSubComponents() {
        return mAllSubComponents;
    }

    /**
     * Set filter to write out all sub-components of the filtered component.
     */
    public void setAllSubComponents() {
        mAllSubComponents = true;
        mSubComponents = null;
    }

    /**
     * Add a sub-component filter to this component filter.
     * 
     * @param filter
     *            the filter to add to this one for sub-components.
     */
    public void addSubComponent(OutputFilter filter) {
        if (mSubComponents == null)
            mSubComponents = new HashMap();

        // Always convert names to uppercase for use in the hashmap
        mSubComponents.put(filter.getComponentName().toUpperCase(), filter);
    }

    // Test to see if sub-component type can be written out
    /**
     * Test to see if sub-component type can be written out.
     * 
     * @param subcomp
     *            the component to test.
     * @return true if the component should be written out, false if the
     *         component should not be written out.
     */
    public boolean testSubComponent(Component subcomp) {
        return mAllSubComponents || (mSubComponents != null)
                && mSubComponents.containsKey(subcomp.getName().toUpperCase());
    }

    /**
     * @return true if there are some sub-component filters, false otherwise.
     */
    public boolean hasSubComponentFilters() {
        return (mSubComponents != null);
    }

    /**
     * Get the filter for the specified sub-component.
     * 
     * @param subcomp
     *            sub-component to retrieve filter for.
     * @return the filter for the specified sub-component.
     */
    public OutputFilter getSubComponentFilter(Component subcomp) {
        if (mSubComponents != null)
            return (OutputFilter) mSubComponents.get(subcomp.getName()
                    .toUpperCase());
        else
            return null;
    }

    /**
     * @return true if all properties are to be written out, false if only
     *         specified properties are to be written out.
     */
    public boolean isAllProperties() {
        return mAllProperties;
    }

    /**
     * Set filter to write out all properties of the filtered component.
     */
    public void setAllProperties() {
        mAllProperties = true;
        mProperties = null;
    }

    /**
     * Add a property name for a specific property to write out.
     * 
     * @param name
     *            name of the property to write out.
     * @param no_value
     *            true if the value of the property should NOT be written out,
     *            false if the value should be written out.
     */
    public void addProperty(String name, boolean no_value) {
        if (mProperties == null)
            mProperties = new HashMap();

        // Hashmap key always uses uppercase names
        mProperties.put(name.toUpperCase(), new Boolean(no_value));
    }

    /**
     * @return true if there are some named properties that have to be written
     *         out, false otherwise.
     */
    public boolean hasPropertyFilters() {
        return (mProperties != null);
    }

    /**
     * Test to see if property can be written out and also return whether the
     * property value is used.
     * 
     * @param name
     *            name of property to test
     * @return a boolean array of size 2:
     * 
     * [0] true if the property should be written out, false otherwise.
     * 
     * [1] true if the property value should NOT be written out, false
     * otherwise.
     */
    public boolean[] testPropertyValue(String name) {

        boolean[] result = new boolean[2];

        if (mAllProperties) {
            result[0] = true;
            result[1] = false;
            return result;
        }

        if (mProperties == null) {
            result[0] = false;
            result[1] = false;
            return result;
        }

        Boolean presult = (Boolean) mProperties.get(name.toUpperCase());
        result[0] = (presult != null);
        if (presult != null)
            result[1] = presult.booleanValue();
        return result;
    }

    /**
     * @return Returns the expand.
     */
    public Period getExpand() {
        return expand;
    }

    /**
     * @param expand The expand to set.
     */
    public void setExpand(Period expand) {
        this.expand = expand;
    }

    /**
     * @return Returns the limit.
     */
    public Period getLimit() {
        return limit;
    }

    /**
     * @param limit The limit to set.
     */
    public void setLimit(Period limit) {
        this.limit = limit;
    }

    /**
     * @return Returns the limitfb.
     */
    public Period getLimitfb() {
        return limitfb;
    }

    /**
     * @param limitfb The limitfb to set.
     */
    public void setLimitfb(Period limitfb) {
        this.limitfb = limitfb;
    }
    
    
}
