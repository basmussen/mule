/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.NestedProcessor;
import org.mule.api.transport.PropertyScope;
import org.mule.extension.ExtensionManager;
import org.mule.extension.annotations.ImplementationOf;
import org.mule.extension.annotations.Operation;
import org.mule.extension.annotations.RestrictedTo;
import org.mule.extension.annotations.param.Optional;
import org.mule.extension.annotations.param.Payload;
import org.mule.util.ValueHolder;

import java.util.List;

import javax.inject.Inject;

@ImplementationOf(HeisenbergExtension.class)
public class HeisenbergOperations
{

    private static final String SECRET_PACKAGE = "secretPackage";
    private static final String METH = "meth";

    public static ValueHolder<MuleEvent> eventHolder = new ValueHolder<>();
    public static ValueHolder<MuleMessage> messageHolder = new ValueHolder<>();

    private final HeisenbergExtension config;

    public HeisenbergOperations(HeisenbergExtension config)
    {
        this.config = config;
        // remove when injector is in place
        event = eventHolder.get();
        message = messageHolder.get();
    }

    @Inject
    private ExtensionManager extensionManager;

    private MuleEvent event;

    private MuleMessage message;

    @Operation
    public String sayMyName()
    {
        return config.getPersonalInfo().getMyName();
    }

    @Operation
    public void die()
    {
        config.setFinalHealth(HealthStatus.DEAD);
    }

    @Operation
    public String getEnemy(int index)
    {
        return config.getEnemies().get(index);
    }

    @Operation
    public String kill(@Payload String victim, String goodbyeMessage) throws Exception
    {
        return killWithCustomMessage(victim, goodbyeMessage);
    }

    @Operation
    public String killWithCustomMessage(@Optional(defaultValue = "#[payload]") String victim, String goodbyeMessage)
    {
        return String.format("%s, %s", goodbyeMessage, victim);
    }

    @Operation
    public String killMany(@RestrictedTo(HeisenbergExtension.class) List<NestedProcessor> killOperations, String reason) throws Exception
    {
        StringBuilder builder = new StringBuilder("Killed the following because " + reason + ":\n");
        for (NestedProcessor processor : killOperations)
        {
            builder.append(processor.process()).append("\n");
        }

        return builder.toString();
    }

    @Operation
    public String killOne(@RestrictedTo(HeisenbergExtension.class) NestedProcessor killOperation, String reason) throws Exception
    {
        StringBuilder builder = new StringBuilder("Killed the following because " + reason + ":\n");
        builder.append(killOperation.process()).append("\n");

        return builder.toString();
    }

    @Operation
    public ExtensionManager getInjectedExtensionManager()
    {
        return extensionManager;
    }

    @Operation
    public void hideMethInEvent()
    {
        event.setFlowVariable(SECRET_PACKAGE, METH);
    }

    @Operation
    public void hideMethInMessage()
    {
        message.setProperty(SECRET_PACKAGE, METH, PropertyScope.INVOCATION);
    }
}
