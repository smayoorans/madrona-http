package org.madrona.http.common;

/**
 * Base interface for things that convert an HTTP header to an appropriate
 * Java object and back.  See <a href="./Headers.html">Headers</a> for
 * useful implementations.
 * @author Mayooran
 */
public interface HeaderValueType<T> {
    /**
     * The Java type
     * @return A type
     */
    public Class<T> type();

    /**
     * The header name as it should appear in HTTP headers
     * @return The name
     */
    public String name();

    /**
     * Convert an object to a String suitable for inclusion in headers.
     * 
     * @param value A value
     * @return A header value
     */
    public String toString(T value);
    
    /**
     * Parse the value of a header of this type, returning an appropriate
     * Java object or null
     * @param value A header
     * @return An object that represents the header appropriately, such as a
     * <code>DateTime</code> for a date header.
     */
    public T toValue(String value);
}