package com.scorch.core.modules;

/**
 * The priority the modules are loaded at.
 * The order is from {@link ModulePriority#HIGHEST} to {@link ModulePriority#LOWEST}
 *
 * So modules that are registered with a ModulePriority of {@link ModulePriority#LOWEST} will be initialised last.
 *
 * @see AbstractModule#initialize()
 */
public enum ModulePriority {
    HIGHEST, HIGH, MEDIUM, LOW, LOWEST;
}
