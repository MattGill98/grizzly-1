/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.grizzly.spdy;

import org.glassfish.grizzly.http.util.HttpStatus;

/**
 * The class represents the data to be pushed from server to client.
 * SPDY unidirectional stream will be used in order to push this data.
 * 
 * The {@link #builder()} has to be used in order to
 * create a <tt>PushData</tt> instance.
 * 
 * @author Alexey Stashok.
 */
public final class PushData {
    private OutputResource resource;
    
    private int priority;
    
    private HttpStatus statusCode = HttpStatus.OK_200;
    
    private String contentType;

    public static PushDataBuilder builder() {
        return new PushDataBuilder();
    }
    
    private PushData() {
    }

    /**
     * Returns the {@link OutputResource} to be pushed.
     */
    public OutputResource getOutputResource() {
        return resource;
    }

    /**
     * Returns the SPDY stream priority to be used.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the {@link HttpStatus} to be pushed along with the resource.
     */
    public HttpStatus getStatusCode() {
        return statusCode;
    }

    /**
     * Returns data content-type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * PushData builder to be used to create {@link PushData} instance.
     */
    public static class PushDataBuilder {
        private final PushData pushData = new PushData();
        
        /**
         * Sets the {@link OutputResource} to be pushed.
         * @param resource the {@link OutputResource} to be pushed.
         * 
         * @return {@link PushDataBuilder}.
         */
        public PushDataBuilder outputResource(final OutputResource resource) {
            pushData.resource = resource;
            return this;
        }
        
        /**
         * Sets the SPDY stream priority to be used.
         * @param priority the SPDY stream priority to be used.
         * 
         * @return {@link PushDataBuilder}.
         */
        public PushDataBuilder priority(final int priority) {
            pushData.priority = priority;
            return this;
        }
        
        /**
         * Sets the {@link HttpStatus} to be pushed along with the resource.
         * @param statusCode the {@link HttpStatus} to be pushed along with the resource.
         * 
         * @return {@link PushDataBuilder}.
         */
        public PushDataBuilder statusCode(final HttpStatus statusCode) {
            pushData.statusCode = statusCode;
            return this;
        }
        
        /**
         * Sets the HTTP status to be pushed along with the resource.
         * @param statusCode the HTTP status to be pushed along with the resource.
         * 
         * @return {@link PushDataBuilder}.
         */
        public PushDataBuilder statusCode(final int statusCode) {
            pushData.statusCode = HttpStatus.getHttpStatus(statusCode);
            return this;
        }

        /**
         * Sets the HTTP status and the reason phrase to be pushed along with the resource.
         * @param statusCode the HTTP status to be pushed along with the resource.
         * @param reasonPhrase the HTTP status reason phrase to be pushed along with the resource.
         * 
         * @return {@link PushDataBuilder}.
         */
        public PushDataBuilder statusCode(final int statusCode,
                final String reasonPhrase) {
            pushData.statusCode =
                    HttpStatus.newHttpStatus(statusCode, reasonPhrase);
            return this;
        }

        /**
         * Sets the push data content-type.
         * @param contentType the push data content-type.
         * 
         * @return {@link PushDataBuilder}.
         */
        public PushDataBuilder contentType(final String contentType) {
            pushData.contentType = contentType;
            return this;
        }

        /**
         * Returns the {@link PushData} instance.
         */
        public PushData build() {
            return pushData;
        }
    }
}
