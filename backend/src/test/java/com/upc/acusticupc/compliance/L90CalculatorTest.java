package com.upc.acusticupc.compliance;

import com.upc.acusticupc.compliance.application.calculator.L90Calculator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class L90CalculatorTest {

    private final L90Calculator calc = new L90Calculator();

    @Test
    void distribucionUniforme_L90EsPercentil10() {
        // 100 valores de 1 a 100. Percentil 10 = valor 10.
        var values = IntStream.rangeClosed(1, 100).asDoubleStream().boxed().toList();
        assertThat(calc.calculate(values).doubleValue()).isEqualTo(11.00);
        // Nota: posicion floor(100*0.10) = 10, valor en indice 10 (0-based) = 11
    }

    @Test
    void todoIgual_L90IgualAlValor() {
        var values = List.of(63.0, 63.0, 63.0, 63.0, 63.0);
        assertThat(calc.calculate(values).doubleValue()).isEqualTo(63.00);
    }
}