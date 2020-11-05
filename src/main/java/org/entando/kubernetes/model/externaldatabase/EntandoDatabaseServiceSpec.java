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

package org.entando.kubernetes.model.externaldatabase;

import static org.entando.kubernetes.model.Coalescence.coalesce;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.entando.kubernetes.model.DbmsVendor;
import org.entando.kubernetes.model.EntandoDeploymentSpec;

@JsonInclude(Include.NON_NULL)
@JsonSerialize
@JsonDeserialize
@JsonAutoDetect(fieldVisibility = Visibility.ANY, isGetterVisibility = Visibility.NONE, getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE)
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntandoDatabaseServiceSpec extends EntandoDeploymentSpec {

    private DbmsVendor dbms;
    private String host;
    private Integer port;
    private String databaseName;
    private String tablespace;
    private String secretName;
    private Boolean createDeployment;
    private Map<String, String> jdbcParameters = new ConcurrentHashMap<>();

    public EntandoDatabaseServiceSpec() {

    }

    @JsonCreator
    public EntandoDatabaseServiceSpec(
            @JsonProperty("dbms") DbmsVendor dbms,
            @JsonProperty("host") String host,
            @JsonProperty("port") Integer port,
            @JsonProperty("databaseName") String databaseName,
            @JsonProperty("tablespace") String tablespace,
            @JsonProperty("secretName") String secretName,
            @JsonProperty("createDeployment") Boolean createDeployment,
            @JsonProperty("jdbcParameters") Map<String, String> jdbcParameters) {
        super();
        this.dbms = dbms;
        this.host = host;
        this.tablespace = tablespace;
        this.secretName = secretName;
        this.port = port;
        this.databaseName = databaseName;
        this.createDeployment = createDeployment;
        this.jdbcParameters = coalesce(jdbcParameters, this.jdbcParameters);
    }

    public DbmsVendor getDbms() {
        return dbms;
    }

    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    public Optional<String> getSecretName() {
        return Optional.ofNullable(secretName);
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(port);
    }

    public Optional<String> getDatabaseName() {
        return Optional.ofNullable(databaseName);
    }

    public Map<String, String> getJdbcParameters() {
        return jdbcParameters;
    }

    public Optional<String> getTablespace() {
        return Optional.ofNullable(tablespace);
    }

    public Optional<Boolean> getCreateDeployment() {
        return Optional.ofNullable(this.createDeployment);
    }
}
