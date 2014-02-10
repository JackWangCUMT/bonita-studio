/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.studio.common.emf.tools;

import static org.fest.assertions.Assertions.assertThat;

import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.form.DateFormField;
import org.bonitasoft.studio.model.form.FormFactory;
import org.bonitasoft.studio.model.form.Widget;
import org.bonitasoft.studio.model.process.DataType;
import org.bonitasoft.studio.model.process.JavaObjectData;
import org.bonitasoft.studio.model.process.ProcessFactory;
import org.eclipse.emf.ecore.EObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Romain Bioteau
 *
 */
public class ExpressionHelperTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.bonitasoft.studio.common.emf.tools.ExpressionHelper#createDependencyFromEObject(org.eclipse.emf.ecore.EObject)}.
	 */
	@Test
	public void shouldCreateDependencyFromEObject_CopyDataAndRemoveDefaultValue() throws Exception {
		JavaObjectData myData = ProcessFactory.eINSTANCE.createJavaObjectData();
		myData.setName("dataName");
		myData.setDatasourceId("datasourceId");
		myData.setClassName("org.bonita.test.MyClass");
		myData.setTransient(true);
		myData.setGenerated(false);
		myData.setMultiple(true);
		myData.setDocumentation("some doc");
		myData.setDefaultValue(ExpressionFactory.eINSTANCE.createExpression());
		DataType dataType = ProcessFactory.eINSTANCE.createJavaType();
		myData.setDataType(dataType);
		
		EObject dependencyFromEObject = ExpressionHelper.createDependencyFromEObject(myData);
		
		assertThat(dependencyFromEObject).isNotNull().isNotSameAs(myData).isInstanceOf(JavaObjectData.class);
		assertThat(((JavaObjectData)dependencyFromEObject).getName()).isEqualTo(myData.getName());
		assertThat(((JavaObjectData)dependencyFromEObject).getDatasourceId()).isEqualTo(myData.getDatasourceId());
		assertThat(((JavaObjectData)dependencyFromEObject).getDocumentation()).isEqualTo(myData.getDocumentation());
		assertThat(((JavaObjectData)dependencyFromEObject).isGenerated()).isEqualTo(myData.isGenerated());
		assertThat(((JavaObjectData)dependencyFromEObject).isMultiple()).isEqualTo(myData.isMultiple());
		assertThat(((JavaObjectData)dependencyFromEObject).isTransient()).isEqualTo(myData.isTransient());
		assertThat(((JavaObjectData)dependencyFromEObject).getDataType()).isEqualTo(myData.getDataType());
		assertThat(((JavaObjectData)dependencyFromEObject).getDefaultValue()).isNull();
	}
	
	@Test
	public void shouldCreateDependencyFromEObject_CopyWidgetWithNameOnly() throws Exception {
		Widget widget = FormFactory.eINSTANCE.createDateFormField();
		widget.setName("myWidget");
		widget.setDisplayLabel(ExpressionFactory.eINSTANCE.createExpression());
		widget.setInputExpression(ExpressionFactory.eINSTANCE.createExpression());
		
		EObject dependencyFromEObject = ExpressionHelper.createDependencyFromEObject(widget);
		assertThat(dependencyFromEObject).isNotNull().isNotSameAs(widget).isInstanceOf(DateFormField.class);
		assertThat(((Widget)dependencyFromEObject).getName()).isEqualTo(widget.getName());
		assertThat(((Widget)dependencyFromEObject).getDisplayLabel()).isNull();
		assertThat(((Widget)dependencyFromEObject).getInputExpression()).isNull();
	}

}
