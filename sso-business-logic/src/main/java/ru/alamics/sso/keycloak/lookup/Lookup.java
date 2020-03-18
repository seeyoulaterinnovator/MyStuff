package ru.alamics.sso.keycloak.lookup;

import lombok.extern.slf4j.Slf4j;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Slf4j
public class Lookup {

    public static Object lookup(Class clazz) {
        try {
            return new InitialContext().lookup("java:global/domru-sso/" + clazz.getSimpleName());
        } catch (NamingException e) {
            log.error("{}:", e);
        }
        return null;
    }
}
