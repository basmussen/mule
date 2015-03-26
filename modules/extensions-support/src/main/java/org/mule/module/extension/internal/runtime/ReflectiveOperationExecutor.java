/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal.runtime;

import static java.lang.String.format;
import static org.mule.config.i18n.MessageFactory.createStaticMessage;
import static org.mule.util.Preconditions.checkArgument;
import static org.reflections.ReflectionUtils.getConstructors;
import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withParametersAssignableTo;
import static org.reflections.ReflectionUtils.withParametersCount;
import static org.springframework.util.ReflectionUtils.invokeMethod;
import org.mule.api.MuleException;
import org.mule.api.MuleRuntimeException;
import org.mule.extension.ExtensionManager;
import org.mule.extension.introspection.Operation;
import org.mule.extension.introspection.OperationContext;
import org.mule.extension.introspection.OperationExecutor;
import org.mule.extension.introspection.Parameter;
import org.mule.module.extension.internal.capability.metadata.ImplicitArgumentCapability;
import org.mule.module.extension.internal.runtime.resolver.ResolverSetResult;
import org.mule.module.extension.internal.util.ValueSetter;

import com.google.common.util.concurrent.Futures;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.commons.collections.CollectionUtils;

/**
 * Implementation of {@link OperationExecutor} which relies on a
 * {@link Class} and a reference to one of its methods. That {@link Class}
 * is expected to have a public constructor which takes a configuration instance
 * as its only argument, since per the rules of {@link ExtensionManager#getOperationExecutor(Operation, Object)}
 * only one executor is to be available per each {@link Operation}|configurationInstance pair
 *
 * @since 3.7.0
 */
final class ReflectiveOperationExecutor<C, E> implements DelegatingOperationExecutor<E>
{

    private final Method operationMethod;
    private final ReturnDelegate returnDelegate;
    private final E executorDelegate;

    ReflectiveOperationExecutor(Class<E> implementationClass, Method operationMethod, C configurationInstance, ReturnDelegate returnDelegate)
    {
        checkArgument(implementationClass != null, "implementation class cannot be null");
        checkArgument(configurationInstance != null, "Configuration instance cannot be null");
        this.operationMethod = operationMethod;
        this.returnDelegate = returnDelegate;
        this.executorDelegate = createExecutor(implementationClass, configurationInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Object> execute(OperationContext operationContext) throws Exception
    {
        Object result = invokeMethod(operationMethod, executorDelegate, getParameterValues(operationContext));
        return Futures.immediateFuture(returnDelegate.asReturnValue(result, operationContext));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getExecutorDelegate()
    {
        return executorDelegate;
    }

    private Object[] getParameterValues(OperationContext operationContext)
    {
        //TODO: move logic of setInstanceLevenParameterGroups here
        Map<Parameter, Object> parameters = operationContext.getParametersValues();
        List<Object> values = new ArrayList<>(parameters.size());
        for (Map.Entry<Parameter, Object> parameter : parameters.entrySet())
        {
            if (!parameter.getKey().isCapableOf(ImplicitArgumentCapability.class))
            {
                values.add(parameter.getValue());
            }
        }

        return values.toArray();
    }

    private E createExecutor(Class<E> executorClass, C configurationInstance)
    {
        Set<Constructor> suitableConstructors = getConstructors(executorClass,
                                                                withModifier(Modifier.PUBLIC),
                                                                withParametersCount(1),
                                                                withParametersAssignableTo(configurationInstance.getClass()));
        if (suitableConstructors.isEmpty())
        {
            throw new IllegalArgumentException(format("Class %s was expected to have one public constructor with one argument of type %s but it was not found. Add such constructor in order" +
                                                      "to execute those operations with a configuration of that type", executorClass.getName(), configurationInstance.getClass().getName()));
        }

        E executor;
        try
        {
            executor = (E) suitableConstructors.iterator().next().newInstance(configurationInstance);
        }
        catch (Exception e)
        {
            throw new MuleRuntimeException(createStaticMessage(format("Could not create instance of class %s using configuration of type %s",
                                                                      executorClass.getName(), configurationInstance.getClass().getName())),
                                           e);
        }

        return executor;
    }

    private void setInstanceLevelParameterGroups(Object instance, OperationContext context) throws MuleException
    {
        List<ValueSetter> groupSetters = ((DefaultOperationContext) context).getGroupValueSetters();
        if (!CollectionUtils.isEmpty(groupSetters))
        {
            ResolverSetResult resolverSetResult = ((DefaultOperationContext) context).getParameters();
            for (ValueSetter setter : groupSetters)
            {
                setter.set(instance, resolverSetResult);
            }
        }
    }
}
