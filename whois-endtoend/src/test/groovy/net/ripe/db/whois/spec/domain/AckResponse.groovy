package net.ripe.db.whois.spec.domain

import com.google.common.base.Splitter
import com.google.common.collect.Lists

import javax.mail.internet.MimeMessage
import java.util.regex.Pattern

class AckResponse extends Response {

    AckResponse(MimeMessage mimeMessage) {
        this.subject = mimeMessage.subject
        this.contents = mimeMessage.content.toString()
    }

    AckResponse(String subject, String contents) {
        this.subject = subject
        this.contents = contents
    }

    def getSuccess() {
        subject =~ "^SUCCESS"
    }

    def getFailed() {
        subject =~ "^FAILED"
    }

    def getQuote() {
        return new Quote()
    }

    def getSummary() {
        return new Summary()
    }

    def getAllInfos() { strings(/(?m)^\*\*\*Info:\s*(.*)$/) }

    def getAllWarnings() { strings(/(?m)^\*\*\*Warning:\s*(.*)$/) }

    def getAllErrors() { strings(/(?m)^\*\*\*Error:\s*(.*)$/) }

    String getErrorSection() { string(/(?s)\~+.*?ERRORS:\s*(.*?\s*\~+)/) }

    String getSuccessSection() { string(/(?s)\~+.*?SUCCESSFULLY:\s*(.*?\s*\~+)/) }

    String getParagraphSection() { string(/(?s)\~+.*?NOT PROCESSED:(.*?)\s*\~+/) }

    List<Error> getErrors() {
        def split = Lists.newArrayList(Splitter.on(Pattern.compile("(?m)(\n|^)---\n")).omitEmptyStrings().trimResults().split(errorSection));

        split.findResults {
            def matcher = it =~ /(?s)\s*(.*?)\s*?FAILED:\s*([^\n]*)\n\s*(.*)/

            if (!matcher.matches()) {
                return null
            }
//            println "ERRORS section string[\n" + matcher.group(3) + "\n]"

            List<String> errors = (matcher.group(3) =~ /(?m)^\*\*\*Error:\s*((.*)(\n[ ]+.*)*)$/).collect(removeNewLines)
            List<String> warnings = (matcher.group(3) =~ /(?m)^\*\*\*Warning:\s*((.*)(\n[ ]+.*)*)$/).collect(removeNewLines)
            List<String> infos = (matcher.group(3) =~ /(?m)^\*\*\*Info:\s*((.*)(\n[ ]+.*)*)$/).collect (removeNewLines)

            new Error(operation: matcher.group(1).trim(), key: matcher.group(2).trim(), object: matcher.group(3).trim(), errors: errors, warnings: warnings, infos: infos)
        } as List<Error>
    }

    def removeNewLines = { it[1].toString().replaceAll("[\\n ]+", " ") }

    // TODO: [AH] no method for testing if an error is per-attribute (i.e., tied to a specific attribute)
    String[] errorMessagesFor(String operation, String key) {
        def error = getErrors().find { it.operation == operation && it.key.startsWith(key) }
        error == null ? [] : error.errors
    }

    String[] warningMessagesFor(String operation, String key) {
        def error = getErrors().find { it.operation == operation && it.key.startsWith(key) }
        error == null ? [] : error.warnings
    }

    String[] infoMessagesFor(String operation, String key) {
        def error = getErrors().find { it.operation == operation && it.key.startsWith(key) }
        error == null ? [] : error.infos
    }

    List<Success> getSuccesses() {
        def split = Lists.newArrayList(Splitter.on("---").omitEmptyStrings().trimResults().split(successSection));

        split.findResults {
            def matcher = it =~ /(?s)\s*(.*?)\s*[SUCCEEDED]*:\s*([^\n]*)\n?\s*(.*)/

            if (!matcher.matches()) {
                return null
            }

//            println "SUCCESS section string[\n" + matcher.group(3) + "\n]"

            List<String> warnings = (matcher.group(3) =~ /(?m)^\*\*\*Warning:\s*((.*)(\n[ ]+.*)*)$/).collect(removeNewLines)
            List<String> infos = (matcher.group(3) =~ /(?m)^\*\*\*Info:\s*((.*)(\n[ ]+.*)*)$/).collect(removeNewLines)
            new Success(operation: matcher.group(1).trim(), key: matcher.group(2).trim(), object: matcher.group(3).trim(), warnings: warnings, infos: infos)
        } as List<Success>
    }

