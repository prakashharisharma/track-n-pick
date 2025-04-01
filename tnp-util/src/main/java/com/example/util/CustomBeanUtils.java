package com.example.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

// @Slf4j
public class CustomBeanUtils {

    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }

    public static void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());

            if (srcValue == null) {
                emptyNames.add(pd.getName());
            } else if (srcValue instanceof Collection<?>) {
                Collection<?> list = (Collection<?>) srcValue;
                if (list.isEmpty()) {
                    emptyNames.add(pd.getName());
                }
            }
        }
        return emptyNames.toArray(new String[emptyNames.size()]);
    }

    public static Map<String, Object> getNonNullProperties(Object source) {
        Map<String, Object> attributes = new LinkedHashMap<>();

        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        // Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());

            if (srcValue != null) {
                // emptyNames.add(pd.getName());
                if (!pd.getName().equalsIgnoreCase("class")
                        && !pd.getName().equalsIgnoreCase("_id")
                        && !pd.getName().equalsIgnoreCase("id")
                        && !pd.getName().equalsIgnoreCase("parentId")
                        && !pd.getName().equalsIgnoreCase("sequence")
                        && !pd.getName().equalsIgnoreCase("createdBy")
                        && !pd.getName().equalsIgnoreCase("createdDate")
                        && !pd.getName().equalsIgnoreCase("_class")
                        && !pd.getName().equalsIgnoreCase("lastModifiedBy")
                        && !pd.getName().equalsIgnoreCase("lastModifiedDate")
                        && !pd.getName().equalsIgnoreCase("objectType")) {
                    attributes.put(pd.getName(), srcValue);
                }
            }
        }

        return attributes;
    }
}
