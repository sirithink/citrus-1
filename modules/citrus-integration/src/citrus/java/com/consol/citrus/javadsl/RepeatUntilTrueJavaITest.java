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
public class RepeatUntilTrueJavaITest extends TestNGCitrusTestBuilder {
    
    @CitrusTest
    public void RepeatUntilTrueJavaITest() {
        variable("max", "3");
        
        repeat(echo("index is: ${i}")).until("i gt citrus:randomNumber(1)").index("i");
        
        repeat(echo("index is: ${i}")).until("i gt= 5").index("i");
        
        repeat(echo("index is: ${i}")).until("(i gt 5) or (i = 5)").index("i");
        
        repeat(echo("index is: ${i}")).until("(i gt 5) and (i gt 3)").index("i");
        
        repeat(echo("index is: ${i}")).until("i gt 0").index("i");
        
        repeat(echo("index is: ${i}")).until("${max} lt i").index("i");
    }
}