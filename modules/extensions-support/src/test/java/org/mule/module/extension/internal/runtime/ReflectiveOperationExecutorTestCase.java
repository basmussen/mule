/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.internal.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.module.extension.HealthStatus.DEAD;
import static org.mule.module.extension.HeisenbergExtension.HEISENBERG;
import org.mule.api.MuleEvent;
import org.mule.extension.introspection.OperationContext;
import org.mule.extension.introspection.Parameter;
import org.mule.module.extension.HeisenbergExtension;
import org.mule.module.extension.HeisenbergOperations;
import org.mule.module.extension.internal.runtime.resolver.ResolverSetResult;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ReflectiveOperationExecutorTestCase extends AbstractMuleTestCase
{

    @Mock(answer = RETURNS_DEEP_STUBS)
    private MuleEvent muleEvent;

    @Mock
    private ResolverSetResult parameters;

    private Map<Parameter, Object> parameterValues = new HashMap<>();

    private ReflectiveOperationExecutor implementation;
    private HeisenbergExtension config;
    private OperationContext operationContext;


    @Before
    public void before()
    {
        initHeisenberg();
        operationContext = new DefaultOperationContext(parameters, muleEvent, null);
        when(operationContext.getParametersValues()).thenReturn(parameterValues);
    }

    @Test
    public void operationWithReturnValueAndWithoutParameters() throws Exception
    {
        Method method = ClassUtils.getMethod(HeisenbergOperations.class, "sayMyName", new Class<?>[] {});
        implementation = new ReflectiveOperationExecutor(HeisenbergOperations.class, method, config, ValueReturnDelegate.INSTANCE);
        assertResult(implementation.execute(operationContext), HEISENBERG);
    }

    @Test
    public void voidOperationWithoutParameters() throws Exception
    {
        Method method = ClassUtils.getMethod(HeisenbergOperations.class, "die", new Class<?>[] {});
        implementation = new ReflectiveOperationExecutor(HeisenbergOperations.class, method, config, VoidReturnDelegate.INSTANCE);
        assertSameInstance(implementation.execute(operationContext), muleEvent);
        assertThat(config.getFinalHealth(), is(DEAD));
    }

    @Test
    public void withArgumentsAndReturnValue() throws Exception
    {
        Method method = ClassUtils.getMethod(HeisenbergOperations.class, "getEnemy", new Class<?>[] {int.class});
        implementation = new ReflectiveOperationExecutor(HeisenbergOperations.class, method, config, ValueReturnDelegate.INSTANCE);
        parameterValues.put(mock(Parameter.class), 0);
        assertResult(implementation.execute(operationContext), "Hank");
    }

    @Test
    public void voidWithArguments() throws Exception
    {
        HeisenbergOperations.eventHolder.set(muleEvent);
        Method method = ClassUtils.getMethod(HeisenbergOperations.class, "hideMethInEvent", new Class<?>[] {});
        implementation = new ReflectiveOperationExecutor(HeisenbergOperations.class, method, config, VoidReturnDelegate.INSTANCE);
        assertSameInstance(implementation.execute(operationContext), muleEvent);
        verify(muleEvent).setFlowVariable("secretPackage", "meth");
    }

    private void initHeisenberg()
    {
        config = new HeisenbergExtension();
        config.getPersonalInfo().setMyName(HEISENBERG);
        config.setEnemies(Arrays.asList("Hank"));
        HeisenbergOperations.eventHolder.set(muleEvent);
    }

    private void assertResult(Future<Object> result, Object expected) throws Exception
    {
        Object value = result.get();
        assertThat(value, is(expected));
    }

    private void assertSameInstance(Future<Object> result, Object expected) throws Exception
    {
        Object value = result.get();
        assertThat(value, is(sameInstance(expected)));
    }
}
