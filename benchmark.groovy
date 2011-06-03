// Checking parameters
if (this.args?.size() < 3) {
  println "Parametros inválidos"
  println "Ex: groovy benchmark.groovy [numero_experimentos] [pasta_destino] [device_name]"
  return -1
}

// Reading parameters
def executions = this.args[0] ? this.args[0] as Integer : 10
def experimentName =  this.args[1] ?: "output"
def deviceName =  this.args[2] ?: "sda"


println "\n* Iniciando benchmark para dispositivo $deviceName"

// schedulers to test
def schedulers = ["cfq", "noop", "deadline"]

// param -i (types of test to run)
def tests = ["0", "1", "2"]

// param -r (records sizes to test)
//def records = ["4k"]
def records = ["4k", "512k", "16384k"]

// param -s (file sizes to test)
//def sizes = ["64k"]
def sizes = ["64k", "8192k", "524288k"]


// generate process command line
def param_i = tests.collect { "-i $it" }.join(" ")
def param_r = records.collect { "-r $it" }.join(" ")
def param_s = sizes.collect { "-s $it" }.join(" ")

def process_str = "iozone -R $param_i $param_r $param_s"
println "\nO seguinte comando sera executado:"
println process_str
println ""

// create output dir if necessary
new File(experimentName).mkdir()

def getCurrentScheduler = {
  def p = "cat /sys/block/${deviceName}/queue/scheduler".execute()
  return p.text
}

def changeScheduler = { scheduler ->
  new File("/sys/block/${deviceName}/queue/scheduler") << scheduler
}

def clearCaches = {
  println "Limpando cache"

  def f = new File("/proc/sys/vm/drop_caches")
  f << "3"
  Thread.sleep(1000) // TODO don't know if this is necessary 
  f << "0"
}

def timeInSeconds = { start ->
  return ((new Date().time - start.time) / 1000) as Integer
}

def time_total = new Date()

schedulers.each { scheduler ->
  def time_scheduler = new Date()

  changeScheduler(scheduler)
  println "\n* Testando Algoritmo: ${getCurrentScheduler()}"

  executions.times {
    def time_execution = new Date()

    def execution = it + 1
    def filePath = "${experimentName}/${scheduler}_${execution}.txt"
    def fullCommand = "${process_str} > $filePath"

    clearCaches()
    
    println "Executando \'$fullCommand\'"
    def process = process_str.execute()
  
    process.waitFor()
    if (process.exitValue()) {
      println process.err.text
    } else {
      new File(filePath) << process.text
    }

    println "Execução finalizada em ${timeInSeconds(time_execution)} segundos"
  }
  println "Scheduler testado em ${timeInSeconds(time_scheduler)} segundos"
}
println "Teste total terminou em ${timeInSeconds(time_total)} segundos"

println "\nVoltando Scheduler para CFQ"
changeScheduler("cfq")
println getCurrentScheduler()
