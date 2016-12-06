class Config {
    String jenkinsConfigRepo = ""
    List<String> emailRecipients = []
    List<String> slackChannels = []
    String slackTeam = ''
    String slackToken = ''
}

class ColorCode {
    static String SUCCESS = '#00FF00'
    static String FAIL = '#FF0000'
}

def notifyBuild(Config config, boolean buildSuccess) {
    setBuildColor(buildSuccess)
    // Send notifications
    String buildMessage = buildSuccess ? "SUCCESS" : "FAIL"
    sendSlack(config.slackTeam, config.slackToken, config.slackChannels, getNotificationColor(buildSuccess), buildMessage)
    sendEmail(config.emailRecipients, buildMessage)
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

def getConfig() {
    return new Config();
}

return this