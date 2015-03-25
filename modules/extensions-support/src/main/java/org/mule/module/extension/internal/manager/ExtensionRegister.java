/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal.manager;

import org.mule.extension.exception.NoSuchExtensionException;
import org.mule.extension.introspection.Configuration;
import org.mule.extension.introspection.Extension;
import org.mule.extension.introspection.Operation;
import org.mule.util.CollectionUtils;

import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.Predicate;

final class ExtensionRegister
{

    private final Map<String, Extension> extensions = new ConcurrentHashMap<>();
    private final Map<Configuration, Extension> configuration2ExtensionCache = new ConcurrentHashMap<>();
    private final Map<Operation, Extension> operation2ExtensionCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Set<Extension>> capability2ExtensionCache = new ConcurrentHashMap<>();

    ExtensionRegister()
    {
    }

    void registerExtension(String name, Extension extension)
    {
        extensions.put(name, extension);
        clearCaches();
    }

    Set<Extension> getExtensions()
    {
        return ImmutableSet.copyOf(extensions.values());
    }

    boolean containsExtension(String name)
    {
        return extensions.containsKey(name);
    }

    Extension getExtension(String name)
    {
        return extensions.get(name);
    }

    Extension getExtension(Configuration configuration)
    {
        Extension extension = lookupInCache(configuration2ExtensionCache, configuration);

        if (extension == null)
        {
            throw new NoSuchExtensionException("Could not find a registered extension which contains the configuration " + configuration.getName());
        }

        return extension;
    }

    Extension getExtension(Operation operation)
    {
        Extension extension = lookupInCache(operation2ExtensionCache, operation);

        if (extension == null)
        {
            throw new NoSuchExtensionException("Could not find a registered extension which contains the operation " + operation.getName());
        }

        return extension;
    }

    private <K, V> V lookupInCache(Map<K, V> cache, final K key)
    {
        V value = cache.get(key);
        if (value == null)
        {
            value = (V) CollectionUtils.find(extensions.values(), new Predicate()
            {
                @Override
                public boolean evaluate(Object object)
                {
                    return key == object;
                }
            });

            if (value != null)
            {
                cache.put(key, value);
            }
        }

        return value;
    }

    <C> Set<Extension> getExtensionsCapableOf(Class<C> capabilityType)
    {
        Set<Extension> cachedCapables = capability2ExtensionCache.get(capabilityType);
        if (CollectionUtils.isEmpty(cachedCapables))
        {
            ImmutableSet.Builder<Extension> capables = ImmutableSet.builder();
            for (Extension extension : getExtensions())
            {
                if (extension.isCapableOf(capabilityType))
                {
                    capables.add(extension);
                }
            }

            cachedCapables = capables.build();
            capability2ExtensionCache.put(capabilityType, cachedCapables);
        }

        return cachedCapables;
    }

    private void clearCaches()
    {
        configuration2ExtensionCache.clear();
        operation2ExtensionCache.clear();
        capability2ExtensionCache.clear();
    }
}
