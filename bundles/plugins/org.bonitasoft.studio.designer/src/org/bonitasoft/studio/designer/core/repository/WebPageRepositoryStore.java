/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.studio.designer.core.repository;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.bonitasoft.studio.common.repository.store.AbstractFolderRepositoryStore;
import org.bonitasoft.studio.designer.UIDesignerPlugin;
import org.bonitasoft.studio.designer.core.bos.WebFormBOSArchiveFileStoreProvider;
import org.bonitasoft.studio.designer.i18n.Messages;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.swt.graphics.Image;

/**
 * @author Romain Bioteau
 */
public class WebPageRepositoryStore extends AbstractFolderRepositoryStore<WebPageFileStore> {

    private static final String PAGE_ICON_PATH = "page.png";
    private final static Set<String> extensions = new HashSet<String>();
    public static final String JSON_EXTENSION = "json";
    public static final String WEB_FORM_REPOSITORY_NAME = "web_page";

    @Inject
    private WebFormBOSArchiveFileStoreProvider filseStoreProvider;

    static {
        extensions.add(JSON_EXTENSION);
    }

    @Override
    public String getName() {
        return WEB_FORM_REPOSITORY_NAME;
    }

    @Override
    public String getDisplayName() {
        return Messages.formRepository;
    }

    @Override
    public Image getIcon() {
        return Pics.getImage(PAGE_ICON_PATH, UIDesignerPlugin.getDefault());
    }

    @Override
    public Set<String> getCompatibleExtensions() {
        return extensions;
    }

    @Override
    public WebPageFileStore createRepositoryFileStore(final String fileName) {
        final WebPageFileStore webPageFileStore = new WebPageFileStore(fileName, this);
        webPageFileStore.setWebFormBOSArchiveFileStoreProvider(filseStoreProvider);
        return webPageFileStore;
    }

}
