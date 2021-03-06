/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
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
package org.bonitasoft.studio.refactoring.core;

import java.util.Collection;
import java.util.List;

import org.bonitasoft.studio.common.DataUtil;
import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ExpressionHelper;
import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionPackage;
import org.bonitasoft.studio.model.process.Data;
import org.bonitasoft.studio.model.process.DataAware;
import org.bonitasoft.studio.model.process.MultiInstantiable;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.DeleteCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;

/**
 * @author Romain Bioteau
 */
public class RefactorDataOperation extends AbstractRefactorOperation<Data, Data, DataRefactorPair> {

    private boolean updateDataReferences = false;

    private EStructuralFeature dataContainmentFeature;

    private DataAware dataContainer;

    public RefactorDataOperation(final RefactoringOperationType operationType) {
        super(operationType);
    }

    @Override
    protected CompoundCommand doBuildCompoundCommand(final CompoundCommand compoundCommand, final IProgressMonitor monitor) {
        Assert.isNotNull(dataContainer);
        final CompoundCommand deleteCommands = new CompoundCommand("Compound commands containing all delete operations to do at last step");
        for (final DataRefactorPair pairToRefactor : pairsToRefactor) {
            Assert.isNotNull(pairToRefactor.getOldValue());
            if (pairToRefactor.getNewValue() != null) {
                updateDataReferenceInVariableExpressions(compoundCommand);
                updateDataReferenceInExpressions(compoundCommand);
                if (updateDataReferences) {
                    updateDataReferenceInMultinstanciation(compoundCommand);
                    final List<?> dataList = (List<?>) dataContainer.eGet(dataContainmentFeature);
                    final int index = dataList.indexOf(pairToRefactor.getOldValue());
                    compoundCommand.append(RemoveCommand.create(getEditingDomain(), dataContainer, dataContainmentFeature, pairToRefactor.getOldValue()));
                    compoundCommand.append(AddCommand.create(getEditingDomain(), dataContainer, dataContainmentFeature, pairToRefactor.getNewValue(), index));
                } else {
                    for (final EStructuralFeature feature : pairToRefactor.getOldValue().eClass().getEAllStructuralFeatures()) {
                        if (pairToRefactor.getNewValue().eClass().getEAllStructuralFeatures().contains(feature)) {
                            compoundCommand.append(SetCommand.create(getEditingDomain(), pairToRefactor.getOldValue(), feature, pairToRefactor.getNewValue()
                                    .eGet(feature)));
                        }
                    }
                }
            } else {
                removeAllDataReferences(compoundCommand, pairToRefactor);
            }
            if (RefactoringOperationType.REMOVE.equals(operationType)) {
                deleteCommands.append(DeleteCommand.create(getEditingDomain(), pairToRefactor.getOldValue()));
            }
        }
        compoundCommand.appendIfCanExecute(deleteCommands);
        return compoundCommand;
    }

    private void updateDataReferenceInExpressions(final CompoundCommand finalCommand) {
        final List<Expression> expressions = ModelHelper.getAllItemsOfType(dataContainer, ExpressionPackage.Literals.EXPRESSION);
        for (final Expression exp : expressions) {
            if (!ExpressionConstants.SCRIPT_TYPE.equals(exp.getType())
                    && !ExpressionConstants.PATTERN_TYPE.equals(exp.getType())
                    && !ExpressionConstants.CONDITION_TYPE.equals(exp.getType())) {
                for (final EObject dependency : exp.getReferencedElements()) {
                    if (dependency instanceof Data) {
                        updateDataReferenceInExpressions(finalCommand, exp, dependency);
                    }
                }
            }
        }
    }

