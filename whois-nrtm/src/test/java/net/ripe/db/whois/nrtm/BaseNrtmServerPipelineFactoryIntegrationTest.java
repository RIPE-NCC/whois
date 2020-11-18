package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.nrtm.integration.AbstractNrtmIntegrationBase;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class BaseNrtmServerPipelineFactoryIntegrationTest extends AbstractNrtmIntegrationBase {
    @Autowired private BaseNrtmServerPipelineFactory nrtmServerPipelineFactory;
    @Autowired private List<ChannelHandler> channelHandlers;

    @Test
    public void testChannelHandlersAddedToPipeline() {
        final ChannelPipeline pipeline1 = nrtmServerPipelineFactory.getPipeline();
        final ChannelPipeline pipeline2 = nrtmServerPipelineFactory.getPipeline();

        final Set<ChannelHandler> toCheck = new HashSet<>(channelHandlers);
        final List<String> names = pipeline1.getNames();
        for (final String name : names) {
            final ChannelHandler channelHandler = pipeline1.get(name);

            final ChannelHandler.Sharable annotation = AnnotationUtils.findAnnotation(channelHandler.getClass(), ChannelHandler.Sharable.class);
            final boolean handlerIsShared = pipeline2.get(channelHandler.getClass()) == channelHandler;
            if (annotation == null) {
                assertFalse("Handler is not sharable, but reused: " + channelHandler, handlerIsShared);
            } else {
                assertTrue("Handler is sharable, but not reused: " + channelHandler, handlerIsShared);
            }

            if (channelHandler.getClass().getName().contains("ripe")) {
                ReflectionUtils.doWithFields(channelHandler.getClass(), field -> {
                    final int modifiers = field.getModifiers();

                    final String fieldName = field.getName();
                    final String className = channelHandler.getClass().getName();

                    if (fieldName.startsWith("$SWITCH_TABLE$")) {   // hidden enum switch helper field, generated by javac
                        return;
                    } else if (fieldName.startsWith("$jacoco")) { // added by sonar
                        return;
                    }  else if (fieldName.startsWith("ajc$")) { // aspectj generated
                        return;
                    }

                    if (handlerIsShared) {
                        /* Shared handlers can not have state */
                        if (!Modifier.isFinal(modifiers)) {
                            fail("Non final field '" + fieldName + "' in reused channel handler " + className);
                        }
                    } else {
                        /* non-shared handlers can still be executed in parallel. this is a primitive (and definitely not full) check
                         * for thread safety. It is meant as a basic safety net only. */
                        if (!Modifier.isVolatile(modifiers) && !Modifier.isFinal(modifiers) && (field.getType() != Annotation.class)) {
                            fail("Field '" + fieldName + "' in channel handler " + className + " must be volatile or final");
                        }
                    }
                });
            }

            toCheck.remove(channelHandler);
        }

        if (!toCheck.isEmpty()) {
            fail("Unused channel handlers: " + toCheck);
        }
    }
}

