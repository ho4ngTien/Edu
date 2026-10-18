package com.edu.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class SharedKernelArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter().importPackages("com.edu");
    }

    @Test
    @DisplayName("AR-01: Shared Kernel khong duoc phu thuoc vao cac Business Modules")
    void sharedKernelMustNotDependOnBusinessModules() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.edu.shared..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.edu.identity..",
                        "com.edu.organization..",
                        "com.edu.people..",
                        "com.edu.curriculum..",
                        "com.edu.academic..",
                        "com.edu.enrollment..",
                        "com.edu.teaching..",
                        "com.edu.attendance..",
                        "com.edu.assessment..",
                        "com.edu.ruleengine..",
                        "com.edu.configuration..",
                        "com.edu.certification.."
                )
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("AR-05: Domain Events khong duoc phu thuoc vao JPA Entities")
    void domainEventsMustNotDependOnJpa() {
        ArchRule rule = noClasses()
                .that().implement("com.edu.shared.kernel.domain.event.DomainEvent")
                .should().dependOnClassesThat()
                .resideInAPackage("jakarta.persistence..")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }
}
