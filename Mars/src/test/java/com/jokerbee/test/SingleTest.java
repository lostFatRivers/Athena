package com.jokerbee.test;

import com.jokerbee.protocol.Game.*;
import com.jokerbee.protocol.MessageCode;
import com.jokerbee.protocol.System.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SingleTest {
    private static Logger logger = LoggerFactory.getLogger("Test");

    public static void main(String[] args) {
        PlayerLogin.Builder builder = PlayerLogin.newBuilder().setAccount("joker").setPassword("123321123321");

        ProtocolWrapper.Builder loginBuilder = ProtocolWrapper.newBuilder()
                .setCode(MessageCode.Code.CS_ACCOUNT_LOGIN_VALUE)
                .setBody(builder.build().toByteString());

        logger.info("byte[]: {}", loginBuilder.build().toByteArray());
    }
}
