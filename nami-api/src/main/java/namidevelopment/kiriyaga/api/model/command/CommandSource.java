package namidevelopment.kiriyaga.api.model.command;

import static namidevelopment.kiriyaga.api.NamiApi.CAT_FORMAT;
import static namidevelopment.kiriyaga.api.NamiApi.CHAT_SERVICE;

public class CommandSource {

    public void info(String msg) {
        CHAT_SERVICE.sendPersistent("Command", CAT_FORMAT.format("{gray}"+msg));
    }

    public void error(String msg) {
        CHAT_SERVICE.sendPersistent("Command", CAT_FORMAT.format("{red}" + msg));
    }
}