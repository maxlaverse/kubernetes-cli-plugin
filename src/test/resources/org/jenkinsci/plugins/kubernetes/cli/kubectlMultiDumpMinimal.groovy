node{
  stage('Run') {
    withMultiKubeConfigs([[credentialsId: 'cred1234'], [credentialsId: 'cred9999']]) {
      sh 'kubectl config view > configDump'
    }
  }
}
