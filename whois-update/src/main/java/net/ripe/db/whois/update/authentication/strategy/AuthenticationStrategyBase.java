package net.ripe.db.whois.update.authentication.strategy;

abstract class AuthenticationStrategyBase implements AuthenticationStrategy {
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public int compareTo(AuthenticationStrategy other) {
        return getName().compareTo(other.getName());
    }

}
