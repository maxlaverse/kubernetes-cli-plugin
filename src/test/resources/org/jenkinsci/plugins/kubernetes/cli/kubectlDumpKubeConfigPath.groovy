package org.jenkinsci.plugins.kubernetes.cli

node{
  stage('Run') {
    withKubeConfig([credentialsId: 'cred1234', serverUrl: 'https://localhost:6443']) {
      echo "Using temporary file ${env.KUBECONFIG}"
    }
  }
}
