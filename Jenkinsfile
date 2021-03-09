pipeline {
    agent any
    environment {
        jdk_tool="openjdk-11.0.2"
        maven_tool="maven-3.6.3"
    }
    stages {
        stage('clean') {
            steps {
               withMaven(jdk: "${env.jdk_tool}", maven: "${env.maven_tool}") {
                   sh "mvn clean"
               }
            }
        }
        stage('test') {
            steps {
                withMaven(jdk: "${env.jdk_tool}", maven: "${env.maven_tool}") {
                    sh "mvn test"
                }
            }
        }
    }
}