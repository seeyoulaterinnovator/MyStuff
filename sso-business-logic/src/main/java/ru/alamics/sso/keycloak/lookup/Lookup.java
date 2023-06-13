package ru.alamics.sso.keycloak.lookup;

import lombok.extern.slf4j.Slf4j;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Slf4j
public class Lookup {

    public static <T> T lookup(Class<T> clazz) {
        try {
            return clazz.cast( new InitialContext().lookup("java:global/domru-sso/" + clazz.getSimpleName()));
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }

    /**
     *
     * @param classToCast класс к которому нужно преобразовать найденный бин
     * @param className название бина, или его псевдоним, который нужно найти; псевдоним бина задаётся через аттрибуты аннотации, например, @Stateless(name="<Псевдоним>")
     * @return найденный бин приведённый к классу classToCast
     */
    public static <T> T lookup(Class<T> classToCast, String className) {
        try {
            return classToCast.cast(new InitialContext().lookup("java:global/domru-sso/" + className));
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }
}
