package net.ripe.db.whois.common.oauth.filter;

public record KeycloakUserRepresentation(String id, String username,
                                         String firstName, String lastName,
                                         String email, Boolean emailVerified,
                                         Boolean enabled) {

}
