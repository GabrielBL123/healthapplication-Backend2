package com.gabrielbl.healthaplication.model;


import lombok.Getter;

@Getter
public enum PerguntaKey {

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

    private final short factor;
    private final short numero;

    PerguntaKey(int factor, int numero) {
        this.factor = (short) factor;
        this.numero = (short) numero;
    }



    public String key() { return name(); } // "F1_P1"
}
