package ru.acuma.k.shuffler.service.commands;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.acuma.k.shuffler.cache.EventContextService;
import ru.acuma.k.shuffler.model.enums.Command;
import ru.acuma.k.shuffler.model.enums.EventState;
import ru.acuma.k.shuffler.model.enums.messages.MessageType;
import ru.acuma.k.shuffler.service.EventStateService;
import ru.acuma.k.shuffler.service.ExecuteService;
import ru.acuma.k.shuffler.service.MessageService;
import ru.acuma.k.shuffler.util.BuildMessageUtil;

import static ru.acuma.k.shuffler.model.enums.messages.MessageType.CHECKING_TIMED;

@Component
public class BeginCommand extends BaseBotCommand {

    private final EventContextService eventContextService;
    private final EventStateService eventStateService;
    private final ExecuteService executeService;
    private final MessageService messageService;

    public BeginCommand(EventContextService eventContextService, EventStateService eventStateService, ExecuteService executeService, MessageService messageService) {
        super(Command.BEGIN.getCommand(), "Начать турнир");
        this.eventContextService = eventContextService;
        this.eventStateService = eventStateService;
        this.executeService = executeService;
        this.messageService = messageService;
    }

    @SneakyThrows
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        var event = eventContextService.buildEvent(chat.getId());

        eventStateService.beginCheckState(event);
        executeService.execute(absSender, messageService.updateLobbyMessage(event));
        executeService.executeAsync(absSender, event, messageService.sendMessage(event, CHECKING_TIMED));
    }
}

