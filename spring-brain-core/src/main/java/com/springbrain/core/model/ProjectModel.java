package com.springbrain.core.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public final class ProjectModel {

    private final Path rootPath;
    private final List<ControllerModel> controllers;
    private final List<ServiceModel> services;
    private final List<RepositoryModel> repositories;
    private final List<EntityModel> entities;
    private final List<ConfigPropertyUsageModel> configPropertyUsages;
    private final List<BeanModel> beans;
    private final List<SecurityAnnotationModel> securityAnnotations;
    private final List<SecurityRuleModel> securityRules;
    private final Set<String> definedConfigKeys;

    private ProjectModel(Builder builder) {
        this.rootPath = builder.rootPath;
        this.controllers = List.copyOf(builder.controllers);
        this.services = List.copyOf(builder.services);
        this.repositories = List.copyOf(builder.repositories);
        this.entities = List.copyOf(builder.entities);
        this.configPropertyUsages = List.copyOf(builder.configPropertyUsages);
        this.beans = List.copyOf(builder.beans);
        this.securityAnnotations = List.copyOf(builder.securityAnnotations);
        this.securityRules = List.copyOf(builder.securityRules);
        this.definedConfigKeys = Set.copyOf(builder.definedConfigKeys);
    }

    public Path getRootPath() { return rootPath; }
    public List<ControllerModel> getControllers() { return controllers; }
    public List<ServiceModel> getServices() { return services; }
    public List<RepositoryModel> getRepositories() { return repositories; }
    public List<EntityModel> getEntities() { return entities; }
    public List<ConfigPropertyUsageModel> getConfigPropertyUsages() { return configPropertyUsages; }
    public List<BeanModel> getBeans() { return beans; }
    public List<SecurityAnnotationModel> getSecurityAnnotations() { return securityAnnotations; }
    public List<SecurityRuleModel> getSecurityRules() { return securityRules; }
    public Set<String> getDefinedConfigKeys() { return definedConfigKeys; }

    public static Builder builder(Path rootPath) {
        return new Builder(rootPath);
    }

    public static final class Builder {
        private final Path rootPath;
        private List<ControllerModel> controllers = List.of();
        private List<ServiceModel> services = List.of();
        private List<RepositoryModel> repositories = List.of();
        private List<EntityModel> entities = List.of();
        private List<ConfigPropertyUsageModel> configPropertyUsages = List.of();
        private List<BeanModel> beans = List.of();
        private List<SecurityAnnotationModel> securityAnnotations = List.of();
        private List<SecurityRuleModel> securityRules = List.of();
        private Set<String> definedConfigKeys = Set.of();

        private Builder(Path rootPath) {
            this.rootPath = rootPath;
        }

        public Builder controllers(List<ControllerModel> controllers) {
            this.controllers = controllers; return this;
        }
        public Builder services(List<ServiceModel> services) {
            this.services = services; return this;
        }
        public Builder repositories(List<RepositoryModel> repositories) {
            this.repositories = repositories; return this;
        }
        public Builder entities(List<EntityModel> entities) {
            this.entities = entities; return this;
        }
        public Builder configPropertyUsages(List<ConfigPropertyUsageModel> configPropertyUsages) {
            this.configPropertyUsages = configPropertyUsages; return this;
        }
        public Builder beans(List<BeanModel> beans) {
            this.beans = beans; return this;
        }
        public Builder securityAnnotations(List<SecurityAnnotationModel> securityAnnotations) {
            this.securityAnnotations = securityAnnotations; return this;
        }
        public Builder securityRules(List<SecurityRuleModel> securityRules) {
            this.securityRules = securityRules; return this;
        }
        public Builder definedConfigKeys(Set<String> definedConfigKeys) {
            this.definedConfigKeys = definedConfigKeys; return this;
        }

        public ProjectModel build() {
            return new ProjectModel(this);
        }
    }
}
