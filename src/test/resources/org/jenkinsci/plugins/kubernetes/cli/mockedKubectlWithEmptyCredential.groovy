node{
  label "mocked-kubectl"
  stage('Run') {
    withKubeConfig([credentialsId: '', serverUrl: 'https://localhost:6443']) {
      echo "File has been configured '${env.KUBECONFIG}'"
    }
  }
}
