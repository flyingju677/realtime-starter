package com.realtimes.framework.internal.message;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultMessageRouter extends AbstractMessageRouter {

    private static final Logger log = LoggerFactory.getLogger(DefaultMessageRouter.class);

    @Override
    protected String extractMessageType(String message) {
        try {
            JSONObject root = JSON.parseObject(message);
            Object msgType = root.get("msg_type");
            if (msgType == null) {
                msgType = root.get("messageType");
            }
            return msgType == null ? null : String.valueOf(msgType);
        } catch (Exception e) {
            log.error("Error extracting message type from message: {}", message, e);
            return null;
        }
    }
}

