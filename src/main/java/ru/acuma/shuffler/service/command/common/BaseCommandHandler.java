package ru.acuma.shuffler.service.command.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.acuma.shuffler.context.EventContext;
import ru.acuma.shuffler.context.RenderContext;
import ru.acuma.shuffler.context.StorageContext;
import ru.acuma.shuffler.controller.BaseBotCommand;
import ru.acuma.shuffler.model.constant.EventStatus;
import ru.acuma.shuffler.model.domain.TEvent;

import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class BaseCommandHandler<T extends BaseBotCommand> {

    @Autowired
    protected EventContext eventContext;

    @Autowired
    protected RenderContext renderContext;

    @Autowired
    protected StorageContext storageContext;

    protected abstract List<EventStatus> getSupportedStatuses();

    public final void handle(final Message message, final String... args) {
        Optional.ofNullable(eventContext.findEvent(message.getChatId()))
            .ifPresentOrElse(
                event -> processMessage(message, args, event),
                () -> invokeChatCommand(message, args)
            );
    }

    private void processMessage(Message message, String[] args, TEvent event) {
        Optional.of(event)
            .filter(this::isCommandSupported)
            .ifPresent(activeEvent -> {
                var statusBefore = activeEvent.getEventStatus();
                invokeEventCommand(message.getFrom(), activeEvent, args);
                log.info("User: {}, command: {}, status: {} -> {}", message.getFrom().getUserName(), getClass().getSimpleName(), statusBefore, activeEvent.getEventStatus());
            });
    }

    private boolean isCommandSupported(final TEvent event) {
        return getSupportedStatuses().contains(EventStatus.ANY) || getSupportedStatuses().contains(event.getEventStatus());
    }

    protected void invokeChatCommand(final Message message, final String[] args) {
        log.debug("Event is missing or chat command not supported");
    }

    protected final void idle(final EventStatus eventStatus) {
        log.debug("Idle command %s in event status %s".formatted(getClass().getSimpleName(), eventStatus));
    }

    protected abstract void invokeEventCommand(final User user, final TEvent event, String[] args);
}
