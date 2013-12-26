import static net.grinder.script.Grinder.grinder
import net.grinder.script.Test
import net.mpos.igpe.test.IgpeLoadTestClient 
import net.mpos.igpe.common.tlvutilities.TLVElement
import net.mpos.igpe.common.tlvutilities.TLVParser
import net.mpos.igpe.core.Constants
import net.mpos.igpe.util.TerminalMessageHandler

// A TestRunner instance is created for each thread. It can be used to
// store thread-specific data.
// NOTE: the groovy test script must be wrapper in a class.
// UPDATE DB:
// update merchant m set m.sale_balance=99999999999 where m.merchant_code='78954'; 
// update operator o set o.sale_balance=99999999999 where o.login_name='2785';
// update mm_account m set m.base_amount=99999999999 where m.login_name='15220202189';
class IgpeTest{
	private IgpeLoadTestClient igpeClient;
	// Test configuration
	// have to insert a record to table 'operator_session' manually, or use the existed operator session.
	private String dataKey = "SAnnT6Ha62pBiFdlSlX8hq8crnfMfnr2";
	private String macKey = "I7VZve2MVIsh4sVMmDq2lphSXHyZDpSJibKoCJz98ho=";
	private String igpeHost = "192.168.2.155";
	private int igpePort = 2013;
	private String opLoginName = "222";
	private long deviceId = 542;
	// configuration of message body
	private String user_mobile = "15220202189"
	private String gameId = "ff80808143234d8f014323c991620088"
	private String drawNo = "load-2"		

	IgpeTest(){
		igpeClient = new IgpeLoadTestClient(igpeHost, igpePort)
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
		// issue request to remote service
		def reqBytes = igpeClient.assembleRequest("1.5", traceMsgId, timestamp, opLoginName, Constants.REQ_SELL_TICKET+ "", deviceId + "",
			"1", Constants.GAME_TYPE_LOTTO + "", assembleRequestBody(), dataKey, macKey)

		// Create a Test with a test number and a description. The test will be
		// automatically registered with The Grinder console if you are using
		// it. Instrument the supplied target object. Subsequent calls to target 
		// will be recorded against the statistics for this Test.
		new Test(2, "IGPE Sale").record(igpeClient)
		def respBytes = igpeClient.connectAndRequest(reqBytes);

		// check whether the response is successful
		int respCode = getResponseCode(respBytes)
		if (respCode != 200){
			grinder.logger.warn("Request with traceMsgId($traceMsgId) got unsuccessful response code $respCode")
			println "Request with traceMsgId($traceMsgId) got unsuccessful response code $respCode"
			// Set success = 0 to mark the test as a failure.
			grinder.statistics.forLastTest.setSuccess(false)   
		}
	}

	def String assembleRequestBody() {

		return "#15#${gameId}#${drawNo}#1#1.0#!!!!#_#1,2,3,4:1:0#"
	}

	protected String getPlainMessageBody(LinkedList<TLVElement> respTlvs, String key)
	        throws Exception {
		TLVElement msgBodyTlv = TLVParser.GetObjectFromList(respTlvs, Constants.TAG_MESSAGE_BODY);
		return SecurityMeasurements.TripleDESCBCDecryptToString(msgBodyTlv.GetValueAsString(), key);
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

	def int getResponseCode(byte[] respBytes) {
		// Decode the tags
		LinkedList<TLVElement> respTlvs = TerminalMessageHandler.GetRequestList(respBytes);
		def respStr = TLVParser.GetObjectFromList(respTlvs, Constants.TAG_RESPONSE_CODE).GetValueAsString();
		def (respType, respCode) = respStr.tokenize("#")
		return respCode.toInteger()
	}
}

