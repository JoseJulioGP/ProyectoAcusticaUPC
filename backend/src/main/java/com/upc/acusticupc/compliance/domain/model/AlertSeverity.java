package com.upc.acusticupc.compliance.domain.model;

/**
 * Severidad de una alerta segun cuanto excede el valor medido al estandar.
 *  - LEVE:     excede entre 0 y 5 dB
 *  - MODERADA: excede entre 5 y 10 dB
 *  - CRITICA:  excede mas de 10 dB
 */
public enum AlertSeverity {
    LEVE,
    MODERADA,
    CRITICA;

    public static AlertSeverity fromExcess(double excessDb) {
        if (excessDb <= 5)  return LEVE;
        if (excessDb <= 10) return MODERADA;
        return CRITICA;
    }
}
