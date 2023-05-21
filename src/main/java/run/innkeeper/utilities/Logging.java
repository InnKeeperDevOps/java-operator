package run.innkeeper.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logging {
    private static ObjectMapper om = new ObjectMapper(){{
        this.registerModule(new JavaTimeModule());
    }};

    public static StackTraceElement getStack(){
        return Thread.currentThread().getStackTrace()[3];
    }
    public static void debug(String message){
        StackTraceElement stackTraceElement = getStack();
        LoggerFactory
            .getLogger(stackTraceElement.getClassName())
            .debug("Line "+stackTraceElement.getLineNumber()+" \""+message+"\"");
    }

    public static void error(String message){
        StackTraceElement stackTraceElement = getStack();
        LoggerFactory
            .getLogger(stackTraceElement.getClassName())
            .error("Line "+stackTraceElement.getLineNumber()+" \""+message+"\"");
    }
    public static void info(String message){
        StackTraceElement stackTraceElement = getStack();
        LoggerFactory
            .getLogger(stackTraceElement.getClassName())
            .info("Line "+stackTraceElement.getLineNumber()+" \""+message+"\"");
    }
    public static void info(Object obj){
        try {
            info(om.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            error(e.getMessage());
        }
    }
    public static void debug(Object obj){
        try {
            debug(om.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            error(e.getMessage());
        }
    }
    public static void error(Object obj){
        try {
            error(om.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            error(e.getMessage());
        }
    }
}
