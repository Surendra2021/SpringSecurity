package com.example.SpringJWT.mapper;

import com.example.SpringJWT.dto.response.UserResponse;
import com.example.SpringJWT.entity.User;
import org.mapstruct.Mapper;

// @Mapper — tells MapStruct this interface is a mapper
// componentModel = "spring" — tells MapStruct to register the generated
// implementation as a Spring bean so we can inject it with @RequiredArgsConstructor
@Mapper(componentModel = "spring")
public interface UserMapper {

    // This is a method declaration (no body — interface method)
    // MapStruct reads:
    //   - input type  → User (has getUsername(), getEmail())
    //   - output type → UserResponse (has setUsername(), setEmail())
    // field names match on both sides → MapStruct auto-generates the mapping
    // At compile time MapStruct creates UserMapperImpl with the full mapping code
    UserResponse toUserResponse(User user);
}
