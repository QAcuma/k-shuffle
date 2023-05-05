package ru.acuma.shuffler.service.message;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.acuma.shuffler.model.constant.messages.EventConstant;
import ru.acuma.shuffler.model.constant.messages.MessageType;
import ru.acuma.shuffler.model.domain.TEvent;
import ru.acuma.shuffler.model.domain.TEventPlayer;
import ru.acuma.shuffler.model.domain.TGame;

import java.util.Comparator;
import java.util.stream.Collectors;

import static ru.acuma.shuffler.model.constant.EventStatus.WAITING;
import static ru.acuma.shuffler.model.constant.messages.EventConstant.LET_JOIN_TEXT;
import static ru.acuma.shuffler.model.constant.messages.EventConstant.MEMBERS_TEXT;
import static ru.acuma.shuffler.model.constant.messages.EventConstant.SHUFFLER_LINK;
import static ru.acuma.shuffler.util.Symbols.BLUE_CIRCLE_EMOJI;
import static ru.acuma.shuffler.util.Symbols.RED_CIRCLE_EMOJI;
import static ru.acuma.shuffler.util.Symbols.VS_EMOJI;

@Service
public class MessageContentService {

    public String buildContent(TEvent event, MessageType type) {
        return switch (type) {
            case LOBBY -> buildLobbyContent(event);
            case GAME -> buildGameContent(event);
            case CHECKING -> buildCheckingContent(event);
            case CANCELLED -> buildCancelledContent();
            default -> EventConstant.BLANK_MESSAGE.getText();
        };
    }

    private String buildLobbyContent(final TEvent event) {
        var builder = new StringBuilder();
        return builder
            .append(applyEventHeader(event, builder))
            .append(event.getPlayers().isEmpty() ? LET_JOIN_TEXT.getText() : MEMBERS_TEXT.getText())
            .append(!event.getPlayers().isEmpty() ? getMembersText(event) : "")
            .append(Boolean.TRUE.equals(event.hasAnyGameFinished()) ? EventConstant.WINNERS_MESSAGE.getText() : "")
            .append(Boolean.TRUE.equals(event.hasAnyGameFinished()) ? buildWinnersText(event) : "")
            .append(Boolean.TRUE.equals(event.isCalibrating()) ? EventConstant.CALIBRATING_MESSAGE.getText() : "")
            .append(getEventFooterText(event))
            .toString();
    }

    private String getEventFooterText(final TEvent event) {
        return switch (event.getEventStatus()) {
            case CANCEL_CHECKING -> EventConstant.CANCEL_CHECKING_MESSAGE.getText();
            case BEGIN_CHECKING -> EventConstant.BEGIN_CHECKING_MESSAGE.getText();
            case FINISH_CHECKING -> EventConstant.FINISH_CHECKING_MESSAGE.getText();
            default -> SHUFFLER_LINK.getText();
        };
    }

    private String applyEventHeader(final TEvent event, final StringBuilder builder) {
        return switch (event.getEventStatus()) {
            case CREATED, READY, GAME_CHECKING, CANCEL_CHECKING, BEGIN_CHECKING -> getDisciplineHeader(event, builder);
            case WAITING -> EventConstant.LOBBY_WAITING_MESSAGE.getText();
            case CANCELLED -> EventConstant.LOBBY_CANCELED_MESSAGE.getText();
            case PLAYING -> EventConstant.LOBBY_PLAYING_MESSAGE.getText();
            case FINISHED -> EventConstant.LOBBY_FINISHED_MESSAGE.getText();
            default -> EventConstant.BLANK_MESSAGE.getText();
        };
    }

    private String getMembersText(final TEvent event) {
        return event.getPlayers().values()
            .stream()
            .sorted(Comparator.comparingLong(TEventPlayer::getScoreSorting).reversed())
            .map(TEventPlayer::getLobbyName)
            .collect(Collectors.joining(System.lineSeparator())) +
            System.lineSeparator();
    }

    private String buildWinnersText(final TEvent event) {
        return event.getTgGames()
            .stream()
            .filter(game -> game.getWinnerTeam() != null)
            .map(TGame::getGameResult)
            .collect(Collectors.joining(System.lineSeparator())) +
            System.lineSeparator();
    }

    @SuppressWarnings("UnnecessaryDefault")
    private String getDisciplineHeader(TEvent event, StringBuilder builder) {
        return switch (event.getDiscipline()) {
            case KICKER -> EventConstant.LOBBY_MESSAGE_KICKER.getText();
            case PING_PONG -> EventConstant.LOBBY_MESSAGE_PING_PONG.getText();
            default -> EventConstant.LOBBY_MESSAGE_DEFAULT.getText();
        };
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private String buildGameContent(final TEvent event) {
        var game = event.getCurrentGame();
        var builder = new StringBuilder();

        return builder.append(EventConstant.GAME_MESSAGE.getText())
            .append(game.getIndex())
            .append(EventConstant.SPACE_MESSAGE.getText())
            .append(System.lineSeparator())
            .append(buildGameText(game))
            .toString();
    }

    private String buildGameText(final TGame game) {
        return getSpaces(game)
            + System.lineSeparator()
            + getGameBodyText(game)
            + EventConstant.SINGLE_SPACE_MESSAGE.getText()
            + getGameFooterText(game);
    }

    private String getGameBodyText(final TGame game) {
        return switch (game.getStatus()) {
            case RED_CHECKING -> getRedWinnersBody(game);
            case BLUE_CHECKING -> getBlueWinnersBody(game);
            case ACTIVE, CANCEL_CHECKING -> getDefaultGameBody(game);
            default -> EventConstant.BLANK_MESSAGE.getText();
        };
    }

    private String getDefaultGameBody(final TGame game) {
        return String.format(game.getRedTeam().toString(), RED_CIRCLE_EMOJI)
            + System.lineSeparator()
            + getSpaces(game)
            + VS_EMOJI
            + System.lineSeparator()
            + String.format(game.getBlueTeam().toString(), BLUE_CIRCLE_EMOJI)
            + System.lineSeparator();
    }

    private String getRedWinnersBody(final TGame game) {
        return String.format(game.getRedTeam().toString(), "и");
    }

    private String getBlueWinnersBody(final TGame game) {
        return String.format(game.getBlueTeam().toString(), "и");
    }

    private String getGameFooterText(final TGame game) {
        return switch (game.getStatus()) {
            case RED_CHECKING, BLUE_CHECKING -> EventConstant.WIN_CHECKING_MESSAGE.getText();
            case CANCEL_CHECKING -> EventConstant.NEXT_CHECKING_MESSAGE.getText();
            default -> getSpaces(game);
        };
    }

    private String getSpaces(final TGame tgGame) {
        String spaces = tgGame.getRedTeam().getPlayer1().getName();
        return StringUtils.repeat(" ", spaces.length() * 2);
    }

    private String buildCheckingContent(final TEvent event) {
        return switch (event.getEventStatus()) {
            case EVICTING -> EventConstant.EVICT_PLAYER_CHECKING.getText();
            default -> EventConstant.BLANK_MESSAGE.getText();
        };
    }

    private String buildCancelledContent() {
        return EventConstant.LOBBY_CANCELED_MESSAGE.getText();
    }
}
