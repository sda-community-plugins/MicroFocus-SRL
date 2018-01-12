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
    long testId = props.notNullInt('testId')

    boolean debugMode = props.optionalBoolean("debugMode", false)

    SRLHelper srClient = new SRLHelper(srServerUrl, srUser, srPassword)
    srClient.setTenantId(tenantId)
    srClient.setSSL()
    srClient.login()
    srClient.setDebug(debugMode)

    String runId = srClient.runTest(Long.toString(projectId), Long.toString(testId))
    String url = "${srServerUrl}/run-overview/${runId}/dashboard/?TENANTID=${tenantId}&projectId=${projectId}"
    println "Successfully started test ${testId} as run id: ${runId}"
    println "For details: ${url}"

    apTool.setOutputProperty("runId", runId);
    apTool.setOutputProperties();

} catch (StepFailedException e) {
    println "ERROR: ${e.message}"
    System.exit 1
}