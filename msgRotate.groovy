 /*
 * Message Rotator Tile Device 
 *
 *  Licensed Virtual the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----

 */

static String version()	{  return '0.0.2'  }

metadata {
    definition (
		name: "Message Rotator Tile Device", 
		namespace: "thebearmay", 
		author: "Jean P. May, Jr.",
	        importUrl:"https://raw.githubusercontent.com/thebearmay/hubitat/main/msgRotate.groovy"
	) {
        capability "Actuator"
        attribute "html", "string"
       
        command "addMessage", [[name:"msgID*", type:"STRING", description:"Message ID Tag"],[name:"msgContent*", type:"STRING", description:"Message Content"]]
        command "remMessage", [[name:"msgID*", type:"STRING", description:"Message ID Tag"]]
        command "clearAll"
    }   
}

preferences {
	input("debugEnable", "bool", title: "Enable debug logging?", width:4)
    input("cycleTime", "number", title: "Number of seconds to display each message", defaultValue:3, width:4)
}

def installed() {
    log.trace "${device.properties.displayName} v${version()} installed()"
    state.messages = [:]
    sendEvent(name:"html", value:"No Current Messages")
}

def updated(){
    log.trace "updated()"
    if(debugEnable) runIn(1800,logsOff)
}

void addMessage(mID, mContent){
    if(state.messages == null)  state.messages = [:]
    state.messages["$mID"] = "$mContent"
    if(debugEnabled) log.debug "Number of Messages: ${state.messages.size()}<br>${state.messages}"
    if(cycleTime == null) updateSetting("cycleTime",[type:"number", value:3])
    unschedule("cycleMessages")
    cycleMessages(state.messages.size() - 1)
}

void remMessage(mID){ 
    msgs = [:]
    state.messages.each{
        msgs[it.key]=it.value
    }
    state.messages = [:]
    msgs.each {
        if(it.key != "$mID")
            state.messages[it.key]=it.value
    }
    if(state.messages == null) {
        unschedule("cycleMessages")
        state.messages = [:]
        sendEvent(name:"html", value:"No Current Messages")
    } 
}

void clearAll(){
    state.messages = [:]
    unschedule("cycleMessages")
    sendEvent(name:"html", value:"No Current Messages")
}

void cycleMessages(inx){
    if(data) log.debug "Data: ${data.toInteger()}" 
    if(inx == null) inx = data.toInteger()
    inx = inx.toInteger()
    messageList = state.messages.collect{entry -> entry.value}
    if(debugEnabled) log.debug "$messageList"
    if(debugEnabled) log.debug "${messageList[inx.toInteger()]}"
    sendEvent(name:"html", value:"${messageList[inx.toInteger()]}")
    inx++ 
    if(debugEnabled) log.debug "Inx++ $inx"
    if(inx >= state.messages.size()) inx = 0
    if(debugEnabled) log.debug "Inx post size check: $inx"
    if(state.messages.size() > 1)  
        runIn(cycleTime,"cycleMessages",[data:"$inx"])
}

void logsOff(){
     device.updateSetting("debugEnable",[value:"false",type:"bool"])
}
