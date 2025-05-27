package net.ripe.db.whois.query.support;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;

public class SpyFactoryBean<T> implements FactoryBean<T> {
    private final Class<T> spy;

    public SpyFactoryBean(final Class<T> spy) {
        this.spy = spy;
    }

    @Override
    public T getObject() throws Exception {
        return Mockito.spy(spy);
    }

    @Override
    public Class<?> getObjectType() {
        return spy;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
