node{
  stage('Run') {
    withKubeConfig([credentialsId: 'cred1234', serverUrl: 'https://localhost:6443', contextName: 'test-sample']) {
      sh 'cat "$KUBECONFIG" > configDump'
    }
  }
}
