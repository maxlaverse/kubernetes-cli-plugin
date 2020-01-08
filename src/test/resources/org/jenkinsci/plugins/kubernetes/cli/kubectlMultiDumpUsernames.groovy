node{
  stage('Run') {
    withKubeCredentials([
      [credentialsId: 'cred1234', contextName: 'cont1234', clusterName: 'clus1234', serverUrl: 'https://localhost:1234'],
      [credentialsId: 'cred9999', contextName: 'cont9999', clusterName: 'clus9999', serverUrl: 'https://localhost:9999']
    ]) {
      sh 'kubectl config view > configDump'
    }
  }
}
