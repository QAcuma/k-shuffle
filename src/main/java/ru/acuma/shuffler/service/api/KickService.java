package ru.acuma.shuffler.service.api;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.acuma.shuffler.model.entity.TgEvent;
import ru.acuma.shuffler.model.entity.TgEventPlayer;

import java.util.List;

public interface KickService {

    InlineKeyboardMarkup preparePlayersKeyboard(List<TgEventPlayer> players);

    SendMessage prepareKickMessage(TgEvent event);
}
