pipeline {
  agent any
  stages {
    stage('compile') {
      steps {
        withAnt(installation: 'ant 0.9.6')
      }
    }

    stage('run') {
      steps {
        sh '''#!/bin/bash

java Rayer'''
      }
    }

  }
}