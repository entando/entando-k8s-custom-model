/*
 *
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 */

package org.entando.kubernetes.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.client.CustomResourceList;
import io.fabric8.kubernetes.client.dsl.internal.CustomResourceOperationsImpl;
import java.util.Collections;
import org.entando.kubernetes.model.app.DoneableEntandoApp;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.app.EntandoAppBuilder;
import org.entando.kubernetes.model.gitspec.GitResponsibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//Sonar doesn't pick up that this class is extended in other packages
@SuppressWarnings("java:S5786")
public abstract class AbstractEntandoAppTest implements CustomResourceTestUtil {

    public static final String MY_CUSTOM_SERVER_IMAGE = "somenamespace/someimage:3.2.2";
    public static final String MY_CLUSTER_INFRASTRUCTURE = "my-cluster-infrastructure";
    public static final String MY_CLUSTER_INFRASTRUCTURE_NAMESPACE = "my-cluster-infrastructure-namespace";
    private static final String MY_KEYCLOAK_NAME = "my-keycloak-name";
    private static final String MY_KEYCLOAK_REALM = "my-keycloak-realm";
    private static final String MY_KEYCLOAK_NAME_SPACE = "my-keycloak-namespace";
    public static final String PARAM_VALUE = "my-value";
    public static final String PARAM_NAME = "MY_PARAM";
    public static final String MY_GIT_SECRET = "my-git-secret";
    public static final String MY_BACKUP_GIT_REPO = "https://github.com/entando/doesnoexist.git";
    protected static final String MY_APP = "my-app";
    protected static final String MY_NAMESPACE = TestConfig.calculateNameSpace("my-namespace");
    private static final String ENTANDO_IMAGE_VERSION = "6.1.0-SNAPSHOT";
    private static final String MYINGRESS_COM = "myingress.com";
    private static final String MY_INGRESS_PATH = "/my-ingress-path";
    private static final String MY_VALUE = "my-value";
    private static final String MY_LABEL = "my-label";
    private static final String MY_TLS_SECRET = "my-tls-secret";
    private static final Integer MY_REPLICAS = 5;
    public static final String MY_SERVICE_ACCOUNT = "my-service-account";
    public static final String CPU_LIMIT = "100m";
    public static final String CPU_REQUEST = "10m";
    public static final String FILE_UPLOAD_LIMIT = "10000mb";
    public static final String MEMORY_LIMIT = "1Gi";
    public static final String MEMORY_REQUEST = "0.1gi";
    public static final String STORAGE_LIMIT = "2Gi";
    public static final String STORAGE_REQUEST = "0.2Gi";
    private static final String MY_GIT_SECRET_NAME = "my-git-secret-name";
    public static final String MY_PUBLIC_CLIENT = "my-public-client";
    private EntandoResourceOperationsRegistry registry;

    @BeforeEach
    public void deleteEntandoApps() {
        this.registry = new EntandoResourceOperationsRegistry(getClient());
        prepareNamespace(entandoApps(), MY_NAMESPACE);
    }

    @Test
    void testCreateEntandoApp() {
        //Given
        EntandoApp entandoApp = new EntandoAppBuilder()
                .withNewMetadata().withName(MY_APP)
                .withNamespace(MY_NAMESPACE)
                .endMetadata()
                .withNewSpec()
                .withDbms(DbmsVendor.MYSQL)
                .withCustomServerImage(MY_CUSTOM_SERVER_IMAGE)
                .withStandardServerImage(JeeServer.WILDFLY)
                .withReplicas(MY_REPLICAS)
                .withTlsSecretName(MY_TLS_SECRET)
                .withIngressHostName(MYINGRESS_COM)
                .withEcrGitSshSecretname(MY_GIT_SECRET_NAME)
                .withClusterInfrastructureToUse(MY_CLUSTER_INFRASTRUCTURE_NAMESPACE, MY_CLUSTER_INFRASTRUCTURE)
                .withIngressPath(MY_INGRESS_PATH)
                .withNewBackupGitSpec()
                .withRepository(MY_BACKUP_GIT_REPO)
                .withSecretName(MY_GIT_SECRET)
                .withResponsibility(GitResponsibility.PUSH)
                .withTargertRef("master")
                .endBackupGitSpec()
                .withNewResourceRequirements()
                .withCpuLimit(CPU_LIMIT)
                .withCpuRequest(CPU_REQUEST)
                .withFileUploadLimit(FILE_UPLOAD_LIMIT)
                .withMemoryLimit(MEMORY_LIMIT)
                .withMemoryRequest(MEMORY_REQUEST)
                .withStorageLimit(STORAGE_LIMIT)
                .withStorageRequest(STORAGE_REQUEST)
                .endResourceRequirements()
                .withIngressPath(MY_INGRESS_PATH)
                .addToEnvironmentVariables(PARAM_NAME, PARAM_VALUE)
                .withNewKeycloakToUse()
                .withNamespace(MY_KEYCLOAK_NAME_SPACE)
                .withName(MY_KEYCLOAK_NAME)
                .withRealm(MY_KEYCLOAK_REALM)
                .withPublicClientId(MY_PUBLIC_CLIENT)
                .endKeycloakToUse()
                .endSpec()
                .build();

        entandoApps().inNamespace(MY_NAMESPACE).createNew().withMetadata(entandoApp.getMetadata()).withSpec(entandoApp.getSpec()).done();
        //When
        EntandoApp actual = entandoApps().inNamespace(MY_NAMESPACE).withName(MY_APP).get();
        //Then
        assertThat(actual.getSpec().getDbms().get(), is(DbmsVendor.MYSQL));
        assertThat(actual.getSpec().getIngressHostName().get(), is(MYINGRESS_COM));
        verifyKeycloakToUSer(actual);
        assertThat(actual.getTlsSecretName().get(), is(MY_TLS_SECRET));
        assertThat(actual.getIngressHostName().get(), is(MYINGRESS_COM));
        assertThat(actual.getSpec().getIngressPath().get(), is(MY_INGRESS_PATH));
        assertThat(actual.getSpec().getStandardServerImage().get(), is(JeeServer.WILDFLY));
        assertThat(actual.getSpec().getReplicas().get(), is(5));
        assertThat(actual.getSpec().getTlsSecretName().get(), is(MY_TLS_SECRET));
        assertThat(actual.getSpec().getCustomServerImage().isPresent(), is(false));//because it was overridden by a standard image
        verifyClusterInfrastructureToUse(actual.getSpec().getClusterInfrastructureToUse().get(), MY_CLUSTER_INFRASTRUCTURE,
                MY_CLUSTER_INFRASTRUCTURE_NAMESPACE);
        verifyBackupGitSpec(actual);
        assertThat(actual.getSpec().getEcrGitSshSecretName().get(), is(MY_GIT_SECRET_NAME));
        verifyResourceRequirements(actual);
        assertThat(findParameter(actual.getSpec(), PARAM_NAME).get().getValue(), is(PARAM_VALUE));
        assertThat(actual.getMetadata().getName(), is(MY_APP));
    }

