# Kubernetes CLI Plugin

[![travis-ci](https://travis-ci.org/jenkinsci/kubernetes-cli-plugin.svg?branch=master)](https://travis-ci.org/jenkinsci/kubernetes-cli-plugin)
[![coveralls](https://coveralls.io/repos/github/jenkinsci/kubernetes-cli-plugin/badge.svg?branch=master)](https://coveralls.io/github/jenkinsci/kubernetes-cli-plugin?branch=master)

Allows you to configure [kubectl][kubectl] in your job to interact with Kubernetes clusters.
Any tool built on top of `kubectl` can then be used from your pipelines, e.g. [kubernetes-deploy][kubernetes-deploy] to perform deployments.

Initially extracted and rewritten from the [Kubernetes Plugin][kubernetes-plugin].

```groovy
node {
  stage('Apply Kubernetes files') {
    withKubeConfig([credentialsId: 'user1', serverUrl: 'https://api.k8s.my-company.com']) {
      sh 'kubectl apply -f my-kubernetes-directory'
    }
  }
}
```

## Prerequisites
* An executor with `kubectl` installed (tested against [v1.8 to v1.13][travis-config] included)
* A Kubernetes cluster

## Supported credentials
The following types of credentials are supported and can be used to authenticate against Kubernetes clusters:
* Username and Password (see [Credentials plugin][credentials-plugin])
* Certificates (see [Credentials plugin][credentials-plugin])
* Plain KubeConfig files (see [Plain Credentials plugin][plain-credentials-plugin])
* Token, as secrets (see [Plain Credentials plugin][plain-credentials-plugin])
* OpenShift OAuth tokens, as secrets (see [Kubernetes Credentials plugin][kubernetes-credentials-plugin])

## Quick usage guide

The parameters have a slightly different effect depending if a plain KubeConfig file is provided or not.

### Parameters (without KubeConfig file)
| Name            | Mandatory | Description   |
| --------------- | --------- | ------------- |
| `credentialsId` | yes       | The Jenkins ID of the credentials. |
| `serverUrl`     | yes       | URL of the API server's. |
| `caCertificate` | no        | Cluster Certificate Authority used to validate the API server's certificate. The validation is skipped if the parameter is not provided. |
| `clusterName`   | no        | Name of the generated Cluster configuration. (default: `k8s`) |
| `namespace`     | no        | Namespace for the Context. |
| `contextName`   | no        | Name of the generated Context configuration. (default: `k8s`) |

### Parameters (with KubeConfig file)

The plugin writes the plain KubeConfig file and doesn't change any other field if only `credentialsId` is provided.
The recommended way to use a single KubeConfig file with multiples clusters, users, and default namespaces is to
configure a Context for each of them, and use the `contextName` parameter to switch between them (see [Kubernetes documentation][multi-clusters]).

| Name            | Mandatory | Description   |
| --------------- | --------- | ------------- |
| `credentialsId` | yes       | The Jenkins ID of the plain KubeConfig file. |
| `serverUrl`     | no        | URL of the API server's. This will create a new `cluster` block and modify the current Context to use it. |
| `caCertificate` | no        | Cluster Certificate Authority used to validate the API server's certificate if a `serverUrl` was provided. The validation is skipped if the parameter is not provided. |
| `clusterName`   | no        | Modifies the Cluster of the current Context. Also used for the generated `cluster` block if a `serverUrl` was provided. |
| `namespace`     | no        | Modifies the Namespace of the current Context. |
| `contextName`   | no        | Switch the current Context to this name. The Context must already exist in the KubeConfig file. |


### Pipeline usage
The `kubernetes-cli` plugin provides the function `withKubeConfig()` for Jenkins Pipeline support.
You can go to the *Snippet Generator* page under *Pipeline Syntax* section in Jenkins, select
*withKubeConfig: Setup Kubernetes CLI* from the *Sample Step* dropdown, and it will provide you configuration
interface for the plugin. After filling the entries and click *Generate Pipeline Script* button, you will get the sample scripts which can be used
in your Pipeline definition.

Example:
```groovy
node {
  stage('List pods') {
    withKubeConfig([credentialsId: '<credential-id>',
                    caCertificate: '<ca-certificate>',
                    serverUrl: '<api-server-address>',
                    contextName: '<context-name>',
                    clusterName: '<cluster-name>',
                    namespace: '<namespace>'
                    ]) {
      sh 'kubectl get pods'
    }
  }
}
```

### From the web interface
1. Within the Jenkins dashboard, select a Job and then select Configure
2. Scroll down and click the "Add build step" dropdown
3. Select "Configure Kubernetes CLI (kubectl)"
4. In the "Credential" dropdown, select the credentials to authenticate on the cluster or the kubeconfig stored in Jenkins.

![webui](img/webui.png)

## Reporting an issue
Please file bug reports directly on the Jenkins [issue tracker][issue-tracker]


## Building and Testing
To build the extension, run:
```bash
mvn clean package
```
and upload `target/kubernetes-cli.hpi` to your Jenkins installation.

To run the tests:
```bash
mvn clean test
```

### Perform a release
```bash
mvn release:prepare release:perform
```

## Release
Refer to the [CHANGELOG](CHANGELOG.md) in the plugin repository.

[travis-config]:.travis.yml
[credentials-plugin]:https://github.com/jenkinsci/credentials-plugin
[kubernetes-plugin]:https://github.com/jenkinsci/kubernetes-plugin
[kubernetes-credentials-plugin]:https://github.com/jenkinsci/kubernetes-credentials-plugin
[plain-credentials-plugin]: https://github.com/jenkinsci/plain-credentials-plugin
[kubectl]:https://kubernetes.io/docs/reference/kubectl/overview/
[kubernetes-deploy]:https://github.com/Shopify/kubernetes-deploy
[master-build]: https://ci.jenkins.io/job/Plugins/job/kubernetes-cli-plugin/job/master/
[issue-tracker]: https://issues.jenkins-ci.org/issues/?jql=project%20%3D%20JENKINS%20AND%20status%20in%20(Open%2C%20%22In%20Progress%22%2C%20Reopened%2C%20%22In%20Review%22)%20AND%20component%20%3D%20kubernetes-cli-plugin
[multi-clusters]: https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/