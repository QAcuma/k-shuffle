package ru.acuma.shuffler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.acuma.shuffler.model.enums.Command;
import ru.acuma.shuffler.service.command.CommandHandler;

@Component
public class NoCommand extends BaseBotCommand {

    private CommandHandler<NoCommand> commandHandler;

    public NoCommand() {
        super(Command.NO.getCommand(), "Нет");
    }

    @Autowired
    public void setCommandService(@Lazy CommandHandler<NoCommand> commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void execute(Message message) {
        commandHandler.handle(message);
    }
}

