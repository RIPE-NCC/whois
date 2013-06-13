package spec.domain

import javax.mail.internet.MimeMessage

class NotificationResponse extends Response {

    NotificationResponse(MimeMessage mimeMessage) {
        this.subject = mimeMessage.subject
        this.contents = mimeMessage.content.toString()
    }

    NotificationResponse(final String subject, final String contents) {
        this.subject = subject
        this.contents = contents
    }


    List<Object> getCreated() {
        (contents =~ /(?s)---\s*OBJECT BELOW CREATED:\s*([^:]*):([^\n]*)/).collect { new Object(type: it[1].trim(), key:  it[2].trim())}
    }

    List<Object> getFailedCreated() {
        (contents =~ /(?s)---\s*CREATE REQUESTED FOR:\s*([^:]*):([^\n]*)/).collect { new Object(type: it[1].trim(), key:  it[2].trim())}
    }

    def added(String type, String pkey, String new_str) {
        contents =~ "---\nOBJECT BELOW MODIFIED:\n+${type}:\\s*${pkey}" +
                "(.*\n)+?REPLACED BY:\n+${type}:\\s*${pkey}" +
                "(.*\n)*?${new_str}"

        !(contents =~ "---\nOBJECT BELOW MODIFIED:\n+${type}:\\s*${pkey}" +
                "(.*\n)*?${new_str}(.*\n)*?REPLACED BY:\n+${type}:\\s*${pkey}")
    }

    def removed(String type, String pkey, String old_str) {
        contents =~ "---\nOBJECT BELOW MODIFIED:\n+${type}:\\s*${pkey}" +
                "(.*\n)+?${old_str}" +
                "(.*\n)+?REPLACED BY:\n+${type}:\\s*${pkey}"

        !(contents =~ "---\nOBJECT BELOW MODIFIED:\n+${type}:\\s*${pkey}" +
                "(.*\n)+?REPLACED BY:\n+${type}:\\s*${pkey}" +
                "(.*\n)*?${old_str}.*?\n(.+\n)*?\n")
    }

    def changed(String type, String pkey, String old_str, String new_str) {
        added(type, pkey, new_str)
        removed(type, pkey, old_str)
    }

    List<Object> getModified() {
        (contents =~ /(?s)THIS IS THE NEW VERSION OF THE OBJECT:\s*([^:]*):([^\n]*)/).collect { new Object(type: it[1].trim(), key:  it[2].trim())}
    }

    List<Object> getFailedModified() {
        (contents =~ /(?s)---\s*MODIFY REQUESTED FOR:\s*([^:]*):([^\n]*)/).collect { new Object(type: it[1].trim(), key:  it[2].trim())}
    }

    List<Object> getDeleted() {
        (contents =~ /(?s)---\s*OBJECT BELOW DELETED:\s*([^:]*):([^\n]*)/).collect { new Object(type: it[1].trim(), key:  it[2].trim())}
    }

    List<Object> getFailedDeleted() {
        (contents =~ /(?s)---\s*DELETE REQUESTED FOR:\s*([^:]*):([^\n]*)/).collect { new Object(type: it[1].trim(), key:  it[2].trim())}
    }

    def authFailed(String operation, String type, String pkey) {
        contents =~ "---\n${operation} REQUESTED FOR:\n+${type}:\\s*${pkey}"
        contents =~ "\\*failed\\*"
        contents =~ "the proper authorisation"
    }

    def pendingAuth(String operation, String type, String pkey) {
        contents =~ "---\n${operation} REQUESTED FOR:\n+${type}:\\s*${pkey}"
        contents =~ "\\*exactly as shown\\*"
        contents =~ "This update must be completed within one week.\n"
    }

    class Object {
        String type;
        String key;
    }
}
