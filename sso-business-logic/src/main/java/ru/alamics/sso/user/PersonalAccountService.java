package ru.alamics.sso.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.PersonalAccountPostEntity;
import ru.alamics.sso.jpa.entity.UserPostEntity;
import ru.alamics.sso.jpa.repository.PersonalAccountRepository;
import ru.alamics.sso.jpa.repository.UserPostRepository;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.PersonalAccountModel;
import ru.alamics.sso.user.model.PersonalAccountPostModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class PersonalAccountService {
    @Inject
    PersonalAccountRepository paRepository;
    @Inject
    UserPostRepository userPostRepository;

    private void checkPost(final String postId) throws NotFoundException {

        UserPostEntity post = userPostRepository.getUserPost(postId);
        if (post == null)
            throw new NotFoundException("Post not found");
    }

    public PersonalAccountPostModel getAccountModel(final String postId) throws NotFoundException {

        checkPost(postId);

        PersonalAccountPostEntity pap = paRepository.getAccount(postId);

        PersonalAccountPostModel model = UserMapper.toPAPostDto(pap);

        if (model == null) {
            model = PersonalAccountPostModel.builder()
                    .postId(postId)
                    .accounts(new ArrayList<>())
                    .build();
        }

        return model;
    }

    public PersonalAccountPostModel getActivePAByUser(final String userId) {

        UserPostEntity post = getActiveUserPost(userId);
        if (post == null)
            return null;

        PersonalAccountPostEntity pap = paRepository.getAccount(post.getId());

        return UserMapper.toPAPostDto(pap);
    }

    private UserPostEntity getActiveUserPost(final String userId) {

        List<UserPostEntity> userPostList;
        try {
            userPostList = userPostRepository.findUserPostRoleByUserId(userId);
        } catch (NotFoundException e) {
            return null;
        }

        return userPostList.stream().filter(UserPostEntity::isSelected).findFirst()
                .orElse(null);
    }

    public void setAccountList(final String postId, List<String> paList) throws NotFoundException {

        checkPost(postId);

        paRepository.setAccountList(postId, paList);
    }

    public List<PersonalAccountModel> addAccountList(final String postId, List<String> paList) throws NotFoundException {

        checkPost(postId);

        return paRepository.addAccountList(postId, paList).stream()
                .map(UserMapper::toPADto)
                .collect(Collectors.toList());
    }

    public void subAccountUuidList(final String postId, List<String> paUuidList) throws NotFoundException {

        checkPost(postId);

        paRepository.subAccountUuidList(postId, paUuidList);
    }

    public void deleteAccountList(final String postId) throws NotFoundException {

        checkPost(postId);

        paRepository.deleteAccountList(postId);
    }

}
