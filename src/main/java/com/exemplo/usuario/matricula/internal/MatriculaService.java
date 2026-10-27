package com.exemplo.usuario.matricula.internal;

import com.exemplo.usuario.assinatura.AssinaturaService;
import com.exemplo.usuario.curso.CursoService;
import com.exemplo.usuario.shared.RecursoNaoEncontradoException;
import com.exemplo.usuario.usuario.UsuarioApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Camada: SERVICE.
// Orquestra os casos de uso de matricula.
// Conversa com os outros modulos apenas pela API publica deles
// (UsuarioApi, CursoService e AssinaturaService), nunca pelos repositories.
@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final UsuarioApi usuarioApi;
    private final CursoService cursoService;
    private final AssinaturaService assinaturaService;
    private final MatriculaResponseMapper mapper;

    public MatriculaService(MatriculaRepository matriculaRepository,
                            UsuarioApi usuarioApi,
                            CursoService cursoService,
                            AssinaturaService assinaturaService,
                            MatriculaResponseMapper mapper) {
        this.matriculaRepository = matriculaRepository;
        this.usuarioApi = usuarioApi;
        this.cursoService = cursoService;
        this.assinaturaService = assinaturaService;
        this.mapper = mapper;
    }

    public List<MatriculaResponseDTO> listarPorUsuario(Long usuarioId) {
        return matriculaRepository.findByUsuarioId(usuarioId).stream().map(mapper::paraDTO).toList();
    }

    @Transactional
    public MatriculaResponseDTO matricular(Long usuarioId, Long cursoId, boolean bonus) {
        // 1) Bloqueia a matricula se o usuario nao existir (404) ou nao tiver
        // acesso a plataforma (mensalidade pendente ou inexistente).
        usuarioApi.validarAcessoPlataforma(usuarioId);

        // 2) Garante que o curso existe (404 se nao existir).
        cursoService.buscarPorId(cursoId);

        // 3) Regra de negocio: matricula bonus consome um credito da assinatura.
        if (bonus) {
            assinaturaService.consumirCredito(usuarioId);
        }

        // 4) Cria a entidade do dominio, persiste e devolve o DTO.
        Matricula matricula = new Matricula(usuarioId, cursoId, bonus);
        return mapper.paraDTO(matriculaRepository.save(matricula));
    }

    @Transactional
    public MatriculaResponseDTO concluir(Long matriculaId, Double notaFinal) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(RecursoNaoEncontradoException.com("Matricula nao encontrada"));

        // Bloqueia a conclusao (e a concessao de creditos) se o usuario nao
        // tiver mais acesso a plataforma, mesmo que a matricula ja existisse.
        usuarioApi.validarAcessoPlataforma(matricula.getUsuarioId());

        // Regra de dominio: conclui uma unica vez (lanca excecao se ja concluida).
        matricula.getSituacao().concluir(notaFinal);

        // Se a matricula deve liberar creditos, a assinatura do usuario e atualizada.
        if (matricula.getSituacao().deveLiberarCreditos()) {
            assinaturaService.registrarConclusaoComSucesso(matricula.getUsuarioId());
        }

        return mapper.paraDTO(matriculaRepository.save(matricula));
    }
}
