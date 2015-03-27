/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal.runtime;

import static org.springframework.util.ReflectionUtils.invokeMethod;
import org.mule.api.MuleException;
import org.mule.extension.runtime.OperationContext;
import org.mule.extension.runtime.OperationExecutor;
import org.mule.extension.introspection.Parameter;
import org.mule.module.extension.internal.capability.metadata.ImplicitArgumentCapability;
import org.mule.module.extension.internal.runtime.resolver.ResolverSetResult;
import org.mule.module.extension.internal.util.ValueSetter;

import com.google.common.util.concurrent.Futures;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.collections.CollectionUtils;

/**
 * Implementation of {@link OperationExecutor} which relies on a
 * {@link #executorDelegate} and a reference to one of its methods.
 *
 * @since 3.7.0
 */
final class ReflectiveMethodOperationExecutor<D> implements DelegatingOperationExecutor<D>
{

    private final Method operationMethod;
    private final D executorDelegate;
    private final ReturnDelegate returnDelegate;

    ReflectiveMethodOperationExecutor(Method operationMethod, D executorDelegate, ReturnDelegate returnDelegate)
    {
        this.operationMethod = operationMethod;
        this.executorDelegate = executorDelegate;
        this.returnDelegate = returnDelegate;
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
    public D getExecutorDelegate()
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
