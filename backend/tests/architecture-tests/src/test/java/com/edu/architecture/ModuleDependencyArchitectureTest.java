package com.edu.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class ModuleDependencyArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter().importPackages("com.edu");
    }

    @Test
    @DisplayName("AR-02: Khong duoc co chu trinh phu thuoc vong (cycles) giua cac modules")
    void modulesMustBeFreeOfCycles() {
        slices()
                .matching("com.edu.(*)..")
                .should()
                .beFreeOfCycles()
                .check(importedClasses);
    }
}
