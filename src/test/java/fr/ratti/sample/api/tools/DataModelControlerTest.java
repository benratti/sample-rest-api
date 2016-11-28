package fr.ratti.sample.api.tools;


import fr.ratti.sample.api.annotation.Mandatory;
import fr.ratti.sample.api.exception.MandatoryFieldsNotSetException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.inject.annotation.TestedObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

/**
 * Created by bratti on 18/08/2016.
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class DataModelControlerTest {

    @TestedObject
    private DataModelControler<TestClass> dataModelControler;

    private class TestClass {

        public String optionalField1;
        public Object optionalField2;

        @Mandatory
        public String mandatoryField1;

        @Mandatory
        public Object mandatoryField2;
    }

    @Test
    public void checkMandatoryFields_should_throw_exception_when_mandatory_fields_are_not_set() throws Exception {

        TestClass data = new TestClass();
        data.optionalField1 = "Hello World";
        data.optionalField2 = new Object();
        data.mandatoryField1 = null;
        data.mandatoryField2 = new Object();

        try {
            dataModelControler.checkMandatoryFields(data);
            failBecauseExceptionWasNotThrown(MandatoryFieldsNotSetException.class);
        } catch (MandatoryFieldsNotSetException exception) {
            assertThat(exception.getFieldNames())
                    .hasSize(1)
                    .containsOnly("mandatoryField1");

            assertThat(exception.getClazz()).isEqualTo(TestClass.class);
        }

    }


    @Test
    public void checkMandatoryFields_should_not_throw_exception_when_mandatory_fields_are_set() throws Exception {

        TestClass data = new TestClass();
        data.optionalField1 = "Hello World";
        data.optionalField2 = new Object();
        data.mandatoryField1 = "Hello";
        data.mandatoryField2 = new Object();

        dataModelControler.checkMandatoryFields(data);

    }

    @Test
    public void checkMandatoryFields_should_not_throw_exception_when_optional_fields_are_not_set() throws Exception {

        TestClass data = new TestClass();
        data.optionalField1 = "Hello World";
        data.optionalField2 = null;
        data.mandatoryField1 = "Hello";
        data.mandatoryField2 = new Object();

        dataModelControler.checkMandatoryFields(data);

    }


}
