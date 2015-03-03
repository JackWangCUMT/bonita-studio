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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.contract.core.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.studio.model.process.Contract;
import org.bonitasoft.studio.model.process.ContractInput;
import org.bonitasoft.studio.model.process.ContractInputType;
import org.bonitasoft.studio.model.process.ProcessFactory;
import org.eclipse.core.runtime.IStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Romain Bioteau
 *
 */
public class ContractInputDescriptionValidationRuleTest {

    private ContractInputDescriptionValidationRule validationRule;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
         validationRule = new ContractInputDescriptionValidationRule();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void should_validate_a_contract_input_returns_an_error_status_for_input_with_too_long_description() throws Exception {
        final Contract contract = ProcessFactory.eINSTANCE.createContract();
        final ContractInput contractInput = addInput(
                contract,
                "name",
                ContractInputType.TEXT,
                "kjdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddfgdfgdfkgdfkjghdfkjghdfkjghdfkjghdfkjghdfkjghdfkjghurtyuizeryueiz ueizryueizry ueizryezuirydddddddddddddddddddddddddddddddddddddddddfgdfgdfghdfkjghkjdfhgkjdfhgkjdfghkjdfghkjdfghkjhkjhkjdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
        final IStatus status = validationRule.validate(contractInput);
        assertThat(status.isOK()).isFalse();
    }

    @Test
    public void should_applies_to_contract_input() throws Exception {
        final Contract contract = ProcessFactory.eINSTANCE.createContract();
        final ContractInput contractInput = addInput(contract, "", ContractInputType.TEXT, null);
        assertThat(validationRule.appliesTo(contractInput)).isTrue();
        assertThat(validationRule.appliesTo(contract)).isFalse();
    }

    private ContractInput addInput(final Contract contract, final String inputName, final ContractInputType type, final String description) {
        final ContractInput contractInput = ProcessFactory.eINSTANCE.createContractInput();
        contractInput.setName(inputName);
        contractInput.setType(type);
        contractInput.setDescription(description);
        contract.getInputs().add(contractInput);
        return contractInput;
    }

}