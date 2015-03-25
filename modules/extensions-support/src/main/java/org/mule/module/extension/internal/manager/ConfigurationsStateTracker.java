/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal.manager;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.extension.introspection.Configuration;
import org.mule.extension.introspection.Operation;
import org.mule.extension.introspection.OperationExecutor;
import org.mule.util.CollectionUtils;
import org.mule.util.Preconditions;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import org.apache.commons.collections.Predicate;

final class ConfigurationsStateTracker
{

    private final Multimap<Configuration, ConfigurationInstanceWrapper<?>> configurationInstances = LinkedHashMultimap.create());

    <C> void registerInstance(Configuration configuration, final String name, final C configurationInstance)
    {
        ConfigurationInstanceWrapper<C> instanceWrapper = new ConfigurationInstanceWrapper<>(name, configurationInstance);
        synchronized (configurationInstances) {
            if (configurationInstances.containsValue(instanceWrapper)) {
                throw new IllegalArgumentException("Can't register the sema configuration instance twice");
            }

            configurationInstances.put(configuration, instanceWrapper);
        }
    }

    <C> OperationExecutor getOperationExecutor(Operation operation, C configurationInstance) {
        ConfigurationInstanceWrapper<C> wrapper = locateConfigurationInstanceWrapper(configurationInstance);
        checkArgument(wrapper != null, "Can't create an operation executor for an unregistered configuration instance");
        return wrapper.getOperationExecutor(operation);
    }

    private <C> ConfigurationInstanceWrapper<C> locateConfigurationInstanceWrapper(final C configurationInstance) {
        return (ConfigurationInstanceWrapper<C>) CollectionUtils.find(configurationInstances.values(), new Predicate()
        {
            @Override
            public boolean evaluate(Object object)
            {
                return ((ConfigurationInstanceWrapper<C>) object).getConfigurationInstance() == configurationInstance;
            }
        });
    }
}
