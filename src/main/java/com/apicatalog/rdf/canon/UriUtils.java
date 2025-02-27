/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apicatalog.rdf.canon;

import java.net.URI;

class UriUtils {

    private UriUtils() {
    }

    /**
     * Check if the provided URI ends with generic delimiter.
     *
     * @see <a href="https://tools.ietf.org/html/rfc3986#section-2.2">URI - Reserved
     *      Characters </a>
     *
     * @param uri to check
     * @return <code>true</code> if the provided URI ends with delimiter
     */
    public static final boolean endsWithGenDelim(final String uri) {
        return uri.endsWith(":") || uri.endsWith("/") || uri.endsWith("?") || uri.endsWith("#") || uri.endsWith("[")
                || uri.endsWith("]") || uri.endsWith("@");
    }

    /**
     * Deprecated in favor of {@link UriUtils#isNotAbsoluteUri(String, boolean)}
     *
     * @deprecated since 1.3.0
     *
     * @param uri to check
     * @return <code>true</code> if the given URI is not absolute
     */
    @Deprecated
    public static final boolean isNotAbsoluteUri(final String uri) {
        return isNotAbsoluteUri(uri, UriValidationPolicy.Full);
    }

    /**
     * Deprecated in favor of
     * {@link UriUtils#isNotAbsoluteUri(String, UriValidationPolicy)}
     *
     * @deprecated since 1.3.0
     *
     * @param uri to check
     * @return <code>true</code> if the given URI is absolute
     */
    @Deprecated
    public static final boolean isNotAbsoluteUri(final String uri, final boolean validate) {
        return !isAbsoluteUri(uri, validate ? UriValidationPolicy.Full : UriValidationPolicy.SchemeOnly);
    }

    public static final boolean isNotAbsoluteUri(final String uri, UriValidationPolicy policy) {
        return !isAbsoluteUri(uri, policy);
    }

    /**
     * Deprecated in favor of
     * {@link UriUtils#isAbsoluteUri(String, UriValidationPolicy)}
     *
     * @deprecated since 1.3.0
     *
     * @param uri to check
     * @return <code>true</code> if the given URI is absolute
     */
    @Deprecated
    public static final boolean isAbsoluteUri(final String uri) {
        return isAbsoluteUri(uri, UriValidationPolicy.Full);
    }

    /**
     * Deprecated in favor of
     * {@link UriUtils#isAbsoluteUri(String, UriValidationPolicy)}
     *
     * @deprecated since 1.4.2
     *
     * @param uri to check
     * @return <code>true</code> if the given URI is absolute
     */
    @Deprecated
    public static final boolean isAbsoluteUri(final String uri, boolean validate) {
        return isAbsoluteUri(uri, validate ? UriValidationPolicy.Full : UriValidationPolicy.SchemeOnly);
    }

    public static final boolean isAbsoluteUri(final String uri, final UriValidationPolicy policy) {
        switch (policy) {
        case None:
            return true;
        case SchemeOnly:
            return startsWithScheme(uri);
        case Full:
            if (uri == null
                    || uri.length() < 3 // minimal form s(1):ssp(1)
            ) {
                return false;
            } else {
                try {
                    return URI.create(uri).isAbsolute();
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        default:
            return false;
        }

    }

    private static final boolean startsWithScheme(final String uri) {

        if (uri == null
                || uri.length() < 2 // a scheme must have at least one letter followed by ':'
                || !Character.isLetter(uri.codePointAt(0)) // a scheme name must start with a letter
        ) {
            return false;
        }

        for (int i = 1; i < uri.length(); i++) {

            if (
            // a scheme name must start with a letter followed by a letter/digit/+/-/.
            Character.isLetterOrDigit(uri.codePointAt(i))
                    || uri.charAt(i) == '-' || uri.charAt(i) == '+' || uri.charAt(i) == '.') {
                continue;
            }

            // a scheme name must be terminated by ':'
            return uri.charAt(i) == ':';
        }
        return false;
    }

    protected static final String recompose(final String scheme, final String authority, final String path, final String query, final String fragment) {

        final StringBuilder builder = new StringBuilder();

        if (scheme != null) {
            builder.append(scheme);
            builder.append(":");
        }
        if (authority != null) {
            builder.append("//");
            builder.append(authority);
        }
        if (path != null) {
            builder.append(path);
        }
        if (query != null) {
            builder.append('?');
            builder.append(query);
        }
        if (fragment != null) {
            builder.append('#');
            builder.append(fragment);
        }
        return builder.toString();
    }
}
