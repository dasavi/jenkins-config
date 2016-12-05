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

public enum BuildStatusCode {
    SUCCESS,
    FAIL
}

def notifyBuild(BuildStatusCode buildStatus = BuildStatusCode.FAIL) {
    setBuildColor(buildStatus)
    // Send notifications
    sendSlack(getNotificationColor(buildStatus), buildStatus.toString())
    sendEmail(buildStatus.toString())
}

def setBuildColor(BuildStatusCode statusCode) {
    color = statusCode.equals(BuildStatusCode.SUCCESS) ? 'GREEN' : 'RED'
}

def getNotificationColor(BuildStatusCode statusCode) {
    return statusCode.equals(BuildStatusCode.SUCCESS) ? ColorCode.SUCCESS : ColorCode.FAIL
}

def sendSlack(String colorCode, String statusString) {
    def summary = "${buildNotificationSubject(statusString)} (${env.BUILD_URL})"

    for(channel in Config.slackChannels) {
        slackSend channel: channel, color: colorCode, message: summary, teamDomain: Config.slackTeam, token: Config.slackToken
    }
}

def sendEmail(String statusString) {
    if(Config.emailRecipients.size() > 0) {
        String recipients = Config.emailRecipients.join(",");
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