/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.common.repository.core;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Arrays;
import java.util.Set;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.CommonRepositoryPlugin;
import org.bonitasoft.studio.common.repository.Messages;
import org.bonitasoft.studio.common.repository.model.IRepository;
import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
import org.bonitasoft.studio.common.repository.store.SourceRepositoryStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathsBlock;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

/**
 * @author Romain Bioteau
 */
public class BonitaBPMProjectClasspath {

    private final IProject project;
    private final IRepository repository;

    public BonitaBPMProjectClasspath(final IProject project, final IRepository repository) {
        this.project = project;
        this.repository = repository;
    }

    public void create(final IProgressMonitor monitor) throws CoreException {
        if (!classpathExists()) {
            monitor.subTask(Messages.initializingProjectClasspath);
            final IJavaProject javaProject = asJavaProject();
            final Set<IClasspathEntry> entries = addClasspathEntries();
            BonitaStudioLog.debug("Updating build path...", CommonRepositoryPlugin.PLUGIN_ID);
            javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), true, monitor);
        }
    }

    public void refresh(final IProgressMonitor monitor) throws CoreException {
        javaModel().refreshExternalArchives(null, monitor);
        flushBuildPath(monitor);
    }

    protected void flushBuildPath(final IProgressMonitor monitor) throws CoreException, JavaModelException {
        final IJavaProject javaProject = asJavaProject();
        BuildPathsBlock.flush(Arrays.asList(CPListElement.createFromExisting(javaProject)), javaProject.getOutputLocation(), javaProject, null, monitor);
    }

    protected JavaModel javaModel() {
        return JavaModelManager.getJavaModelManager().getJavaModel();
    }

    @SuppressWarnings("rawtypes")
    protected Set<IClasspathEntry> addClasspathEntries() {
        final Set<IClasspathEntry> entries = newHashSet(
                newContainerEntry(new Path("repositoryDependencies"), true),
                newContainerEntry(newJREContainerPath(javaRuntimeEnvironment()), false),
                newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins"), false),
                newContainerEntry(new Path("GROOVY_SUPPORT"), true));
        // Add src folders in classpath
        for (final IRepositoryStore sourceRepositoryStore : filter(repository.getAllStores(), instanceOf(SourceRepositoryStore.class))) {
            entries.add(newSourceEntry(sourceRepositoryStore.getResource().getFullPath()));
        }
        return entries;
    }

    protected IPath newJREContainerPath(final IExecutionEnvironment executionEnvironment) {
        return JavaRuntime.newJREContainerPath(executionEnvironment);
    }

    protected IExecutionEnvironment javaRuntimeEnvironment() {
        return JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("JavaSE-1.7");
    }

    protected IClasspathEntry newContainerEntry(final IPath path, final boolean isExported) {
        return JavaCore.newContainerEntry(path, isExported);
    }

    protected IClasspathEntry newSourceEntry(final IPath path) {
        return JavaCore.newSourceEntry(path);
    }

    protected IJavaProject asJavaProject() throws CoreException {
        return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
    }

    public boolean classpathExists() {
        return project.findMember(".classpath") != null;
    }

    public void delete(final IProgressMonitor monitor) throws CoreException {
        final IFile classpathFile = project.getFile(".classpath");
        if (classpathFile.exists()) {
            classpathFile.delete(true, false, monitor);
        }
    }

    public IClasspathEntry[] getEntries() throws CoreException {
        final IJavaProject javaProject = asJavaProject();
        return javaProject.getRawClasspath();
    }

}
