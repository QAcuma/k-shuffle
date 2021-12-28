package ru.acuma.k.shuffler.service.commands;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.acuma.k.shuffler.cache.EventContextService;
import ru.acuma.k.shuffler.model.enums.Command;
import ru.acuma.k.shuffler.service.EventStateService;
import ru.acuma.k.shuffler.service.ExecuteService;
import ru.acuma.k.shuffler.service.MessageService;

import static ru.acuma.k.shuffler.model.enums.messages.MessageType.LOBBY;

@Component
public class LeaveCommand extends BaseBotCommand {

    private final EventContextService eventContextService;
    private final EventStateService eventStateService;
    private final MessageService messageService;
    private final ExecuteService executeService;

    public LeaveCommand(EventContextService eventContextService, EventStateService eventStateService, MessageService messageService, ExecuteService executeService) {
        super(Command.LEAVE.getCommand(), "Покинуть список участников");
        this.eventContextService = eventContextService;
        this.eventStateService = eventStateService;
        this.messageService = messageService;
        this.executeService = executeService;
    }

    @SneakyThrows
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (!eventContextService.isUserRegistered(chat.getId(), user)) {
            return;
        }
        var event = eventContextService.getEvent(chat.getId());

        eventContextService.unregisterPlayer(event.getChatId(), user);
        eventStateService.lobbyState(event);
        executeService.execute(absSender, messageService.updateLobbyMessage(event));
    }
}

