package com.gabrielbl.healthaplication.model;


import lombok.Getter;

@Getter
public enum PerguntaKey {

    /*


    F1_P1(1,1),//A quantidade de trabalho é excessiva para o tempo disponível.
    F1_P2(1,2),//Costumo acumular funções além da minha responsabilidade.
    F2_P1(2,1),//Trabalho sob pressão constante por prazos.
    F2_P2(2,2),//O ritmo de trabalho é acelerado demais.
    F3_P1(3,1),//Tenho clareza sobre minhas responsabilidades.
    F3_P2(3,2),//Recebo demandas contraditórias.
    F4_P1(4,1),//Tenho liberdade para organizar minhas tarefas.
    F4_P2(4,2),//Tenho pouca influência sobre decisões que afetam meu trabalho
    F5_P1(5,1),//Minha liderança trata os colaboradores com respeito
    F5_P2(5,2),//Sinto medo da forma como erros são tratados
    F6_P1(6,1),//Meu esforço é reconhecido.
    F6_P2(6,2),//Sinto que meu trabalho é invisível.
    F7_P1(7,1),//As informações chegam com clareza
    F7_P2(7,2),//Mudanças são comunicadas de forma confusa
    F8_P1(8,1),//Todos são tratados de forma justa.
    F8_P2(8,2),//Há favorecimentos ou desigualdades.
    F9_P1(9,1),//O clima entre colegas é saudável.
    F9_P2(9,2),//Existem conflitos frequentes.
    F10_P1(10,1),//Já sofri humilhações no trabalho.
    F10_P2(10,2),//Já presenciei constrangimentos recorrentes.
    F11_P1(11,1),//Consigo fazer pausas adequadas.
    F11_P2(11,2),//Trabalho além do horário com frequência
    F12_P1(12,1),//O trabalho invade minha vida pessoal.
    F12_P2(12,2),//Tenho dificuldade de desconectar fora do expediente.
    F13_P1(13,1),//Sinto ansiedade relacionada ao trabalho.
    F13_P2(13,2),//Sinto esgotamento emocional.
    F13_P3(13,3);//Já pensei em me afastar do trabalho por motivos emocionais


     */

    F1_P1(1,1);
    private final short factor;
    private final short numero;

    PerguntaKey(int factor, int numero) {
        this.factor = (short) factor;
        this.numero = (short) numero;
    }



    public String key() { return name(); } // "F1_P1"

    /*

Tenho mais tarefas do que consigo realizar
Frequentemente me sinto sobrecarregado no trabalho
As demandas são compatíveis com o tempo disponível
Existe controle ou acompanhamento do excesso de trabalho

Preciso trabalhar em ritmo acelerado constantemente
Me sinto pressionado por prazos ou produtividade
O ritmo de trabalho permite pausas adequadas
As metas são realistas e alcançáveis

Minha liderança me trata com respeito
Sinto apoio da minha liderança
Existem critérios claros de cobrança e gestão
A liderança recebe preparo para gestão de pessoas

Já me senti desrespeitado ou constrangido no trabalho
Existe um clima de medo ou tensão no ambiente
Existe um canal seguro para denúncias
Há políticas claras contra assédio

Tenho pouca liberdade para organizar meu trabalho
Sinto que não tenho controle sobre minhas tarefas
Existe autonomia para tomada de decisão dentro da função
As responsabilidades estão claramente definidas

Sinto que meu trabalho não é valorizado
Meu esforço raramente é reconhecido
Existem práticas de reconhecimento na empresa
Há critérios claros para crescimento e valorização

Recebo informações confusas ou incompletas
Tenho dificuldade de entender o que é esperado de mim
Existem canais formais de comunicação
As orientações de trabalho são claras e registradas

Sinto que há tratamento desigual na empresa
Percebo favoritismo ou injustiça
Existem critérios claros para decisões internas
As regras são aplicadas de forma igual

Existem conflitos frequentes entre colegas
O ambiente de trabalho é tenso
Existem ações para mediação de conflitos
Há regras de convivência bem definidas

Trabalho além do meu horário com frequência
Sinto dificuldade de descansar
A jornada é controlada adequadamente
Os intervalos são respeitados

O trabalho interfere na minha vida pessoal
Tenho dificuldade de me desconectar do trabalho
A empresa respeita horários fora do expediente
Existe equilíbrio entre trabalho e vida pessoal

Meu trabalho me causa desgaste emocional
Sinto cansaço mental frequente
Existe apoio emocional ou psicológico
Há ações de cuidado com a saúde mental

Sinto falta de apoio para realizar meu trabalho
Me sinto desamparado em situações difíceis
Existe suporte da empresa quando necessário
Há orientação e acompanhamento das atividades
     */
}
