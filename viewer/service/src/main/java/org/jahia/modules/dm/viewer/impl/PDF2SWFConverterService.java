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
package org.jahia.modules.dm.viewer.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.dm.DocumentOperationException;
import org.jahia.dm.utils.ProcessUtils;
import org.jahia.utils.StringOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Document to SWF converter service that uses pdf2swf from SWFTools for file conversion.
 * 
 * @author Sergiy Shyrkov
 */
public class PDF2SWFConverterService implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(PDF2SWFConverterService.class);

    private boolean autodetect;

    private boolean enabled;

    private String executablePath = "pdf2swf";

    private String parameters = "${input} -o ${output} -f -T 9 -t -s storeallcharacters";

    private File workingDir;

    public void afterPropertiesSet() throws Exception {
        if (autodetect) {
            doAutodetect();
        }
    }

    public File convert(File inputPdfFile) throws DocumentOperationException {
        if (!isEnabled()) {
            logger.info("pdf2swf conversion service is not enabled." + " Skip converting file {}",
                    inputPdfFile);
            return null;
        }

        File out = null;
        try {
            out = createTempFile();
            convert(inputPdfFile, out);
        } catch (DocumentOperationException e) {
            FileUtils.deleteQuietly(out);
            throw e;
        } catch (IOException e) {
            FileUtils.deleteQuietly(out);
            logger.error("Unable to create a temp file for document conversion operation. Cause: "
                    + e.getMessage(), e);
        }

        return out;
    }

    public boolean convert(File inputPdfFile, File outputSwfFile) throws DocumentOperationException {
        if (!isEnabled()) {
            logger.info("pdf2swf conversion service is not enabled." + " Skip converting file {}",
                    inputPdfFile);

            return false;
        }

        long timer = System.currentTimeMillis();

        CommandLine cmd = getConvertCommandLine(inputPdfFile, outputSwfFile);

        if (logger.isDebugEnabled()) {
            logger.debug("Execuiting conversion command: {}", cmd.toString());
        }

        int exitValue = 0;

        StringOutputStream out = new StringOutputStream();
        StringOutputStream err = new StringOutputStream();
        try {
            DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(new PumpStreamHandler(out, err));
            if (workingDir != null) {
                if (workingDir.exists() || workingDir.mkdirs()) {
                    executor.setWorkingDirectory(workingDir);
                }
            }
            exitValue = executor.execute(cmd, System.getenv());
        } catch (Exception e) {
            throw new DocumentOperationException(e);
        } finally {
            if (err.getLength() > 0) {
                logger.error("Conversion process finished with error. Cause: {}", err.toString());
            }
            if (logger.isDebugEnabled() && out.getLength() > 0) {
                logger.debug(out.toString());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Conversion from {} to {} done (exit code: {}) in {} ms", new Object[] {
                    inputPdfFile, outputSwfFile, exitValue, (System.currentTimeMillis() - timer) });
        }

        return exitValue == 0;
    }

    public OutputStream convert(InputStream inputPdfStream) throws DocumentOperationException {
        if (!isEnabled()) {
            logger.info("pdf2swf conversion service is not enabled."
                    + " Skip converting file input stream.");

            return null;
        }

        File in = null;
        File out = null;
        try {
            in = createTempFile();
            FileUtils.copyInputStreamToFile(inputPdfStream, in);
            out = createTempFile();
            convert(in, out);
        } catch (Exception e) {
            FileUtils.deleteQuietly(out);
            throw new DocumentOperationException(e);
        } finally {
            FileUtils.deleteQuietly(in);
        }

        OutputStream os = null;

        if (out != null) {
            try {
                os = new BufferedOutputStream(new FileOutputStream(out));
            } catch (FileNotFoundException e) {
                throw new DocumentOperationException(e);
            }
        }

        return os;
    }

    public boolean convert(InputStream inputPdfStream, OutputStream outputSwfStream)
            throws DocumentOperationException {
        if (!isEnabled()) {
            logger.info("pdf2swf conversion service is not enabled."
                    + " Skip converting file input stream.");

            return false;
        }

        File in = null;
        File out = null;
        try {
            in = createTempFile();
            FileUtils.copyInputStreamToFile(inputPdfStream, in);
            out = createTempFile();
            convert(in, out);
        } catch (Exception e) {
            FileUtils.deleteQuietly(out);
            throw new DocumentOperationException(e);
        } finally {
            FileUtils.deleteQuietly(in);
        }

        if (out != null) {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(out));
                IOUtils.copy(is, outputSwfStream);
            } catch (IOException e) {
                throw new DocumentOperationException(e);
            } finally {
                IOUtils.closeQuietly(is);
                FileUtils.deleteQuietly(out);
            }
        }

        return true;
    }

    protected File createTempFile() throws IOException {
        return File.createTempFile("doc-viewer", null);
    }

    protected void doAutodetect() {
        logger.info("Checking if the {} is present in the current path", executablePath);

        enabled = ProcessUtils.commandPresent(executablePath, workingDir);

        if (enabled) {
            logger.info("Found {} in the current system path." + " Service will be enabled.",
                    executablePath);
        } else {
            logger.info("Command {} cannot be found in the current system path."
                    + " The service will be disabled.", executablePath);
        }
    }

    protected CommandLine getConvertCommandLine(File inputFile, File outputFile) {
        CommandLine cmd = new CommandLine(executablePath);
        cmd.addArguments(parameters);

        Map<String, File> params = new HashMap<String, File>(2);
        params.put("input", inputFile);
        params.put("output", outputFile);

        cmd.setSubstitutionMap(params);

        return cmd;
    }

    /**
     * Returns <code>true</code> if the conversion service is enabled; <code>false</code> otherwise.
     * 
     * @return <code>true</code> if the conversion service is enabled; <code>false</code> otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the conversion service
     * 
     * @param activate
     *            set to <code>true</code> to enable the service; to <code>false</code> to disable it and to <code>auto</code> to
     *            auto-detect if the executable is present in the path and than enable the service.
     */
    public void setActivate(String activate) {
        activate = activate != null ? activate.trim() : activate;
        this.enabled = Boolean.valueOf(activate);
        if (!this.enabled && activate != null) {
            this.autodetect = "auto".equalsIgnoreCase(activate)
                    || "autodetect".equalsIgnoreCase(activate);
        }
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }
}
