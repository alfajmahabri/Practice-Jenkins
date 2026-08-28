pipeline{
    agent any

    tools{
        maven 'Maven 3.6.3'
        jdk 'JDK-21'
    }

    stages{

        stage('Validation'){
            steps{
                echo 'Validating the code'
                sh 'mvn checkstyle:check -Dcheckstyle.config.location=config/checkstyle.xml'
            }
        }

        stage('Compile'){
            steps{
                echo "Compiling the code..."
                sh 'mvn compile'
            }
        }

        stage('Test'){
            steps{
                echo "Running tests..."
                sh 'mvn test'
            }
        }

        stage('Coverage'){
            steps{
                echo "Generating code coverage report..."
                sh 'mvn verify'
            }
        }

        stage('Package'){
            steps{
                echo "Packaging the application..."
                sh 'mvn package -DskipTests'
            }
        }

        stage('Upload'){
            steps{
                echo "Uploading to VPS"
            }
        }

        stage('Deploy'){
            steps{
                echo "Deploying the application..."
            }
        }
    }

    post{
        always{
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
            cleanWs()
        }
        success{
            echo 'Build completed successfully!'
        }
        failure{
            echo 'Build failed. Please check the logs.'
        }
}