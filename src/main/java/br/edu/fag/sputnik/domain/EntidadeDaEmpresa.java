package br.edu.fag.sputnik.domain;

import br.edu.fag.sputnik.tenant.EmpresaContext;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@FilterDef(name = "empresaAtual", parameters = @ParamDef(name = "empresaId", type = Long.class))
@Filter(name = "empresaAtual", condition = "empresa_id = :empresaId")
public abstract class EntidadeDaEmpresa {
    @Column(name = "empresa_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long empresaId;

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    @PrePersist
    void definirEmpresaDaRequisicao() {
        if (EmpresaContext.empresaId() != null) empresaId = EmpresaContext.empresaId();
    }
}
