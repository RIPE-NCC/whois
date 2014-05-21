package net.ripe.db.whois.spec.domain

abstract class BasicResponse {
    String subject
    String contents

    def string = {
        def matcher = contents =~ it
        if (matcher.find()) {
            return matcher[0][1]
        }

        return "";
    }
    def strings = { (contents =~ it).collect { it[1] } }

    def number = {
        def s = string(it).toString()
        s == "" ? s : s.toInteger()
    }

    @Override
    public String toString() {
        return contents
    }
}
