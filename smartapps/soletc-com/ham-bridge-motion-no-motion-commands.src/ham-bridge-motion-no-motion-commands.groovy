/**
 *  HAM Bridge Motion/No Motion Commands 
 *
 *  Copyright 2014 Scottin Pollock
 *
 *
 */
definition(
    name: "HAM Bridge Motion/No Motion Commands",
    namespace: "soletc.com",
    author: "Scottin Pollock",
    description: "Sends HAM Bridge command on motion and another after interval of no motion.",
    category: "My Apps",
    iconUrl: "http://solutionsetcetera.com/stuff/STIcons/HB.png",
    iconX2Url: "http://solutionsetcetera.com/stuff/STIcons/HB@2x.png"
)


preferences {
	section("When someone's around because of...") {
		input name: "motionSensors", title: "Motion here", type: "capability.motionSensor", required: true
	}
	section("Send this command to HAM Bridge"){
		input "HAMBcommandOn", "text", title: "Command to send on Motion", required: true
	}
    section("Send this command to HAM Bridge after ? minutes"){
		input "HAMBcommandOff", "text", title: "Command to send on No Motion", required: true
        input "minutes", "number", title: "Minutes", required: true

	}
	section("Server address and port number"){
		input "server", "text", title: "Server IP", description: "Your HAM Bridger Server IP", required: true
		input "port", "number", title: "Port", description: "Port Number", required: true
    }
}

def installed() {
	subscribeToEvents()
}

def updated() {
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(motionSensors, "motion.active", motionActive)
	subscribe(motionSensors, "motion.inactive", motionInactive)
}

def motionActive(evt) {
	log.debug "$evt.name: $evt.value"
	sendOn()
	
}

def motionInactive(evt) {
	log.debug "$evt.name: $evt.value"
	if (allQuiet()) {
		sendOff()
	}
}

def allQuiet() {
	def result = true
	for (it in motionSensors) {
		if (it.currentMotion == "active") {
			result = false
			break
		}
	}
	return result
}


def sendOn() {
	sendHttpOn()
	unschedule("scheduledTurnOff")
}

def sendOff() {
	def delay = minutes * 60
	runIn(delay, "scheduledTurnOff")
}

def scheduledTurnOff() {
	sendHttpOff()
	unschedule("scheduledTurnOff") // Temporary work-around to scheduling bug
}


def sendHttpOn() {
def ip = "${settings.server}:${settings.port}"
sendHubCommand(new physicalgraph.device.HubAction("""GET /?${settings.HAMBcommandOn} HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN))
}

def sendHttpOff() {
def ip = "${settings.server}:${settings.port}"
sendHubCommand(new physicalgraph.device.HubAction("""GET /?${settings.HAMBcommandOff} HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN))
}