package com.sistema.agendamento.sistema_agendamento.dto;

import com.sistema.agendamento.sistema_agendamento.Utils.ConstrainsMessages;
import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @NotBlank(message = ConstrainsMessages.USERNAME_INVALID)
    private String username;

    @Email(message = ConstrainsMessages.EMAIL_INVALID)
    private String email;

    @NotNull(message = ConstrainsMessages.ROLE_INVALID)
    private TipoUsuario userType;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = ConstrainsMessages.PASSWORD_INVALID)
    private String password;


}
