node{
  label "mocked-kubectl"
  stage('Run') {
    withKubeConfig([credentialsId: 'cred1234', serverUrl: 'https://localhost:6443']) {
      error("Build failed because of this and that..")
    }
  }
}
