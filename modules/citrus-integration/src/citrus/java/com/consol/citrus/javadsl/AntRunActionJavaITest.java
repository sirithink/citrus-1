/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.javadsl;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class AntRunActionJavaITest extends TestNGCitrusTestBuilder {

    @CitrusTest
    public void AntRunActionJavaITest() {
        variable("welcomeText", "Hello Citrus today is citrus:currentDate()!");
        variable("checked", "true");
        
        antrun("classpath:com/consol/citrus/actions/build.xml")
            .target("sayHello");
        
        antrun("classpath:com/consol/citrus/actions/build.xml")
            .targets("sayHello", "sayGoodbye");
        
        antrun("classpath:com/consol/citrus/actions/build.xml")
            .target("sayHello")
            .property("welcomeText", "${welcomeText}")
            .property("goodbyeText", "Goodbye!");
        
        antrun("classpath:com/consol/citrus/actions/build.xml")
            .target("checkMe")
            .propertyFile("classpath:com/consol/citrus/actions/build.properties");
    }
}