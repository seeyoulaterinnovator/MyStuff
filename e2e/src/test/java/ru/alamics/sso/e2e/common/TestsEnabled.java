package ru.alamics.sso.e2e.common;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnabledIfEnvironmentVariable(named = Tests.CONDITION_VARIABLE, matches = "true")
public @interface TestsEnabled {}
