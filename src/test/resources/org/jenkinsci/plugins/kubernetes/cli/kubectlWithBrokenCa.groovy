node {
  stage('Run') {
    withKubeConfig([credentialsId: 'cred1234', caCertificate: 'broken-non-based-64XX&', serverUrl: 'https://localhost:6443']) {
      sh 'cat "$KUBECONFIG" > configDump'
    }
  }
}
