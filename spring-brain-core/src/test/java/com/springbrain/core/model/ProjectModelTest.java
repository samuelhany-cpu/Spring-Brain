package com.springbrain.core.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectModelTest {

    @Test
    void builderCreatesEmptyProjectModel() {
        Path root = Path.of("/tmp/sample-app");
        ProjectModel model = ProjectModel.builder(root).build();

        assertThat(model.getRootPath()).isEqualTo(root);
        assertThat(model.getControllers()).isEmpty();
        assertThat(model.getServices()).isEmpty();
        assertThat(model.getRepositories()).isEmpty();
        assertThat(model.getEntities()).isEmpty();
        assertThat(model.getConfigPropertyUsages()).isEmpty();
    }

    @Test
    void builderPreservesRootPath() {
        Path root = Path.of("/tmp/my-spring-app");
        ProjectModel model = ProjectModel.builder(root).build();

        assertThat(model.getRootPath()).isEqualTo(root);
    }
}
