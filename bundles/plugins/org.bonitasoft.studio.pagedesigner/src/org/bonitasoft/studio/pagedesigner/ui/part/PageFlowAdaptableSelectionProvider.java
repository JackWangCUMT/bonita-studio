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
package org.bonitasoft.studio.pagedesigner.ui.part;

import org.bonitasoft.studio.model.process.Lane;
import org.bonitasoft.studio.model.process.MainProcess;
import org.bonitasoft.studio.model.process.PageFlow;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @author Romain Bioteau
 */
@Creatable
public class PageFlowAdaptableSelectionProvider extends EObjectAdaptableSelectionProvider {

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.studio.pagedesigner.ui.part.AdaptableSelectionProvider#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(final Class adapter) {
        if (EObject.class.equals(adapter)) {
            if (selection instanceof IStructuredSelection) {
                final Object selectedElement = ((IStructuredSelection) selection).getFirstElement();
                EObject semanticElement = null;
                if (selectedElement instanceof IAdaptable) {
                    semanticElement = (EObject) ((IAdaptable) selectedElement).getAdapter(EObject.class);
                } else if (selectedElement instanceof EObject) {
                    semanticElement = (EObject) selectedElement;
                }
                return asPageflow(semanticElement);
            }

        }
        return null;
    }

    private static PageFlow asPageflow(final EObject semanticElement) {
        if (semanticElement instanceof PageFlow && !(semanticElement instanceof MainProcess)) {
            return (PageFlow) semanticElement;
        }
        if (semanticElement instanceof Lane) {
            return (PageFlow) semanticElement.eContainer();
        }
        return null;
    }

}