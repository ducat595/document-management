/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.dm;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.dm.thumbnails.DocumentThumbnailService;
import org.jahia.dm.thumbnails.VideoThumbnailService;
import org.jahia.dm.viewer.DocumentViewerService;
import org.jahia.services.transform.DocumentConverterService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi component for obtaining various document management services.
 * 
 * @author Sergiy Shyrkov
 */
public class DocumentManagement implements BundleContextAware {

    private static DocumentManagement instance;

    private static final Logger logger = LoggerFactory.getLogger(DocumentManagement.class);

    public static DocumentManagement getInstance() {
        if (instance == null) {
            instance = new DocumentManagement();
        }

        return instance;
    }

    private BundleContext bundleContext;

    private DocumentConverterService documentConverterService;

    private DocumentThumbnailService documentThumbnailService;

    private DocumentViewerService documentViewerService;

    private VideoThumbnailService videoThumbnailService;

    public void bindDocumentThumbnailService(ServiceReference ref) {
        documentThumbnailService = (DocumentThumbnailService) bundleContext.getService(bundleContext
                .getServiceReference(DocumentThumbnailService.class.getName()));
        logger.info("Bound instance of {} service ({})", DocumentThumbnailService.class.getName(),
                documentThumbnailService.isEnabled() ? "enabled" : "disabled");
    }

    public void bindDocumentViewerService(ServiceReference ref) {
        documentViewerService = (DocumentViewerService) bundleContext.getService(bundleContext
                .getServiceReference(DocumentViewerService.class.getName()));
        logger.info("Bound instance of {} service ({})", DocumentViewerService.class.getName(),
                documentViewerService.isEnabled() ? "enabled" : "disabled");
    }

    public void bindVideoThumbnailService(ServiceReference ref) {
        videoThumbnailService = (VideoThumbnailService) bundleContext.getService(bundleContext
                .getServiceReference(VideoThumbnailService.class.getName()));
        logger.info("Bound instance of {} service ({})", VideoThumbnailService.class.getName(),
                videoThumbnailService.isEnabled() ? "enabled" : "disabled");
    }

    public DocumentConverterService getDocumentConverterService() {
        return documentConverterService;
    }

    public DocumentThumbnailService getDocumentThumbnailService() {
        return documentThumbnailService;
    }

    public DocumentViewerService getDocumentViewerService() {
        return documentViewerService;
    }

    public VideoThumbnailService getVideoThumbnailService() {
        return videoThumbnailService;
    }

    public boolean isDocumentConverterServiceEnabled() {
        return documentConverterService != null && documentConverterService.isEnabled();
    }

    public boolean isDocumentThumbnailServiceEnabled() {
        return documentThumbnailService != null && documentThumbnailService.isEnabled();
    }

    public boolean isDocumentViewerServiceEnabled() {
        return documentViewerService != null && documentViewerService.isEnabled();
    }

    public boolean isVideoThumbnailServiceEnabled() {
        return videoThumbnailService != null && videoThumbnailService.isEnabled();
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setDocumentConverterService(DocumentConverterService service) {
        documentConverterService = service;
    }

    public void unbindDocumentThumbnailService(ServiceReference ref) {
        documentThumbnailService = null;
        logger.info("Instance of the service {} is now unbound", DocumentThumbnailService.class.getName());
    }

    public void unbindDocumentViewerService(ServiceReference ref) {
        documentViewerService = null;
        logger.info("Instance of the service {} is now unbound", DocumentViewerService.class.getName());
    }

    public void unbindVideoThumbnailService(ServiceReference ref) {
        videoThumbnailService = null;
        logger.info("Instance of the service {} is now unbound", VideoThumbnailService.class.getName());
    }
}
