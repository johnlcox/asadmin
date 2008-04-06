/**
 * asadmin-glassfish-plugin : a maven plugin for glassfish administratives tasks
 * 
 * Copyright (C) 2008  Paul Merlin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.n0pe.mojo.asadmin;


import java.io.File;
import java.util.Iterator;

import org.apache.commons.lang.SystemUtils;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import org.n0pe.asadmin.commands.AsAdmin;
import org.n0pe.asadmin.commands.AsAdminException;
import org.n0pe.asadmin.commands.AsCommandList;
import org.n0pe.asadmin.commands.IAsAdminConfigurationProvider;
import org.n0pe.asadmin.commands.IAsCommand;


/**
 * Base class of all asadmin mojos.
 * Provides common parameters and basic check configuration, implements asadmin configuration provider.
 * 
 * @author Paul Merlin <eskatos@n0pe.org>
 */
public abstract class AbstractAsadminMojo
        extends AbstractMojo
        implements IAsAdminConfigurationProvider {


    /**
     * @parameter default-value="ENV"
     * @required
     */
    private String glassfishHome;


    private File glassfishHomeDir;


    /**
     * @parameter default-value="admin"
     * @required
     */
    private String user;


    /**
     * @parameter default-value="HOME"
     * @required
     */
    private String passwordfile;


    /**
     * @parameter default-value="localhost"
     * @required
     */
    private String host;


    /**
     * @parameter default-value="4848"
     * @required
     */
    private String port;


    /**
     * @parameter default-value="domain1"
     * @required
     */
    protected String domain;


    /**
     * @parameter expression="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     * @required
     */
    protected String appArchive;


    /**
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    protected String appName;


    /**
     * Overrided context root for WAR archives, default to project.build.finalName.
     * 
     * @parameter expression="${project.build.finalName}"
     */
    protected String contextRoot;


    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject mavenProject;


    public final void execute()
            throws MojoExecutionException, MojoFailureException {
        displayCopyrightNotice();
        checkConfig();
        final AsAdmin asadmin = AsAdmin.getInstance(this);
        try {
            final Iterator it = getAsCommandList().iterator();
            IAsCommand cmd;
            while (it.hasNext()) {
                cmd = (IAsCommand) it.next();
                asadmin.run(cmd);
            }
        } catch (AsAdminException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }

    }


    public final String getGlassfishHome() {
        return glassfishHome;
    }


    public final String getUser() {
        return user;
    }


    public final String getPasswordFile() {
        return passwordfile;
    }


    protected abstract AsCommandList getAsCommandList()
            throws MojoExecutionException, MojoFailureException;


    private void checkConfig()
            throws MojoExecutionException,
                   MojoFailureException {
        if (glassfishHome == null || glassfishHome.equals("ENV")) {
            if (SystemUtils.JAVA_VERSION_FLOAT < 1.5) {
                throw new MojoExecutionException(
                        "Neither GLASSFISH_HOME, AS_HOME nor the glassfishHome configuration parameter is set! Also, to save you the trouble, environment cannot be read running maven with a VM < 1.5, so set the glassFishHome configuration parameter or use -D.");
            }
            glassfishHome = System.getenv("GLASSFISH_HOME");
            if (glassfishHome == null) {
                glassfishHome = System.getenv("AS_HOME");
            }
        }
        if (glassfishHome == null) {
            throw new MojoExecutionException(
                    "Neither GLASSFISH_HOME, AS_HOME nor the glassfishHome configuration parameter is set!");
        }
        glassfishHomeDir = new File(glassfishHome);
        if (!glassfishHomeDir.exists()) {
            throw new MojoFailureException(
                    "The specifed glassfishHome does not exist.");
        }
        if (passwordfile == null || passwordfile.equals("HOME")) {
            passwordfile = System.getenv("HOME") + File.separator +
                    ".asadminpass";
        }
    }


    private void displayCopyrightNotice() {
        getLog().info("asadmin-glassfish-plugin  Copyright (C) 2008 Paul Merlin");
        getLog().info("This program comes with ABSOLUTELY NO WARRANTY; for details type `mvn asadmin:help'.");
        getLog().info("This is free software, and you are welcome to redistribute it");
        getLog().info("under certain conditions; type `mvn asadmin:help' for details.");
    }


}
