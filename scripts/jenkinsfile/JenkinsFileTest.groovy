import groovy.test.GroovyTestCase

class JenkinsFileTest extends GroovyTestCase {

  final SNAPSHOT_DIR = 'scripts/jenkinsfile/snapshots'

  JenkinsFileTest() {
    super()
    new File(SNAPSHOT_DIR).mkdirs()
  }

  void testSuccess() {
    def jenkinsPipeline = getJenkinsPipeline()
    jenkinsPipeline.metaClass.currentBuild = [
      result: 'SUCCESS',
      previousBuild: [
        result: 'FAILURE'
      ]
    ]
    captureStdOut { buffer ->
      jenkinsPipeline.main()
      def actual = buffer.toString()
      if (System.getenv('UPDATE_SNAPSHOTS') == '1') {
        new File("${SNAPSHOT_DIR}/success.txt").write(actual)
      }
      def expected = new File("${SNAPSHOT_DIR}/success.txt").text
      assertEquals expected, actual
    }
  }

  void testFailure() {
    def jenkinsPipeline = getJenkinsPipeline()
    jenkinsPipeline.metaClass.currentBuild = [
      result: 'FAILURE',
      previousBuild: [
        result: 'SUCCESS'
      ]
    ]
    captureStdOut { buffer ->
      jenkinsPipeline.main()
      def actual = buffer.toString()
      if (System.getenv('UPDATE_SNAPSHOTS') == '1') {
        new File("${SNAPSHOT_DIR}/failure.txt").write(actual)
      }
      def expected = new File("${SNAPSHOT_DIR}/failure.txt").text
      assertEquals expected, actual
    }
  }

  void testException() {
    def jenkinsPipeline = getJenkinsPipeline()
    jenkinsPipeline.metaClass.stage = { name, func ->
      println("Running stage: ${name}")
      throw new Exception('asdf')
    }
    jenkinsPipeline.metaClass.currentBuild = [
      result: 'FAILURE',
      previousBuild: [
        result: 'SUCCESS'
      ]
    ]
    captureStdOut { buffer ->
      shouldFail Exception, {
        jenkinsPipeline.main()
      }
      def actual = buffer.toString()
      if (System.getenv('UPDATE_SNAPSHOTS') == '1') {
        new File("${SNAPSHOT_DIR}/exception.txt").write(actual)
      }
      def expected = new File("${SNAPSHOT_DIR}/exception.txt").text
      assertEquals expected, actual
    }
  }

  def getJenkinsPipeline() {
    def shell = new GroovyShell()
    def jenkinsPipeline = shell.parse(new File('Jenkinsfile'))
    stubJenkinsApi(jenkinsPipeline)
    return jenkinsPipeline
  }

  def captureStdOut(func) {
    def oldOut = System.out
    def buffer = new ByteArrayOutputStream()
    def newOut = new PrintStream(buffer)
    System.out = newOut
    func(buffer)
    System.out = oldOut
  }

  def stubJenkinsApi(jenkinsPipeline) {
    def sysEnv = System.getenv()
    jenkinsPipeline.metaClass.workspace = sysEnv.PWD
    jenkinsPipeline.metaClass.buildDiscarder = { args -> }
    jenkinsPipeline.metaClass.checkout = { args -> }
    jenkinsPipeline.metaClass.cron = { }
    jenkinsPipeline.metaClass.currentBuild = [
      result: 'SUCCESS',
      previousBuild: [result: 'SUCCESS'],
    ]
    jenkinsPipeline.metaClass.disableConcurrentBuilds = { }
    def docker = new LinkedHashMap()
    jenkinsPipeline.metaClass.docker = docker
    docker.image = { name ->
      println "Running docker image $name"
      return [inside: { func ->
        println 'Execing inside docker'
        func()
      }]
    }
    jenkinsPipeline.metaClass.echo = { message -> println(message) }
    def env = [:]
    jenkinsPipeline.metaClass.env = env
    env.put('BRANCH_NAME', 'develop')
    env.put('BUILD_NUMBER', '1')
    env.put('JOB_NAME', 'asdf')
    env.put('NODE_LABELS', 'docker us-west-2-dev')
    env.put('RUN_DISPLAY_URL', 'asdf2')
    env.setProperty = { key, value ->
      env.put(key, value)
    }
    jenkinsPipeline.metaClass.fileExists = { path ->
      return new File(path).exists()
    }
    jenkinsPipeline.metaClass.githubNotify = { args ->
      println("Notifying github status: ${args.status}, context: ${args.context}")
    }
    jenkinsPipeline.metaClass.logRotator = { args ->
      println("Setting log rotate to ${args.daysToKeepStr} days")
    }
    jenkinsPipeline.metaClass.node = { name = "default", func ->
      println("Running on node ${name}"); func()
    }
    jenkinsPipeline.metaClass.parallel = { args ->
      println('Running in parallel')
      args.each { arg ->
        println("Running target ${arg.key}")
        arg.value()
      }
    }
    jenkinsPipeline.metaClass.pipelineTriggers = { }
    jenkinsPipeline.metaClass.properties = { }
    jenkinsPipeline.metaClass.readFile = { path ->
      return new File(path).getText().trim()
    }
    jenkinsPipeline.metaClass.scm = [:]
    jenkinsPipeline.metaClass.sh = { command ->
      println("Running sh command: ${command}")
    }
    jenkinsPipeline.metaClass.slackSend = { args ->
      println("slackSend channel:${args.channel} "
        + "message:${args.message} color:${args.color}")
    }
    jenkinsPipeline.metaClass.stage = { name, func ->
      println("Running stage: ${name}"); func()
    }
    jenkinsPipeline.metaClass.timeout = { args, func ->
      println("Setting timeout to ${args.time} ${args.unit}"); func()
    }
    jenkinsPipeline.metaClass.usernamePassword = { args ->
      println("Using username and password ${args}")
    }
    jenkinsPipeline.metaClass.withCredentials = { creds, func -> func() }
    jenkinsPipeline.metaClass.withPyenv = { verison, func -> func() }
    jenkinsPipeline.metaClass.writeFile = { path, text ->
      new File(path) << text
    }
  }

}
