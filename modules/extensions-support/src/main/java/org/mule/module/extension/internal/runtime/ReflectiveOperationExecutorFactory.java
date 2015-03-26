/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal.runtime;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.extension.introspection.OperationExecutor;
import org.mule.extension.introspection.declaration.OperationExecutorFactory;

import java.lang.reflect.Method;

/**
 * An implementation of {@link OperationExecutorFactory} which produces instances
 * of {@link ReflectiveOperationExecutor}.
 *
 * @param <T> the type of the class in which the implementing method is declared
 * @since 3.7.0
 */
public final class ReflectiveOperationExecutorFactory<T> implements OperationExecutorFactory
{

    private final Class<T> implementationClass;
    private final Method operationMethod;
    private final ReturnDelegate returnDelegate;

    public ReflectiveOperationExecutorFactory(Class<T> implementationClass, Method operationMethod)
    {
        checkArgument(implementationClass != null, "operation implementation class cannot be null");
        checkArgument(operationMethod != null, "operation method cannot be null");
        this.implementationClass = implementationClass;
        this.operationMethod = operationMethod;
        returnDelegate = isVoid() ? VoidReturnDelegate.INSTANCE : ValueReturnDelegate.INSTANCE;
    }

    @Override
    public <C> OperationExecutor createExecutor(C configurationInstance)
    {
        return new ReflectiveOperationExecutor<>(implementationClass, operationMethod, configurationInstance, returnDelegate);
    }

    private boolean isVoid()
    {
        Class<?> returnType = operationMethod.getReturnType();
        return returnType.equals(void.class) || returnType.equals(Void.class);
    }
}
