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

package com.consol.citrus.config.xml;

import java.util.*;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.ExecuteSQLAction;
import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.validation.script.ScriptValidationContext;

/**
 * Bean definition parser for sql action in test case.
 * 
 * @author Christoph Deppisch
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class SQLActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;
        
        String dataSource = element.getAttribute("datasource");
        if (!StringUtils.hasText(dataSource)) {
            throw new BeanCreationException("Missing proper data source reference");
        }

        List<Element> validateElements = DomUtils.getChildElementsByTagName(element, "validate");
        List<Element> extractElements = DomUtils.getChildElementsByTagName(element, "extract");
        Element scriptValidationElement = DomUtils.getChildElementByTagName(element, "validate-script");
        
        if (CollectionUtils.isEmpty(validateElements) && CollectionUtils.isEmpty(extractElements) && scriptValidationElement == null) {
            beanDefinition = parseSqlAction(element);
            beanDefinition.addPropertyValue("name", "sqlUpdate:" + dataSource);
        } else {
            beanDefinition = parseSqlQueryAction(element, scriptValidationElement, validateElements, extractElements);
            beanDefinition.addPropertyValue("name", "sqlQuery:" + dataSource);
        }
        
        beanDefinition.addPropertyReference("dataSource", dataSource);
        
        DescriptionElementParser.doParse(element, beanDefinition);

        List<String> statements = new ArrayList<String>();
        List<?> stmtElements = DomUtils.getChildElementsByTagName(element, "statement");
        for (Iterator<?> iter = stmtElements.iterator(); iter.hasNext();) {
            Element stmt = (Element) iter.next();
            statements.add(DomUtils.getTextValue(stmt));
        }
        beanDefinition.addPropertyValue("statements", statements);

        Element sqlResourceElement = DomUtils.getChildElementByTagName(element, "resource");
        if (sqlResourceElement != null) {
            beanDefinition.addPropertyValue("sqlResourcePath", sqlResourceElement.getAttribute("file"));
        }

        return beanDefinition.getBeanDefinition();
    }
    
    /**
     * Parses SQL action just executing a set of statements.
     * @param element
     * @return
     */
    private BeanDefinitionBuilder parseSqlAction(Element element) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ExecuteSQLAction.class);
        
        String ignoreErrors = element.getAttribute("ignore-errors");
        if (ignoreErrors != null && ignoreErrors.equals("true")) {
            beanDefinition.addPropertyValue("ignoreErrors", true);
        }
        
        return beanDefinition;
    }

    /**
     * Parses SQL query action with result set validation elements.
     * @param element the root element.
     * @param scriptValidationElement the optional script validation element.
     * @param validateElements validation elements.
     * @param extractElements variable extraction elements.
     * @return
     */
    private BeanDefinitionBuilder parseSqlQueryAction(Element element, Element scriptValidationElement, 
            List<Element> validateElements, List<Element> extractElements) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ExecuteSQLQueryAction.class);

        // check for script validation
        if (scriptValidationElement != null) {
            beanDefinition.addPropertyValue("scriptValidationContext", getScriptValidationContext(scriptValidationElement));
        }
        
        Map<String, List<String>> controlResultSet = new HashMap<String, List<String>>();
        for (Iterator<?> iter = validateElements.iterator(); iter.hasNext();) {
            Element validateElement = (Element) iter.next();
            Element valueListElement = DomUtils.getChildElementByTagName(validateElement, "values");
            
            if (valueListElement != null) {
                List<String> valueList = new ArrayList<String>();
                List<?> valueElements = DomUtils.getChildElementsByTagName(valueListElement, "value");
                for (Iterator<?> valueElementsIt = valueElements.iterator(); valueElementsIt.hasNext();) {
                    Element valueElement = (Element) valueElementsIt.next();
                    valueList.add(DomUtils.getTextValue(valueElement));
                }
                controlResultSet.put(validateElement.getAttribute("column"), valueList);
            } else if (validateElement.getAttribute("value") != null) {
                controlResultSet.put(validateElement.getAttribute("column"), Collections.singletonList(validateElement.getAttribute("value")));
            } else {
                throw new BeanCreationException(element.getLocalName(), 
                        "Neither value attribute nor value list is set for column validation: " + validateElement.getAttribute("column"));
            }
        }
        
        beanDefinition.addPropertyValue("controlResultSet", controlResultSet);
        
        Map<String, String> extractVariables = new HashMap<String, String>();
        for (Iterator<?> iter = extractElements.iterator(); iter.hasNext();) {
            Element validate = (Element) iter.next();
            extractVariables.put(validate.getAttribute("column"), validate.getAttribute("variable"));
        }
        
        beanDefinition.addPropertyValue("extractVariables", extractVariables);
        
        return beanDefinition;
    }

    /**
     * Constructs the script validation context.
     * @param scriptElement
     * @return
     */
    private ScriptValidationContext getScriptValidationContext(Element scriptElement) {
        String type = scriptElement.getAttribute("type");
        
        ScriptValidationContext validationContext = new ScriptValidationContext(type);
        String filePath = scriptElement.getAttribute("file");
        if (StringUtils.hasText(filePath)) {
            validationContext.setValidationScriptResourcePath(filePath);
        } else {
            validationContext.setValidationScript(DomUtils.getTextValue(scriptElement));
        }
        
        return validationContext;
    }
}