    private void updateDataReferenceInExpressions(final CompoundCommand finalCommand, final Expression exp, final EObject oldDependency) {
        for (final DataRefactorPair pairToRefactor : pairsToRefactor) {
            if (((Data) oldDependency).getName().equals(pairToRefactor.getOldValue().getName())
                    && !isReturnFixedOnExpressionWithUpdatedType(pairToRefactor, exp)) {
                finalCommand.append(RemoveCommand.create(getEditingDomain(), exp, ExpressionPackage.Literals.EXPRESSION__REFERENCED_ELEMENTS, oldDependency));
                final EObject newDependency = ExpressionHelper.createDependencyFromEObject(pairToRefactor.getNewValue());
                finalCommand.append(AddCommand.create(getEditingDomain(), exp, ExpressionPackage.Literals.EXPRESSION__REFERENCED_ELEMENTS, newDependency));
            }
        }
    }

    private void removeAllDataReferences(final CompoundCommand cc, final DataRefactorPair pairToRefactor) {
        final List<Expression> expressions = retrieveExpressionsInTheContainer(pairToRefactor);
        for (final Expression exp : expressions) {
            if (ExpressionConstants.VARIABLE_TYPE.equals(exp.getType()) && exp.getName().equals(pairToRefactor.getOldValue().getName())) {
                // update name and content
                cc.append(SetCommand.create(getEditingDomain(), exp, ExpressionPackage.Literals.EXPRESSION__NAME, ""));
                cc.append(SetCommand.create(getEditingDomain(), exp, ExpressionPackage.Literals.EXPRESSION__CONTENT, ""));
                // update return type
                cc.append(SetCommand.create(getEditingDomain(), exp, ExpressionPackage.Literals.EXPRESSION__RETURN_TYPE, String.class.getName()));
                cc.append(SetCommand.create(getEditingDomain(), exp, ExpressionPackage.Literals.EXPRESSION__TYPE, ExpressionConstants.CONSTANT_TYPE));
                // update referenced data
                cc.append(RemoveCommand.create(getEditingDomain(), exp, ExpressionPackage.Literals.EXPRESSION__REFERENCED_ELEMENTS, exp.getReferencedElements()));
            }
        }
    }

    private List<Expression> retrieveExpressionsInTheContainer(
            final DataRefactorPair pairToRefactor) {
        List<Expression> expressions = null;
        if (pairToRefactor.getOldValue().eContainer() instanceof Pool) {
            expressions = ModelHelper.getAllItemsOfType(dataContainer, ExpressionPackage.Literals.EXPRESSION);
        } else {
            expressions = ModelHelper.getAllItemsOfType(pairToRefactor.getOldValue().eContainer(), ExpressionPackage.Literals.EXPRESSION);
        }
        return expressions;
    }

    protected void updateDataInListsOfData(final CompoundCommand cc) {
        final List<Data> data = ModelHelper.getAllItemsOfType(dataContainer, ProcessPackage.Literals.DATA);
        for (final DataRefactorPair pairToRefactor : pairsToRefactor) {
            for (final Data d : data) {
                if (!d.equals(pairToRefactor.getNewValue()) && d.getName().equals(pairToRefactor.getOldValue().getName())) {
                    final Data copy = EcoreUtil.copy(pairToRefactor.getNewValue());
                    final EObject container = d.eContainer();
                    final EReference eContainmentFeature = d.eContainmentFeature();
                    if (container != null && container.eGet(eContainmentFeature) instanceof Collection<?>) {
                        final List<?> dataList = (List<?>) container.eGet(eContainmentFeature);
                        final int index = dataList.indexOf(d);
                        cc.append(RemoveCommand.create(getEditingDomain(), container, eContainmentFeature, d));
                        cc.append(AddCommand.create(getEditingDomain(), container, eContainmentFeature, copy, index));
                    } else if (container != null && container.eGet(eContainmentFeature) instanceof Data) {
                        cc.append(SetCommand.create(getEditingDomain(), container, eContainmentFeature, copy));
                    }
                }
            }
        }
    }

    protected void updateDataReferenceInVariableExpressions(final CompoundCommand cc) {
        final List<Expression> expressions = ModelHelper.getAllItemsOfType(dataContainer, ExpressionPackage.Literals.EXPRESSION);
        for (final DataRefactorPair pairToRefactor : pairsToRefactor) {
            for (final Expression exp : expressions) {
                if (ExpressionConstants.VARIABLE_TYPE.equals(exp.getType()) && exp.getName().equals(pairToRefactor.getOldValue().getName())) {
                    updateDataReferenceInVariableExpression(cc, pairToRefactor, exp);
                }
            }
        }
    }

