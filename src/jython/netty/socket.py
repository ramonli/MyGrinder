# Hello World
#
# A minimal script that tests The Grinder logging facility.
#
# This script shows the recommended style for scripts, with a
# TestRunner class. The script is executed just once by each worker
# process and defines the TestRunner class. The Grinder creates an
# instance of TestRunner for each worker thread, and repeatedly calls
# the instance for each run of that thread.
from net.grinder.script.Grinder import grinder
from net.grinder.script import Test

from java.io import *
from java.net import *

## A shorter alias for the grinder.logger.output() method.
#log = grinder.logger.output
#
## Create a Test with a test number and a description. The test will be
## automatically registered with The Grinder console if you are using
## it.
test1 = Test(1, "NIO")
#test2 = Test(2, "BIO")

# Wrap the log() method with our Test and call the result logWrapper.
# Calls to logWrapper() will be recorded and forwarded on to the real
# log() method.
#logWrapper = test1.wrap(log)

req = ""
for i in range(1024):
    req += "x"

def foo():
    socket = None
    try :
        socket = Socket("localhost", 7890)
        pw = PrintWriter(socket.getOutputStream(), True)
        # write request
        pw.println(req)

        # read response
        br = BufferedReader(InputStreamReader(socket.getInputStream()))
        resp = br.readLine()
#        while resp != None :
#            resp = br.readLine()
#        log("[RESP] %s" % resp)
#        print(resp)

        pw.close()
        br.close()
    finally :
        if socket != None:
            socket.close()

"""
when record(), it will instrument the original input(java object, jython method, etc),
and won't return a WRAPPER like wrap()
"""
#cw = test1.wrap(c)
#print("record(): %s" % str(cw))
test1.record(foo)

# A TestRunner instance is created for each thread. It can be used to
# store thread-specific data.
class TestRunner:

    # This method is called for every run.
    def __call__(self):
        foo()

#if __name__ == "__main__" :
#    foo()

