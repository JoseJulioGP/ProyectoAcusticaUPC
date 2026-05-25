package com.upc.acusticupc.compliance;

import com.upc.acusticupc.compliance.application.calculator.LAeqCalculator;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LAeqCalculatorTest {

    private final LAeqCalculator calc = new LAeqCalculator();

    @Test
    void todasLasMedicionesIguales_LAeqIgualAlValor() {
        var values = List.of(60.0, 60.0, 60.0, 60.0);
        assertThat(calc.calculate(values).doubleValue()).isEqualTo(60.00);
    }

    @Test
    void unSoloPicoAlto_LAeqMayorAlPromedioAritmetico() {
        // Promedio aritmetico: (90 + 50*976) / 977 = 50.04
        // LAeq deberia ser mayor por el peso energetico del pico
        var values = new java.util.ArrayList<Double>();
        values.add(90.0);
        for (int i = 0; i < 976; i++) values.add(50.0);
        double laeq = calc.calculate(values).doubleValue();
        assertThat(laeq).isGreaterThan(50.04);
        assertThat(laeq).isLessThan(70.0);
    }

    @Test
    void coleccionVacia_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> calc.calculate(Collections.emptyList()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}