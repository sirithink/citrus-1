/*
 * Copyright 2006-2012 the original author or authors.
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
package com.consol.citrus.channel.selector;

import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class RootQNameMessageSelectorTest {

    @Test
    public void testQNameSelector() {
        RootQNameMessageSelector messageSelector = new RootQNameMessageSelector("Foo");
        
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<Foo><text>foobar</text></Foo>").build()));
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<Bar><text>foobar</text></Bar>").build()));
        
        messageSelector = new RootQNameMessageSelector("{}Foo");
        
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<Foo><text>foobar</text></Foo>").build()));
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<Bar><text>foobar</text></Bar>").build()));
    }
    
    @Test
    public void testQNameWithNamespaceSelector() {
        RootQNameMessageSelector messageSelector = new RootQNameMessageSelector("{http://citrusframework.org/schema}Foo");
        
        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("<Foo xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Foo>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<Foo><text>foobar</text></Foo>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<Foo xmlns=\"http://citrusframework.org/schema/foo\"><text>foobar</text></Foo>").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("<Bar xmlns=\"http://citrusframework.org/schema\"><text>foobar</text></Bar>").build()));
    }
    
    @Test
    public void testNonXmlPayload() {
        RootQNameMessageSelector messageSelector = new RootQNameMessageSelector("{http://citrusframework.org/schema}Foo");
        
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("PLAINTEXT").build()));
    }
    
    @Test
    public void testInvalidQName() {
        try {
            new RootQNameMessageSelector("{http://citrusframework.org/schemaFoo");
            Assert.fail("Missing exception due to invalid QName");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid root QName"));
        }
    }
    
}