    List<Success> getPendingUpdates() {
        def split = Lists.newArrayList(Splitter.on("---").omitEmptyStrings().trimResults().split(successSection));

        split.findResults {
            def matcher = it =~ /(?s)\s*(.*?)\s*[PENDING]*:\s*([^\n]*)\n?\s*(.*)/

            if (!matcher.matches()) {
                return null
            }

//            println "PENDING section string[\n" + matcher.group(3) + "\n]"

            List<String> warnings = (matcher.group(3) =~ /(?m)^\*\*\*Warning:\s*((.*)(\n[ ]+.*)*)$/).collect(removeNewLines)
            List<String> infos = (matcher.group(3) =~ /(?m)^\*\*\*Info:\s*((.*)(\n[ ]+.*)*)$/).collect(removeNewLines)
            new Success(operation: matcher.group(1).trim(), key: matcher.group(2).trim(), object: matcher.group(3).trim(), warnings: warnings, infos: infos)
        } as List<Success>
    }

    String[] warningSuccessMessagesFor(String operation, String key) {
        def success = getSuccesses().find { it.operation == operation && it.key.startsWith(key) }
        success == null ? [] : success.warnings
    }

    String[] infoSuccessMessagesFor(String operation, String key) {
        def success = getSuccesses().find { it.operation == operation && it.key.startsWith(key) }
        success == null ? [] : success.infos
    }

    String[] warningPendingMessagesFor(String operation, String key) {
        def pending = getPendingUpdates().find { it.operation == operation && it.key.startsWith(key) }
        pending == null ? [] : pending.warnings
    }

    String[] infoPendingMessagesFor(String operation, String key) {
        def pending = getPendingUpdates().find { it.operation == operation && it.key.startsWith(key) }
        pending == null ? [] : pending.infos
    }

    def objErrorContains(String op, String result, String objType, String key, String errorStr) {
        def error = errorSection
        error =~ /(?is)---\s*${op} ${result}: \[${objType}\] ${key}\n*(.*?)${errorStr}.*?[---\n|~~~\n]/
    }

    def authFailCheck(String op, String result, String objType, String key, String parent, String authType, String authObj, String mntType, String mnt) {
        def error = errorSection
        println "\nerrorSection [\n${error}\n]\n"
        error =~ /(?is)---\s*${op} ${result}: \[${objType}\] ${key}\n*(.*?)\*\*\*Error:   Authorisation for\s*(${parent})?\s*\[${authType}\]\s*${authObj}\s*failed\s*using "${mntType}:"\s*not authenticated by:\s*${mnt}.*?[---\n|~~~\n]/
    }

    def garbageContains(String garbageStr) {
        def garbage = paragraphSection
        garbage =~ /${garbageStr}/
    }

    void countErrorWarnInfo(int expectedErrorCount, int expectedWarningCount, int expectedInfoCount) {
        assert getAllErrors().size() == expectedErrorCount
        assert getAllWarnings().size() == expectedWarningCount
        assert getAllInfos().size() == expectedInfoCount
    }

    class Quote {
        def getFrom() { string(/>\s*From:\s*(.*)/) }

        def getSubject() { string(/>\s*Subject:\s*(.*)/) }

        def getDate() { string(/>\s*Date:\s*(.*)/) }

        def getReplyTo() { string(/>\s*Reply\-To:\s*(.*)/) }

        def getMessageId() { string(/>\s*Message\-ID:\s*(.*)/) }
    }

    class Summary {
        def getNrFound() { number(/Number of objects found:\s*(\d+)/) }

        def getNrSuccess() { number(/Number of objects processed successfully:\s*(\d+)/) }

        def getNrSuccessCreate() { number(/(?s)successfully:.*?Create:\s*(\d+)/) }

        def getNrSuccessModify() { number(/(?s)successfully:.*?Modify:\s*(\d+)/) }

        def getNrSuccessDelete() { number(/(?s)successfully:.*?Delete:\s*(\d+)/) }

        def getNrSuccessNoop() { number(/(?s)successfully:.*?No Operation:\s*(\d+)/) }

        def getNrErrors() { number(/Number of objects processed with errors:\s*(\d+)/) }

        def getNrErrorsCreate() { number(/(?s)errors:.*?Create:\s*(\d+)/) }

        def getNrErrorsModify() { number(/(?s)errors:.*?Modify:\s*(\d+)/) }

        def getNrErrorsDelete() { number(/(?s)errors:.*?Delete:\s*(\d+)/) }

        void assertSuccess(int total, int create, int modify, int delete, int noop) {
            assert total == nrSuccess
            assert create == nrSuccessCreate
            assert modify == nrSuccessModify
            assert delete == nrSuccessDelete
            assert noop == nrSuccessNoop
        }

        void assertErrors(int total, int create, int modify, int delete) {
            assert total == nrErrors
            assert create == nrErrorsCreate
            assert modify == nrErrorsModify
            assert delete == nrErrorsDelete
        }
    }

    class Error {
        String operation
        String key
        String object
        List<String> errors
        List<String> warnings
        List<String> infos
    }

    class Success {
        String operation
        String key
        String object
        List<String> warnings
        List<String> infos
    }
}
