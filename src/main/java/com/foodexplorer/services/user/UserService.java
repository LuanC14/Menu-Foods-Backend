package com.foodexplorer.services.user;

import com.foodexplorer.exceptions.custom.DataConflictException;
import com.foodexplorer.exceptions.custom.DataNotFoundException;
import com.foodexplorer.exceptions.GlobalExceptionHandler;
import com.foodexplorer.exceptions.custom.UnathourizedException;
import com.foodexplorer.model.dto.CreateOrUpdateUserDTO;
import com.foodexplorer.model.dto.UserResponseDTO;
import com.foodexplorer.model.entities.User;
import com.foodexplorer.model.entities.UserRole;
import com.foodexplorer.providers.mapper.DozzerMapper;
import com.foodexplorer.repositories.UserRepository;
import com.foodexplorer.services.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements  iUserService {
    @Autowired
    UserRepository repository;

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    public UserResponseDTO create(CreateOrUpdateUserDTO data) {

            Optional<User> optionalUser = repository.findByEmail(data.email());
            if(optionalUser.isPresent()) {
                throw new DataConflictException("Este email já está em uso!");
            }

            var encoder = new BCryptPasswordEncoder();
            String hashedPassword = encoder.encode(data.password());

            User newUser = new User();
            newUser.setName(data.name());
            newUser.setEmail(data.email());
            newUser.setLogin(data.email());
            newUser.setPassword(hashedPassword);
            repository.save(newUser);

            var getUserForSetRole = repository.findByEmail(data.email()).get();

            if(getUserForSetRole.getId() == 1) {
                getUserForSetRole.setRole(UserRole.MASTER);
            } else if(getUserForSetRole.getId() == 2 ) {
                getUserForSetRole.setRole(UserRole.ADMIN);
            } else {
                getUserForSetRole.setRole(UserRole.USER);
            }
            repository.save(getUserForSetRole);

            return DozzerMapper.parseObject(getUserForSetRole, UserResponseDTO.class);
    }

    public UserResponseDTO updateUser(CreateOrUpdateUserDTO data) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var emailAuthenticated = authentication.getName();
        User user = repository.findByEmail(emailAuthenticated).get();

        var encoder = new BCryptPasswordEncoder();
        boolean checkPasswordMatch = encoder.matches(data.password(), user.getPassword());

        if(!checkPasswordMatch) {
            throw new UnathourizedException("Senha incorreta.");
        }

        user.setName(data.name() != null ? data.name() : user.getName());
        user.setEmail(data.email() != null ? data.email() : user.getEmail());
        user.setPassword(data.password() != null ? encoder.encode(data.password()) : user.getPassword());
        repository.save(user);

        return DozzerMapper.parseObject(user, UserResponseDTO.class);
    }

    public UserResponseDTO getByEmail(String email) {

        Optional<User> optionalUser = repository.findByEmail(email);

        if(!optionalUser.isPresent()) {
            throw  new DataNotFoundException("Esse email não consta em nosso banco de dados.");
        }

        return DozzerMapper.parseObject(optionalUser.get(), UserResponseDTO.class);
    }

    public void toggleLevelUser(String email, int level) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var emailAuthenticated = authentication.getName();
        User authenticatedUser = repository.findByEmail(emailAuthenticated).get();

        Optional<User> optionalUserTarget = repository.findByEmail(email);

        if(!optionalUserTarget.isPresent()) {
            throw new DataNotFoundException("Usuário não encontrado");
        } else if(optionalUserTarget.get().getId() == 1) {
            throw new UnathourizedException("O primeiro usuário mestre não pode ter suas permissões alteradas!");
        } else if(optionalUserTarget.get().getRole() == UserRole.MASTER && authenticatedUser.getRole() != UserRole.MASTER) {
            throw new UnathourizedException("Apenas usuários mestres podem alterar permissões de outros mestres.");
        } else if(authenticatedUser.getRole() == UserRole.ADMIN && level == 0) {
            throw new UnathourizedException("Você não pode inserir um novo mestre");
        }  else if(authenticatedUser.getRole() == UserRole.MASTER
                && authenticatedUser.getId() != 1
                && optionalUserTarget.get().getRole() == UserRole.MASTER
                && level != 0
        ) {
            throw new UnathourizedException("Apenas o mestre primário pode remover outro mestre");
        }

        User userTarget = optionalUserTarget.get();
        if(level == 0)  userTarget.setRole(UserRole.MASTER);
        if(level == 1) userTarget.setRole(UserRole.ADMIN);
        if(level == 2) userTarget.setRole(UserRole.USER);

        repository.save(userTarget);
    }
}
