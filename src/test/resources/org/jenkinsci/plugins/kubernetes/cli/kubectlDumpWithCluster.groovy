node{
  stage('Run') {
    withKubeConfig([credentialsId: 'cred1234', clusterName: 'test-cluster']) {
      sh 'cat "$KUBECONFIG" > configDump'
    }
  }
}
