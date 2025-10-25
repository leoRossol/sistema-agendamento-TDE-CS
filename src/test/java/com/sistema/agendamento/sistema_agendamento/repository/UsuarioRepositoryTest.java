package com.sistema.agendamento.sistema_agendamento.repository;

import com.sistema.agendamento.sistema_agendamento.entity.Usuario;
import com.sistema.agendamento.sistema_agendamento.enums.TipoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioAtivoAdmin;
    private Usuario usuarioInativoUser;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        usuarioAtivoAdmin = new Usuario("Admin Ativo", "admin@teste.com", "123" , TipoUsuario.ADMINISTRADOR, 100000001);

        usuarioInativoUser = new Usuario("User Inativo", "user@teste.com", "123", TipoUsuario.PROFESSOR, 100000002);
        usuarioInativoUser.setAtivo(false);

        usuarioRepository.save(usuarioAtivoAdmin);
        usuarioRepository.save(usuarioInativoUser);
    }

    @Test
    void findByEmail_DeveRetornarUsuarioQuandoExistir() {
        Optional<Usuario> usuario = usuarioRepository.findByEmail("admin@teste.com");
        assertTrue(usuario.isPresent());
        assertEquals("Admin Ativo", usuario.get().getNome());
    }

    @Test
    void findByEmail_DeveRetornarVazioQuandoNaoExistir() {
        Optional<Usuario> usuario = usuarioRepository.findByEmail("naoexiste@teste.com");
        assertFalse(usuario.isPresent());
    }

    @Test
    void findByTipoUsuario_DeveRetornarUsuariosDoTipo() {
        List<Usuario> admins = usuarioRepository.findByTipoUsuario(TipoUsuario.ADMINISTRADOR);
        assertEquals(1, admins.size());
        assertEquals("Admin Ativo", admins.get(0).getNome());
    }

    @Test
    void findByAtivoTrue_DeveRetornarUsuariosAtivos() {
        List<Usuario> ativos = usuarioRepository.findByAtivoTrue();
        assertEquals(1, ativos.size());
        assertEquals("Admin Ativo", ativos.get(0).getNome());
    }

    @Test
    void findByTipoUsuarioAndAtivo_DeveRetornarUsuariosAtivosDoTipo() {
        List<Usuario> adminsAtivos = usuarioRepository.findByTipoUsuarioAndAtivo(TipoUsuario.ADMINISTRADOR);
        assertEquals(1, adminsAtivos.size());
        assertEquals("Admin Ativo", adminsAtivos.get(0).getNome());

        List<Usuario> usersAtivos = usuarioRepository.findByTipoUsuarioAndAtivo(TipoUsuario.PROFESSOR);
        assertTrue(usersAtivos.isEmpty());
    }
}
