package org.jenkinsci.plugins.kubernetes.cli

node{
  label "mocked-kubectl"
  stage('Run') {
    withKubeConfig([credentialsId: 'cred1234', serverUrl: 'https://localhost:6443']) {
      echo "File has been configured '${env.KUBECONFIG}'"
    }
  }
}
