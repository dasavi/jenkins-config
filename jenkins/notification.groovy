public class Config {
    static List<String> emailRecipients = []
    static List<String> slackChannels = []
    static String slackTeam = ''
    static String slackToken = ''
}

public class ColorCode {
    static String SUCCESS = '#00FF00'
    static String FAIL = '#FF0000'
}

def notifyBuild(Config config, boolean buildSuccess) {
    setBuildColor(buildSuccess)
    // Send notifications
    String buildMessage = buildSuccess ? "SUCCESS" : "FAIL"
    sendSlack(getNotificationColor(buildSuccess), buildMessage)
    sendEmail(buildMessage)
}

def setBuildColor(boolean buildSuccess) {
    color = buildSuccess ? 'GREEN' : 'RED'
}

def getNotificationColor(boolean buildSuccess) {
    return buildSuccess ? ColorCode.SUCCESS : ColorCode.FAIL
}

def sendSlack(Config config, String colorCode, String statusString) {
    def summary = "${buildNotificationSubject(statusString)} (${env.BUILD_URL})"

    for(channel in config.slackChannels) {
        slackSend channel: channel, color: colorCode, message: summary, teamDomain: config.slackTeam, token: config.slackToken
    }
}

def sendEmail(Config config, String statusString) {
    if(config.emailRecipients.size() > 0) {
        String recipients = config.emailRecipients.join(",");
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

return this