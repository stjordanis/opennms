package org.opennms.web.controller.ksc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class CustomGraphChooseParentResourceController extends AbstractController implements InitializingBean {
    
    private ResourceService m_resourceService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        /*
        ModelAndView modelAndView = new ModelAndView("KSC/customGraphChooseParentResource");

        modelAndView.addObject("nodeResources", getResourceService().findNodeResources());
        modelAndView.addObject("domainResources", getResourceService().findDomainResources());
        
        return modelAndView;
        */
        
        ModelAndView modelAndView = new ModelAndView("KSC/customGraphChooseResource");

        String selectedResourceId = request.getParameter("selectedResourceId");
        if (selectedResourceId != null) {
            try {
                OnmsResource selectedResource = m_resourceService.getResourceById(selectedResourceId);

                Map<String, OnmsResource> selectedResourceAndParents = new HashMap<String, OnmsResource>();
                OnmsResource r = selectedResource;
                while (r != null) {
                    selectedResourceAndParents.put(r.getId(), r);
                    r = r.getParent();
                }
                
                modelAndView.addObject("selectedResourceAndParents", selectedResourceAndParents);
            } catch (DataAccessException e) {
                // Don't do anything
            }
        }

        
        /*
        OnmsResource resource = getResourceService().getResourceById(resourceId);
        modelAndView.addObject("parentResource", resource);
        
        modelAndView.addObject("parentResourcePrefabGraphs", m_resourceService.findPrefabGraphsForResource(resource));
        */

        //List<OnmsResource> childResources = getResourceService().findChildResources(resource);
        List<OnmsResource> nodeResources = getResourceService().findNodeResources();
        List<OnmsResource> domainResources = getResourceService().findDomainResources();
        
        List<OnmsResource> childResources = new ArrayList<OnmsResource>(nodeResources.size() + domainResources.size());
        childResources.addAll(nodeResources);
        childResources.addAll(domainResources);

        modelAndView.addObject("resources", childResources);
        
        return modelAndView;
    }

    public ResourceService getResourceService() {
        return m_resourceService;
    }

    public void setResourceService(ResourceService resourceService) {
        m_resourceService = resourceService;
    }

    public void afterPropertiesSet() throws Exception {
        if (m_resourceService == null) {
            throw new IllegalStateException("property resourceService must be set");
        }
    }

}
