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

package com.consol.citrus.validation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;

/**
 * Deprecated since version 1.2
 * TODO: Remove this class in major version update
 *
 * @author Christoph Deppisch
 * @since 2007
 * @deprecated since 1.2, in favor of using {@link DomXmlMessageValidator}
 */
@Deprecated
public class DefaultXMLMessageValidator implements MessageValidator<XmlMessageValidationContext> {
    
    @Autowired
    private DomXmlMessageValidator domXmlMessageValidatorDelegate;
    
    /**
     * Delegate to new dom tree xml validator
     */
    public void validateMessage(Message<?> receivedMessage, TestContext context, List<ValidationContext> validationContexts) {
        domXmlMessageValidatorDelegate.validateMessage(receivedMessage, context, validationContexts);
    }
    
    /**
     * Delegate to new dom tree xml validator
     */
    public void validateMessage(Message<?> receivedMessage, TestContext context, XmlMessageValidationContext validationContext) {
        domXmlMessageValidatorDelegate.validateMessage(receivedMessage, context, validationContext);
    }
    
    /**
     * Delegate to new dom tree xml validator
     */
    public XmlMessageValidationContext findValidationContext(List<ValidationContext> validationContexts) {
        return domXmlMessageValidatorDelegate.findValidationContext(validationContexts);
    }

    /**
     * Delegate to new dom tree xml validator
     */
    public boolean supportsMessageType(String messageType) {
        return domXmlMessageValidatorDelegate.supportsMessageType(messageType);
    }
}
