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

package org.opennms.netmgt.collectd;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;

/**
 * TEMP CLASS for API seam to Thresholding service.
 * This class will move to features/collection/thresholding/api
 */
public class ThresholdingFactory {

    public CollectionSetVisitor createThresholder() {

        return new ThresholdingVisitor();

    }

    private class ThresholdingVisitor implements CollectionSetVisitor {

        @Override
        public void visitCollectionSet(CollectionSet set) {
            // TODO Auto-generated method stub

        }

        @Override
        public void visitResource(CollectionResource resource) {
            // TODO Auto-generated method stub

        }

        @Override
        public void visitGroup(AttributeGroup group) {
            // TODO Auto-generated method stub

        }

        @Override
        public void visitAttribute(CollectionAttribute attribute) {
            // TODO Auto-generated method stub

        }

        @Override
        public void completeAttribute(CollectionAttribute attribute) {
            // TODO Auto-generated method stub

        }

        @Override
        public void completeGroup(AttributeGroup group) {
            // TODO Auto-generated method stub

        }

        @Override
        public void completeResource(CollectionResource resource) {
            // TODO Auto-generated method stub

        }

        @Override
        public void completeCollectionSet(CollectionSet set) {
            // TODO Auto-generated method stub

        }

    }

}
