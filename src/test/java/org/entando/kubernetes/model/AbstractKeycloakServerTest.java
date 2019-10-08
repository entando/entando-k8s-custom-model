package org.entando.kubernetes.model;

import static org.entando.kubernetes.model.keycloakserver.KeycloakServerOperationFactory.produceAllKeycloakServers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.fabric8.kubernetes.client.dsl.internal.CustomResourceOperationsImpl;
import org.entando.kubernetes.model.keycloakserver.DoneableKeycloakServer;
import org.entando.kubernetes.model.keycloakserver.KeycloakServer;
import org.entando.kubernetes.model.keycloakserver.KeycloakServerBuilder;
import org.entando.kubernetes.model.keycloakserver.KeycloakServerList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractKeycloakServerTest implements CustomResourceTestUtil {

    protected static final String MY_NAMESPACE = "my-namespace";
    protected static final String MY_KEYCLOAK = "my-keycloak";

    private static final String SNAPSHOT = "6.1.0-SNAPSHOT";
    private static final String ENTANDO_SOMEKEYCLOAK = "entando/somekeycloak";
    private static final String MYHOST_COM = "myhost.com";
    private static final String MY_TLS_SECRET = "my-tls-secret";

    @BeforeEach
    public void deleteKeycloakServer() throws InterruptedException {
        prepareNamespace(keycloakServers(), MY_NAMESPACE);
    }

    @Test
    public void testCreateKeycloakServer() throws InterruptedException {
        //Given
        KeycloakServer keycloakServer = new KeycloakServerBuilder()
                .withNewMetadata().withName(MY_KEYCLOAK)
                .withNamespace(MY_NAMESPACE)
                .endMetadata()
                .withNewSpec()
                .withDbms(DbmsImageVendor.MYSQL)
                .withEntandoImageVersion(SNAPSHOT)
                .withImageName(ENTANDO_SOMEKEYCLOAK)
                .withReplicas(5)
                .withDefault(true)
                .withIngressHostName(MYHOST_COM)
                .withTlsSecretName(MY_TLS_SECRET)
                .endSpec()
                .build();
        getClient().namespaces().createOrReplaceWithNew().withNewMetadata().withName(MY_NAMESPACE).endMetadata().done();
        keycloakServers().inNamespace(MY_NAMESPACE).create(keycloakServer);
        //When
        KeycloakServerList list = keycloakServers().inNamespace(MY_NAMESPACE).list();
        KeycloakServer actual = list.getItems().get(0);
        //Then
        assertThat(actual.getSpec().getDbms().get(), is(DbmsImageVendor.MYSQL));
        assertThat(actual.getSpec().getEntandoImageVersion().get(), is(SNAPSHOT));
        assertThat(actual.getSpec().getImageName().get(), is(ENTANDO_SOMEKEYCLOAK));
        assertThat(actual.getSpec().getIngressHostName().get(), is(MYHOST_COM));
        assertThat(actual.getSpec().getReplicas(), is(5));
        assertThat(actual.getSpec().getTlsSecretName().get(), is(MY_TLS_SECRET));
        assertThat(actual.getSpec().isDefault(), is(true));
        assertThat(actual.getMetadata().getName(), is(MY_KEYCLOAK));
    }

    @Test
    public void testEditKeycloakServer() throws InterruptedException {
        //Given
        KeycloakServer keycloakServer = new KeycloakServerBuilder()
                .withNewMetadata()
                .withName(MY_KEYCLOAK)
                .withNamespace(MY_NAMESPACE)
                .endMetadata()
                .withNewSpec()
                .withDbms(DbmsImageVendor.POSTGRESQL)
                .withEntandoImageVersion("6.2.0-SNAPSHOT")
                .withIngressHostName(MYHOST_COM)
                .withImageName("entando/anotherkeycloak")
                .withReplicas(3)
                .withTlsSecretName("some-othersecret")
                .withDefault(false)
                .endSpec()
                .build();
        getClient().namespaces().createOrReplaceWithNew().withNewMetadata().withName(MY_NAMESPACE).endMetadata().done();

        //When
        //We are not using the mock server here because of a known bug
        KeycloakServer actual = editKeycloakServer(keycloakServer)
                .editMetadata().addToLabels("my-label", "my-value")
                .endMetadata()
                .editSpec()
                .withDbms(DbmsImageVendor.MYSQL)
                .withEntandoImageVersion(SNAPSHOT)
                .withImageName(ENTANDO_SOMEKEYCLOAK)
                .withIngressHostName(MYHOST_COM)
                .withReplicas(5)
                .withDefault(true)
                .withTlsSecretName(MY_TLS_SECRET)
                .endSpec()
                .withStatus(new WebServerStatus("some-qualifier"))
                .withStatus(new WebServerStatus("some-other-qualifier"))
                .withStatus(new WebServerStatus("some-qualifier"))
                .withStatus(new DbServerStatus("another-qualifier"))
                .withPhase(EntandoDeploymentPhase.STARTED)
                .done();
        //Then
        assertThat(actual.getSpec().getDbms().get(), is(DbmsImageVendor.MYSQL));
        assertThat(actual.getSpec().getEntandoImageVersion().get(), is(SNAPSHOT));
        assertThat(actual.getSpec().getImageName().get(), is(ENTANDO_SOMEKEYCLOAK));
        assertThat(actual.getSpec().getIngressHostName().get(), is(MYHOST_COM));
        assertThat(actual.getSpec().getReplicas(), is(5));
        assertThat(actual.getSpec().getTlsSecretName().get(), is(MY_TLS_SECRET));
        assertThat(actual.getSpec().isDefault(), is(true));
        assertThat(actual.getMetadata().getName(), is(MY_KEYCLOAK));
        assertThat("the status reflects", actual.getStatus().forServerQualifiedBy("some-qualifier").isPresent());
        assertThat("the status reflects", actual.getStatus().forServerQualifiedBy("some-other-qualifier").isPresent());
        assertThat("the status reflects", actual.getStatus().forDbQualifiedBy("another-qualifier").isPresent());
    }

    protected abstract DoneableKeycloakServer editKeycloakServer(KeycloakServer keycloakServer) throws InterruptedException;

    protected CustomResourceOperationsImpl<KeycloakServer, KeycloakServerList, DoneableKeycloakServer> keycloakServers()
            throws InterruptedException {
        return produceAllKeycloakServers(getClient());
    }
}