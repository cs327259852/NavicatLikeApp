package com.chensong.main.support;

import com.chensong.main.entitys.MessageLocale;
import com.chensong.main.listener.SystemLanguageListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统语言事件对象
 */
public class SystemLanguage {

    /**
     * 系统语言事件监听者
     */
    private List<SystemLanguageListener> listenerList = new ArrayList<>();

    public boolean addListener(SystemLanguageListener listener){
        return listenerList.add(listener);
    }

    public void systemLanguageNotify(MessageLocale messageLocale){
        for(SystemLanguageListener listener:listenerList){
            listener.systemLanguageChanged(messageLocale);
        }
    }
}
