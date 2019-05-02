/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TEMP CLASS TO become seam to Thresholding Service
 * 
 * @author smith
 */
public class PollerThresholds {

    private static final Logger LOG = LoggerFactory.getLogger(PollerThresholds.class);

    private static LatencyThresholdingSetImpl m_thresholdingSet;

    private static ResourceStorageDao m_resourceStorageDao; // TODO where should this best come from?

    public void applyThresholds(String rrdPath, MonitoredService service, String dsName, Map<String, Number> entries) {
        try {
            if (m_thresholdingSet == null) {
                RrdRepository repository = new RrdRepository();
                repository.setRrdBaseDir(new File(rrdPath));
                m_thresholdingSet = new LatencyThresholdingSetImpl(service.getNodeId(), 
                                                               service.getIpAddr(), 
                                                               service.getSvcName(), 
                                                               service.getNodeLocation(), 
                                                               repository,
                                                               m_resourceStorageDao);
            }
            LinkedHashMap<String, Double> attributes = new LinkedHashMap<String, Double>();
            for (String ds : entries.keySet()) {
                Number sampleValue = entries.get(ds);
                if (sampleValue == null) {
                    attributes.put(ds, Double.NaN);
                } else {
                    attributes.put(ds, sampleValue.doubleValue());
                }
            }
            if (m_thresholdingSet.isNodeInOutage()) {
                LOG.info("applyThresholds: the threshold processing will be skipped because the service {} is on a scheduled outage.", service);
            } else if (m_thresholdingSet.hasThresholds(attributes)) {
                List<Event> events = m_thresholdingSet.applyThresholds(dsName, attributes);
                if (events.size() > 0) {
                    ThresholdingEventProxy proxy = new ThresholdingEventProxy();
                    proxy.add(events);
                    proxy.sendAllEvents();
                }
            }
        } catch (Throwable e) {
            LOG.error("Failed to threshold on {} for {} because of an exception", service, dsName, e);
        }
    }
}
