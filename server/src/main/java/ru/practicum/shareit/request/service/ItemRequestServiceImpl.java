package ru.practicum.shareit.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.support.OffsetPageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoFromClient;
import ru.practicum.shareit.request.dto.ItemRequestDtoToClient;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    UserRepository userRepository;

    ItemRequestRepository repository;

    ItemRepository itemRepository;

    DefaultLocaleMessageSource messageSource;

    ItemRequestMapper mapper;

    @Override
    public ItemRequestDtoToClient create(Long userId, ItemRequestDtoFromClient dto) {
        User requestor = userRepository.findById(userId).orElse(null);

        if (requestor == null) {
            throw new NotFoundException("userId",
                    messageSource.get("itemRequest.ItemRequestService.notFoundUserById") + ": " + userId);
        }

        ItemRequest entity = mapper.toEntity(dto);
        entity.setRequestor(requestor);
        entity.setCreated(LocalDateTime.now());

        return mapper.toDto(repository.saveAndFlush(entity), null);
    }

    @Override
    public List<ItemRequestDtoToClient> readByUser(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("userId",
                    messageSource.get("itemRequest.ItemRequestService.notFoundUserById") + ": " + userId);
        }

        return mapper.toDtoList(repository.findByRequestor_IdOrderByCreatedDesc(userId), itemRepository);
    }

    @Override
    public List<ItemRequestDtoToClient> readAll(Long userId, Integer from, Integer size) {
        return mapper.toDtoList(repository.findByRequestor_IdNot(userId,
                OffsetPageRequest.ofOffset(from, size, Sort.by("created").descending())).getContent(),
                itemRepository);
   }

    @Override
    public ItemRequestDtoToClient readById(Long userId, Long id) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("userId",
                    messageSource.get("itemRequest.ItemRequestService.notFoundUserById") + ": " + userId);
        }

        ItemRequest entity = repository.findById(id).orElse(null);
        if (entity == null) {
            throw new NotFoundException("id", messageSource.get("itemRequest.ItemRequestService.notFoundById") + ": " +
                    id);
        }

        return mapper.toDto(entity, itemRepository);
    }
}