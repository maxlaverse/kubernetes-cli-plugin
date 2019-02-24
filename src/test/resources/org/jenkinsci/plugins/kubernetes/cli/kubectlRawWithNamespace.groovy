node{
  stage('Run') {
    withKubeConfig([credentialsId: 'cred1234', namespace: 'test-ns']) {
      sh 'cat "$KUBECONFIG" > configDump'
    }
  }
}
