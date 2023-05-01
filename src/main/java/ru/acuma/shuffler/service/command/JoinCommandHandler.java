package ru.acuma.shuffler.service.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.acuma.shuffler.controller.JoinCommand;
import ru.acuma.shuffler.model.constant.EventStatus;
import ru.acuma.shuffler.model.constant.messages.MessageType;
import ru.acuma.shuffler.model.domain.TRender;
import ru.acuma.shuffler.model.domain.TEvent;
import ru.acuma.shuffler.service.event.EventStatusService;
import ru.acuma.shuffler.service.telegram.PlayerService;

import java.util.List;
import java.util.Optional;

import static ru.acuma.shuffler.model.constant.EventStatus.CREATED;
import static ru.acuma.shuffler.model.constant.EventStatus.PLAYING;
import static ru.acuma.shuffler.model.constant.EventStatus.READY;
import static ru.acuma.shuffler.model.constant.EventStatus.WAITING;
import static ru.acuma.shuffler.model.constant.EventStatus.WAITING_WITH_GAME;

@Service
@RequiredArgsConstructor
public class JoinCommandHandler extends BaseCommandHandler<JoinCommand> {

    private final EventStatusService eventStateService;
    private final GameFacade gameFacade;
    private final PlayerService playerService;

    @Override
    protected List<EventStatus> getSupportedStatuses() {
        return List.of(CREATED, READY, PLAYING, WAITING, WAITING_WITH_GAME);
    }

    @Override
    protected void invokeEventCommand(final User user, final TEvent event, final String... args) {
        playerService.join(user, event);

        switch (event.getEventStatus()) {
            case CREATED, READY -> onPreparing(event);
            case PLAYING, WAITING_WITH_GAME -> onPlaying(event);
            case WAITING -> onWaiting(event);
            default -> idle();
        }
    }

    private void onPreparing(final TEvent event) {
        eventStateService.prepare(event);

        event.render(TRender.forUpdate(MessageType.LOBBY, event.getMessageId(MessageType.LOBBY)));
    }

    private void onPlaying(final TEvent event) {
        eventStateService.resume(event);

        event.render(TRender.forUpdate(MessageType.LOBBY, event.getMessageId(MessageType.LOBBY)));
    }

    private void onWaiting(final TEvent event) {
        Optional.of(eventStateService.resume(event))
            .filter(PLAYING::equals)
            .ifPresent(status -> {
                gameFacade.nextGameActions(event);
                event.render(TRender.forSend(MessageType.GAME));
            });
    }
}
