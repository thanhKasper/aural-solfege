package vn.ktt.eartraining.application.outbound;

public interface ICurrentUserPort {
    /** Id (Keycloak {@code sub}) of the authenticated user. Fails if nobody is authenticated. */
    String userId();
}
