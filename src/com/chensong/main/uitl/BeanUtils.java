package com.chensong.main.uitl;

import java.lang.reflect.Field;

public class BeanUtils {

    /**
     * 对象字段拷贝
     * @param origin
     * @param desti
     * @param <O>
     * @param <D>
     */
    public static <O,D> void deepCopy(O origin,D desti ){
        Field[] oFields = origin.getClass().getDeclaredFields();
        if(oFields != null && oFields.length > 0){
            for(Field of:oFields){
                Field[] dFields = desti.getClass().getDeclaredFields();
                for(Field df:dFields){
                    if(df.getClass() == of.getClass() && df.getName().equals(of.getName())){
                        try {
                            df.setAccessible(true);
                            of.setAccessible(true);
                            df.set(desti,of.get(origin));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
            }
        }
    }
}
