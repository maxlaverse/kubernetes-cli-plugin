# Kubernetes Cli Plugin

[![build-status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/kubernetes-cli-plugin/master/)][master-build]

Allows you to setup [kubectl][kubectl] to access Kubernetes clusters from your jobs.

Extracted and rewritten from the [Kubernetes Plugin][kubernetes-plugin].

## Supported credentials
The following types of credentials are supported and can be used to authenticate against Kubernetes clusters:
* Username and Password, from the [Credentials][credentials-plugin].
* Certificates, from the [Credentials plugin][credentials-plugin].
* Token, as secrets from the [Plain Credentials plugin][plain-credentials-plugin].
* OpenShift tokens, as secrets from the [Kubernetes Credentials plugin][kubernetes-credentials-plugin]

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
* `caCertificate` - an optional base64-encoded certificate to check the Kubernetes api server's against
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
[master-build]: https://ci.jenkins.io/job/Plugins/job/kubernetes-cli-plugin/job/master/
