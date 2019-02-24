node{
  stage('Run') {
    withKubeConfig([credentialsId: 'cred1234', contextName: 'minikube']) {
      sh 'cat "$KUBECONFIG" > configDump'
    }
  }
}
