package com.springbrain.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringBrainVersionTest {

    @Test
    void versionIsNotBlank() {
        assertThat(SpringBrainVersion.VERSION).isNotBlank();
    }

    @Test
    void schemaVersionIsNotBlank() {
        assertThat(SpringBrainVersion.SCHEMA_VERSION).isNotBlank();
    }

    @Test
    void toolNameIsSpringBrain() {
        assertThat(SpringBrainVersion.TOOL_NAME).isEqualTo("spring-brain");
    }
}
