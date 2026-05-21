package com.springbrain.core.diagnostic.rules;

import com.springbrain.core.diagnostic.Diagnostic;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
import com.springbrain.core.graph.GraphBuilder;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ControllerModel;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.model.RouteModel;
import com.springbrain.core.model.SecurityRuleModel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PublicRiskyEndpointRuleTest {

    private static final Path FILE = Path.of("src/main/java/com/example/AuthController.java");

    @Test
    void flagsPublicMutatingEndpoint() {
        ProjectModel model = modelWith(
                route("POST", "/signup", "signup"),
                rule("/signup", "PUBLIC", "permitAll"));

        List<Diagnostic> diagnostics = analyze(model);

        assertThat(diagnostics).singleElement().satisfies(diagnostic -> {
            assertThat(diagnostic.code()).isEqualTo("SPRING_BRAIN_PUBLIC_RISKY_ENDPOINT");
            assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.WARNING);
            assertThat(diagnostic.message()).contains("POST /signup").contains("public");
            assertThat(diagnostic.relatedNodeIds()).containsExactly("route:POST:/signup");
            assertThat(diagnostic.suggestedFixes()).anyMatch(fix -> fix.contains("authentication"));
        });
    }

    @Test
    void ignoresPublicReadOnlyEndpoint() {
        ProjectModel model = modelWith(
                route("GET", "/login", "login"),
                rule("/login", "PUBLIC", "permitAll"));

        assertThat(analyze(model)).isEmpty();
    }

    @Test
    void ignoresProtectedMutatingEndpoint() {
        ProjectModel model = modelWith(
                route("POST", "/admin/tours", "createTour"),
                rule("/admin/**", "PROTECTED", "authenticated"));

        assertThat(analyze(model)).isEmpty();
    }

    private List<Diagnostic> analyze(ProjectModel model) {
        GraphDocument graph = GraphBuilder.build(model, "test", Instant.EPOCH);
        return new PublicRiskyEndpointRule().analyze(model, graph);
    }

    private ProjectModel modelWith(RouteModel route, SecurityRuleModel rule) {
        return ProjectModel.builder(Path.of("."))
                .controllers(List.of(new ControllerModel(
                        "AuthController",
                        "com.example",
                        "com.example.AuthController",
                        FILE,
                        1,
                        List.of(route),
                        List.of())))
                .securityRules(List.of(rule))
                .build();
    }

    private RouteModel route(String httpMethod, String path, String methodName) {
        return new RouteModel(httpMethod, path, "com.example.AuthController", methodName, FILE, 10);
    }

    private SecurityRuleModel rule(String pathPattern, String accessType, String detail) {
        return new SecurityRuleModel(pathPattern, accessType, "SecurityFilterChain", detail, FILE, 5);
    }
}
