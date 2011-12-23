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

import org.jboss.forge.project.Facet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.WebResourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.*;
import org.jboss.forge.spec.javaee.CDIFacet;
import org.jboss.forge.spec.javaee.ServletFacet;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.impl.spec.servlet.web.WebAppDescriptorImpl;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;
import org.primefaces.forge.data.PrimefacesThemes;
import org.primefaces.forge.data.TextResources;
import org.primefaces.forge.template.TemplateEvaluator;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.List;
import java.util.Locale;


/**
 * @author
 * @author Rudy De Busscher - www.c4j.be
 */
@Alias("primefaces")
@RequiresProject
public class PrimefacesPlugin implements Plugin {

    private final Project project;

    private final Event<InstallFacets> installFacets;

    private static final String PRIMEFACES_THEME = "primefaces.THEME";

    @Inject
    private ShellPrompt prompt;

    @Inject
    public PrimefacesPlugin(final Project project, final Event<InstallFacets> event) {
        this.project = project;
        this.installFacets = event;
    }

    @DefaultCommand
    public void status(final PipeOut out) {
        PrimefacesFacet primefacesFacet = tryToGetFacet(PrimefacesFacet.class);
        if (primefacesFacet != null) {
            out.println("Primefaces " + primefacesFacet.getVersion().getVersion() + " is installed.");
        } else {
            out.println(TextResources.FACET_NOT_INSTALLED);
        }
    }

    private <T extends Facet> T tryToGetFacet(Class<T> someFacet) {
        T result = null;
        if (project.hasFacet(someFacet)) {
            result = project.getFacet(someFacet);
        }
        return result;
    }

    // confirmed working
    @Command("setup")
    public void setup(final PipeOut out) {
        if (!project.hasFacet(PrimefacesFacet.class)) {
            installFacets.fire(new InstallFacets(PrimefacesFacet.class));
        }
        // After the setup is performed, we get an error with null ??
        // FIXME
        if (project.hasFacet(PrimefacesFacet.class)) {
            ShellMessages.success(out, "PrimefacesFacet is configured.");
        }
    }

    private void assertInstalled() {
        if (!project.hasFacet(PrimefacesFacet.class)) {
            throw new RuntimeException("PrimefacesFacet is not installed. Use 'primefaces setup' to get started.");
        }
    }

    @Command("help")
    public void exampleDefaultCommand(@Option final String opt, final PipeOut pipeOut) {
        pipeOut.println(ShellColor.BLUE, "Use the install commands to install:");
        pipeOut.println(ShellColor.BLUE, "  install-example-facelet: a sample Primefaces enabled facelet file");
    }

    @Command("install-example-facelet")
    public void installExampleFacelets(final PipeOut pipeOut) {
        assertInstalled();
        TemplateEvaluator evaluator = TemplateEvaluator.getInstance();
        addTemplateVariables(evaluator);
        createFaceletFiles(evaluator, pipeOut);
        createPrimeBean(evaluator, pipeOut);
        changeWelcomeFile();
    }

    private void addTemplateVariables(TemplateEvaluator someEvaluator) {
        PrimefacesFacet primeFacet = project.getFacet(PrimefacesFacet.class);

        if (primeFacet.getVersion().getVersion() == 2) {
            someEvaluator.addParameters("primefacesTargetName", "http://primefaces.prime.com.tr/ui");
        } else {
            someEvaluator.addParameters("primefacesTargetName", "http://primefaces.org/ui");
        }

        
        
        boolean hasCDI = project.hasFacet(CDIFacet.class);
        JavaSourceFacet source = project.getFacet(JavaSourceFacet.class);

        someEvaluator.addParameters("basePackage", source.getBasePackage());

        if (hasCDI) {
            someEvaluator.addParameters("managedBeanFullClassName", "javax.inject.Named");
            someEvaluator.addParameters("scopeClassName", "javax.enterprise.context.SessionScoped" );
            someEvaluator.addParameters("scopeAnnotation", "@SessionScoped");
            someEvaluator.addParameters("managedBeanAnnotation", "@Named");

        } else {
            someEvaluator.addParameters("managedBeanFullClassName", "javax.faces.bean.ManagedBean" );
            someEvaluator.addParameters("scopeClassName", "javax.faces.bean.ViewScoped");
            someEvaluator.addParameters("scopeAnnotation", "@ViewScoped");
            someEvaluator.addParameters("managedBeanAnnotation", "@ManagedBean(name=\"primeBean\")");

        }

    }

    @Command("set-theme")
    public void setTheme(@Option(description = "Sets the theme for primefaces", required = false) String theme, 
                         final PipeOut pipeOut) {
        assertInstalled();

        ServletFacet servlet = project.getFacet(ServletFacet.class);

        if (theme == null) {
            theme = prompt.promptChoiceTyped("Install which theme?", PrimefacesThemes.list);
        }

        if (!PrimefacesThemes.THEME_NONE.equals(theme)) {
            PrimefacesFacet primefacesFacet = project.getFacet(PrimefacesFacet.class);
            String version = getPrimefacesThemeVersion(primefacesFacet);
            Dependency primefacesTheme = DependencyBuilder.create().setGroupId(PrimefacesThemes
                    .PRIMEFACES_THEMES_GROUPID).setArtifactId(theme).setVersion(version);
            DependencyFacet df = project.getFacet(DependencyFacet.class);
            df.addDirectDependency(primefacesTheme);
        }

        WebAppDescriptorImpl webxml = (WebAppDescriptorImpl) servlet.getConfig();

        List<Node> nodes = webxml.getRootNode().get("context-param/param-name");

        boolean themeUpdated = false;
        for (Node node : nodes) {
            if (PRIMEFACES_THEME.equals(node.getText())) {
                node.getParent().getOrCreate("param-value").text(theme.toLowerCase(Locale.ENGLISH));
                themeUpdated = true;
                continue;
            }
        }
        if (!themeUpdated) {
            webxml.contextParam(PRIMEFACES_THEME, theme);
        }
        servlet.saveConfig(webxml);

    }

