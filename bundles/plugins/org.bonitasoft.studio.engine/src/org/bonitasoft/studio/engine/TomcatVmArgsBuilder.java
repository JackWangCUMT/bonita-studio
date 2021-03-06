/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.engine;

import java.io.File;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.bonitasoft.studio.common.repository.core.BonitaHomeHandler;
import org.bonitasoft.studio.designer.core.WorkspaceResourceServerManager;
import org.bonitasoft.studio.designer.core.WorkspaceSystemProperties;
import org.bonitasoft.studio.engine.server.WatchdogManager;
import org.eclipse.core.runtime.Platform;

public class TomcatVmArgsBuilder {

    private static final String WATCHDOG_TIMER = "org.bonitasoft.studio.watchdog.timer";
    protected static final String WATCHDOG_PORT_PROPERTY = "org.bonitasoft.studio.watchdog.port";
    protected static final String BONITA_WEB_REGISTER = "bonita.web.register";


    private final RepositoryAccessor repositoryAccessor;

    public TomcatVmArgsBuilder(final RepositoryAccessor repositoryAccessor) {
        this.repositoryAccessor = repositoryAccessor;
    }

    public String getVMArgs(final String tomcatInstanceLocation) {
        final StringBuilder args = new StringBuilder();
        addMemoryOptions(args);
        final String tomcatExtraParams = System.getProperty("tomcat.extra.params");
        if (tomcatExtraParams != null) {
            args.append(" " + tomcatExtraParams);
        }
        addSystemProperty(args, "catalina.home", "\"" + tomcatInstanceLocation + "\"");
        addSystemProperty(args, "CATALINA_HOME", "\"" + tomcatInstanceLocation + "\"");
        addSystemProperty(args, "btm.root", "\"" + tomcatInstanceLocation + "\"");
        addSystemProperty(args, "wtp.deploy", "\"" + tomcatInstanceLocation + File.separatorChar + "webapps\"");
        addSystemProperty(args, "java.endorsed.dirs", "\"" + tomcatInstanceLocation + File.separatorChar + "endorsed\"");
        addSystemProperty(args, "bonita.home", "\"" + getBonitaHomeRoot() + "\"");
        addSystemProperty(args, "sysprop.bonita.db.vendor", "h2");
        addSystemProperty(args, "org.bonitasoft.platform.setup.folder", "\"" + tomcatInstanceLocation + File.separatorChar + "setup\"");
        addSystemProperty(args, "bitronix.tm.configuration",
                "\"" + tomcatInstanceLocation + File.separatorChar + "conf" + File.separatorChar + "bitronix-config.properties\"");
        addSystemProperty(args, "java.util.logging.manager", "org.apache.juli.ClassLoaderLogManager");
        if (tomcatExtraParams == null || !tomcatExtraParams.contains("-Djava.util.logging.config.file=")) {
            addSystemProperty(args, "java.util.logging.config.file",
                    "\"" + tomcatInstanceLocation + File.separatorChar + "conf" + File.separatorChar + "logging.properties\"");
        }
        addSystemProperty(args, "file.encoding", "UTF-8");
        addWatchDogProperties(args);
        addSystemProperty(args, "eclipse.product", getProductApplicationId());
        addSystemProperty(args, BONITA_WEB_REGISTER, System.getProperty(BONITA_WEB_REGISTER, "1"));
        addSystemProperty(args, BonitaHomeHandler.DB_LOCATION_PROPERTY,
                "\"" + getDBLocation().getAbsolutePath() + "\"");

        addUIDesignerOptions(args);
        final String res = args.toString();
        if (System.getProperty("log.tomcat.vm.args") != null) {
            BonitaStudioLog.info(res, EnginePlugin.PLUGIN_ID);
        }
        return res;
    }

    public File getDBLocation() {
        return repositoryAccessor.getCurrentRepository().getBonitaHomeHandler().getDBLocation();
    }

    /**
     * /!\ Public for test purpose only
     * 
     * @return
     */
    public String getBonitaHomeRoot() {
        return repositoryAccessor.getCurrentRepository().getBonitaHomeHandler().getRoot();
    }

    public String getProductApplicationId() {
        return Platform.getProduct() != null ? Platform.getProduct().getApplication() : null;
    }

    public void addWatchDogProperties(final StringBuilder args) {
        addSystemProperty(args, WATCHDOG_PORT_PROPERTY, String.valueOf(WatchdogManager.WATCHDOG_PORT));
        addSystemProperty(args, WATCHDOG_TIMER, System.getProperty(WATCHDOG_TIMER, "20000"));
    }

    public void addUIDesignerOptions(final StringBuilder args) {
        final WorkspaceSystemProperties workspaceSystemProperties = new WorkspaceSystemProperties(repositoryAccessor);
        addSystemProperty(args, workspaceSystemProperties.getPageRepositoryLocation());
        addSystemProperty(args, workspaceSystemProperties.getWidgetRepositoryLocation());
        addSystemProperty(args, workspaceSystemProperties.getFragmentRepositoryLocation());
        addSystemProperty(args, workspaceSystemProperties.getRestAPIURL(WorkspaceResourceServerManager.getInstance().runningPort()));
        addSystemProperty(args, workspaceSystemProperties.activateSpringProfile("studio"));
    }

    protected void addMemoryOptions(final StringBuilder args) {
        args.append("-Xms128m");
        args.append(" ");
        args.append("-Xmx768m");
        args.append(" ");
        args.append("-XX:MaxPermSize=256m");
    }

    protected void addSystemProperty(final StringBuilder sBuilder, final String key, final String value) {
        sBuilder.append(" ");
        sBuilder.append("-D" + key + "=" + value);
    }

    protected void addSystemProperty(final StringBuilder sBuilder, final String systemPropertyArgument) {
        sBuilder.append(" ");
        sBuilder.append(systemPropertyArgument);
    }

}
