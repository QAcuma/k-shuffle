package ru.acuma.shuffler.service.command;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.acuma.shuffler.controller.BaseBotCommand;
import ru.acuma.shuffler.service.executor.Executor;


public abstract class CommandHandler<T extends BaseBotCommand> {

    @Autowired
    protected Executor executor;

    @PostConstruct
    protected abstract void init();

    public abstract Class<T> getCommandClass();

    public void handle(Message message) {
        executor.doExecute(message, getCommandClass());
    }

}
