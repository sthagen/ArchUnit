package com.tngtech.archunit.core.importer;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.testutil.ArchConfigurationRule;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.testutil.Assertions.assertThatType;
import static com.tngtech.archunit.testutil.assertion.ExpectedConcreteType.ExpectedConcreteClass.concreteClass;
import static com.tngtech.archunit.testutil.assertion.ExpectedConcreteType.ExpectedConcreteGenericArray.genericArray;
import static com.tngtech.archunit.testutil.assertion.ExpectedConcreteType.ExpectedConcreteGenericArray.parameterizedTypeArrayName;
import static com.tngtech.archunit.testutil.assertion.ExpectedConcreteType.ExpectedConcreteGenericArray.typeVariableArrayName;
import static com.tngtech.archunit.testutil.assertion.ExpectedConcreteType.ExpectedConcreteParameterizedType.parameterizedType;
import static com.tngtech.archunit.testutil.assertion.ExpectedConcreteType.ExpectedConcreteTypeVariable.typeVariable;
import static com.tngtech.archunit.testutil.assertion.ExpectedConcreteType.ExpectedConcreteWildcardType.wildcardType;

@RunWith(DataProviderRunner.class)
public class ClassFileImporterGenericSuperclassTest {

    @Rule
    public final ArchConfigurationRule configurationRule = new ArchConfigurationRule().resolveAdditionalDependenciesFromClassPath(false);

