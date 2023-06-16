package ru.acuma.shuffler.service.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.acuma.shuffler.context.cotainer.StorageTask;
import ru.acuma.shuffler.mapper.GameMapper;
import ru.acuma.shuffler.model.constant.StorageTaskType;
import ru.acuma.shuffler.model.domain.TGame;
import ru.acuma.shuffler.model.entity.Event;
import ru.acuma.shuffler.repository.GameRepository;
import ru.acuma.shuffler.repository.ReferenceService;

@Service
@RequiredArgsConstructor
public class GameBegins extends Storable<TGame> {

    private final GameMapper gameMapper;
    private final GameRepository gameRepository;
    private final ReferenceService referenceService;

    @Override
    public StorageTaskType getTaskType() {
        return StorageTaskType.GAME_BEGINS;
    }

    @Override
    public void store(final StorageTask<TGame> storageTask) {
        var event = eventContext.findEvent(storageTask.getChatId());
        var eventReference = referenceService.getReference(Event.class, event.getId());
        var currentGame = storageTask.getSubject();
        var game = gameMapper.toGame(currentGame, eventReference);

        gameRepository.save(game);
        currentGame.setId(game.getId());
    }
}
