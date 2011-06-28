/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.grizzly.websockets.draft06;

import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.util.http.MimeHeaders;
import com.sun.grizzly.util.net.URL;
import com.sun.grizzly.websockets.HandShake;
import com.sun.grizzly.websockets.HandshakeException;
import com.sun.grizzly.websockets.NetworkHandler;
import com.sun.grizzly.websockets.WebSocketEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HandShake06 extends HandShake {

    private final SecKey secKey;
    private List<String> enabledExtensions;
    private List<String> enabledProtocols;

    public HandShake06(URL url) {
        super(url);
        secKey = new SecKey();
    }

    public HandShake06(MimeHeaders mimeHeaders) {
        determineHostAndPort(mimeHeaders);

        checkForHeader(mimeHeaders, "Upgrade", "WebSocket");
        checkForHeader(mimeHeaders, "Connection", "Upgrade");

        setSubProtocol(split(mimeHeaders.getHeader(WebSocketEngine.SEC_WS_PROTOCOL_HEADER)));
        setExtensions(split(mimeHeaders.getHeader(WebSocketEngine.SEC_WS_EXTENSIONS_HEADER)));
        secKey = SecKey.generateServerKey(new SecKey(mimeHeaders.getHeader(WebSocketEngine.SEC_WS_KEY_HEADER)));

        setOrigin(readHeader(mimeHeaders, WebSocketEngine.SEC_WS_ORIGIN_HEADER));
        buildLocation();
        if (getServerHostName() == null || getOrigin() == null) {
            throw new HandshakeException("Missing required headers for WebSocket negotiation");
        }
    }

    public void setHeaders(Response response) {
        response.setMessage(WebSocketEngine.RESPONSE_CODE_MESSAGE);
        response.setHeader(WebSocketEngine.SEC_WS_ACCEPT, secKey.getSecKey());
        if (getEnabledExtensions() != null) {
            response.setHeader(WebSocketEngine.SEC_WS_EXTENSIONS_HEADER, join(getSubProtocol()));
        }
    }

    public void initiate(NetworkHandler handler) {
        try {
            ByteArrayOutputStream chunk = new ByteArrayOutputStream();
            chunk.write(String.format("GET %s HTTP/1.1\r\n", getResourcePath()).getBytes());
            chunk.write(String.format("Host: %s\r\n", getServerHostName()).getBytes());
            chunk.write(String.format("Connection: Upgrade\r\n").getBytes());
            chunk.write(String.format("Upgrade: WebSocket\r\n").getBytes());
            chunk.write(String.format("%s: %s\r\n", WebSocketEngine.SEC_WS_KEY_HEADER, secKey).getBytes());
            chunk.write(String.format("%s: %s\r\n", WebSocketEngine.SEC_WS_ORIGIN_HEADER, getOrigin()).getBytes());
            chunk.write(String.format("%s: %s\r\n", WebSocketEngine.SEC_WS_VERSION, getVersion()).getBytes());
            if (!getSubProtocol().isEmpty()) {
                chunk.write(String.format("%s: %s\r\n", WebSocketEngine.SEC_WS_PROTOCOL_HEADER,
                        join(getSubProtocol())).getBytes());
            }
            if (!getExtensions().isEmpty()) {
                chunk.write(String.format("%s: %s\r\n", WebSocketEngine.SEC_WS_EXTENSIONS_HEADER,
                        join(getExtensions())).getBytes());
            }
            chunk.write("\r\n".getBytes());

            handler.write(chunk.toByteArray());
        } catch (IOException e) {
            throw new HandshakeException(e.getMessage(), e);
        }
    }

    protected int getVersion() {
        return 6;
    }

    public void validateServerResponse(final Map<String, String> headers) throws HandshakeException {
        super.validateServerResponse(headers);
        secKey.validateServerKey(headers.get(WebSocketEngine.SEC_WS_ACCEPT));
    }

    public List<String> getEnabledExtensions() {
        return enabledExtensions;
    }

    public List<String> getEnabledProtocols() {
        return enabledProtocols;
    }
}
