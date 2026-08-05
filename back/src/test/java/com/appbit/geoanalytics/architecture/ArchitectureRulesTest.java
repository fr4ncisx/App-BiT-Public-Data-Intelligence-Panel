package com.appbit.geoanalytics.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.appbit.geoanalytics", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {

    private static final String DOMAIN = "com.appbit.geoanalytics.domain..";
    private static final String APPLICATION = "com.appbit.geoanalytics.application..";
    private static final String INFRASTRUCTURE = "com.appbit.geoanalytics.infrastructure..";

    private static final String OUT_PORTS =
            "com.appbit.geoanalytics.application..out..";
    private static final String PORT_PACKAGES =
            "com.appbit.geoanalytics.application..port..";

    @ArchTest
    static final ArchRule domainDoesNotDependOnApplicationOrInfrastructure =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAnyPackage(APPLICATION, INFRASTRUCTURE);

    @ArchTest
    static final ArchRule applicationDoesNotDependOnInfrastructure =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE);

    @ArchTest
    static final ArchRule noThreadSleepInApplicationLayer =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().callMethod(Thread.class, "sleep", long.class)
                    .because("ingestion retries must be non-blocking in the application layer");

    @ArchTest
    static final ArchRule outPortImplementationsResideInInfrastructure =
            classes().that().implement(resideInAnyPackage(OUT_PORTS, PORT_PACKAGES))
                    .should().resideInAPackage(INFRASTRUCTURE)
                    .because("outbound ports must be implemented by infrastructure adapters");

    @ArchTest
    static final ArchRule cleanArchitectureLayers =
            Architectures.layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("domain").definedBy(DOMAIN)
                    .layer("application").definedBy(APPLICATION)
                    .layer("infrastructure").definedBy(INFRASTRUCTURE)
                    .whereLayer("domain").mayOnlyBeAccessedByLayers("application", "infrastructure")
                    .whereLayer("application").mayOnlyBeAccessedByLayers("infrastructure")
                    .whereLayer("infrastructure").mayNotBeAccessedByAnyLayer();
}