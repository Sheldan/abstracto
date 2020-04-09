package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.*;
import dev.sheldan.abstracto.core.models.converter.PostTargetConverter;
import dev.sheldan.abstracto.core.models.converter.ServerConverter;
import dev.sheldan.abstracto.core.models.dto.*;
import dev.sheldan.abstracto.core.repository.ServerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ServerManagementServiceBean {

    @Autowired
    private ServerRepository repository;

    @Autowired
    private PostTargetManagementBean postTargetManagement;

    @Autowired
    private ChannelManagementServiceBean channelManagementService;

    @Autowired
    private UserManagementServiceBean userManagementService;

    @Autowired
    private ServerConverter serverConverter;

    @Autowired
    private PostTargetConverter postTargetConverter;

    public ServerDto createServer(Long id) {
        AServer aServer = AServer.builder().id(id).build();
        return serverConverter.convertServer(repository.save(aServer));
    }

    public ServerDto loadOrCreate(Long id) {
        if(repository.existsById(id)) {
            return serverConverter.convertServer(repository.getOne(id));
        } else {
            return createServer(id);
        }
    }

    public void addChannelToServer(ServerDto server, ChannelDto channel) {
        server.getChannels().add(channel);
        channel.setServer(server);
        repository.save(serverConverter.fromDto(server));
    }

    public UserInServerDto addUserToServer(ServerDto server, UserDto user) {
        return this.addUserToServer(server.getId(), user.getId());
    }

    public UserInServerDto addUserToServer(Long serverId, Long userId) {
        log.info("Adding user {} to server {}", userId, serverId);
        ServerDto server = ServerDto.builder().id(serverId).build();
        UserDto user = userManagementService.loadUser(userId);
        UserInServerDto aUserInAServer = UserInServerDto.builder().server(server).user(user).build();
        server.getUsers().add(aUserInAServer);
        repository.save(serverConverter.fromDto(server));
        return aUserInAServer;
    }

    public ChannelDto getPostTarget(Long serverId, String name) {
        ServerDto serverDto = ServerDto.builder().id(serverId).build();
        return getPostTarget(serverDto, name);
    }

    public ChannelDto getPostTarget(Long serverId, PostTargetDto target) {
        ServerDto serverDto = ServerDto.builder().id(serverId).build();
        return getPostTarget(serverDto, target);
    }

    public ChannelDto getPostTarget(ServerDto server, PostTargetDto target) {
        return target.getChannelReference();
    }

    public ChannelDto getPostTarget(ServerDto server, String name) {
        PostTarget target = postTargetManagement.getPostTarget(name, server);

        return getPostTarget(server, postTargetConverter.fromPostTarget(target));
    }

    public List<ServerDto> getAllServers() {
        List<ServerDto> servers = new ArrayList<>();
        List<AServer> all = repository.findAll();
        all.forEach(aServer -> {
            servers.add(serverConverter.convertServer(aServer));
        });
        return servers;
    }


}
