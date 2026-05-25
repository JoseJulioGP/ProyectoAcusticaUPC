package com.upc.acusticupc.compliance.application.calculator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Calculadora del percentil L90 segun definicion de Anexo 1 Res. 627:
 *
 *   "El nivel sonoro en dBA que se sobrepasa durante el 90% del tiempo
 *    de observacion."
 *
 * El 90% de las mediciones tiene un valor MAYOR a L90 → L90 corresponde
 * al percentil 10 cuando se ordenan las mediciones de menor a mayor.
 */
@Component
public class L90Calculator {

    public BigDecimal calculate(Collection<Double> dbValues) {
        if (dbValues == null || dbValues.isEmpty()) {
            throw new IllegalArgumentException("No se puede calcular L90 sobre una coleccion vacia");
        }
        List<Double> sorted = new ArrayList<>(dbValues);
        Collections.sort(sorted);

        // Posicion del percentil 10. Para 977 valores: floor(977 * 0.10) = 97.
        int index = (int) Math.floor(sorted.size() * 0.10);
        if (index >= sorted.size()) index = sorted.size() - 1;

        double l90 = sorted.get(index);
        return BigDecimal.valueOf(l90).setScale(2, RoundingMode.HALF_UP);
    }
}