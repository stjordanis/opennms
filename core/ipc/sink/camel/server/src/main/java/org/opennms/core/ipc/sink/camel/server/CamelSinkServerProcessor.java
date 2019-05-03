/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.ipc.sink.camel.server;

import static org.opennms.core.ipc.sink.camel.CamelSinkConstants.JMS_SINK_TRACING_INFO;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.tracing.util.TracingInfoCarrier;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;

public class CamelSinkServerProcessor implements Processor {

    private final CamelMessageConsumerManager consumerManager;
    private final SinkModule<?, Message> module;
    private final Tracer tracer;

    public CamelSinkServerProcessor(CamelMessageConsumerManager consumerManager, SinkModule<?, Message> module, Tracer tracer) {
        this.consumerManager = Objects.requireNonNull(consumerManager);
        this.module = Objects.requireNonNull(module);
        this.tracer = Objects.requireNonNull(tracer);
    }

    @Override
    public void process(Exchange exchange) {
        final byte[] messageBytes = exchange.getIn().getBody(byte[].class);
        // build span from message headers and retrieve custom tags into tracing info.
        Map<String, String> tracingInfo = new HashMap<>();
        Tracer.SpanBuilder spanBuilder = buildSpanFromHeaders(exchange.getIn(), tracingInfo);
        Span span = spanBuilder.start();
        final Message message = module.unmarshal(messageBytes);
        consumerManager.dispatch(module, message);
        span.finish();
    }

    private Tracer.SpanBuilder buildSpanFromHeaders(org.apache.camel.Message message, Map<String, String> tracingInfo) {
        String tracingInfoObj = message.getHeader(JMS_SINK_TRACING_INFO, String.class);
        if (tracingInfoObj != null) {
            tracingInfo.putAll(TracingInfoCarrier.unmarshalTracinginfo(tracingInfoObj));
        }
        Tracer.SpanBuilder spanBuilder;
        SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(tracingInfo));
        if (context != null) {
            spanBuilder = tracer.buildSpan(module.getId()).asChildOf(context);
        } else {
            spanBuilder = tracer.buildSpan(module.getId());
        }
        return spanBuilder;
    }
}
