import static net.grinder.script.Grinder.grinder
import net.grinder.script.Test
import net.grinder.plugin.http.HTTPRequest
import HTTPClient.NVPair
import com.mpos.lottery.te.common.encrypt.*

// Test client for Standard Edition
//
// A TestRunner instance is created for each thread. It can be used to
// store thread-specific data.
// NOTE: the groovy test script must be wrapper in a class.
// UPDATE DB:
// update operator o set o.sale_balance=99999999999 where o.login_name='2785';
class TETest{
	private HTTPRequest httpRequest;
	private String testName;
	// Test configuration
	private String dataKey = "W0JAMWUwYzM4NmRkZDZmZDJjMC0zNzdk"
	private String macKey = "aTLW6Ux3c+x8/EZNSmaRvavYrwBViQAt"
	private String teUrl = "http://192.168.100.69:8169/mlottery_te/transaction_engine/"
	// Message body variables
	private String user_mobile = "15220202189"
	private String gameId = "4028e4c242b787220142b78ea8a30004"
	private String drawNo = "5901"	
	// Message header variables
	private String protocolVersion = "1.0"
	private String gpeId = "WGPE"
	private String deviceId = "261"
	private String operatorId = "4028e4c24246428801424a224c50001c"
	private String batchNo = "1"
	private int transType = 200	
	private String traceMsgId = "1"
	private String timestamp = ""
	private String mac = ""
	private String gameTypeId = -1
	private String plainMessageBody;
	private String encryptMessageBody;

	TETest(){
		plainMessageBody = assembleL590RequestBody()
		/**
		 * So STRANGE, if put below statements here, seem Grinder-Groovy will to call this constructor
		 * twice, why? Anyway if put these logic into 'testRunner' closure, it works normally.
		 */
		//// initialize request variables
		//traceMsgId = genTraceMsgId(grinder.agentNumber, grinder.processNumber)
		//timestamp = genTimestamp()
		//mac = genMac()
		//encryptMessageBody = desMessageBody()

		httpRequest = new HTTPRequest()
		// Create a Test with a test number and a description. The test will be
		// automatically registered with The Grinder console if you are using
		// it.
		new Test(1, "${testName}").record(httpRequest)
	}

    // There must be closure named 'testRunner', which will be ran once for each 'grinder run'.
	def testRunner = { 
		// initialize request variables
		traceMsgId = genTraceMsgId(grinder.agentNumber, grinder.processNumber)
		timestamp = genTimestamp()
		mac = genMac()
		encryptMessageBody = desMessageBody()		

        // Normally test results are reported automatically when the test returns. If you want to 
        // alter the statistics after a test has completed, you must set delayReports = 1 to delay 
        // the reporting before performing the test. This only affects the current worker thread.
        grinder.statistics.delayReports = 1

		//println "send request: ${req}"
		//println "agent number: ${grinder.agentNumber}"
		//println "process number: ${grinder.processNumber}"
		//println "thread number: ${grinder.threadNumber}"
		//println "run number: ${grinder.runNumber}"

		// issue request to remote service
		def response = httpRequest.POST(teUrl, encryptMessageBody.bytes, genHttpHeaders())
		//println response.text

		// check whether the response is successful
		int respCode = Integer.parseInt(response.getHeader("X-Response-Code"))
		if (respCode != 200){
			grinder.logger.warn("Request with traceMsgId($traceMsgId) got unsuccessful response code $respCode")
			println "Request with traceMsgId($traceMsgId) got unsuccessful response code $respCode"
			// Set success = 0 to mark the test as a failure.
			grinder.statistics.forLastTest.setSuccess(false)   
		}
	}

	def String genMac(){
        String macInput =  "Protocal-Version:${protocolVersion}|"
        macInput += "GPE_Id:${gpeId}|"
        macInput += "Terminal-Id:${deviceId}|"
        macInput += "Operator-Id:${operatorId}|"
        macInput += "Trans-BatchNumber:${batchNo}|"
        macInput += "TractMsg-Id:${traceMsgId}|"
        macInput += "Timestamp:${timestamp}|"
        macInput += "Transaction-Type:${transType}|"
        macInput += "Transaction-Id:|"
        macInput += "Response-Code:|"        
        macInput += plainMessageBody
        
        println "Ready to MAC:" + macInput.trim()
        return HMacMd5Cipher.doDigest(macInput.trim(), macKey)
    }

    def String desMessageBody() {
        // encrypt raw_body by 3DES/CBC
        return TriperDESCipher.encrypt(dataKey, plainMessageBody, TriperDESCipher.STR_IV)
    }

	/**
	 * Generate the required http headers of TE interface specification.
	 */
	def NVPair[] genHttpHeaders() {
        return [
                new NVPair("X-Protocal-Version", protocolVersion),
                new NVPair("X-Trace-Message-Id", traceMsgId),
                new NVPair("X-Timestamp", timestamp),
                new NVPair("X-Transaction-Type", "${transType}"),
                new NVPair("X-GPE-Id", gpeId),
                new NVPair("X-Terminal-Id", deviceId),
                new NVPair("X-Operator-Id", operatorId),
                new NVPair("X-Trans-BatchNumber", batchNo),
                new NVPair("X-MAC", mac),
                new NVPair("Content-Type", "text/xml"),
                new NVPair("X-Game-Type-Id", "${gameTypeId}")
            ]
	}

	//@Synchronized
	def String genTraceMsgId(int agentId, int processNumber){
		if (agentId < 0) agentId = 0
		if (agentId > 9) throw new IllegalArgumentException("AgentID cant'be greater than 9")

		// ** Simply set grinder.processes=1, then no need to scratch your head to seek how to generate a globally unique trace message id.
		//if (processNumber < 0) agentId = 0
		// when reset worker process from console, the process number will be increased automatically, that says 
		// after several times of reset, the process number will be greater thatn 10 soon.
		//if (processNumber > 9) throw new IllegalArgumentException("processNumber cant'be greater than 9")

		/**
		 * Must synchronized on 'grinder' instance, as it is singleton in a worker process(jvm), each worker thread will 
		 * create its own instance of test class, in this case test class is WGPETest.
		 */
		synchronized(grinder){
			// read sequence number from file
			def sequenceFile = new File("sequence.var")
			if (!sequenceFile.exists())
			    sequenceFile.createNewFile();
			
			int seq = sequenceFile.text.trim()==""?0:sequenceFile.text.trim().toInteger()
			sequenceFile.withWriter {out ->
			    out.writeLine("${seq+1}")
			}
			def traceMsgId = "0" + agentId + String.format('%012d',seq)	
			//grinder.logger.info(traceMsgId)
			return traceMsgId
		}
	}

	def String genTimestamp(){
		new Date().format("yyyyMMddHHmmss")
	}

	// ----------------------------------------------------------------------//
	// - RequestBody of Different Game                                       //
	// ----------------------------------------------------------------------//
	def String assembleL590RequestBody() {
		testName = "TE Sale(L590)"
		// set game type
		gameTypeId = 15

		return """
		<Ticket multipleDraws="1" totalAmount="450.0" PIN="123456">
		    <GameDraw number="${drawNo}" gameId="${gameId}" />
		    <Entry selectedNumber="1" betOption="1" inputChannel="1" amount="100.0"/>
		    <Entry selectedNumber="1,2,6" betOption="52" inputChannel ="0" amount="350.0"/>
		    <User mobile="${user_mobile}" />
		</Ticket>
		"""
	}
}

