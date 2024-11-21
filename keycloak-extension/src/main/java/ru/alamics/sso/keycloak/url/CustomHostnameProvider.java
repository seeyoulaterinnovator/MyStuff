package ru.alamics.sso.keycloak.url;

import jakarta.ws.rs.core.UriInfo;
import org.keycloak.url.HostnameV2Provider;
import org.keycloak.urls.HostnameProvider;
import org.keycloak.urls.UrlType;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;

public class CustomHostnameProvider implements HostnameProvider {
    final HostnameV2Provider delegate;

    final ApplicationProperties properties;
    
    public CustomHostnameProvider(HostnameV2Provider delegate) {
        this.delegate = delegate;
        this.properties = Lookup.lookup(ApplicationProperties.class);
    }

    @Override
    public String getScheme(UriInfo originalUriInfo, UrlType type) {
        return delegate.getScheme(originalUriInfo, type);
    }

    @Override
    public String getScheme(UriInfo originalUriInfo) {
        return delegate.getScheme(originalUriInfo);
    }

    @Override
    public String getHostname(UriInfo originalUriInfo, UrlType type) {
        String hostname = delegate.getHostname(originalUriInfo, type);
        String originalHost = originalUriInfo.getBaseUri().getHost();
        if(hostname != null && !hostname.equals(originalHost)
                && properties.getPropertyList("hostname.allowedOrigins").contains(originalHost)) {
            return originalHost;
        }
        return hostname;
    }

    @Override
    public String getHostname(UriInfo originalUriInfo) {
        return delegate.getHostname(originalUriInfo);
    }

    @Override
    public int getPort(UriInfo originalUriInfo, UrlType type) {
        return delegate.getPort(originalUriInfo, type);
    }

    @Override
    public int getPort(UriInfo originalUriInfo) {
        return delegate.getPort(originalUriInfo);
    }

    @Override
    public String getContextPath(UriInfo originalUriInfo, UrlType type) {
        return delegate.getContextPath(originalUriInfo, type);
    }

    @Override
    public String getContextPath(UriInfo originalUriInfo) {
        return delegate.getContextPath(originalUriInfo);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
