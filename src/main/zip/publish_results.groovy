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
    String outputFile = props.notNull("outputFile")

    boolean debugMode = props.optionalBoolean("debugMode", false)

    SRLHelper srClient = new SRLHelper(srServerUrl, srUser, srPassword)
    srClient.setTenantId(tenantId)
    srClient.setSSL()
    srClient.login()
    srClient.setDebug(debugMode)

    def runResults = srClient.runResults(Long.toString(runId))
    def sb = new StringBuilder()
    sb << """
<style type='text/css'>
    body { padding: 10px; 	background: #F5F5F5; font-family: Arial; }
    div { 
        background: #fff; box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        -moz-box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        -webkit-box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        margin-bottom: 20px; }
    a { color: #337ab7; text-decoration: underline; }
    a:hover { text-decoration: none; }
    span { font-weight: bold; }
    span.failed { color: #df1e1e; }
    span.success { color: #32ad12; }
    table { border-collapse: collapse; width: 100%; color: #333; }
    table td, table th { border: 1px solid #ddd; }
    table th { padding: 10px; text-align: center; background: #eee; font-size: 16px; }
    table td { padding: 7px; font-size: 12px; } " +
    table tr th:first-child { max-width: 200px; min-width: 200px; }
    table tr td:first-child { width: 200px; text-align: right; font-weight: bold;}
    table tr td:first-child:after {content: ':' }
</style>
"""
    sb << "<div> <table> <tr> <th colspan='2'>Test Run: ${runId} </th> </tr>"
    sb << "<tr> <td> Date & time </td> <td> ${runResults?.dateTime} </td> </tr>"
    sb << "<tr> <td> Status </td> <td> <span class=" + (("PASSED".equalsIgnoreCase(runResults?.status)) ? "success >" : "failed >") +  runResults?.status + "</td> </tr>"
    sb << "<tr> <td> Duration </td> <td> ${runResults?.duration} </td> </tr>"
    sb << "<tr> <td> GUI Vusers </td> <td> " + srClient.fNull(runResults?.uiVusers) + " </td> </tr>"
    sb << "<tr> <td> GUI VUH </td> <td> " + srClient.fNull(runResults?.uiVUH) + " </td> </tr>"
    sb << "<tr> <td> Average Throughput </td> <td> " + srClient.fNull(runResults?.averageThroughput) + " </td> </tr>"
    sb << "<tr> <td> Total Throughput </td> <td> " + srClient.fNull(runResults?.totalThroughput) + " </td> </tr>"
    sb << "<tr> <td> Average Hits </td> <td> " + srClient.fNull(runResults?.averageHits) + " </td> </tr>"
    sb << "<tr> <td> Total Hits </td> <td> " + srClient.fNull(runResults?.totalHits) + " </td> </tr>"
    sb << "<tr> <td> Total Transactions Passed </td> <td> " + srClient.fNull(runResults?.totalTransactionsPassed) + " </td> </tr>"
    sb << "<tr> <td> Total Transactions Failed </td> <td> " + srClient.fNull(runResults?.totalTransactionsFailed) + " </td> </tr>"
    String url = "${srServerUrl}/run-overview/${runId}/dashboard/?TENANTID=${tenantId}&projectId=${projectId}"
    sb << "<tr> <td> Run URL" + "</td> <td> <a href='${url}'  target=_blank>" + url + "</a> </td> </tr>"
    sb << "<br>"
    sb << "</table> </div>"
    sb << "</body>"
    sb << "</html>"

    File f = new File(outputFile)
    BufferedWriter bw = new BufferedWriter(new FileWriter(f))
    bw.write(sb.toString())
    bw.close()

    println "Succesfully created file ${outputFile} in work area..."

} catch (StepFailedException e) {
    println "ERROR: ${e.message}"
    System.exit 1
}