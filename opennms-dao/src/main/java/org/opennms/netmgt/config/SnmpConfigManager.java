//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;

/**
 * This class handles merging a new Definition into the current running SNMP Configuration.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 */
public class SnmpConfigManager {

    private SnmpConfig m_config;
    private List<MergeableDefinition> m_definitions = new ArrayList<MergeableDefinition>();
    
    
    public SnmpConfigManager(SnmpConfig config) {
        m_config = config;
        for(Definition def : m_config.getDefinitionCollection()) {
            m_definitions.add(new MergeableDefinition(def));
        }
    }

    public SnmpConfig getConfig() {
        return m_config;
    }
    
    private List<MergeableDefinition> getDefinitions() {
//        List<MergeableDefinition> definitions = new ArrayList<MergeableDefinition>();
//        for(Definition def : m_config.getDefinitionCollection()) {
//            definitions.add(new MergeableDefinition(def));
//        }
//        return definitions;
        return m_definitions;
        
    }
    
    private void addDefinition(MergeableDefinition def) {
        m_definitions.add(def);
        getConfig().addDefinition(def.getConfigDef());
    }

    private void removeEmptyDefinitions() {
        for(Iterator<MergeableDefinition> iter = getDefinitions().iterator(); iter.hasNext();) {
            MergeableDefinition def = iter.next();
            if (def.isEmpty()) {
                getConfig().removeDefinition(def.getConfigDef());
                iter.remove();
            }
        }
    }


    
    /**
     * This is the exposed method for moving the data from a configureSNMP event
     * into the SnmpConfig from SnmpPeerFactory.
     * 
     * @param config
     */
    public void mergeIntoConfig(final Definition eventDef)  {

        MergeableDefinition eventDefinition = new MergeableDefinition(eventDef);

        // remove pass
        purgeRangesFromDefinitions(eventDefinition);
        
        if (eventDefinition.isTrivial()) return;

        // add pass
        MergeableDefinition matchingDef = findMatchingDefinition(eventDefinition);
        if (matchingDef == null) {
            addDefinition(eventDefinition);
        } else {
            matchingDef.mergeMatchingAttributeDef(eventDefinition);
        }

    }
    
    private void purgeRangesFromDefinitions(MergeableDefinition eventDefinition) {
        purgeOtherDefs(null, eventDefinition);
    }
    
    
    /**
     * This method is used to find a definition with then current config that matches the
     * attributes of a Definition.
     * 
     * @param eventDef
     * @return
     */
    MergeableDefinition findDefMatchingAttributes(final Definition eventDef) {
        
        MergeableDefinition newDef = new MergeableDefinition(eventDef);
        return findMatchingDefinition(newDef);
    }

    private MergeableDefinition findMatchingDefinition(MergeableDefinition def) {

        for(MergeableDefinition d : getDefinitions()) {
            if (d.matches(def)) {
                return d;
            }
        }
        return null;
    }

    /**
     * This method purges specifics and ranges from definitions that don't
     * match the attributes specified in the event (the updateDef)
     * 
     * @param updatedDef
     * @param eventDef
     */
    private void purgeOtherDefs(final MergeableDefinition updatedDef, MergeableDefinition eventDefinition) {
        
        for(MergeableDefinition def : getDefinitions()) {

            //don't mess with current updated def
            if (updatedDef != null && def.getConfigDef() == updatedDef.getConfigDef()) continue;

            def.removeRanges(eventDefinition);

        }
        
        removeEmptyDefinitions();
        
        
    }



    /**
     * Optimize all definitions in the current configuration.
     *
     */
    public void optimizeAllDefs() {
        // This needs to be called only by code holding the SnmpPeerFactory writeLock
        Definition[] defs = getConfig().getDefinition();
        for (int i = 0; i < defs.length; i++) {
            MergeableDefinition definition = new MergeableDefinition(defs[i]);
            if (definition.getConfigDef().getSpecificCount() > 0) {
                definition.optimizeSpecifics();
            }
            if (definition.getConfigDef().getRangeCount() > 1) {
                definition.optimizeRanges();
            }
        }
        getConfig().setDefinition(defs);
    }
}
