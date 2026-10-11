package br.edu.fag.sputnik.service;

import br.edu.fag.sputnik.domain.Empresa;
import br.edu.fag.sputnik.repository.EmpresaRepository;
import br.edu.fag.sputnik.repository.UsuarioSistemaRepository;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmpresaService {
    private final EmpresaRepository empresas;
    private final UsuarioSistemaRepository usuarios;

    public EmpresaService(EmpresaRepository empresas, UsuarioSistemaRepository usuarios) {
        this.empresas = empresas;
        this.usuarios = usuarios;
    }

    public Map<String, Object> assinatura(Long empresaId) {
        Empresa empresa =
                empresas.findById(empresaId)
                        .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada."));
        long usados =
                usuarios.findAll().stream()
                        .filter(
                                usuario ->
                                        usuario.getEmpresa() != null
                                                && empresaId.equals(usuario.getEmpresa().getId()))
                        .count();
        return Map.of(
                "id",
                empresa.getId(),
                "nome",
                empresa.getNome(),
                "slug",
                empresa.getSlug(),
                "plano",
                empresa.getPlano().name(),
                "status",
                empresa.getStatusAssinatura().name(),
                "periodoTesteAte",
                empresa.getPeriodoTesteAte() == null ? "" : empresa.getPeriodoTesteAte().toString(),
                "limiteUsuarios",
                empresa.getPlano().getLimiteUsuarios(),
                "usuariosAtivos",
                usados);
    }
}
