# Kubernetes Cli Plugin

[![build-status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/kubernetes-cli-plugin/master/)][master-build]
[![Coverage Status](https://coveralls.io/repos/github/jenkinsci/kubernetes-cli-plugin/badge.svg?branch=master)](https://coveralls.io/github/jenkinsci/kubernetes-cli-plugin?branch=master)

Allows you to configure [kubectl][kubectl] in your job to interact with Kubernetes clusters.
Any tool built on top of `kubectl` can then be used from your pipelines, e.g. [kubernetes-deploy][kubernetes-deploy] to perform deployments.

Initially extracted and rewritten from the [Kubernetes Plugin][kubernetes-plugin].

## Supported credentials
The following types of credentials are supported and can be used to authenticate against Kubernetes clusters:
* Username and Password (see [Credentials plugin][credentials-plugin])
* Certificates (see [Credentials plugin][credentials-plugin])
* Plain KubeConfig files (see [Plain Credentials plugin][plain-credentials-plugin])
* Token, as secrets (see [Plain Credentials plugin][plain-credentials-plugin])
* OpenShift tokens, as secrets (see [Kubernetes Credentials plugin][kubernetes-credentials-plugin])

## Quick usage guide

### Pipeline usage
The `kubernetes-cli` plugin provides the function `withKubeConfig()` for Jenkins Pipeline support.
You can go to the *Snippet Generator* page under *Pipeline Syntax* section in Jenkins, select
*withKubeConfig: Setup Kubernetes CLI* from the *Sample Step* dropdown, and it will provide you configuration
interface for the plugin.
After filling the entries and click *Generate Pipeline Script* button, you will get the sample scripts which can be used
in your Pipeline definition.

The arguments to the `withKubeConfig` step are:
* `credentialsId` - the Jenkins identifier of the credentials to use.
* `caCertificate` - an optional base64-encoded certificate to check the Kubernetes api server's against. If you don't specify one, the CA verification will be skipped.
* `serverUrl` - the url of the api server

Example:
```groovy
node {
  stage('List pods') {
    withKubeConfig([credentialsId: '<credential-id>', caCertificate: '<ca-certificate>', serverUrl: '<api-server-address>']) {
      sh 'kubectl get pods'
    }
  }
}
```

### From the web interface
In Jenkins > *job name* > Configure > **Build Environment**

![webui](img/webui.png)


Brief description of the named fields:
* **credentialsId** - the Jenkins identifier of the credentials to use.
* **caCertificate** - an optional base64-encoded certificate to check the Kubernetes api server's against
* **serverUrl** - the url of the api server


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

[credentials-plugin]:https://github.com/jenkinsci/credentials-plugin
[kubernetes-plugin]:https://github.com/jenkinsci/kubernetes-plugin
[kubernetes-credentials-plugin]:https://github.com/jenkinsci/kubernetes-credentials-plugin
[plain-credentials-plugin]: https://github.com/jenkinsci/plain-credentials-plugin
[kubectl]:https://kubernetes.io/docs/reference/kubectl/overview/
[kubernetes-deploy]:https://github.com/Shopify/kubernetes-deploy
[master-build]: https://ci.jenkins.io/job/Plugins/job/kubernetes-cli-plugin/job/master/
[issue-tracker]: https://issues.jenkins-ci.org/issues/?jql=project%20%3D%20JENKINS%20AND%20status%20in%20(Open%2C%20%22In%20Progress%22%2C%20Reopened%2C%20%22In%20Review%22)%20AND%20component%20%3D%20kubernetes-cli-plugin
