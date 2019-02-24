node{
    label "mocked-kubectl"
    stage('Run') {
        withKubeConfig([credentialsId: 'cred1234', clusterName: 'name']) {
            echo "File has been configured '${env.KUBECONFIG}'"
        }
    }
}
