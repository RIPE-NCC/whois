package net.ripe.db.whois.query.support;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;

public class MockFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> mock;

    public MockFactoryBean(final Class<T> mock) {
        this.mock = mock;
    }

    @Override
    public T getObject() throws Exception {
        return Mockito.mock(mock);
    }

    @Override
    public Class<?> getObjectType() {
        return mock;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
