/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal.manager;

import org.mule.extension.introspection.Configuration;
import org.mule.extension.introspection.Operation;
import org.mule.extension.introspection.OperationExecutor;

final class ExtensionState
{

    private final ConfigurationsStateTracker configurationsState = new ConfigurationsStateTracker();

    <C> void registerConfigurationInstance(String name, Configuration configuration, C configurationInstance)
    {
        configurationsState.registerInstance(configuration, name, configurationInstance);
    }

    <C> OperationExecutor getOperationExecutor(Configuration configuration, Operation operation, C configurationInstance) {
        return configurationsState.getOperationExecutor(configuration, operation, configurationInstance);
    }

    <C> void registerOperationExecutor(C configurationI)
}
