# Kubernetes CLI Plugin

[![travis-ci](https://travis-ci.org/jenkinsci/kubernetes-cli-plugin.svg?branch=master)](https://travis-ci.org/jenkinsci/kubernetes-cli-plugin)
[![coveralls](https://coveralls.io/repos/github/jenkinsci/kubernetes-cli-plugin/badge.svg?branch=master)](https://coveralls.io/github/jenkinsci/kubernetes-cli-plugin?branch=master)

Allows you to configure [kubectl][kubectl] in your job to interact with Kubernetes clusters.
Any tool built on top of `kubectl` can then be used from your pipelines, e.g. [kubernetes-deploy][kubernetes-deploy] to perform deployments.

Initially extracted and rewritten from the [Kubernetes Plugin][kubernetes-plugin].

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
                    clusterName: '<cluster-name>'
                    ]) {
      sh 'kubectl get pods'
    }
  }
}
```

The arguments to the `withKubeConfig` step are:
* `credentialsId` - the Jenkins identifier of the credentials to use.
* `caCertificate` - an optional certificate to check the Kubernetes api server's against. If you don't specify one, the CA verification will be skipped.
* `serverUrl` - the url of the api server
* `contextName` - name of the context to create or to switch to if a raw kubeconfig was provided
* `clusterName` - name of the cluster to create or to switch to if a raw kubeconfig was provided


### From the web interface
1. Within the Jenkins dashboard, select a Job and then select Configure
2. Scroll down and click the "Add build step" dropdown
3. Select "Configure Kubernetes CLI (kubectl)"
4. In the "Credential" dropdown, select the credentials to authenticate on the cluster or the kubeconfig stored in Jenkins.

![webui](img/webui.png)


Brief description of the named fields:
* **Credentials** - the Jenkins identifier of the credentials to use.
* **Kubernetes server endpoint** - the url of the api server
* **Context name** - name of the context to create or to switch to if a raw kubeconfig was provided
* **Cluster name** - name of the cluster to create or to switch to if a raw kubeconfig was provided
* **Certificate of certificate authority** - an optional certificate to check the Kubernetes api server's against

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
