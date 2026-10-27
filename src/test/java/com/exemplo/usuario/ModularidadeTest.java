package com.exemplo.usuario;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

// Teste de ARQUITETURA do monolito modular (Spring Modulith).
// Nao sobe o Spring: apenas analisa os pacotes compilados.
// Cada subpacote direto de com.exemplo.usuario e um modulo.
class ModularidadeTest {

    // ApplicationModules le a estrutura de pacotes a partir da classe principal.
    private final ApplicationModules modules = ApplicationModules.of(UsuarioApplication.class);

    @Test
    void deveRespeitarAsFronteirasEntreModulos() {
        // DADO os modulos detectados a partir de UsuarioApplication
        // QUANDO verificamos as dependencias
        // ENTAO nao pode haver ciclos nem acesso a pacotes internos de outro modulo
        //       (verify() lanca excecao se alguma regra for violada)
        modules.verify();
    }

    @Test
    void deveGerarDocumentacaoDosModulos() {
        // DADO os modulos detectados
        // QUANDO geramos a documentacao
        // ENTAO os diagramas PlantUML sao escritos em target/spring-modulith-docs
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
