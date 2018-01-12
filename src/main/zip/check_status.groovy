import com.serena.air.StepFailedException
import com.serena.air.StepPropertiesHelper
import com.serena.air.srl.SRLHelper
import com.urbancode.air.AirPluginTool

def apTool = new AirPluginTool(args[0], args[1])
def props = new StepPropertiesHelper(apTool.stepProperties, true)

try {
    String srServerUrl = props.notNull('serverUrl')
    String srUser = props.notNull('user')
    String srPassword = props.notNull('password')
    long tenantId = props.notNullInt('tenantId')
    long projectId = props.notNullInt('projectId')
    long runId = props.notNullInt('runId')
    boolean pollStatus = props.optionalBoolean('pollStatus', false)
    long pollInterval = props.optionalInt('pollInterval', 60)

    boolean debugMode = props.optionalBoolean("debugMode", false)

    SRLHelper srClient = new SRLHelper(srServerUrl, srUser, srPassword)
    srClient.setTenantId(tenantId)
    srClient.setSSL()
    srClient.login()
    srClient.setDebug(debugMode)

    String runStatus = srClient.runStatus(Long.toString(runId))
    println "Test run ${runId} status: ${runStatus}"

    if (pollStatus) {
        while (runStatus.equals("in-progress")) {
            println "Test run ${runId} is in-progress... sleeping..."
            sleep(pollInterval*1000)
            runStatus = srClient.runStatus(Long.toString(runId))
        }
        println "Test run ${runId} final status: ${runStatus}"
    }
    apTool.setOutputProperty("runStatus", runStatus);
    apTool.setOutputProperties();

} catch (StepFailedException e) {
    println "ERROR: ${e.message}"
    System.exit 1
}