/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
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

package org.bonitasoft.studio.simulation.expression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ExpressionHelper;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.expression.core.provider.IExpressionEditor;
import org.bonitasoft.studio.expression.core.provider.IExpressionProvider;
import org.bonitasoft.studio.expression.core.scope.ContextFinder;
import org.bonitasoft.studio.expression.core.scope.ModelLocation;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.simulation.SimulationData;
import org.bonitasoft.studio.model.simulation.SimulationDataContainer;
import org.bonitasoft.studio.model.simulation.SimulationTransition;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.pics.PicsConstants;
import org.bonitasoft.studio.simulation.SimulationDataUtil;
import org.bonitasoft.studio.simulation.i18n.Messages;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Romain Bioteau
 *
 */
public class SimulationDataExpressionProvider implements IExpressionProvider {

    private final ComposedAdapterFactory adapterFactory;
    private final AdapterFactoryLabelProvider adapterLabelProvider;


    public SimulationDataExpressionProvider(){
        adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
        adapterLabelProvider  = new AdapterFactoryLabelProvider(adapterFactory) ;
    }

    @Override
    public Set<Expression> getExpressions(EObject context) {
        final Set<Expression> result = new HashSet<Expression>() ;
        final List<SimulationData> simData = ModelHelper.getAccessibleSimulationData(context) ;
        for(final SimulationData d : simData){
            result.add(createExpression(d)) ;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.studio.expression.core.provider.IExpressionProvider#getExpressions(org.bonitasoft.studio.expression.core.scope.ModelLocation)
     */
    @Override
    public Set<Expression> getExpressions(ModelLocation location) {
        final Set<Expression> result = new HashSet<Expression>();
        final ContextFinder contextFinder = new ContextFinder(location);
        final SimulationDataContainer container = contextFinder.find(SimulationDataContainer.class);
        if(container != null){
            for (final SimulationData d : ModelHelper.getAccessibleSimulationData(container)) {
                result.add(createExpression(d));
            }
        }
        final SimulationTransition transition = contextFinder.find(SimulationTransition.class);
        if(transition != null){
            for (final SimulationData d : ModelHelper.getAccessibleSimulationData(transition)) {
                result.add(createExpression(d));
            }
        }
        return result;
    }

    @Override
    public String getExpressionType() {
        return ExpressionConstants.SIMULATION_VARIABLE_TYPE;
    }

    @Override
    public Image getIcon(Expression expression) {
        if(expression.getReferencedElements().isEmpty()){
            return null ;
        }

        final EObject reference = expression.getReferencedElements().get(0);
        return adapterLabelProvider.getImage(reference) ;
    }

    @Override
    public String getProposalLabel(Expression expression) {
        return expression.getName();
    }



    private Expression createExpression(SimulationData d) {
        final Expression exp = ExpressionFactory.eINSTANCE.createExpression() ;
        exp.setType(getExpressionType()) ;
        exp.setContent(d.getName()) ;
        exp.setName(d.getName()) ;
        exp.setReturnType(SimulationDataUtil.getTechnicalTypeFor(d)) ;
        exp.getReferencedElements().add(ExpressionHelper.createDependencyFromEObject(d)) ;
        return exp;
    }

    @Override
    public boolean isRelevantFor(EObject context) {
        return (context instanceof SimulationDataContainer || context instanceof SimulationTransition)
                && !ModelHelper.getAccessibleSimulationData(context).isEmpty();
    }

    @Override
    public Image getTypeIcon() {
        return Pics.getImage(PicsConstants.data);
    }

    @Override
    public String getTypeLabel() {
        return Messages.simulationVariableType;
    }

    @Override
    public IExpressionEditor getExpressionEditor(Expression expression,EObject context) {
        return new SimulationDataExpressionEditor();
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.studio.expression.core.provider.IExpressionProvider#isRelevantFor(org.bonitasoft.studio.expression.core.scope.ModelLocation)
     */
    @Override
    public boolean isRelevantFor(ModelLocation location) {
        final ContextFinder contextFinder = new ContextFinder(location);
        final SimulationDataContainer container = contextFinder.find(SimulationDataContainer.class);
        final SimulationTransition transition = contextFinder.find(SimulationTransition.class);
        return container != null && !ModelHelper.getAccessibleSimulationData(container).isEmpty() ||
                transition != null && !ModelHelper.getAccessibleSimulationData(transition).isEmpty();

    }


}
