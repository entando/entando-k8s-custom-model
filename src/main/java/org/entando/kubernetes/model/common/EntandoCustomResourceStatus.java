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

package org.entando.kubernetes.model.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@JsonSerialize
@JsonDeserialize
@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, isGetterVisibility = Visibility.NONE, getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE)
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntandoCustomResourceStatus implements Serializable {

    private Map<String, AbstractServerStatus> serverStatuses = new ConcurrentHashMap<>();

    private Long observedGeneration;

    private EntandoDeploymentPhase phase;

    public EntandoCustomResourceStatus() {
        phase = EntandoDeploymentPhase.REQUESTED;
    }

    public EntandoDeploymentPhase getPhase() {
        return phase;
    }

    public void updateDeploymentPhase(EntandoDeploymentPhase entandoDeploymentPhase, Long observedGeneration) {
        this.phase = entandoDeploymentPhase;
        this.observedGeneration = observedGeneration;
    }

    public boolean hasFailed() {
        return serverStatuses.values().stream().anyMatch(AbstractServerStatus::hasFailed);
    }

    public void putServerStatus(AbstractServerStatus status) {
        serverStatuses.put(status.getQualifier(), status);
    }

    public Optional<InternalServerStatus> forDbQualifiedBy(String qualifier) {
        return Optional.ofNullable((InternalServerStatus) serverStatuses.get(qualifier));
    }

    public Optional<ExposedServerStatus> forServerQualifiedBy(String qualifier) {
        return Optional.ofNullable((ExposedServerStatus) serverStatuses.get(qualifier));
    }

    public Optional<AbstractServerStatus> findCurrentServerStatus() {
        return this.serverStatuses.values().stream().reduce((abstractServerStatus, abstractServerStatus2) -> {
            if (abstractServerStatus.getStarted().before(abstractServerStatus2.getStarted())) {
                return abstractServerStatus2;
            } else {
                return abstractServerStatus;
            }
        });
    }

    public EntandoDeploymentPhase calculateFinalPhase() {
        return hasFailed() ? EntandoDeploymentPhase.FAILED : EntandoDeploymentPhase.SUCCESSFUL;
    }

    public Long getObservedGeneration() {
        return observedGeneration;
    }

}