    private void verifyBackupGitSpec(EntandoApp actual) {
        assertThat(actual.getSpec().getBackupGitSpec().get().getRepository(), is(MY_BACKUP_GIT_REPO));
        assertThat(actual.getSpec().getBackupGitSpec().get().getSecretName().get(), is(MY_GIT_SECRET));
        assertThat(actual.getSpec().getBackupGitSpec().get().getResponsibility(), is(GitResponsibility.PUSH));
        assertThat(actual.getSpec().getBackupGitSpec().get().getTargetRef().get(), is("master"));
    }

    private void verifyResourceRequirements(EntandoApp actual) {
        assertThat(actual.getSpec().getResourceRequirements().get().getCpuLimit().get(), is(CPU_LIMIT));
        assertThat(actual.getSpec().getResourceRequirements().get().getCpuRequest().get(), is(CPU_REQUEST));
        assertThat(actual.getSpec().getResourceRequirements().get().getFileUploadLimit().get(), is(FILE_UPLOAD_LIMIT));
        assertThat(actual.getSpec().getResourceRequirements().get().getMemoryLimit().get(), is(MEMORY_LIMIT));
        assertThat(actual.getSpec().getResourceRequirements().get().getMemoryRequest().get(), is(MEMORY_REQUEST));
        assertThat(actual.getSpec().getResourceRequirements().get().getStorageLimit().get(), is(STORAGE_LIMIT));
        assertThat(actual.getSpec().getResourceRequirements().get().getStorageRequest().get(), is(STORAGE_REQUEST));
    }

