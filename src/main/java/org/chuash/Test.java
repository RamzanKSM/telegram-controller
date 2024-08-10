package org.chuash;

import org.chuash.annotation.CallBackQueryMapping;
import org.chuash.annotation.UpdateController;
import org.chuash.annotation.MessageMapping;
import org.telegram.telegrambots.meta.api.objects.Update;

@UpdateController
public class Test {

    @MessageMapping("justGet")
    public void get(Update update) {
        System.out.println("JustGet");
    }

    @MessageMapping(value = "getWithRegex", isRegex = true)
    public void getter(Update update) {
        System.out.println("getWithRegex");
    }
    @CallBackQueryMapping(value = "justCallBackQuery")
    public void setter(Update update) {
        System.out.println("justCallBackQuery");
    }
    @CallBackQueryMapping(value = "callBackQueryRegex", isRegex = true)
    public void callBackQueryRegex(Update update) {
        System.out.println("callBackQueryRegex");
    }
}
