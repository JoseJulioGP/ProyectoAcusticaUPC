package com.upc.acusticupc.compliance.application.calculator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/**
 * Calculadora pura del Nivel Sonoro Continuo Equivalente ponderado A (LAeq).
 *
 *   LAeq = 10 * log10( (1/N) * Sigma(10^(Li/10)) )
 *
 * Es un promedio energetico, no aritmetico. Un solo pico alto pesa mucho.
 */
@Component
public class LAeqCalculator {

    /**
     * @param dbValues coleccion no vacia de valores en dB(A)
     * @return LAeq redondeado a 2 decimales
     * @throws IllegalArgumentException si la coleccion esta vacia
     */
    public BigDecimal calculate(Collection<Double> dbValues) {
        if (dbValues == null || dbValues.isEmpty()) {
            throw new IllegalArgumentException("No se puede calcular LAeq sobre una coleccion vacia");
        }
        double n = dbValues.size();
        double energySum = dbValues.stream()
                .mapToDouble(db -> Math.pow(10, db / 10.0))
                .sum();
        double laeq = 10.0 * Math.log10(energySum / n);
        return BigDecimal.valueOf(laeq).setScale(2, RoundingMode.HALF_UP);
    }
}