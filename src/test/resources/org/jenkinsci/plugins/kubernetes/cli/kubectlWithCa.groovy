node {
  stage('Run') {
    withKubeConfig([credentialsId: 'cred1234', caCertificate: 'YS1jZXJ0aWZpY2F0ZQ==', serverUrl: 'https://localhost:6443']) {
      sh 'cat "$KUBECONFIG" > configDump'
    }
  }
}
