//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 09: Organize imports. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.monitoringLocations.LocationDef;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class
})
public class MonitoringLocationsFactoryTest {

    private MonitoringLocationsFactory m_locationFactory;

    private PollerConfigManager m_pollerConfigManager;

    @Before
    public void setUp() throws Exception {
        
        MockNetwork network = new MockNetwork();

        MockDatabase db = new MockDatabase();
        db.populate(network);

        DataSourceFactory.setInstance(db);

        InputStream stream = getClass().getResourceAsStream("/org/opennms/netmgt/config/monitoring-locations.testdata.xml");
        m_locationFactory = new MonitoringLocationsFactory(stream);
        stream.close();
        
        SnmpPeerFactory.init();
        
        stream = getClass().getResourceAsStream("/org/opennms/netmgt/config/poller-configuration.testdata.xml");
        m_pollerConfigManager = new TestPollerConfigManager(stream, "localhost", false);
        stream.close();
        
    }
    
    @Test
    public void testGetName() throws MarshalException, ValidationException,
            IOException {
        final String locationName = "RDU";
        LocationDef def = m_locationFactory.getDef(locationName);
        assertNotNull(def);
        assertEquals(locationName, def.getLocationName());
        assertEquals("raleigh", def.getMonitoringArea());

        assertNotNull(m_pollerConfigManager.getPackage(def.getPollingPackageName()));

    }

    static class TestPollerConfigManager extends PollerConfigManager {
        String m_xml;

        @Deprecated
        public TestPollerConfigManager(Reader rdr, String localServer, boolean verifyServer) throws MarshalException, ValidationException, IOException {
            super(rdr, localServer, verifyServer);
            save();
        }

        public TestPollerConfigManager(InputStream stream, String localServer, boolean verifyServer) throws MarshalException, ValidationException {
            super(stream, localServer, verifyServer);
        }

        public void update() throws IOException, MarshalException, ValidationException {
            m_config = CastorUtils.unmarshal(PollerConfiguration.class, new ByteArrayInputStream(m_xml.getBytes("UTF-8")));
            setUpInternalData();
        }

        protected void saveXml(String xml) throws IOException {
            m_xml = xml;
        }

        public String getXml() {
            return m_xml;
        }

    }
}