    @Test
    void testEditEntandoApp() {
        //Given
        EntandoApp entandoApp = new EntandoAppBuilder()
                .withNewMetadata()
                .withName(MY_APP)
                .withNamespace(MY_NAMESPACE)
                .endMetadata()
                .withNewSpec()
                .withDbms(DbmsVendor.POSTGRESQL)
                .withCustomServerImage("asdfasdf/asdf:2")
                .withStandardServerImage(JeeServer.WILDFLY)
                .withReplicas(4)
                .withTlsSecretName("another-tls-secret")
                .withIngressHostName("anotheringress.com")
                .withNewKeycloakToUse()
                .withNamespace("somenamespace")
                .withName("another-keycloak")
                .withRealm("somerealm")
                .withPublicClientId("some-public-client")
                .endKeycloakToUse()
                .addToEnvironmentVariables("anotherparam", "123123")
                .withClusterInfrastructureToUse("thingy-namespace", "some-cluster-infrastructure")
                .withNewBackupGitSpec()
                .withRepository("somerip.git")
                .withSecretName("some-secert")
                .withResponsibility(GitResponsibility.PULL)
                .withTargertRef("pr1")
                .endBackupGitSpec()
                .withEcrGitSshSecretname("somesecret-that-doesnt-exst")
                .withNewResourceRequirements()
                .withCpuLimit("123")
                .withCpuRequest("123")
                .withFileUploadLimit("123")
                .withMemoryLimit("123")
                .withMemoryRequest("123")
                .withStorageLimit("123")
                .withStorageRequest("123")
                .endResourceRequirements()
                .withServiceAccountToUse("someserviceacount")
                .endSpec()
                .build();
        //When
        //We are not using the mock server here because of a known bug
        EntandoApp actual = editEntandoApp(entandoApp)
                .editMetadata().addToLabels(MY_LABEL, MY_VALUE)
                .endMetadata()
                .editSpec()
                .withDbms(DbmsVendor.MYSQL)
                .withStandardServerImage(JeeServer.WILDFLY)
                .withCustomServerImage(MY_CUSTOM_SERVER_IMAGE)
                .withReplicas(5)
                .withTlsSecretName(MY_TLS_SECRET)
                .withIngressHostName(MYINGRESS_COM)
                .editKeycloakToUse()
                .withNamespace(MY_KEYCLOAK_NAME_SPACE)
                .withName(MY_KEYCLOAK_NAME)
                .withRealm(MY_KEYCLOAK_REALM)
                .withPublicClientId(MY_PUBLIC_CLIENT)
                .endKeycloakToUse()
                .withClusterInfrastructureToUse(MY_CLUSTER_INFRASTRUCTURE_NAMESPACE, MY_CLUSTER_INFRASTRUCTURE)
                .editBackupGitSpec()
                .withRepository(MY_BACKUP_GIT_REPO)
                .withSecretName(MY_GIT_SECRET)
                .withResponsibility(GitResponsibility.PUSH)
                .withTargertRef("master")
                .endBackupGitSpec()
                .withEcrGitSshSecretname(MY_GIT_SECRET_NAME)
                .editResourceRequirements()
                .withCpuLimit(CPU_LIMIT)
                .withCpuRequest(CPU_REQUEST)
                .withFileUploadLimit(FILE_UPLOAD_LIMIT)
                .withMemoryLimit(MEMORY_LIMIT)
                .withMemoryRequest(MEMORY_REQUEST)
                .withStorageLimit(STORAGE_LIMIT)
                .withStorageRequest(STORAGE_REQUEST)
                .endResourceRequirements()
                .withServiceAccountToUse(MY_SERVICE_ACCOUNT)
                .withEnvironmentVariables(Collections.singletonList(new EnvVar(PARAM_NAME, PARAM_VALUE, null)))
                .endSpec()
                .done();
        //Then
        verifySpec(actual);
        verifyKeycloakToUSer(actual);
        verifyClusterInfrastructureToUse(actual.getSpec().getClusterInfrastructureToUse().get(), MY_CLUSTER_INFRASTRUCTURE,
                MY_CLUSTER_INFRASTRUCTURE_NAMESPACE);
        verifyBackupGitSpec(actual);
        verifyResourceRequirements(actual);
        assertThat(actual.getMetadata().getLabels().get(MY_LABEL), is(MY_VALUE));
    }

    private void verifySpec(EntandoApp actual) {
        assertThat(actual.getSpec().getDbms().get(), is(DbmsVendor.MYSQL));
        assertThat(actual.getSpec().getIngressHostName().get(), is(MYINGRESS_COM));
        assertThat(actual.getSpec().getStandardServerImage().isPresent(), is(false));//overridden by customServerImage
        assertThat(actual.getSpec().getCustomServerImage().get(), is(MY_CUSTOM_SERVER_IMAGE));
        assertThat(actual.getSpec().getReplicas().get(), is(5));
        assertThat(actual.getSpec().getTlsSecretName().get(), is(MY_TLS_SECRET));
        assertThat(actual.getSpec().getEcrGitSshSecretName().get(), is(MY_GIT_SECRET_NAME));
        assertThat(actual.getSpec().getServiceAccountToUse().get(), is(MY_SERVICE_ACCOUNT));
        assertThat(findParameter(actual.getSpec(), PARAM_NAME).get().getValue(), is(PARAM_VALUE));
    }

    private void verifyClusterInfrastructureToUse(ResourceReference resourceReference, String myClusterInfrastructure,
            String myClusterInfrastructureNamespace) {
        assertThat(resourceReference.getName(), is(myClusterInfrastructure));
        assertThat(resourceReference.getNamespace().get(), is(myClusterInfrastructureNamespace));
    }

    private void verifyKeycloakToUSer(EntandoApp actual) {
        verifyClusterInfrastructureToUse(actual.getSpec().getKeycloakToUse().get(), MY_KEYCLOAK_NAME, MY_KEYCLOAK_NAME_SPACE);
        assertThat(actual.getSpec().getKeycloakToUse().get().getRealm().get(), is(MY_KEYCLOAK_REALM));
    }

    protected DoneableEntandoApp editEntandoApp(EntandoApp entandoApp) {
        entandoApp.setApiVersion("entando.org/v1");
        entandoApps().inNamespace(MY_NAMESPACE).create(entandoApp);
        EntandoApp entandoApp1 = entandoApps().inNamespace(MY_NAMESPACE).withName(MY_APP).get();
        return entandoApps().inNamespace(MY_NAMESPACE).withName(MY_APP).edit();
    }

    protected CustomResourceOperationsImpl<EntandoApp, CustomResourceList<EntandoApp>, DoneableEntandoApp> entandoApps() {
        return registry.getOperations(EntandoApp.class);
    }

}
