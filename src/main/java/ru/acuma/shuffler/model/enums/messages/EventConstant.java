package ru.acuma.shuffler.model.enums.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.acuma.shuffler.service.buttons.EventConstantButton;

@Getter
@AllArgsConstructor
public enum EventConstant implements EventConstantButton {

    BLANK_MESSAGE(""),
    SPACE_MESSAGE("⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n"),
    LOBBY_MESSAGE_KICKER("🍢 Время шашлыков! 🍢\n" + SPACE_MESSAGE.getText() +
            "\uD83D\uDC68\u200D\uD83D\uDC66\u200D\uD83D\uDC66 Участники:\n"),
    LOBBY_MESSAGE_PING_PONG("\uD83C\uDFD3 Время бить мяч! \uD83C\uDFD3\n" + SPACE_MESSAGE.getText() +
            "\uD83D\uDC68\u200D\uD83D\uDC66\u200D\uD83D\uDC66 Участники:\n"),
    LOBBY_PLAYING_MESSAGE("🍢 Чемпионат в разгаре! 🍢\n" + SPACE_MESSAGE.getText() +
            "\uD83D\uDC68\u200D\uD83D\uDC66\u200D\uD83D\uDC66 Участники:\n"),
    LOBBY_FINISHED_MESSAGE("🍢 Чемпионат завершен! 🍢\n" + SPACE_MESSAGE.getText() +
            "\uD83D\uDC68\u200D\uD83D\uDC66\u200D\uD83D\uDC66 Участники:\n"),
    LOBBY_WAITING_MESSAGE("⏳ Ждём игроков! ⏳\n" + SPACE_MESSAGE.getText() +
            "\uD83D\uDC68\u200D\uD83D\uDC66\u200D\uD83D\uDC66 Участники:\n"),
    WINNERS_MESSAGE("\n\uD83C\uDFC6Победители: \n"),
    GAME_MESSAGE("<b>Игра №</b>"),
    CANCEL_CHECKING_MESSAGE("⚠️ Отменить чемпионат?\n"),
    NEXT_CHECKING_MESSAGE("⚠️ Отменить текущую игру и начать новую?\n"),
    FINISH_CHECKING_MESSAGE("⚠️ Завершить чемпионат?\n"),
    WAITING_MESSAGE("⚠️ Недостаточно игроков для начала игры!\n"),
    BEGIN_CHECKING_MESSAGE("\uD83D\uDC65 Все участники в сборе?\n"),
    RED_CHECKING_MESSAGE("\uD83D\uDD3A   Победа красных?   \uD83D\uDD3A\n"),
    BLUE_CHECKING_MESSAGE("\uD83D\uDD39   Победа синих?   \uD83D\uDD39\n"),
    LOBBY_CANCELED_MESSAGE("Чемпионат был отменен \uD83C\uDF1A");

    private final String text;
}
