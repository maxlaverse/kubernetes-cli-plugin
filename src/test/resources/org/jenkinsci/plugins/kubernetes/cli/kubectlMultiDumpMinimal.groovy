node{
  stage('Run') {
    withKubeCredentials([[credentialsId: 'cred1234'], [credentialsId: 'cred9999']]) {
      sh 'kubectl config view > configDump'
    }
  }
}