    private void updateDataReferenceInVariableExpression(final CompoundCommand cc, final DataRefactorPair pairToRefactor, final Expression exp) {
        if (isReturnFixedOnExpressionWithUpdatedType(pairToRefactor, exp)) {
            cc.append(ExpressionHelper.clearExpression(exp, getEditingDomain()));
            setAskConfirmation(true);
        } else {
            // update name and content
            final Data newValue = pairToRefactor.getNewValue();
            final String newValueName = newValue.getName();
            cc.append(SetCommand.create(getEditingDomain(), exp, ExpressionPackage.Literals.EXPRESSION__NAME, newValueName));
            cc.append(SetCommand.create(getEditingDomain(), exp, ExpressionPackage.Literals.EXPRESSION__CONTENT, newValueName));
            // update return type
            cc.append(SetCommand.create(getEditingDomain(), exp, ExpressionPackage.Literals.EXPRESSION__RETURN_TYPE, DataUtil.getTechnicalTypeFor(newValue)));
        }
    }

    private boolean isReturnFixedOnExpressionWithUpdatedType(final DataRefactorPair pairToRefactor, final Expression exp) {
        return exp.isReturnTypeFixed() && !exp.getReturnType().equals(DataUtil.getTechnicalTypeFor(pairToRefactor.getNewValue()));
    }

    protected void updateDataReferenceInMultinstanciation(final CompoundCommand cc) {
        final List<MultiInstantiable> multiInstanciations = ModelHelper.getAllItemsOfType(dataContainer, ProcessPackage.Literals.MULTI_INSTANTIABLE);
        for (final DataRefactorPair pairToRefactor : pairsToRefactor) {
            for (final MultiInstantiable multiInstantiation : multiInstanciations) {
                final Data outputData = multiInstantiation.getOutputData();
                if (outputData != null && outputData.equals(pairToRefactor.getOldValue())) {
                    cc.append(SetCommand.create(getEditingDomain(), multiInstantiation, ProcessPackage.Literals.MULTI_INSTANTIABLE__OUTPUT_DATA,
                            pairToRefactor.getNewValue()));
                }
                final Data collectionToMultiinstanciateData = multiInstantiation.getCollectionDataToMultiInstantiate();
                if (collectionToMultiinstanciateData != null && collectionToMultiinstanciateData.equals(pairToRefactor.getOldValue())) {
                    cc.append(SetCommand.create(getEditingDomain(), multiInstantiation,
                            ProcessPackage.Literals.MULTI_INSTANTIABLE__COLLECTION_DATA_TO_MULTI_INSTANTIATE,
                            pairToRefactor.getNewValue()));
                }
                final Data listDataContainingOutputResults = multiInstantiation.getListDataContainingOutputResults();
                if (listDataContainingOutputResults != null && listDataContainingOutputResults.equals(pairToRefactor.getOldValue())) {
                    cc.append(SetCommand.create(getEditingDomain(), multiInstantiation,
                            ProcessPackage.Literals.MULTI_INSTANTIABLE__LIST_DATA_CONTAINING_OUTPUT_RESULTS,
                            pairToRefactor.getNewValue()));
                }
            }
        }
    }

    public void setDataContainer(final DataAware dataContainer) {
        this.dataContainer = dataContainer;
    }

    public void setUpdateDataReferences(final boolean updateDataReferences) {
        this.updateDataReferences = updateDataReferences;
    }

    @Override
    protected EObject getContainer(final Data oldValue) {
        return dataContainer;
    }

    public void setDataContainmentFeature(final EStructuralFeature dataContainmentFeature) {
        this.dataContainmentFeature = dataContainmentFeature;
    }

    @Override
    protected DataRefactorPair createRefactorPair(final Data newItem, final Data oldItem) {
        return new DataRefactorPair(newItem, oldItem);
    }

}
