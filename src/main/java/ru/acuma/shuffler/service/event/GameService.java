package ru.acuma.shuffler.service.event;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.acuma.shuffler.exception.DataException;
import ru.acuma.shuffler.mapper.GameMapper;
import ru.acuma.shuffler.model.constant.ExceptionCause;
import ru.acuma.shuffler.model.constant.GameStatus;
import ru.acuma.shuffler.model.domain.TEvent;
import ru.acuma.shuffler.model.domain.TEventPlayer;
import ru.acuma.shuffler.model.domain.TGame;
import ru.acuma.shuffler.model.entity.Game;
import ru.acuma.shuffler.model.entity.Player;
import ru.acuma.shuffler.model.entity.Team;
import ru.acuma.shuffler.model.entity.TeamPlayer;
import ru.acuma.shuffler.repository.GameRepository;
import ru.acuma.shuffler.util.TimeMachine;

import javax.management.InstanceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final TeamService teamService;
    private final ShuffleService shuffleService;
    private final RatingService ratingService;
    private final EventService eventService;
    private final GameMapper gameMapper;
    private final GameRepository gameRepository;

    @SneakyThrows
    private TGame buildGame(TEvent event) {
        var players = Optional
            .ofNullable(shuffleService.shuffle(event))
            .orElseThrow(() -> new InstanceNotFoundException("Not enough players to start"));
        var redTeam = Optional
            .ofNullable(teamService.buildTeam(players))
            .orElseThrow(() -> new IllegalArgumentException("Red team is null"));
        var blueTeamPlayers = players.stream()
            .filter(Predicate.not(redTeam.getPlayers()::contains))
            .collect(Collectors.toList());
        var blueTeam = teamService.buildTeam(blueTeamPlayers);

        ratingService.applyBet(redTeam, blueTeam);

        return TGame.builder()
            .redTeam(redTeam)
            .blueTeam(blueTeam)
            .index(event.getTgGames().size() + 1)
            .order(event.getFinishedGames().size() + 1)
            .startedAt(TimeMachine.localDateTimeNow())
            .status(GameStatus.ACTIVE)
            .build();
    }

    public void beginGame(TEvent event) {
        var game = buildGame(event);
        event.applyGame(game);
    }

    public void finishGame(TEvent event) {
        var game = event.getCurrentGame();
        switch (game.getStatus()) {
            case RED_CHECKING -> {
                game.getRedTeam().setIsWinner(Boolean.TRUE);
                finishGameWithWinner(game);
                increasePlayersGameCount(game);
            }
            case BLUE_CHECKING -> {
                game.getBlueTeam().setIsWinner(Boolean.TRUE);
                finishGameWithWinner(game);
                increasePlayersGameCount(game);
            }
            case CANCELLED, CANCEL_CHECKING, EVENT_CHECKING -> finishCancelledGame(game);
        }
        game.getPlayers().forEach(TEventPlayer::increaseGameCount);
    }

    private void increasePlayersGameCount(final TGame game) {
        game.getPlayers().forEach(TEventPlayer::increaseGameCount);
    }

    private void finishGameWithWinner(final TGame game) {
        game.setStatus(GameStatus.FINISHED)
            .setFinishedAt(TimeMachine.localDateTimeNow());
        game.getWinnerTeam().applyRating(game.getId());
        game.getLoserTeam().applyRating(game.getId());
        teamService.fillLastGameMate(game.getWinnerTeam());
        teamService.fillLastGameMate(game.getLoserTeam());
    }

    private void finishCancelledGame(final TGame game) {
        game.setFinishedAt(TimeMachine.localDateTimeNow());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Game find(final Long id) {
        return gameRepository.findById(id)
            .orElseThrow(() -> new DataException(ExceptionCause.GAME_NOT_FOUND, id));
    }

    @Transactional
    public void save(final TGame game, Long eventId) {
        var event = eventService.getReference(eventId);
        var mappedGame = gameMapper.toGame(game, event);
        gameRepository.save(mappedGame);
        game.setId(mappedGame.getId());

        var redTeam = getTeamBySide(mappedGame, game.getRedTeam().getPlayers());
        var blueTeam = getTeamBySide(mappedGame, game.getBlueTeam().getPlayers());

        game.getRedTeam().setId(redTeam.getId());
        game.getBlueTeam().setId(blueTeam.getId());
    }

    private Team getTeamBySide(final Game mappedGame, final List<TEventPlayer> players) {
        return mappedGame.getTeams().stream()
            .filter(team -> hasAnyPlayer(team, players))
            .findFirst()
            .orElseThrow(() -> new DataException(ExceptionCause.MISSING_WINNER_TEAM));
    }

    private boolean hasAnyPlayer(final Team team, final List<TEventPlayer> players) {
        var playersId = players.stream()
            .map(TEventPlayer::getId)
            .toList();

        return team.getTeamPlayers().stream()
            .map(TeamPlayer::getPlayer)
            .map(Player::getId)
            .anyMatch(playersId::contains);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void update(final TGame game) {
        var savedGame = find(game.getId());
        gameMapper.updateGame(savedGame, game);
    }
}
