//public class Config {
//    static List<String> emailRecipients = []
//    static List<String> slackChannels = []
//    static String slackTeam = ''
//    static String slackToken = ''
//}

public class ColorCode {
    static String SUCCESS = '#00FF00'
    static String FAIL = '#FF0000'
}

def notifyBuild(List<String> emailRecipients, String slackTeam, String slackToken, List<String> slackChannels, boolean buildSuccess) {
    setBuildColor(buildSuccess)
    // Send notifications
    String buildMessage = buildSuccess ? "SUCCESS" : "FAIL"
    sendSlack(slackTeam, slackToken, slackChannels, getNotificationColor(buildSuccess), buildMessage)
    sendEmail(emailRecipients, buildMessage)
}

def setBuildColor(boolean buildSuccess) {
    color = buildSuccess ? 'GREEN' : 'RED'
}

def getNotificationColor(boolean buildSuccess) {
    return buildSuccess ? ColorCode.SUCCESS : ColorCode.FAIL
}

def sendSlack(String slackTeam, String slackToken, List<String> slackChannels, String colorCode, String statusString) {
    def summary = "${buildNotificationSubject(statusString)} (${env.BUILD_URL})"

    for(channel in slackChannels) {
        slackSend channel: channel, color: colorCode, message: summary, teamDomain: slackTeam, token: slackToken
    }
}

def sendEmail(List<String> emailRecipients, String statusString) {
    if(emailRecipients.size() > 0) {
        String recipients = emailRecipients.join(",");
        emailext (
                subject: buildNotificationSubject(statusString),
                body: buildEmailBody(statusString),
                recipientProviders: [[$class: 'DevelopersRecipientProvider']],
                to: recipients
        )
    }
}

def buildEmailBody(String statusString) {
    return """<p>${statusString}: <a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>"""
}

def buildNotificationSubject(String statusString) {
    return "${statusString}: ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
}

def testLoad() {
    System.out.println("Test Load success")
}

return this