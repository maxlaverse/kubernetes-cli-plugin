package org.jenkinsci.plugins.kubernetes.cli.utils;

import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

public class UnsupportedCredential extends BaseStandardCredentials {

    public UnsupportedCredential(String id, String description) {
        super(id, description);
    }
}