    @Test
    public void imports_non_generic_superclass() {
        class BaseClass {
        }
        class Child extends BaseClass {
        }

        JavaType genericSuperclass = new ClassFileImporter().importClass(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").matches(BaseClass.class);
    }

    @Test
    public void imports_generic_superclass_with_one_type_argument() {
        @SuppressWarnings("unused")
        class BaseClass<T> {
        }
        class Child extends BaseClass<String> {
        }

        JavaType genericSuperclass = new ClassFileImporter().importClasses(Child.class, String.class).get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass")
                .hasErasure(BaseClass.class)
                .hasActualTypeArguments(String.class);
    }

    @Test
    public void imports_raw_generic_superclass_as_JavaClass_instead_of_JavaParameterizedType() {
        @SuppressWarnings("unused")
        class BaseClass<T> {
        }
        @SuppressWarnings("rawtypes")
        class Child extends BaseClass {
        }

        JavaType rawGenericSuperclass = new ClassFileImporter().importClasses(Child.class, BaseClass.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(rawGenericSuperclass).as("raw generic superclass").matches(BaseClass.class);
    }

    @Test
    public void imports_generic_superclass_with_array_type_argument() {
        @SuppressWarnings("unused")
        class BaseClass<T> {
        }
        class Child extends BaseClass<String[]> {
        }

        JavaType genericSuperClass = new ClassFileImporter().importClasses(Child.class, String.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperClass).as("generic superclass")
                .hasErasure(BaseClass.class)
                .hasActualTypeArguments(String[].class);
    }

    @Test
    public void imports_generic_superclass_with_primitive_array_type_argument() {
        @SuppressWarnings("unused")
        class BaseClass<T> {
        }
        class Child extends BaseClass<int[]> {
        }

        JavaType genericSuperClass = new ClassFileImporter().importClasses(Child.class, int.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperClass).as("generic superclass")
                .hasErasure(BaseClass.class)
                .hasActualTypeArguments(int[].class);
    }

    @Test
    public void imports_generic_superclass_with_multiple_type_arguments() {
        @SuppressWarnings("unused")
        class BaseClass<A, B, C> {
        }
        @SuppressWarnings("unused")
        class Child extends BaseClass<String, Serializable, File> {
        }

        JavaType genericSuperclass = new ClassFileImporter().importClasses(Child.class, String.class, Serializable.class, File.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass")
                .hasErasure(BaseClass.class)
                .hasActualTypeArguments(String.class, Serializable.class, File.class);
    }

    @Test
    public void imports_generic_superclass_with_single_actual_type_argument_parameterized_with_concrete_class() {
        @SuppressWarnings("unused")
        class BaseClass<T> {
        }
        class Child extends BaseClass<ClassParameterWithSingleTypeParameter<String>> {
        }

        JavaType genericSuperclass = new ClassFileImporter().importClasses(Child.class, ClassParameterWithSingleTypeParameter.class, String.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withTypeArguments(String.class)
        );
    }

    @Test
    public void imports_generic_superclass_with_multiple_actual_type_arguments_parameterized_with_concrete_classes() {
        @SuppressWarnings("unused")
        class BaseClass<A, B, C> {
        }
        class Child extends BaseClass<
                ClassParameterWithSingleTypeParameter<File>,
                InterfaceParameterWithSingleTypeParameter<Serializable>,
                InterfaceParameterWithSingleTypeParameter<String>> {
        }

        JavaType genericSuperclass = new ClassFileImporter()
                .importClasses(
                        Child.class, ClassParameterWithSingleTypeParameter.class, InterfaceParameterWithSingleTypeParameter.class,
                        File.class, Serializable.class, String.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withTypeArguments(File.class),
                parameterizedType(InterfaceParameterWithSingleTypeParameter.class)
                        .withTypeArguments(Serializable.class),
                parameterizedType(InterfaceParameterWithSingleTypeParameter.class)
                        .withTypeArguments(String.class)
        );
    }

    @Test
    public void imports_generic_superclass_with_single_actual_type_argument_parameterized_with_unbound_wildcard() {
        @SuppressWarnings("unused")
        class BaseClass<T> {
        }
        class Child extends BaseClass<ClassParameterWithSingleTypeParameter<?>> {
        }

        JavaType genericSuperclass = new ClassFileImporter().importClasses(Child.class, ClassParameterWithSingleTypeParameter.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withWildcardTypeParameter()
        );
    }

    @Test
    public void imports_generic_superclass_with_actual_type_arguments_parameterized_with_bounded_wildcards() {
        @SuppressWarnings("unused")
        class BaseClass<A, B> {
        }
        class Child extends BaseClass<
                ClassParameterWithSingleTypeParameter<? extends String>,
                ClassParameterWithSingleTypeParameter<? super File>> {
        }

        JavaType genericSuperclass = new ClassFileImporter().importClasses(Child.class, ClassParameterWithSingleTypeParameter.class, String.class, File.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withWildcardTypeParameterWithUpperBound(String.class),
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withWildcardTypeParameterWithLowerBound(File.class)
        );
    }

    @Test
    public void imports_generic_superclass_with_actual_type_arguments_with_multiple_wildcards_with_various_bounds() {
        @SuppressWarnings("unused")
        class BaseClass<A, B> {
        }
        class Child extends BaseClass<
                ClassParameterWithSingleTypeParameter<Map<? extends Serializable, ? super File>>,
                ClassParameterWithSingleTypeParameter<Reference<? super String>>> {
        }

        JavaType genericSuperclass = new ClassFileImporter()
                .importClasses(
                        Child.class, ClassParameterWithSingleTypeParameter.class,
                        Map.class, Serializable.class, File.class, Reference.class, String.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withTypeArguments(parameterizedType(Map.class)
                                .withWildcardTypeParameters(
                                        wildcardType().withUpperBound(Serializable.class),
                                        wildcardType().withLowerBound(File.class))),
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withTypeArguments(parameterizedType(Reference.class)
                                .withWildcardTypeParameterWithLowerBound(String.class))
        );
    }

    @Test
    public void imports_generic_superclass_parameterized_with_type_variable() {
        @SuppressWarnings("unused")
        class BaseClass<SUPER> {
        }
        class Child<SUB> extends BaseClass<SUB> {
        }

        JavaType genericSuperclass = new ClassFileImporter().importClasses(Child.class, BaseClass.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(typeVariable("SUB"));
    }

    @Test
    public void imports_generic_superclass_with_actual_type_argument_parameterized_with_type_variable() {
        @SuppressWarnings("unused")
        class BaseClass<SUPER> {
        }
        class Child<SUB> extends BaseClass<ClassParameterWithSingleTypeParameter<SUB>> {
        }

        JavaType genericSuperclass = new ClassFileImporter().importClasses(Child.class, ClassParameterWithSingleTypeParameter.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withTypeArguments(typeVariable("SUB"))
        );
    }

    @Test
    public void references_type_variable_assigned_to_actual_type_argument_of_generic_superclass() {
        @SuppressWarnings("unused")
        class BaseClass<SUPER> {
        }
        class Child<SUB extends String> extends BaseClass<ClassParameterWithSingleTypeParameter<SUB>> {
        }

        JavaType genericSuperclass = new ClassFileImporter().importClasses(Child.class, ClassParameterWithSingleTypeParameter.class, String.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withTypeArguments(typeVariable("SUB").withUpperBounds(String.class))
        );
    }

    @Test
    public void references_outer_type_variable_assigned_to_actual_type_argument_of_generic_superclass_of_inner_class() {
        @SuppressWarnings("unused")
        class OuterWithTypeParameter<OUTER extends String> {
            class SomeInner {
                @SuppressWarnings("unused")
                class BaseClass<T> {
                }

                class Child extends BaseClass<OUTER> {
                }
            }
        }

        JavaType genericSuperclass = new ClassFileImporter()
                .importClasses(
                        OuterWithTypeParameter.class,
                        OuterWithTypeParameter.SomeInner.class,
                        OuterWithTypeParameter.SomeInner.Child.class,
                        String.class)
                .get(OuterWithTypeParameter.SomeInner.Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                typeVariable("OUTER").withUpperBounds(String.class)
        );
    }

    @Test
    public void creates_new_stub_type_variables_for_type_variables_of_enclosing_classes_that_are_out_of_context_for_generic_superclass_of_inner_class() {
        @SuppressWarnings("unused")
        class OuterWithTypeParameter<OUTER extends String> {
            class SomeInner {
                @SuppressWarnings("unused")
                class BaseClass<T> {
                }

                class Child extends BaseClass<OUTER> {
                }
            }
        }

        JavaType genericSuperclass = new ClassFileImporter()
                .importClasses(OuterWithTypeParameter.SomeInner.Child.class, String.class)
                .get(OuterWithTypeParameter.SomeInner.Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                typeVariable("OUTER").withoutUpperBounds()
        );
    }

    @Test
    public void considers_hierarchy_of_methods_and_classes_for_type_parameter_context() throws ClassNotFoundException {
        @SuppressWarnings("unused")
        class Level1<T1 extends String> {
            <T2 extends T1> void level2() {
                class BaseClass<T> {
                }
                class Level3<T3 extends T2> extends BaseClass<T3> {
                }
            }
        }

        Class<?> innermostClass = Class.forName(Level1.class.getName() + "$1Level3");
        JavaType genericSuperclass = new ClassFileImporter()
                .importClasses(innermostClass, Level1.class, String.class)
                .get(innermostClass).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass")
                .hasActualTypeArguments(
                        typeVariable("T3").withUpperBounds(
                                typeVariable("T2").withUpperBounds(
                                        typeVariable("T1").withUpperBounds(String.class))));
    }

    @Test
    public void imports_wildcards_of_generic_superclass_bound_by_type_variables() {
        @SuppressWarnings("unused")
        class BaseClass<A, B> {
        }

        class Child<FIRST extends String, SECOND extends Serializable> extends BaseClass<
                ClassParameterWithSingleTypeParameter<? extends FIRST>,
                ClassParameterWithSingleTypeParameter<? super SECOND>> {
        }

        JavaType genericSuperclass = new ClassFileImporter()
                .importClasses(Child.class, ClassParameterWithSingleTypeParameter.class, String.class, Serializable.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withWildcardTypeParameterWithUpperBound(
                                typeVariable("FIRST").withUpperBounds(String.class)),
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withWildcardTypeParameterWithLowerBound(
                                typeVariable("SECOND").withUpperBounds(Serializable.class))
        );
    }

    @Test
    public void imports_wildcards_of_generic_superclass_bound_by_type_variables_of_enclosing_classes() {
        @SuppressWarnings("unused")
        class OuterWithTypeParameter<OUTER_ONE extends String, OUTER_TWO extends Serializable> {
            class SomeInner {
                @SuppressWarnings("unused")
                class BaseClass<A, B> {
                }

                class Child extends BaseClass<
                        ClassParameterWithSingleTypeParameter<? extends OUTER_ONE>,
                        ClassParameterWithSingleTypeParameter<? super OUTER_TWO>> {
                }
            }
        }

        JavaType genericSuperclass = new ClassFileImporter()
                .importClasses(
                        OuterWithTypeParameter.class,
                        OuterWithTypeParameter.SomeInner.class,
                        OuterWithTypeParameter.SomeInner.Child.class,
                        ClassParameterWithSingleTypeParameter.class, String.class, Serializable.class)
                .get(OuterWithTypeParameter.SomeInner.Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withWildcardTypeParameterWithUpperBound(
                                typeVariable("OUTER_ONE").withUpperBounds(String.class)),
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withWildcardTypeParameterWithLowerBound(
                                typeVariable("OUTER_TWO").withUpperBounds(Serializable.class))
        );
    }

    @Test
    public void creates_new_stub_type_variables_for_wildcards_bound_by_type_variables_of_enclosing_classes_that_are_out_of_context() {
        @SuppressWarnings("unused")
        class OuterWithTypeParameter<OUTER_ONE extends String, OUTER_TWO extends Serializable> {
            class SomeInner {
                @SuppressWarnings("unused")
                class BaseClass<A, B> {
                }

                class Child extends BaseClass<
                        ClassParameterWithSingleTypeParameter<? extends OUTER_ONE>,
                        ClassParameterWithSingleTypeParameter<? super OUTER_TWO>> {
                }
            }
        }

        JavaType genericSuperclass = new ClassFileImporter()
                .importClasses(
                        OuterWithTypeParameter.SomeInner.Child.class,
                        ClassParameterWithSingleTypeParameter.class, String.class, Serializable.class)
                .get(OuterWithTypeParameter.SomeInner.Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withWildcardTypeParameterWithUpperBound(
                                typeVariable("OUTER_ONE").withoutUpperBounds()),
                parameterizedType(ClassParameterWithSingleTypeParameter.class)
                        .withWildcardTypeParameterWithLowerBound(
                                typeVariable("OUTER_TWO").withoutUpperBounds())
        );
    }

    @Test
    public void imports_complex_type_with_multiple_nested_actual_type_arguments_of_generic_superclass_with_self_referencing_type_definitions() {
        @SuppressWarnings("unused")
        class BaseClass<A, B, C> {
        }

        class Child<FIRST extends String & Serializable, SECOND extends Serializable & Cloneable> extends BaseClass<
                // assigned to BaseClass<A,_,_>
                List<? extends FIRST>,
                // assigned to BaseClass<_,B,_>
                Map<
                        Map.Entry<FIRST, Map.Entry<String, SECOND>>,
                        Map<? extends String,
                                Map<? extends Serializable, List<List<? extends Set<? super Iterable<? super Map<SECOND, ?>>>>>>>>,
                // assigned to BaseClass<_,_,C>
                Comparable<Child<FIRST, SECOND>>> {
        }

        JavaType genericSuperclass = new ClassFileImporter()
                .importClasses(Child.class, String.class, Serializable.class, Cloneable.class,
                        List.class, Map.class, Map.Entry.class, Set.class, Iterable.class, Comparable.class)
                .get(Child.class).getSuperclass().get();

        // @formatter:off
        assertThatType(genericSuperclass).as("generic superclass").hasActualTypeArguments(
            // assigned to BaseClass<A,_,_>
            parameterizedType(List.class)
                .withWildcardTypeParameterWithUpperBound(
                    typeVariable("FIRST").withUpperBounds(String.class, Serializable.class)),
            // assigned to BaseClass<_,B,_>
            parameterizedType(Map.class).withTypeArguments(
                parameterizedType(Map.Entry.class).withTypeArguments(
                    typeVariable("FIRST").withUpperBounds(String.class, Serializable.class),
                    parameterizedType(Map.Entry.class).withTypeArguments(
                        concreteClass(String.class),
                        typeVariable("SECOND").withUpperBounds(Serializable.class, Cloneable.class))),
                parameterizedType(Map.class).withTypeArguments(
                    wildcardType().withUpperBound(String.class),
                    parameterizedType(Map.class).withTypeArguments(
                        wildcardType().withUpperBound(Serializable.class),
                        parameterizedType(List.class).withTypeArguments(
                            parameterizedType(List.class).withTypeArguments(
                                wildcardType().withUpperBound(
                                    parameterizedType(Set.class).withTypeArguments(
                                        wildcardType().withLowerBound(
                                            parameterizedType(Iterable.class).withTypeArguments(
                                                wildcardType().withLowerBound(
                                                    parameterizedType(Map.class).withTypeArguments(
                                                        typeVariable("SECOND").withUpperBounds(Serializable.class, Cloneable.class),
                                                        wildcardType()))))))))))),
            // assigned to BaseClass<_,_,C>
            parameterizedType(Comparable.class).withTypeArguments(
                parameterizedType(Child.class).withTypeArguments(
                    typeVariable("FIRST").withUpperBounds(String.class, Serializable.class),
                    typeVariable("SECOND").withUpperBounds(Serializable.class, Cloneable.class))));
        // @formatter:on
    }

    @Test
    public void imports_complex_type_with_multiple_nested_actual_type_arguments_of_generic_superclass_with_concrete_array_bounds() {
        @SuppressWarnings("unused")
        class BaseClass<A, B, C> {
        }

        @SuppressWarnings("unused")
        class Child extends BaseClass<
                List<Serializable[]>,
                List<? extends Serializable[][]>,
                Map<? super String[], Map<Map<? super String[][][], ?>, Serializable[][]>>
                > {
        }

        JavaClasses classes = new ClassFileImporter().importClasses(Child.class,
                List.class, Serializable.class, Map.class, String.class);

        JavaType genericSuperclass = classes.get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).hasActualTypeArguments(
                parameterizedType(List.class).withTypeArguments(Serializable[].class),
                parameterizedType(List.class).withWildcardTypeParameterWithUpperBound(Serializable[][].class),
                parameterizedType(Map.class).withTypeArguments(
                        wildcardType().withLowerBound(String[].class),
                        parameterizedType(Map.class).withTypeArguments(
                                parameterizedType(Map.class).withTypeArguments(
                                        wildcardType().withLowerBound(String[][][].class),
                                        wildcardType()),
                                concreteClass(Serializable[][].class))));
    }

    @Test
    public void imports_type_of_generic_superclass_with_parameterized_array_bounds() {
        @SuppressWarnings("unused")
        class BaseClass<A, B, C> {
        }

        class Child extends BaseClass<List<String>[], List<String[]>[][], List<String[][]>[][][]> {
        }

        JavaType genericSuperclass = new ClassFileImporter().importClasses(Child.class, List.class, String.class)
                .get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).hasActualTypeArguments(
                genericArray(parameterizedTypeArrayName(List.class, String.class, 1)).withComponentType(
                        parameterizedType(List.class).withTypeArguments(String.class)),
                genericArray(parameterizedTypeArrayName(List.class, String[].class, 2)).withComponentType(
                        genericArray(parameterizedTypeArrayName(List.class, String[].class, 1)).withComponentType(
                                parameterizedType(List.class).withTypeArguments(String[].class))),
                genericArray(parameterizedTypeArrayName(List.class, String[][].class, 3)).withComponentType(
                        genericArray(parameterizedTypeArrayName(List.class, String[][].class, 2)).withComponentType(
                                genericArray(parameterizedTypeArrayName(List.class, String[][].class, 1)).withComponentType(
                                        parameterizedType(List.class).withTypeArguments(String[][].class)))));
    }

    @Test
    public void imports_complex_type_with_multiple_nested_actual_type_arguments_of_generic_superclass_with_generic_array_bounds() {
        @SuppressWarnings("unused")
        class BaseClass<A, B, C> {
        }

        @SuppressWarnings("unused")
        class Child<X extends Serializable, Y extends String> extends BaseClass<
                List<X[]>,
                List<? extends X[][]>,
                Map<? super Y[], Map<Map<? super Y[][][], ?>, X[][]>>
                > {
        }

        JavaClasses classes = new ClassFileImporter().importClasses(Child.class,
                List.class, Serializable.class, Map.class, String.class);

        JavaType genericSuperclass = classes.get(Child.class).getSuperclass().get();

        assertThatType(genericSuperclass).hasActualTypeArguments(
                parameterizedType(List.class).withTypeArguments(
                        genericArray(typeVariableArrayName("X", 1)).withComponentType(
                                typeVariable("X").withUpperBounds(Serializable.class))),
                parameterizedType(List.class).withWildcardTypeParameterWithUpperBound(
                        genericArray(typeVariableArrayName("X", 2)).withComponentType(
                                genericArray(typeVariableArrayName("X", 1)).withComponentType(
                                        typeVariable("X").withUpperBounds(Serializable.class)))),
                parameterizedType(Map.class).withTypeArguments(
                        wildcardType().withLowerBound(
                                genericArray(typeVariableArrayName("Y", 1)).withComponentType(
                                        typeVariable("Y").withUpperBounds(String.class))),
                        parameterizedType(Map.class).withTypeArguments(
                                parameterizedType(Map.class).withTypeArguments(
                                        wildcardType().withLowerBound(
                                                genericArray(typeVariableArrayName("Y", 3)).withComponentType(
                                                        genericArray(typeVariableArrayName("Y", 2)).withComponentType(
                                                                genericArray(typeVariableArrayName("Y", 1)).withComponentType(
                                                                        typeVariable("Y").withUpperBounds(String.class))))),
                                        wildcardType()),
                                genericArray(typeVariableArrayName("X", 2)).withComponentType(
                                        genericArray(typeVariableArrayName("X", 1)).withComponentType(
                                                typeVariable("X").withUpperBounds(Serializable.class)))))
        );
    }

    @SuppressWarnings("unused")
    public static class ClassParameterWithSingleTypeParameter<T> {
    }

    @SuppressWarnings("unused")
    public interface InterfaceParameterWithSingleTypeParameter<T> {
    }
}
