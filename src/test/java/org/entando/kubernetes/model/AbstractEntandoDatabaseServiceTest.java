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
import static org.hamcrest.Matchers.nullValue;

import io.fabric8.kubernetes.client.CustomResourceList;
import io.fabric8.kubernetes.client.dsl.internal.CustomResourceOperationsImpl;
import java.util.Collections;
import org.entando.kubernetes.model.externaldatabase.DoneableEntandoDatabaseService;
import org.entando.kubernetes.model.externaldatabase.EntandoDatabaseService;
import org.entando.kubernetes.model.externaldatabase.EntandoDatabaseServiceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractEntandoDatabaseServiceTest implements CustomResourceTestUtil {

    public static final String MY_PARAM_VALUE = "my-param-value";
    public static final String MY_PARAM = "my-param";
    public static final String MY_TABLESPACE = "my_tablespace";
    protected static final String MY_EXTERNAL_DATABASE = "my-external-database";
    protected static final String MY_NAMESPACE = TestConfig.calculateNameSpace("my-namespace");
    private static final String MY_DB = "my_db";
    private static final String MYHOST_COM = "myhost.com";
    private static final int PORT_1521 = 1521;
    private static final String MY_DB_SECRET = "my-db-secret";
    private EntandoResourceOperationsRegistry registry;

    @BeforeEach
    public void deleteEntandoDatabaseService() {
        registry = new EntandoResourceOperationsRegistry(getClient());
        prepareNamespace(externalDatabases(), MY_NAMESPACE);
    }

    @Test
    public void testCreateEntandoDatabaseService() {
        //Given
        EntandoDatabaseService externalDatabase = new EntandoDatabaseServiceBuilder()
                .withNewMetadata().withName(MY_EXTERNAL_DATABASE)
                .withNamespace(MY_NAMESPACE)
                .endMetadata()
                .withNewSpec()
                .withDatabaseName(MY_DB)
                .withHost(MYHOST_COM)
                .withPort(PORT_1521)
                .withTablespace(MY_TABLESPACE)
                .withSecretName(MY_DB_SECRET)
                .addToJdbcParameters(MY_PARAM, MY_PARAM_VALUE)
                .withDbms(DbmsVendor.ORACLE)
                .withCreateDeployment(true)
                .endSpec()
                .build();
        externalDatabases().inNamespace(MY_NAMESPACE).createNew().withMetadata(externalDatabase.getMetadata())
                .withSpec(externalDatabase.getSpec()).done();
        //When
        EntandoDatabaseService actual = externalDatabases().inNamespace(MY_NAMESPACE).withName(MY_EXTERNAL_DATABASE).get();
        //Then
        assertThat(actual.getSpec().getDatabaseName().get(), is(MY_DB));
        assertThat(actual.getSpec().getHost().get(), is(MYHOST_COM));
        assertThat(actual.getSpec().getPort().get(), is(PORT_1521));
        assertThat(actual.getSpec().getDbms(), is(DbmsVendor.ORACLE));
        assertThat(actual.getSpec().getCreateDeployment().get(), is(true));
        assertThat(actual.getSpec().getTablespace().get(), is(MY_TABLESPACE));
        assertThat(actual.getSpec().getSecretName().get(), is(MY_DB_SECRET));
        assertThat(actual.getSpec().getJdbcParameters().get(MY_PARAM), is(MY_PARAM_VALUE));
        assertThat(actual.getMetadata().getName(), is(MY_EXTERNAL_DATABASE));
    }

    @Test
    public void testEditEntandoDatabaseService() {
        //Given
        EntandoDatabaseService externalDatabase = new EntandoDatabaseServiceBuilder()
                .withNewMetadata().withName(MY_EXTERNAL_DATABASE)
                .withNamespace(MY_NAMESPACE)
                .endMetadata()
                .withNewSpec()
                .withDatabaseName("other_db")
                .withHost("otherhost.com")
                .withTablespace(MY_TABLESPACE)
                .withJdbcParameters(Collections.singletonMap("asdfasdf", "afafafaf"))
                .withPort(5555)
                .withSecretName("othersecret")
                .withDbms(DbmsVendor.POSTGRESQL)
                .withCreateDeployment(false)
                .endSpec()
                .build();
        //When
        //We are not using the mock server here because of a known bug
        externalDatabases().inNamespace(MY_NAMESPACE).create(externalDatabase);
        DoneableEntandoDatabaseService doneableEntandoDatabaseService = externalDatabases().inNamespace(MY_NAMESPACE)
                .withName(MY_EXTERNAL_DATABASE).edit();
        EntandoDatabaseService actual = doneableEntandoDatabaseService
                .editMetadata().addToLabels("my-label", "my-value")
                .endMetadata()
                .editSpec()
                .withDatabaseName(MY_DB)
                .withHost(MYHOST_COM)
                .withPort(PORT_1521)
                .withTablespace(MY_TABLESPACE)
                .withJdbcParameters(Collections.singletonMap(MY_PARAM, MY_PARAM_VALUE))
                .withSecretName(MY_DB_SECRET)
                .withCreateDeployment(true)
                .withDbms(DbmsVendor.ORACLE)
                .endSpec()
                .withStatus(new WebServerStatus("some-qualifier"))
                .withStatus(new DbServerStatus("another-qualifier"))
                .withPhase(EntandoDeploymentPhase.STARTED)
                .done();
        //Then
        assertThat(actual.getSpec().getDatabaseName().get(), is(MY_DB));
        assertThat(actual.getSpec().getHost().get(), is(MYHOST_COM));
        assertThat(actual.getSpec().getPort().get(), is(PORT_1521));
        assertThat(actual.getSpec().getDbms(), is(DbmsVendor.ORACLE));
        assertThat(actual.getSpec().getCreateDeployment().get(), is(true));
        assertThat(actual.getSpec().getJdbcParameters().get(MY_PARAM), is(MY_PARAM_VALUE));
        assertThat(actual.getSpec().getJdbcParameters().get("asdfasdf"), is(nullValue()));
        assertThat(actual.getSpec().getSecretName().get(), is(MY_DB_SECRET));
        assertThat(actual.getSpec().getTablespace().get(), is(MY_TABLESPACE));
        assertThat(actual.getMetadata().getLabels().get("my-label"), is("my-value"));
        assertThat("the status reflects", actual.getStatus().forServerQualifiedBy("some-qualifier").isPresent());
        assertThat("the status reflects", actual.getStatus().forDbQualifiedBy("another-qualifier").isPresent());
    }

    protected CustomResourceOperationsImpl<
            EntandoDatabaseService,
            CustomResourceList<EntandoDatabaseService>,
            DoneableEntandoDatabaseService> externalDatabases() {
        return registry.getOperations(EntandoDatabaseService.class);
    }
}