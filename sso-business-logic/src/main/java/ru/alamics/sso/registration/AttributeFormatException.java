package ru.alamics.sso.registration;

public class AttributeFormatException extends Exception{

    public AttributeFormatException(String attributeName) {
        super("Attribute " + attributeName + " is incorrect format");
    }
}
