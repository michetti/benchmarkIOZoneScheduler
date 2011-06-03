// Checking parameters
if (this.args?.size() < 2) {
  println "Parametros inválidos"
  println "Ex: groovy benchmark.groovy [numero_experimentos] [pasta_destino]"
  return -1
}

// Reading parameters
def executions = this.args[0] ? this.args[0] as Integer : 10
def experimentName =  this.args[1] ?: "output"


println "\n* Iniciando benchmark"

// param -i (types of test to run)
def tests = ["0", "1", "2"]

// param -r (records sizes to test)
def records = ["16k", "64k"]

// param -s (file sizes to test)
def sizes = ["64k", "64m"]


// generate process command line
def param_i = tests.collect { "-i $it" }.join(" ")
def param_r = records.collect { "-r $it" }.join(" ")
def param_s = sizes.collect { "-s $it" }.join(" ")

def process_str = "iozone $param_i $param_r $param_s"
println "\nO seguinte comando sera executado ${this.args[0]} vezes:"
println process_str


// create output dir if necessary
new File(experimentName).mkdir()

println ""
executions.times {
  def execution = it + 1
  def filePath = "${experimentName}/${execution}.txt"
  def fullCommand = "${process_str} > $filePath"
  
  println "Executando $fullCommand"
  def process = process_str.execute()

  process.waitFor()
  if (process.exitValue()) {
    println process.err.text
  } else {
    new File(filePath) << process.text
  }
}

