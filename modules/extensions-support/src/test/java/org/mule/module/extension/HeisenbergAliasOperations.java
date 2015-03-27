/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension;

import org.mule.extension.annotations.ImplementationOf;
import org.mule.extension.annotations.Operation;
import org.mule.extension.annotations.Parameters;

public class HeisenbergAliasOperations
{

    private final HeisenbergExtension config;

    public HeisenbergAliasOperations(HeisenbergExtension config)
    {
        this.config = config;
    }

    @Operation
    @ImplementationOf(HeisenbergExtension.class)
    public String alias(@Parameters PersonalInfo personalInfo)
    {
        return String.format("Hello, my name is %s. I'm %d years old", personalInfo.getMyName(), personalInfo.getAge());
    }
}
