package co.edu.unbosque.quickcourier.decorator;

import co.edu.unbosque.quickcourier.model.ShippingExtra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Decorador Concreto para Huella de Carbono Neutra
 * Compensa las emisiones de CO2 del transporte
 *
 * Extra Code: CARBON_NEUTRAL
 * Tipo: FIXED
 * Precio base: $3,000
 */
public class CarbonNeutralDecorator extends OrderDecorator {

    private static final Logger logger = LoggerFactory.getLogger(CarbonNeutralDecorator.class);
    private static final BigDecimal CO2_PER_KM = new BigDecimal("0.12"); // kg CO2 por km
    private static final BigDecimal AVERAGE_DISTANCE_KM = new BigDecimal("15"); // Distancia promedio

    public CarbonNeutralDecorator(OrderComponent wrappedOrder, ShippingExtra shippingExtra) {
        super(wrappedOrder, shippingExtra);

        if (!"CARBON_NEUTRAL".equals(shippingExtra.getCode())) {
            logger.warn("Carbon neutral decorator created with non-CARBON_NEUTRAL extra: {}",
                    shippingExtra.getCode());
        }

        logger.debug("Carbon neutral delivery added to order. Cost: ${}, CO2 offset: {}kg",
                getExtraCost(), getEstimatedCO2Offset());
    }

    @Override
    protected String getExtraDescription() {
        return String.format("Envío Carbono Neutral (compensa %.2f kg CO2) +$%s",
                getEstimatedCO2Offset(), getExtraCost());
    }

    /**
     * Calcula el CO2 estimado a compensar
     */
    public BigDecimal getEstimatedCO2Offset() {
        // CO2 = distancia * factor_emisión
        return AVERAGE_DISTANCE_KM.multiply(CO2_PER_KM)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Obtiene el número de árboles equivalente a plantar
     */
    public int getEquivalentTreesPlanted() {
        // Un árbol absorbe ~20 kg CO2 por año
        BigDecimal co2Offset = getEstimatedCO2Offset();
        BigDecimal treesPerYear = co2Offset.divide(
                new BigDecimal("20"), 2, RoundingMode.HALF_UP);

        return Math.max(1, treesPerYear.intValue());
    }

    /**
     * Obtiene información sobre el programa de compensación
     */
    public String getOffsetProgramInfo() {
        return "Tu contribución apoya proyectos de reforestación y energía renovable " +
                "certificados internacionalmente. Cada envío compensa las emisiones de CO2 " +
                "generadas durante el transporte, equivalente a plantar " +
                getEquivalentTreesPlanted() + " árbol(es).";
    }

    /**
     * Obtiene el certificado de compensación (ID único)
     */
    public String getCertificateId() {
        // Generar ID único para el certificado de compensación
        long timestamp = System.currentTimeMillis();
        return String.format("QC-CO2-%d", timestamp);
    }

    /**
     * Verifica si el envío califica para certificado verde
     */
    public boolean qualifiesForGreenCertificate() {
        return true; // Todos los envíos carbon neutral califican
    }
}