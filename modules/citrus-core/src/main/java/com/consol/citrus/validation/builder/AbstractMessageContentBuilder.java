/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.validation.builder;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.message.MessageHeaderType;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Abstract control message builder is aware of message headers and delegates message payload
 * generation to subclass.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractMessageContentBuilder<T> implements MessageContentBuilder<T> {

    /** The control headers expected for this message */
    private Map<String, Object> messageHeaders = new HashMap<String, Object>();

    /** The message header as a file resource path */
    private String messageHeaderResourcePath;

    /** The message header as inline data */
    private String messageHeaderData;

    /** Optional data dictionary that explicitly modifies control message content before construction */
    private DataDictionary dataDictionary;

    /** List of manipulators for static message payload */
    private List<MessageConstructionInterceptor> messageInterceptors = new ArrayList<MessageConstructionInterceptor>();

    /**
     * Constructs the control message with headers and payload coming from 
     * subclass implementation.
     */
    @Override
    public Message<T> buildMessageContent(TestContext context, String messageType) {

        T payload = buildMessagePayload(context);

        Message<T> message = MessageBuilder.withPayload(payload)
                .copyHeaders(buildMessageHeaders(context))
                .build();

        if (payload != null) {
            if (dataDictionary != null) {
                message = (Message<T>) dataDictionary.interceptMessageConstruction(message, messageType, context);
            }

            message = (Message<T>) context.getMessageConstructionInterceptors().interceptMessageConstruction(message, messageType, context);

            for (MessageConstructionInterceptor modifyer : messageInterceptors) {
                message = (Message<T>) modifyer.interceptMessageConstruction(message, messageType, context);
            }
        }

        return message;
    }
    
    protected abstract T buildMessagePayload(TestContext context);

    protected Map<String, Object> buildMessageHeaders(TestContext context) {
        try {
            Map<String, Object> headers = context.resolveDynamicValuesInMap(messageHeaders);

            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                String value = entry.getValue().toString();
                
                if (MessageHeaderType.isTyped(value)) {
                    MessageHeaderType type = MessageHeaderType.fromTypedValue(value);
                    Constructor<?> constr = type.getHeaderClass().getConstructor(new Class[] { String.class });
                    entry.setValue(constr.newInstance(MessageHeaderType.removeTypeDefinition(value)));
                }
            }
            
            String headerContent = null;
            if (messageHeaderResourcePath != null) {
                headerContent = context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(messageHeaderResourcePath, context)));
            } else if (messageHeaderData != null){
                headerContent = context.replaceDynamicContentInString(messageHeaderData.trim());
            }
            
            if (StringUtils.hasText(headerContent)) {
                headers.put(CitrusMessageHeaders.HEADER_CONTENT, headerContent);
            }
            
            checkHeaderTypes(headers);
            
            return headers;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to build message content", e);
        }
    }
    
    /**
     * Method checks all header types to meet Spring Integration type requirements. For instance
     * sequence number must be of type {@link Integer}.
     * 
     * @param headers the headers to check.
     */
    private void checkHeaderTypes(Map<String, Object> headers) {
        if (headers.containsKey(MessageHeaders.SEQUENCE_NUMBER)) {
            String number = headers.get(MessageHeaders.SEQUENCE_NUMBER).toString();
            headers.put(MessageHeaders.SEQUENCE_NUMBER, Integer.valueOf(number));
        }
        
        if (headers.containsKey(MessageHeaders.SEQUENCE_SIZE)) {
            String size = headers.get(MessageHeaders.SEQUENCE_SIZE).toString();
            headers.put(MessageHeaders.SEQUENCE_SIZE, Integer.valueOf(size));
        }
    }

    @Override
    public void setDataDictionary(DataDictionary dataDictionary) {
        this.dataDictionary = dataDictionary;
    }

    /**
     * Gets the data dictionary.
     * @return
     */
    public DataDictionary getDataDictionary() {
        return dataDictionary;
    }

    /**
     * Sets the message headers for this control message.
     * @param messageHeaders the controlMessageHeaders to set
     */
    public void setMessageHeaders(Map<String, Object> messageHeaders) {
        this.messageHeaders = messageHeaders;
    }

    /**
     * Sets the message header resource.
     * @param messageHeaderResource the messageHeaderResource to set
     */
    public void setMessageHeaderResourcePath(String messageHeaderResource) {
        this.messageHeaderResourcePath = messageHeaderResource;
    }

    /**
     * Sets the message header data.
     * @param messageHeaderData the messageHeaderData to set
     */
    public void setMessageHeaderData(String messageHeaderData) {
        this.messageHeaderData = messageHeaderData;
    }

    /**
     * Gets the messageHeaders.
     * @return the messageHeaders
     */
    public Map<String, Object> getMessageHeaders() {
        return messageHeaders;
    }

    /**
     * Gets the messageHeaderResource.
     * @return the messageHeaderResource the messageHeaderResource to get.
     */
    public String getMessageHeaderResourcePath() {
        return messageHeaderResourcePath;
    }

    /**
     * Gets the messageHeaderData.
     * @return the messageHeaderData the messageHeaderData to get.
     */
    public String getMessageHeaderData() {
        return messageHeaderData;
    }

    /**
     * Adds a new interceptor to the message construction process.
     * @param interceptor
     */
    public void add(MessageConstructionInterceptor interceptor) {
        messageInterceptors.add(interceptor);
    }

    /**
     * Gets the messageInterceptors.
     * @return the messageInterceptors
     */
    public List<MessageConstructionInterceptor> getMessageInterceptors() {
        return messageInterceptors;
    }

    /**
     * Sets the messageInterceptors.
     * @param messageInterceptors the messageInterceptors to set
     */
    public void setMessageInterceptors(
            List<MessageConstructionInterceptor> messageInterceptors) {
        this.messageInterceptors = messageInterceptors;
    }
}
