import static net.grinder.script.Grinder.grinder
import net.grinder.script.Test
import net.grinder.plugin.http.HTTPRequest
import groovy.xml.Namespace
import groovy.transform.Synchronized

// Test client for AFM WGPE.
//
// A TestRunner instance is created for each thread. It can be used to
// store thread-specific data.
// NOTE: the groovy test script must be wrapper in a class.
// UPDATE DB:
// update merchant m set m.sale_balance=99999999999 where m.merchant_code='78954'; 
// update operator o set o.sale_balance=99999999999 where o.login_name='2785';
// update mm_account m set m.base_amount=99999999999 where m.login_name='15220202189';
class WGPETest{
	private HTTPRequest httpRequest;
	// Test configuration
	private String wgpeUrl = "http://192.168.100.68:8168/mlottery_wgpe/ServicePort/"
	private long merchantId = 1182504
	private String user_mobile = "15220202189"
	//private String gameId = "4028e4c242356054014235849f6f001e"
	//private String drawNo = "M6360005"
	private String gameId = "4028e4c2425b2ef101425b45f23a0005"
	private String drawNo = "Load-001"	

	WGPETest(){
		httpRequest = new HTTPRequest()
		// Create a Test with a test number and a description. The test will be
		// automatically registered with The Grinder console if you are using
		// it.
		new Test(2, "WGPE Sale").record(httpRequest)
	}

    // There must be closure named 'testRunner', which will be ran once for each 'grinder run'.
	def testRunner = { 
        // Normally test results are reported automatically when the test returns. If you want to 
        // alter the statistics after a test has completed, you must set delayReports = 1 to delay 
        // the reporting before performing the test. This only affects the current worker thread.
        grinder.statistics.delayReports = 1

		//println "send request: ${req}"
		//println "agent number: ${grinder.agentNumber}"
		//println "process number: ${grinder.processNumber}"
		//println "thread number: ${grinder.threadNumber}"
		//println "run number: ${grinder.runNumber}"

		String timestamp = genTimestamp()
		String traceMsgId = genTraceMsgId(grinder.agentNumber, grinder.processNumber)
		String req = assembleRequest(merchantId, timestamp, traceMsgId)
		// issue request to remote service
		def response = httpRequest.POST(wgpeUrl, req.bytes)
		//println response.text

		// check whether the response is successful
		int respCode = getSuccessfulResponse(response.text)
		if (respCode != 200){
			grinder.logger.warn("Request with traceMsgId($traceMsgId) got unsuccessful response code $respCode")
			println "Request with traceMsgId($traceMsgId) got unsuccessful response code $respCode"
			// Set success = 0 to mark the test as a failure.
			grinder.statistics.forLastTest.setSuccess(false)   
		}
	}

	def String assembleRequest(long merchantId, String timestamp, String traceMsgId) {

		return """
		<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
			<soap:Header>
				<ns2:headReq xmlns:ns2="http://www.lottery.mpos.com/wgpe/port/schemas/">
					<ns2:ProtocalVersion>2.1</ns2:ProtocalVersion>
					<ns2:Timestamp>${timestamp}</ns2:Timestamp>
					<ns2:MerchantId>${merchantId}</ns2:MerchantId>
					<ns2:TransMessageID>${traceMsgId}</ns2:TransMessageID>
					<ns2:GameTypeId>1</ns2:GameTypeId>
					<ns2:SystemId>1</ns2:SystemId>
					<ns2:MPOSSignature>11111</ns2:MPOSSignature>
				</ns2:headReq>
			</soap:Header>
			<soap:Body>
				<ns2:SellReq xmlns:ns2="http://www.lottery.mpos.com/wgpe/port/schemas/">
					<ns2:multipleDraws>1</ns2:multipleDraws>
					<ns2:totalAmount>100</ns2:totalAmount>
					<ns2:PIN>!!!!</ns2:PIN>
					<ns2:GameDraw>
						<ns2:number>${drawNo}</ns2:number>
						<ns2:gameId>${gameId}</ns2:gameId>
					</ns2:GameDraw>
					<ns2:Entry>
						<ns2:selectedNumber>1,2,3,4,5,6</ns2:selectedNumber>
						<ns2:betOption>1</ns2:betOption>
						<ns2:isQuickPick>0</ns2:isQuickPick>						
					</ns2:Entry>
					<ns2:User mobile="${user_mobile}"/>
				</ns2:SellReq>
			</soap:Body>
		</soap:Envelope>
		"""
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

	def getSuccessfulResponse(String xml) {
		def ns2 = new Namespace("http://www.lottery.mpos.com/wgpe/port/schemas/", 'ns2')
		def soap = new Namespace("http://schemas.xmlsoap.org/soap/envelope/", 'soap')
		def envelope = new XmlParser().parseText(xml)
		envelope[soap.Header][ns2.headResp][ns2.ResponseCode].text().toInteger()
	}
}

