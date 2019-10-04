package org.entando.kubernetes.model.keycloakserver;

import org.entando.kubernetes.model.DbmsImageVendor;

public class KeycloakServerSpecBuilder<N extends KeycloakServerSpecBuilder> {

    private String imageName;
    private DbmsImageVendor dbms;
    private String ingressHostName;
    private String entandoImageVersion;
    private String tlsSecretName;
    private int replicas = 1;
    private boolean isDefault;

    public KeycloakServerSpecBuilder(KeycloakServerSpec spec) {
        this.imageName = spec.getImageName().orElse(null);
        this.dbms = spec.getDbms().orElse(null);
        this.ingressHostName = spec.getIngressHostName().orElse(null);
        this.entandoImageVersion = spec.getEntandoImageVersion().orElse(null);
        this.tlsSecretName = spec.getTlsSecretName().orElse(null);
        this.replicas = spec.getReplicas();
        this.isDefault = spec.isDefault();
    }

    public KeycloakServerSpecBuilder() {
        //required default constructor
    }

    public N withDefault(boolean isDefault) {
        this.isDefault = isDefault;
        return (N) this;
    }

    public N withImageName(String imageName) {
        this.imageName = imageName;
        return (N) this;
    }

    public N withDbms(DbmsImageVendor dbms) {
        this.dbms = dbms;
        return (N) this;
    }

    public N withEntandoImageVersion(String entandoImageVersion) {
        this.entandoImageVersion = entandoImageVersion;
        return (N) this;
    }

    public N withTlsSecretName(String tlsSecretName) {
        this.tlsSecretName = tlsSecretName;
        return (N) this;
    }

    public N withReplicas(int replicas) {
        this.replicas = replicas;
        return (N) this;
    }

    public N withIngressHostName(String ingressHostName) {
        this.ingressHostName = ingressHostName;
        return (N) this;
    }

    public KeycloakServerSpec build() {
        return new KeycloakServerSpec(imageName, dbms, ingressHostName, entandoImageVersion, tlsSecretName, replicas, isDefault);
    }

}
