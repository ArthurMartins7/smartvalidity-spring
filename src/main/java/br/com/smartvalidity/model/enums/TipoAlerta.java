package br.com.smartvalidity.model.enums;

public enum TipoAlerta {
    VENCIMENTO_HOJE,     // Automático: produto vence hoje
    VENCIMENTO_AMANHA,   // Automático: produto vence amanhã  
    VENCIMENTO_ATRASO,   // Automático: produto vencido há 1+ dias não inspecionado
    PERSONALIZADO        // Manual: criado pelo admin
} 