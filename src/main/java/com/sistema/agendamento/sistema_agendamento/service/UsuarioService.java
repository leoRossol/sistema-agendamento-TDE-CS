package com.sistema.agendamento.sistema_agendamento.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sistema.agendamento.sistema_agendamento.dto.Usuario.NovaSenhaRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.NovaSenhaResponseDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin.RegistroRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin.RegistroResponseDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin.RemoverUsuarioRequestDTO;
import com.sistema.agendamento.sistema_agendamento.dto.Usuario.Admin.RemoverUsuarioResponseDTO;
import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.sistema.agendamento.sistema_agendamento.exception.Usuario.EmailJaCadastradoException;
import com.sistema.agendamento.sistema_agendamento.exception.Usuario.MatriculaInvalidaException;
import com.sistema.agendamento.sistema_agendamento.exception.Usuario.NovaSenhaInvalidaException;
import com.sistema.agendamento.sistema_agendamento.exception.Usuario.SenhaAntigaException;
import com.sistema.agendamento.sistema_agendamento.exception.Usuario.UsuarioInativoException;
import com.sistema.agendamento.sistema_agendamento.exception.Usuario.UsuarioNaoEncontradoException;

@Service
public class UsuarioService {

//#region ADMIN
    @Autowired
    private UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public RegistroResponseDTO registrarUsuario(RegistroRequestDTO dto) {
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent())
            throw new EmailJaCadastradoException();

        if (usuarioRepository.findByMatricula(dto.getMatricula()).isPresent())
            throw new MatriculaInvalidaException();

        if (!novaSenhaValida(dto.getSenha()))
            throw new NovaSenhaInvalidaException();

        Usuario novoUsuario = new Usuario(dto.getNome(), dto.getEmail(), passwordEncoder.encode(dto.getSenha()), dto.getTipoUsuario(), dto.getMatricula());

        usuarioRepository.save(novoUsuario);

        RegistroResponseDTO response = new RegistroResponseDTO();
        response.setMensagem("Usuário registrado com sucesso!");
        response.setId(novoUsuario.getId());

        return response;
    }

    public RemoverUsuarioResponseDTO removerUsuario(RemoverUsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getIdUsuario()).orElseThrow(() -> new UsuarioNaoEncontradoException());

        usuario.desativar(); // estamos somente desativando o usuario
        usuarioRepository.save(usuario);
        // usuarioRepository.delete(usuario);

        RemoverUsuarioResponseDTO response = new RemoverUsuarioResponseDTO();
        response.setMensagem("Usuário removido com sucesso!");
        response.setIdUsuarioRemovido(dto.getIdUsuario());

        return response;
    }
//#endregion ADMIN

    public NovaSenhaResponseDTO redefinirSenha(NovaSenhaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new UsuarioNaoEncontradoException());

        if (!usuario.isAtivo())
            throw new UsuarioInativoException();

        if (!passwordEncoder.matches(dto.getSenhaAntiga(), usuario.getSenha()))
            throw new SenhaAntigaException();

        if (!novaSenhaValida(dto.getNovaSenha()))
            throw new NovaSenhaInvalidaException();

        if (passwordEncoder.matches(dto.getNovaSenha(), usuario.getSenha()))
            throw new NovaSenhaInvalidaException("A nova senha deve ser diferente da senha antiga.");

        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        usuarioRepository.save(usuario);

        NovaSenhaResponseDTO response = new NovaSenhaResponseDTO();
        response.setMensagem("Senha redefinida com sucesso!");

        return response;
    }

    public Boolean novaSenhaValida(String senha) {
        return senha != null 
        && senha.length() >= 6              
        && senha.length() <= 20 
        && senha.matches(".*\\d.*") // pelo menos um digito
        && senha.matches(".*[A-Z].*") // pelo menos uma letra maiuscula
        && senha.matches(".*[a-z].*") // pelo menos uma letra minuscula
        && senha.matches(".*[!@#$%^&*()\\[\\]{}<>?~].*"); // pelo menos um caractere especial
    }
}