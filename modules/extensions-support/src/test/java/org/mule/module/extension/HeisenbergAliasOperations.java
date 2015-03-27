/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension;

import static org.mule.module.extension.HeisenbergExtension.HEISENBERG;
import org.mule.extension.annotations.ImplementationOf;
import org.mule.extension.annotations.Operation;
import org.mule.extension.annotations.param.Optional;


public class HeisenbergAliasOperations
{

    private final HeisenbergExtension config;

    public HeisenbergAliasOperations(HeisenbergExtension config)
    {
        this.config = config;
    }

    @Operation
    @ImplementationOf(HeisenbergExtension.class)
    public String alias(@Optional(defaultValue = HEISENBERG) String alias)
    {
        return String.format("Hello, my name is %s.", alias);
    }
}
