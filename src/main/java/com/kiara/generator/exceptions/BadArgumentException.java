package com.kiara.generator.exceptions;

public class BadArgumentException extends Exception
{
    public BadArgumentException()
    {
        super();
    }
    
    public BadArgumentException(String message)
    {
        super(message);
    }
}
