node{
  stage('Run') {
    withKubeConfig([credentialsId: 'cred1234', contextName: 'test-sample']) {
      sh 'cat "$KUBECONFIG" > configDump'
    }
  }
}
