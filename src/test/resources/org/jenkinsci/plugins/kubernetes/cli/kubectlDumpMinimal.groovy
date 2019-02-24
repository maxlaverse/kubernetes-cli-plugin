node{
  stage('Run') {
    withKubeConfig([credentialsId: 'cred1234']) {
      sh 'cat "$KUBECONFIG" > configDump'
    }
  }
}
