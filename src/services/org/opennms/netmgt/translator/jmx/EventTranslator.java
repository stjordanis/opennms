//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.translator.jmx;

import java.io.IOException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.EventTranslatorConfigFactory;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;

public class EventTranslator implements EventTranslatorMBean {

    public final static String LOG4J_CATEGORY = "OpenNMS.EventTranslator";

    public void init() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);


        Category log = ThreadCategory.getInstance();
        try {
            DatabaseConnectionFactory.init();
            EventTranslatorConfigFactory.init();
        } catch (MarshalException e) {
            log.error("Could not unmarshall configuration", e);
        } catch (ValidationException e) {
            log.error("validation error ", e);
        } catch (IOException e) {
            log.error("IOException: ", e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to initialize database: "+e.getMessage(), e);
        }
        
        EventIpcManagerFactory.init();
        EventIpcManager mgr = EventIpcManagerFactory.getIpcManager();

        org.opennms.netmgt.translator.EventTranslator keeper = getEventTranslator();
        keeper.setConfig(EventTranslatorConfigFactory.getInstance());
        keeper.setEventManager(mgr);
        keeper.setDbConnectionFactory(DatabaseConnectionFactory.getInstance());
        keeper.init();
    }

    public void start() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        getEventTranslator().start();
    }

    public void stop() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        getEventTranslator().stop();
    }

    public int getStatus() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        return getEventTranslator().getStatus();
    }

    public String status() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    public String getStatusText() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    private org.opennms.netmgt.translator.EventTranslator getEventTranslator() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        return org.opennms.netmgt.translator.EventTranslator.getInstance();
    }


}