    private String getPrimefacesThemeVersion(PrimefacesFacet somePrimefacesFacet) {
        String version = "1.0.2"; // For the 3.0 versions
        if (somePrimefacesFacet.getVersion().getVersion() == 2) {
            version = "1.0.1";
        }
        return version;
    }

    @Command("list-theme")
    public void listThemes(final PipeOut pipeOut) {
        assertInstalled();

        String themeName = null;
        DependencyFacet df = project.getFacet(DependencyFacet.class);
        for (Dependency dependency : df.getDependencies()) {
            if (PrimefacesThemes.PRIMEFACES_THEMES_GROUPID.equals(dependency.getGroupId())) {
                themeName = dependency.getArtifactId();
                break;
            }
        }

        // No theme dependency.  If not set none in web.xml we are using aristo by default.
        if (themeName == null) {
            ServletFacet servlet = project.getFacet(ServletFacet.class);
            WebAppDescriptorImpl webxml = (WebAppDescriptorImpl) servlet.getConfig();

            List<Node> nodes = webxml.getRootNode().get("context-param/param-name");
            for (Node node : nodes) {
                if (PRIMEFACES_THEME.equals(node.getText())) {
                    themeName = node.getParent().getSingle("param-value").getText();
                    break;
                }
            }
            if (themeName == null) {
                themeName = "aristo"; //Default
            }
        }
        pipeOut.println(ShellColor.GREEN, "Current Primefaces theme is " + themeName);
    }

    @Command("delete-theme")
    public void deleteTheme(final PipeOut pipeOut) {
        assertInstalled();

        DependencyFacet df = project.getFacet(DependencyFacet.class);
        for (Dependency dependency : df.getDependencies()) {
            if (PrimefacesThemes.PRIMEFACES_THEMES_GROUPID.equals(dependency.getGroupId())) {
                df.removeDependency(dependency);
                break;
            }
        }

        ServletFacet servlet = project.getFacet(ServletFacet.class);
        WebAppDescriptorImpl webxml = (WebAppDescriptorImpl) servlet.getConfig();

        List<Node> nodes = webxml.getRootNode().get("context-param/param-name");
        for (Node node : nodes) {
            if (PRIMEFACES_THEME.equals(node.getText())) {
                webxml.getRootNode().removeChild(node.getParent());
                break;
            }
        }
        servlet.saveConfig(webxml);
        pipeOut.println(ShellColor.GREEN, "Primefaces theme is reset to aristo (default theme)");

    }

    /**
     * Create a simple template file, and a Primefaces enabled index file that uses the template
     *
     * @param someEvaluator
     * @param pipeOut
     */
    private void createFaceletFiles(final TemplateEvaluator someEvaluator, final PipeOut pipeOut) {
        DirectoryResource webRoot = project.getFacet(WebResourceFacet.class).getWebRootDirectory();
        DirectoryResource templateDirectory = webRoot.getOrCreateChildDirectory("templates");
        FileResource<?> templatePage = (FileResource<?>) templateDirectory.getChild("template.xhtml");

        templatePage.setContents(someEvaluator.evaluate("/org/primefaces/forge/template.vm"));
        pipeOut.println(ShellColor.YELLOW, String.format(PrimefacesFacet.SUCCESS_MSG_FMT, "template.xhtml", "file"));

        FileResource<?> indexPage = (FileResource<?>) webRoot.getChild("index.xhtml");
        indexPage.setContents(someEvaluator.evaluate("/org/primefaces/forge/index.vm"));
        pipeOut.println(ShellColor.YELLOW, String.format(PrimefacesFacet.SUCCESS_MSG_FMT, "index.xhtml", "file"));

        FileResource<?> forgeIndexPage = (FileResource<?>) webRoot.getChild("index.html");
        forgeIndexPage.delete();
    }

    /**
     * Create a simple JSF managed bean to back the Primefaces input in the example facelet file
     *
     * @param someEvaluator
     * @param pipeOut
     */
    private void createPrimeBean(TemplateEvaluator someEvaluator, final PipeOut pipeOut) {
        JavaSourceFacet source = project.getFacet(JavaSourceFacet.class);
        DirectoryResource sourceRoot = source.getBasePackageResource();
        FileResource<?> indexPage = (FileResource<?>) sourceRoot.getChild("PrimeBean.java");



        indexPage.setContents(someEvaluator.evaluate("/org/primefaces/forge/PrimeBean.vm"));
        pipeOut.println(ShellColor.YELLOW, String.format(PrimefacesFacet.SUCCESS_MSG_FMT, "PrimeBean", "class"));
    }


    private void changeWelcomeFile() {
        ServletFacet servlet = project.getFacet(ServletFacet.class);

        WebAppDescriptor config = servlet.getConfig();
        List<String> welcomeFiles = config.getWelcomeFiles();
        if (!welcomeFiles.contains("/index.jsf")) {
            welcomeFiles.add("/index.jsf");
        }
        config.welcomeFiles(welcomeFiles.toArray(new String[welcomeFiles.size()]));

        servlet.saveConfig(config);
    }
}