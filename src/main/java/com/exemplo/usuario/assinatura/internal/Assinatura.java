package com.exemplo.usuario.assinatura.internal;

import com.exemplo.usuario.assinatura.AssinaturaResponseDTO;
import jakarta.persistence.*;

@Entity
@Table(name = "assinaturas")
public class Assinatura {

    private static final int CREDITOS_POR_CONCLUSAO = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private CarteiraAssinatura carteira = new CarteiraAssinatura();

    @Embedded
    private ProgressoAssinatura progresso = new ProgressoAssinatura();

    // Monolito modular: o usuario pertence a outro modulo.
    // Em vez de @OneToOne Usuario, guardamos so o id, na MESMA coluna "usuario_id".
    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    // Construtor vazio exigido pelo JPA.
    protected Assinatura() {
    }

    // Construtor de negocio.
    // Todo novo usuario nasce com assinatura BASICO e zero creditos/moedas.
    public Assinatura(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public CarteiraAssinatura getCarteira() {
        return carteira;
    }

    public ProgressoAssinatura getProgresso() {
        return progresso;
    }

    // Metodo de dominio rico:
    // - soma conclusao com sucesso (e promove para PREMIUM ao atingir 12)
    // - entrega 3 creditos
    public void registrarConclusaoComSucesso() {
        progresso.registrarConclusao();
        carteira.adicionarCreditos(CREDITOS_POR_CONCLUSAO);
    }

    // Converte a assinatura no resumo publico usado pelo modulo usuario.
    public AssinaturaResponseDTO paraResumo() {
        return new AssinaturaResponseDTO(
                progresso.getPlano().name(),
                carteira.getCreditosCursos(),
                progresso.getCursosConcluidosComSucesso(),
                carteira.getMoedas()
        );
    }
}
