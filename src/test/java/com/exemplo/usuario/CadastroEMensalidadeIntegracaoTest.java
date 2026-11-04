package com.exemplo.usuario;

import com.exemplo.usuario.mensalidade.MensalidadeService;
import com.exemplo.usuario.mensalidade.internal.dto.MensalidadeRequestDTO;
import com.exemplo.usuario.mensalidade.internal.service.AtualizacaoMensalidadeService;
import com.exemplo.usuario.usuario.internal.dto.UsuarioRequestDTO;
import com.exemplo.usuario.usuario.internal.service.CadastroUsuarioService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Teste de integracao (Spring + H2): confere a conversa real entre os modulos.
// - Cadastro: o usuario publica UsuarioCriadoEvent e o modulo mensalidade cria a mensalidade PAGA
//   na mesma transacao, sem que o usuario conheca o modulo mensalidade.
// - Atualizacao: o modulo mensalidade grava o status e o usuario reavalia o acesso.
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class CadastroEMensalidadeIntegracaoTest {

    @Autowired
    private CadastroUsuarioService cadastro;

    @Autowired
    private AtualizacaoMensalidadeService atualizacao;

    @Autowired
    private MensalidadeService mensalidadeService;

    private UsuarioRequestDTO novoUsuario() {
        var dto = new UsuarioRequestDTO();
        dto.setNome("Fulano");
        dto.setEmail("fulano@integracao.com");
        dto.setSenha("senha123");
        return dto;
    }

    private MensalidadeRequestDTO status(String valor) {
        var dto = new MensalidadeRequestDTO();
        dto.setStatus(valor);
        return dto;
    }

    @Test
    void cadastroDeveCriarMensalidadePagaPeloEventoELiberarAcesso() {
        // QUANDO
        var usuario = cadastro.criar(novoUsuario());

        // ENTAO
        assertTrue(mensalidadeService.estaPaga(usuario.id()));
        assertEquals("PAGA", usuario.acesso().statusMensalidade());
        assertTrue(usuario.acesso().temAcessoAoCurso());
        assertEquals("BASICO", usuario.assinatura().plano());
    }

    @Test
    void atualizarMensalidadeDeveCongelarELiberarDeNovo() {
        // DADO
        var id = cadastro.criar(novoUsuario()).id();

        // QUANDO
        var pendente = atualizacao.atualizarStatus(id, status("PENDENTE"));
        var paga = atualizacao.atualizarStatus(id, status("paga"));

        // ENTAO
        assertEquals("PENDENTE", pendente.acesso().statusMensalidade());
        assertTrue(pendente.acesso().plataformaCongelada());
        assertEquals("PAGA", paga.acesso().statusMensalidade());
        assertFalse(paga.acesso().plataformaCongelada());
    }
}
