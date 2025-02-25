package example.mapper;

import example.dto.UserCreateDTO;
import example.dto.UserResponseDTO;
import example.entity.User;
import example.service.FriendshipService;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class UserMapper {

    protected FriendshipService friendshipService;

    @Autowired
    public void setFriendshipService(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    public abstract UserResponseDTO toUserResponseDTO(User user);

    public abstract List<UserResponseDTO> toListUserResponseDTO(List<User> users);

    public abstract User fromUserCreateDTO(UserCreateDTO userCreateDTO);

    public abstract List<User> fromListUserCreateDTO(List<UserCreateDTO> userCreateDTOList);

    protected int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    protected LocalDate parseBirthDate(String birthDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(birthDate, formatter);
    }
}
