/*
 * Copyright (c) 2010-2015 Osman Shoukry
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License or any
 *    later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.openpojo.reflection.impl;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

import com.openpojo.business.annotation.BusinessKey;
import com.openpojo.random.RandomFactory;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoField;
import com.openpojo.reflection.construct.InstanceFactory;
import com.openpojo.reflection.exception.ReflectionException;
import com.openpojo.reflection.impl.sampleannotation.SomeAnnotation;
import com.openpojo.reflection.impl.sampleclasses.AClassWithSyntheticField;
import com.openpojo.reflection.impl.sampleclasses.AClassWithVariousAnnotatedFields;
import com.openpojo.reflection.impl.sampleclasses.ClassWithGenericTypes;
import com.openpojo.reflection.impl.sampleclasses.PojoFieldImplClass;
import com.openpojo.validation.affirm.Affirm;
import org.junit.Before;
import org.junit.Test;

/**
 * @author oshoukry
 */
public class PojoFieldImplTest {
  private PojoClass pojoClass = PojoClassFactory.getPojoClass(PojoFieldImplClass.class);
  private Object pojoClassInstance;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    pojoClassInstance = InstanceFactory.getInstance(pojoClass);
  }

  /**
   * Test method for: {@link com.openpojo.reflection.impl.PojoFieldImpl#get(java.lang.Object)}.
   * {@link com.openpojo.reflection.impl.PojoFieldImpl#set(java.lang.Object, java.lang.Object)}.
   */
  @Test
  public void testSetAndGet() {
    for (PojoField pojoField : pojoClass.getPojoFields()) {
      if (!pojoField.isFinal() && !pojoField.isPrimitive()) {
        Affirm.affirmNull(String.format("Field=[%s] should have null default value", pojoField),
            pojoField.get(pojoClassInstance));
        Object randomValue = RandomFactory.getRandomValue(pojoField.getType());
        pojoField.set(pojoClassInstance, randomValue);
        Affirm.affirmEquals(String.format("PojoField.get() result=[%s] different from what was set=[%s] for " +
            "PojoFieldImpl=[%s]", pojoField.get(pojoClassInstance), randomValue, pojoField), randomValue, pojoField.get
            (pojoClassInstance));
      }
    }
  }

  private PojoField getPrivateStringField() {
    for (PojoField pojoField : pojoClass.getPojoFields()) {
      if (pojoField.getName().equals("privateString")) {
        return pojoField;
      }
    }
    Affirm.fail("Field with name 'privateString' removed from class" + pojoClass.getName());
    return null;
  }

  @Test(expected = ReflectionException.class)
  public void shouldFailSet() {
    PojoField pojoField = getPrivateStringField();
    pojoField.set(null, RandomFactory.getRandomValue(pojoField.getType()));
  }

  @Test(expected = ReflectionException.class)
  public void shouldFailGet() {
    PojoField pojoField = getPrivateStringField();
    pojoField.get(null);
  }

  @Test(expected = ReflectionException.class)
  public void shouldFailSetter() {
    PojoField pojoField = getPrivateStringField();
    pojoField.invokeSetter(null, RandomFactory.getRandomValue(pojoField.getType()));
  }

  @Test(expected = ReflectionException.class)
  public void shouldFailGetter() {
    PojoField pojoField = getPrivateStringField();
    pojoField.invokeGetter(null);
  }

  @Test
  public void shouldGetParameterizedType() {
    PojoClass pojoClass = PojoClassFactory.getPojoClass(ClassWithGenericTypes.class);
    Affirm.affirmEquals("Fields added/removed?!", 4, pojoClass.getPojoFields().size());

    int affirmChecks = 0;
    for (PojoField pojoField : pojoClass.getPojoFields()) {
      if (pojoField.getName().equals("parameterizedChildren")) {
        Affirm.affirmTrue("Not Generic?!", pojoField.isParameterized());
        Affirm.affirmTrue("Wrong Parameterization!?", pojoField.getParameterTypes().contains(ClassWithGenericTypes.class));
        affirmChecks++;
      }

      if (pojoField.getName().equals("nonparameterizedList") || pojoField.getName().equals("nonParameterizedString")) {
        Affirm.affirmFalse("Turned generic?!", pojoField.isParameterized());
        Affirm.affirmEquals("Returned non-empty list for nonParameterized type!? [" + pojoField.getParameterTypes() + "]", 0,
            pojoField.getParameterTypes().size());
        affirmChecks++;
      }

      if (pojoField.getName().equals("parameterizedMap")) {
        Affirm.affirmEquals("MultipTypeGeneric failed!!", 2, pojoField.getParameterTypes().size());
        Affirm.affirmTrue(String.format("Type not found [%s]", String.class), pojoField.getParameterTypes().contains(String
            .class));
        Affirm.affirmTrue(String.format("Type not found [%s]", Integer.class), pojoField.getParameterTypes().contains(Integer
            .class));
        affirmChecks++;
      }
    }
    Affirm.affirmEquals("Fields added/removed/renamed? expected 4 checks!!", 4, affirmChecks);
  }

  @Test
  public void annotationlessShouldNotReturnNull() {
    PojoClass pojoClass = PojoClassFactory.getPojoClass(AClassWithVariousAnnotatedFields.class);
    List<PojoField> allFields = pojoClass.getPojoFields();

    for (PojoField pojoField : allFields) {
      if (pojoField.getName().equals("nonAnnotatedField")) {
        Affirm.affirmNotNull("getAnnotations should not return null.", pojoField.getAnnotations());
        return;
      }
    }
    Affirm.fail(String.format("nonAnnotatedField renamed? expected in [%s]", pojoClass));
  }

  @Test
  public void multipleAnnotationsShouldBeReturned() {
    PojoClass pojoClass = PojoClassFactory.getPojoClass(AClassWithVariousAnnotatedFields.class);
    List<PojoField> allFields = pojoClass.getPojoFields();

    for (PojoField pojoField : allFields) {
      if (pojoField.getName().equals("multipleAnnotationField")) {
        Affirm.affirmEquals(String.format("Annotations added/removed from field=[%s]", pojoField), 2, pojoField.getAnnotations
            ().size());
        List<Class<?>> expectedAnnotations = new LinkedList<Class<?>>();
        expectedAnnotations.add(SomeAnnotation.class);
        expectedAnnotations.add(BusinessKey.class);
        for (Annotation annotation : pojoField.getAnnotations()) {
          Affirm.affirmTrue(String.format("Expected annotations [%s] not found, instead found [%s]", expectedAnnotations,
              annotation.annotationType()), expectedAnnotations.contains(annotation.annotationType()));
        }
        return;
      }
    }
    Affirm.fail(String.format("multipleAnnotationField renamed? expected in [%s]", pojoClass));
  }

  /**
   * Test method for {@link com.openpojo.reflection.impl.PojoFieldImpl#isPrimitive()}.
   */
  @Test
  public void testIsPrimitive() {
    for (PojoField pojoField : pojoClass.getPojoFields()) {
      if (pojoField.getName().startsWith("primitive")) {
        Affirm.affirmTrue(String.format("isPrimitive() check on primitive field=[%s] returned false!!", pojoField), pojoField
            .isPrimitive());
      }
    }
  }

  /**
   * Test method for {@link com.openpojo.reflection.impl.PojoFieldImpl#isStatic()}.
   */
  @Test
  public void testIsStatic() {
    for (PojoField pojoField : pojoClass.getPojoFields()) {
      if (pojoField.getName().startsWith("static")) {
        Affirm.affirmTrue(String.format("isStatic() check on field=[%s] returned false!!", pojoField), pojoField.isStatic());
      }
    }
  }

  @Test
  public void testIsTransient() {
    for (PojoField pojoField : pojoClass.getPojoFields()) {
      if (pojoField.getName().equals("transientString")) {
        Affirm.affirmTrue(String.format("isTransient() check on field=[%s] returned false!!", pojoField), pojoField.isTransient
            ());
      }
    }

  }

  @Test
  public void testIsVolatile() {
    for (PojoField pojoField : pojoClass.getPojoFields()) {
      if (pojoField.getName().equals("volatileString")) {
        Affirm.affirmTrue(String.format("isVolatile() check on field=[%s] returned false!!", pojoField), pojoField.isVolatile());
      }
    }

  }

  @Test
  public void testIsPrivate() {
    String prefix = "private";
    PojoField pojoField = getFieldStartingWith(prefix);
    Affirm.affirmNotNull("Field not found [" + prefix + "]", pojoField);
    Affirm.affirmTrue("isPrivate() check on field=[" + pojoField + "] returned false!!", pojoField.isPrivate());
  }

  @Test
  public void isPackagePrivate() {
    String prefix = "packagePrivate";
    PojoField pojoField = getFieldStartingWith(prefix);
    Affirm.affirmNotNull("Field not found [" + prefix + "]", pojoField);
    Affirm.affirmTrue("isPackagePrivate() check on field=[" + pojoField + "] returned false!!", pojoField.isPackagePrivate());
  }

  @Test
  public void testIsProtected() {
    String prefix = "protected";
    PojoField pojoField = getFieldStartingWith(prefix);
    Affirm.affirmNotNull("Field not found [" + prefix + "]", pojoField);
    Affirm.affirmTrue("isProtected() check on field=[" + pojoField + "] returned false!!", pojoField.isProtected());
  }

  @Test
  public void testIsPublic() {
    String prefix = "public";
    PojoField pojoField = getFieldStartingWith(prefix);
    Affirm.affirmNotNull("Field not found [" + prefix + "]", pojoField);
    Affirm.affirmTrue("isPublic() check on field=[" + pojoField + "] returned false!!", pojoField.isPublic());
  }

  private PojoField getFieldStartingWith(String prefix) {
    for (PojoField pojoField : pojoClass.getPojoFields())
      if (pojoField.getName().startsWith(prefix))
        return pojoField;
    throw ReflectionException.getInstance("Request field with prefix [" + prefix + "} not found.");
  }

  @Test
  public void testIsSynthetic() {
    PojoClass classWithSyntheticField = PojoClassFactory.getPojoClass(AClassWithSyntheticField.SyntheticFieldContainer.class);
    Affirm.affirmEquals("Failed to find field in class[" + classWithSyntheticField + "]", 1, classWithSyntheticField
        .getPojoFields().size());
    PojoField pojoField = classWithSyntheticField.getPojoFields().get(0);
    Affirm.affirmTrue("Failed to check isSynthetic + [" + pojoField + "]", pojoField.isSynthetic());
  }
}
