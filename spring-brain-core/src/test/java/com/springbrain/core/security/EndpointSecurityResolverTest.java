package com.springbrain.core.security;

import com.springbrain.core.model.ControllerModel;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.model.RouteModel;
import com.springbrain.core.model.SecurityAnnotationModel;
import com.springbrain.core.model.SecurityRuleModel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EndpointSecurityResolverTest {

    private static final Path FILE = Path.of("src/main/java/com/example/SampleController.java");

    @Test
    void methodLevelAnnotationResolvesEndpointAsProtected() {
        ProjectModel model = modelWith(
                List.of(route("GET", "/admin/users", "adminUsers")),
                List.of(annotation("adminUsers", "hasRole('ADMIN')")),
                List.of(rule("/admin/**", "PROTECTED", "authenticated")));

        List<EndpointSecurityModel> endpoints = EndpointSecurityResolver.resolve(model);

        assertThat(endpoints).singleElement().satisfies(endpoint -> {
            assertThat(endpoint.getAccess()).isEqualTo("PROTECTED");
            assertThat(endpoint.getSource()).isEqualTo("method_annotation");
            assertThat(endpoint.getDetail()).contains("hasRole('ADMIN')");
        });
    }

    @Test
    void classLevelAnnotationAppliesWhenMethodAnnotationIsAbsent() {
        ProjectModel model = modelWith(
                List.of(route("GET", "/admin/reports", "reports")),
                List.of(new SecurityAnnotationModel(
                        "com.example.SampleController", "", "PreAuthorize",
                        "hasRole('ADMIN')", FILE, 4)),
                List.of());

        List<EndpointSecurityModel> endpoints = EndpointSecurityResolver.resolve(model);

        assertThat(endpoints).singleElement().satisfies(endpoint -> {
            assertThat(endpoint.getAccess()).isEqualTo("PROTECTED");
            assertThat(endpoint.getSource()).isEqualTo("class_annotation");
        });
    }

    @Test
    void permitAllRuleResolvesEndpointAsPublic() {
        ProjectModel model = modelWith(
                List.of(route("GET", "/login", "login")),
                List.of(),
                List.of(rule("/login", "PUBLIC", "permitAll")));

        assertThat(EndpointSecurityResolver.resolve(model))
                .singleElement()
                .extracting(EndpointSecurityModel::getAccess, EndpointSecurityModel::getSource)
                .containsExactly("PUBLIC", "security_filter_chain");
    }

    @Test
    void authenticatedAndRoleRulesResolveEndpointAsProtected() {
        ProjectModel model = modelWith(
                List.of(
                        route("GET", "/admin/dashboard", "adminDashboard"),
                        route("GET", "/client/profile", "clientProfile")),
                List.of(),
                List.of(
                        rule("/admin/**", "PROTECTED", "authenticated"),
                        rule("/client/**", "PROTECTED", "hasRole(\"CLIENT\")")));

        assertThat(EndpointSecurityResolver.resolve(model))
                .extracting(EndpointSecurityModel::getPath, EndpointSecurityModel::getAccess)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("/admin/dashboard", "PROTECTED"),
                        org.assertj.core.groups.Tuple.tuple("/client/profile", "PROTECTED"));
    }

    @Test
    void unmatchedEndpointResolvesAsUnknown() {
        ProjectModel model = modelWith(
                List.of(route("GET", "/health", "health")),
                List.of(),
                List.of(rule("/admin/**", "PROTECTED", "authenticated")));

        assertThat(EndpointSecurityResolver.resolve(model))
                .singleElement()
                .extracting(EndpointSecurityModel::getAccess, EndpointSecurityModel::getSource)
                .containsExactly("UNKNOWN", "none");
    }

    private ProjectModel modelWith(List<RouteModel> routes,
                                   List<SecurityAnnotationModel> annotations,
                                   List<SecurityRuleModel> rules) {
        return ProjectModel.builder(Path.of("."))
                .controllers(List.of(new ControllerModel(
                        "SampleController",
                        "com.example",
                        "com.example.SampleController",
                        FILE,
                        1,
                        routes,
                        List.of())))
                .securityAnnotations(annotations)
                .securityRules(rules)
                .build();
    }

    private RouteModel route(String httpMethod, String path, String methodName) {
        return new RouteModel(httpMethod, path, "com.example.SampleController", methodName, FILE, 10);
    }

    private SecurityAnnotationModel annotation(String methodName, String expression) {
        return new SecurityAnnotationModel(
                "com.example.SampleController",
                methodName,
                "PreAuthorize",
                expression,
                FILE,
                5);
    }

    private SecurityRuleModel rule(String pathPattern, String accessType, String detail) {
        return new SecurityRuleModel(pathPattern, accessType, "SecurityFilterChain", detail, FILE, 20);
    }
}
