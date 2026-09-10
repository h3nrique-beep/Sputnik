# Documento da ordem de serviço

O formulário de referência foi adaptado para a identidade Sputnik. Não utiliza dados, logotipos ou termos de concessionária da imagem.

## Uso

Em **Nova OS** ou **Editar ordem**, abra **Dados do documento da OS**. Todos os complementos são opcionais. Cliente, veículo, quilometragem e serviços continuam no fluxo existente.

- Atendimento: tipo, pagamento, validade, previsão de orçamento e entrega, consultor, prisma e retirada.
- Proprietário: o cliente vinculado à OS; endereço e e-mail complementares.
- Pagador: quando o nome estiver vazio, usa os dados do proprietário. Para outro pagador, preencha sua identificação e contato.
- Veículo: motor, cor, ano-modelo, combustível de entrada e certificado de garantia.
- Oficina: nome, documento, endereço e contato reais. Sem nome informado, aparece Sputnik.
- Observações e informações de reembolso são livres.

Os complementos ficam na OS, não alteram o cadastro mestre do cliente/veículo. Não preencha os dados da concessionária do exemplo. Recursos exclusivos de concessionária (primeira/última venda, concessionária vendedora, reembolso Honda) não são exigidos; a garantia/reembolso usa um campo genérico.

## Impressão e PDF

Os dois formatos são produzidos pelo mesmo `DocumentoOsLayout`, em A4: oficina, atendimento, cliente pagador, proprietário, veículo, solicitação, peças, observações, serviços executados, despesas, assinaturas e comprovante de retirada. Os totais são calculados dos itens reais da ordem. Não há cobrança adicional criada apenas pelo preenchimento do documento.

**Imprimir** abre uma página exclusiva, sem menu ou formulários. Use o botão dessa página e desative cabeçalhos/rodapés do navegador. **Baixar PDF** faz o download direto. Textos excedentes e o histórico ficam em anexos identificados pela OS; não são descartados para caber em uma página.

## Persistência e segurança

`DadosDocumentoOs` é um componente JPA embutido com colunas opcionais na tabela da OS. Instalações existentes continuam legíveis; o `ddl-auto=update` atual acrescenta colunas no próximo carregamento. Faça backup antes de atualizar qualquer ambiente de produção.

Endpoints de documento exigem a permissão de ordens; não aceitam caminhos de arquivo. Conteúdo do usuário é escapado no HTML/PDF. Comprimentos, e-mails, ano, validade e percentual de combustível são validados no servidor.

## Verificação

`OrdemDocumentoTest`: documento antigo, acentuação, totais, paginação, escape HTML e validação dos campos. Amostras são geradas em `target/pdf-qa` com dados fictícios.

`OrdemDocumentoIntegrationTest`: banco H2 em memória, criação/edição/persistência e controle de acesso aos endpoints de impressão/PDF. Não usa o banco real.
