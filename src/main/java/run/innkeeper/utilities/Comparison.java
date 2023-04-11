package run.innkeeper.utilities;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Comparison {
    private static Method getGetter(Object obj, String propertyName) throws IntrospectionException {
        PropertyDescriptor descriptor = new PropertyDescriptor(propertyName, obj.getClass());
        return descriptor.getReadMethod();
    }

    public static List<String> compare(Object newObj, Object existingObj, String clazz, String... fields) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        List<String> changedFields = new ArrayList<>();
        if(newObj == null &&  existingObj == null){
            return changedFields;
        } else if(newObj == null ){
            return Arrays.asList(fields);
        } else if(existingObj == null){
            return Arrays.asList(fields);
        }
        if(newObj.getClass() != existingObj.getClass()){
            return changedFields;
        }
        for (String field : fields) {
            Object val = getGetter(newObj, field).invoke(newObj);
            Object existingVal = getGetter(existingObj, field).invoke(existingObj);
            if(existingVal!=null && val!=null && !val.equals(existingVal)){
                changedFields.add(clazz+"."+field);
            }
        }
        return changedFields;
    }
}
