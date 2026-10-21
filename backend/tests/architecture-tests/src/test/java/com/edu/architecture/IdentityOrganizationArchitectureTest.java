package com.edu.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class IdentityOrganizationArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter().importPackages("com.edu");
    }

    @Test
    @DisplayName("AR-07: NoCrossSchemaDirectEntityAccess - organization must not depend on identity persistence")
    void organizationMustNotDependOnIdentityPersistence() {
        noClasses()
                .that().resideInAPackage("com.edu.organization..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.edu.identity.infrastructure.persistence.entity..",
                        "com.edu.identity.infrastructure.persistence.repository.."
                )
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    @DisplayName("AR-08: PublicPortDependencyOnly - organization must not depend on identity internal implementation")
    void organizationMustNotDependOnIdentity() {
        noClasses()
                .that().resideInAPackage("com.edu.organization..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.edu.identity..")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    @DisplayName("AR-09: NoPasswordLeakageInDTOs - identity DTOs must not contain passwordHash or rawPassword fields")
    void identityDtosMustNotLeakPasswordFields() {
        fields()
                .that().areDeclaredInClassesThat().resideInAPackage("com.edu.identity.api.dto..")
                .should().notHaveName("passwordHash")
                .andShould().notHaveName("rawPassword")
                .allowEmptyShould(true)
                .check(importedClasses);
    }
}
