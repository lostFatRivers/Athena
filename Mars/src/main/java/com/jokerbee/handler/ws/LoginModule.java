package com.jokerbee.handler.ws;

import com.jokerbee.anno.MessageHandler;
import com.jokerbee.handler.AbstractModule;
import com.jokerbee.player.Player;
import com.jokerbee.protocol.Game.*;
import com.jokerbee.protocol.MessageCode.Code;
import com.jokerbee.protocol.System.ProtocolWrapper;

public class LoginModule extends AbstractModule {

    @MessageHandler(code = Code.CS_ACCOUNT_LOGIN_VALUE)
    private void login(Player player, ProtocolWrapper wrapper) throws Exception {
        PlayerLogin loginMessage = PlayerLogin.parseFrom(wrapper.getBody());
        logger.info("player login:{}", loginMessage);
    }

}
