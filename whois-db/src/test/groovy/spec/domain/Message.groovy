package spec.domain

class Message {
    String from = "me@ripe.net"
    String subject = ""
    String body = ""

    @Override
    public String toString() {
        return from + "\n" + subject + "\n" + body + "\n"
    }
}
