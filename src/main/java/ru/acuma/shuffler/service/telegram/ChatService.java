package ru.acuma.shuffler.service.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Chat;
import ru.acuma.shuffler.exception.DataException;
import ru.acuma.shuffler.mapper.ChatMapper;
import ru.acuma.shuffler.model.constant.AuthStatus;
import ru.acuma.shuffler.model.constant.ExceptionCause;
import ru.acuma.shuffler.model.entity.GroupInfo;
import ru.acuma.shuffler.repository.GroupInfoRepository;
import ru.acuma.shuffler.repository.ReferenceService;

@Service
@RequiredArgsConstructor
public class ChatService implements Authenticatable<Long> {

    private final ChatMapper chatMapper;
    private final ReferenceService referenceService;
    private final GroupInfoRepository groupInfoRepository;

    @Transactional(readOnly = true)
    public GroupInfo getGroupInfo(final Long chatId) {
        return groupInfoRepository.findById(chatId)
            .orElseThrow(() -> new DataException(ExceptionCause.GROUP_NOT_FOUND, chatId));
    }

    @Transactional
    public void update(final Chat chat) {
        var groupInfo = getGroupInfo(chat.getId());
        chatMapper.updateGroupInfo(groupInfo, chat);
    }

    @Transactional
    public void signUp(final Chat chat) {
        var groupInfo = chatMapper.toGroupInfo(chat);
        groupInfoRepository.save(groupInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthStatus authenticate(final Long chatId) {
        return groupInfoRepository.findById(chatId)
            .map(info -> Boolean.TRUE.equals(info.getIsActive())
                         ? AuthStatus.SUCCESS
                         : AuthStatus.DENY)
            .orElse(AuthStatus.UNREGISTERED);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public GroupInfo getReference(final Long chatId) {
        return referenceService.getReference(GroupInfo.class, chatId);
    }
}
