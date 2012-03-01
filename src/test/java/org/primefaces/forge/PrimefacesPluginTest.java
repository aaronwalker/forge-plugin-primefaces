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

import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.packaging.PackagingType;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.primefaces.forge.data.TextResources;
import org.primefaces.forge.util.CaptureOutput;

/**
 * @author Rudy De Busscher
 */

@Ignore // There are persons that problems with running the test
public class PrimefacesPluginTest extends AbstractShellTest {

    private static final String NEW_LINE = "\r\n";

    @Deployment
    public static JavaArchive getDeployment() {

        JavaArchive archive = AbstractShellTest.getDeployment().addPackages(true,
                PrimefacesPlugin.class.getPackage());
        return archive;
    }

    @Test
    public void testCheckPrimefacesFacet() throws Exception {

        Project p = initializeProject(PackagingType.JAR);

        Assert.assertNotNull(p);
        Assert.assertFalse(p.hasFacet(PrimefacesFacet.class));
        CaptureOutput output = new CaptureOutput(getShell());

        getShell().execute("primefaces");
        Assert.assertEquals(TextResources.FACET_NOT_INSTALLED+ NEW_LINE, output.getOutputContents());
    }

    @Test
    public void testSetupPrimefacesFacet() throws Exception {

        Project p = initializeProject(PackagingType.JAR);

        queueInputLines("2");
        Assert.assertNotNull(p);
        Assert.assertFalse(p.hasFacet(PrimefacesFacet.class));
        CaptureOutput output = new CaptureOutput(getShell());

        getShell().execute("primefaces setup");
        String outputContents = output.getOutputContents();
        Assert.assertTrue(outputContents.contains("***SUCCESS*** Installed [org.primefaces] successfully."));
        Assert.assertTrue(outputContents.contains("***SUCCESS*** PrimefacesFacet is configured."));
        // FIXME We have to test if the correct dependencies are added.
    }


}
