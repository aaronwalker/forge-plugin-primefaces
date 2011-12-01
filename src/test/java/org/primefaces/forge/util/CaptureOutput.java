package org.primefaces.forge.util;

import org.jboss.forge.shell.Shell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Rudy De Busscher
 */
public class CaptureOutput {

    private ByteArrayOutputStream out;

    public CaptureOutput(final Shell someShell) throws IOException {
        out = new ByteArrayOutputStream();
        someShell.setOutputStream(out);
    }

    public String getOutputContents() {
        String result = String.valueOf(out.toString());
        System.out.println(result); // So that the console has the same output then under normal usages.
        return result;
    }
}
