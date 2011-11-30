/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.primefaces.forge;

import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.WebResourceFacet;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.spec.javaee.ServletFacet;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.ServletDef;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.primefaces.forge.data.PrimefacesVersion;

import javax.faces.webapp.FacesServlet;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author rvkuijk
 * @author bleathem
 */
@Alias("org.primefaces")
@RequiresFacet({DependencyFacet.class, ServletFacet.class, WebResourceFacet.class})
public class PrimefacesFacet extends BaseFacet {

    static final String SUCCESS_MSG_FMT = "***SUCCESS*** %s %s has been installed.";

    static final String ALREADY_INSTALLED_MSG_FMT = "***INFO*** %s %s is already present.";

    @Inject
    private ShellPrompt prompt;

    @Inject
    private ShellPrintWriter writer;

    private PrimefacesVersion version;

    @Override
    public boolean install() {
        writer.println();
        PrimefacesVersion version = prompt.promptChoiceTyped("Which version of Primefaces?",
                Arrays.asList(PrimefacesVersion.values()));
        installDependencies(version);
        installDescriptor(version);
        return true;
    }

    @Override
    public boolean isInstalled() {
        DependencyFacet deps = getProject().getFacet(DependencyFacet.class);
        if (getProject().hasAllFacets(Arrays.asList(DependencyFacet.class, WebResourceFacet.class,
                ServletFacet.class))) {
            for (PrimefacesVersion version : PrimefacesVersion.values()) {
                boolean hasVersionDependencies = true;
                for (Dependency dependency : version.getDependencies()) {
                    if (!hasDependency(deps, dependency)) {
                        hasVersionDependencies = false;
                        break;
                    }
                }
                if (hasVersionDependencies) {
                    this.version = version;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasDependency(DependencyFacet depFacet, Dependency dependency) {
        boolean result = false;
        for (Dependency projectDependency : depFacet.getDependencies()) {
            // FIXME
            boolean same = !(projectDependency.getArtifactId() != null ? !projectDependency.getArtifactId().equals
                    (dependency.getArtifactId()) : dependency.getArtifactId() != null) && !(projectDependency
                    .getGroupId() != null ? !projectDependency.getGroupId().equals(dependency.getGroupId()) :
                    dependency.getGroupId() != null);
            if (same && projectDependency.getVersion().charAt(0) == dependency.getVersion().charAt(0)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Set the context-params and Servlet definition if they are not yet set.
     *
     * @param version
     */
    private void installDescriptor(PrimefacesVersion version) {
        ServletFacet servlet = project.getFacet(ServletFacet.class);
        WebAppDescriptor descriptor = servlet.getConfig();
        if (descriptor.getContextParam("javax.faces.SKIP_COMMENTS") == null) {
            descriptor.contextParam("javax.faces.SKIP_COMMENTS", "true");
        }

        if (!isFacesServletDefined(descriptor) & version.getVersion() != 3) {
            descriptor.facesServlet();
        }

        descriptor.sessionTimeout(30);
        descriptor.welcomeFile("faces/index.xhtml");
        servlet.saveConfig(descriptor);
    }

    /**
     * A helper method to determine if the Faces Servlet is defined in the web.xml
     *
     * @param descriptor
     * @return true if the Faces Servlet is defined, false otherwise
     */
    private boolean isFacesServletDefined(WebAppDescriptor descriptor) {

        // TODO: When WebAppDescriptor.getServlets is implemented:
        List<ServletDef> servlets = descriptor.getServlets();
        if (servlets != null && !servlets.isEmpty()) {
            for (ServletDef servlet : servlets) {
                writer.println(ShellColor.MAGENTA, servlet.getName());
                if (servlet.getName().equals("Faces Servlet")) {
                    writer.println(ShellColor.YELLOW, String.format(ALREADY_INSTALLED_MSG_FMT, "Faces Servlet",
                            "mapping"));
                    return true;
                }
            }
        } else {
            writer.println("servlets list is empty");
        }
        return descriptor.exportAsString().contains(FacesServlet.class.getName());
    }

    /**
     * Install the repository and maven dependencies required for Primefaces
     */
    private void installDependencies(PrimefacesVersion version) {
        // installDependencyManagement(version);

        DependencyFacet deps = project.getFacet(DependencyFacet.class);
        for (Dependency dependency : version.getDependencies()) {
            deps.addDependency(dependency);
        }

        deps.addRepository("Prime Technology Maven Repository", "http://repository.prime.com.tr");

        // TODO: When forge has classifier support (<classifier>jdk15</classifier>)
        // dependency = DependencyBuilder.create();
        // dependency.setArtifactId("testng").setGroupId("org.testng").setVersion("5.1.0").setScopeType(ScopeType
        // .TEST);
        // installDependency(deps, dependency);

    }

    public PrimefacesVersion getVersion() {
        return version;
    }
}
