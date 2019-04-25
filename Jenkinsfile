pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'echo "Hello world!"'
                print "${env.CHANGE_BRANCH}"
            }
        }
    }
}
  
